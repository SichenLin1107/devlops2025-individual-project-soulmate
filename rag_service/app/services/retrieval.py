"""
检索服务 - ChromaDB 向量存储和检索
"""
import os
import logging
from typing import List, Dict, Any

import chromadb
from chromadb.config import Settings

from app.config import settings
from app.services.embedding import embedding_service

logger = logging.getLogger(__name__)


class RetrievalService:
    """向量检索服务"""
    
    def __init__(self):
        """初始化 ChromaDB 客户端"""
        # 确保持久化目录存在
        persist_path = settings.CHROMA_PERSIST_PATH
        os.makedirs(persist_path, exist_ok=True)
        
        # 初始化 ChromaDB 客户端（持久化模式）
        self.client = chromadb.PersistentClient(
            path=persist_path,
            settings=Settings(
                anonymized_telemetry=False,
                allow_reset=True
            )
        )
        logger.info(f"ChromaDB 客户端初始化成功，持久化路径: {persist_path}")
    
    def get_or_create_collection(self, kb_id: str):
        """
        获取或创建集合（按知识库ID）
        
        Args:
            kb_id: 知识库ID，用作集合名称
            
        Returns:
            ChromaDB Collection 对象
        """
        try:
            # 尝试获取已存在的集合
            collection = self.client.get_collection(name=kb_id)
            logger.debug(f"获取已存在的集合: {kb_id}")
            return collection
        except Exception:
            # 集合不存在，创建新集合
            collection = self.client.create_collection(
                name=kb_id,
                metadata={"kb_id": kb_id}
            )
            logger.info(f"创建新集合: {kb_id}")
            return collection
    
    def index_segments(
        self, 
        kb_id: str, 
        doc_id: int, 
        segments: List[str]
    ) -> Dict[str, Any]:
        """
        索引文档切片到 ChromaDB
        
        Args:
            kb_id: 知识库ID
            doc_id: 文档ID
            segments: 文本片段列表
            
        Returns:
            包含成功数量和向量ID列表的字典
        """
        if not segments:
            raise ValueError("文本片段列表不能为空")
        
        # 过滤空文本
        valid_segments = [seg for seg in segments if seg and seg.strip()]
        if not valid_segments:
            raise ValueError("文本片段列表中不包含有效文本")
        
        try:
            # 获取或创建集合
            collection = self.get_or_create_collection(kb_id)
            
            # 批量向量化文本片段
            logger.info(f"开始向量化 {len(valid_segments)} 个文本片段...")
            embeddings = embedding_service.embed_batch(valid_segments)
            logger.info(f"向量化完成，共 {len(embeddings)} 个向量")
            
            # 生成向量ID列表（格式：doc_{doc_id}_seg_{position}）
            # 这样可以方便地按文档ID删除
            ids = [f"doc_{doc_id}_seg_{i}" for i in range(len(valid_segments))]
            
            # 准备元数据
            metadatas = [
                {
                    "kb_id": kb_id,
                    "doc_id": str(doc_id),
                    "position": i  # 片段在文档中的位置
                }
                for i in range(len(valid_segments))
            ]
            
            # 存入 ChromaDB
            collection.add(
                ids=ids,
                embeddings=embeddings,
                documents=valid_segments,
                metadatas=metadatas
            )
            
            logger.info(f"成功索引 {len(valid_segments)} 个片段到集合 {kb_id}")
            
            return {
                "count": len(valid_segments),
                "ids": ids,
                "kb_id": kb_id,
                "doc_id": doc_id
            }
        
        except Exception as e:
            logger.error(f"索引文档切片失败: {str(e)}")
            raise RuntimeError(f"索引文档切片失败: {str(e)}")
    
    def delete_document_vectors(self, kb_id: str, doc_id: int) -> int:
        """
        删除指定文档的所有向量数据
        
        Args:
            kb_id: 知识库ID
            doc_id: 文档ID
            
        Returns:
            删除的向量数量
        """
        try:
            collection = self.get_or_create_collection(kb_id)
            
            # 查询该文档的所有向量
            results = collection.get(
                where={"doc_id": str(doc_id)}
            )
            
            if results['ids']:
                # 删除这些向量
                collection.delete(ids=results['ids'])
                count = len(results['ids'])
                logger.info(f"成功删除文档 {doc_id} 的 {count} 个向量")
                return count
            else:
                logger.info(f"文档 {doc_id} 没有向量数据")
                return 0
        
        except Exception as e:
            logger.error(f"删除文档向量失败: {str(e)}")
            raise RuntimeError(f"删除文档向量失败: {str(e)}")
    
    def search(
        self, 
        kb_id: str, 
        query: str, 
        top_k: int = 3
    ) -> List[Dict[str, Any]]:
        """
        在指定知识库中检索相关文档片段
        
        Args:
            kb_id: 知识库ID
            query: 查询文本
            top_k: 返回结果数量
            
        Returns:
            检索结果列表，每个结果包含：text, score, doc_id, segment_id, metadata
        """
        if not query or not query.strip():
            raise ValueError("查询文本不能为空")
        
        if top_k <= 0:
            raise ValueError("top_k 必须大于 0")
        
        try:
            # 获取集合
            collection = self.get_or_create_collection(kb_id)
            
            # 向量化查询文本
            logger.info(f"开始向量化查询文本: {query[:50]}...")
            query_embedding = embedding_service.embed(query)
            logger.info(f"查询文本向量化完成")
            
            # 执行向量检索
            results = collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k,
                include=["documents", "metadatas", "distances"]
            )
            
            # 处理检索结果
            search_results = []
            if results['ids'][0]:  # 确保有结果
                for i in range(len(results['ids'][0])):
                    # ChromaDB 返回的是 distance（越小越相似），转换为 score（越大越相似）
                    distance = results['distances'][0][i]
                    score = 1.0 / (1.0 + distance)  # 转换为相似度分数
                    
                    # 从 ID 中提取 segment_id（格式：doc_{doc_id}_seg_{position}）
                    vector_id = results['ids'][0][i]
                    segment_id = vector_id  # 使用完整的 vector_id 作为 segment_id
                    
                    result = {
                        "text": results['documents'][0][i],
                        "score": float(score),
                        "doc_id": int(results['metadatas'][0][i]['doc_id']),
                        "segment_id": segment_id,
                        "metadata": results['metadatas'][0][i]
                    }
                    search_results.append(result)
            
            logger.info(f"检索完成，找到 {len(search_results)} 个相关片段")
            return search_results
        
        except Exception as e:
            logger.error(f"向量检索失败: {str(e)}")
            raise RuntimeError(f"向量检索失败: {str(e)}")
    
    def delete_collection(self, kb_id: str) -> bool:
        """
        删除整个知识库的向量集合
        
        Args:
            kb_id: 知识库ID
            
        Returns:
            是否删除成功
        """
        try:
            self.client.delete_collection(name=kb_id)
            logger.info(f"成功删除集合: {kb_id}")
            return True
        except Exception as e:
            # 集合不存在也算成功
            if "does not exist" in str(e).lower():
                logger.info(f"集合 {kb_id} 不存在，无需删除")
                return True
            logger.error(f"删除集合失败: {str(e)}")
            raise RuntimeError(f"删除集合失败: {str(e)}")
    
    def get_collection_stats(self, kb_id: str) -> Dict[str, Any]:
        """
        获取知识库集合的统计信息
        
        Args:
            kb_id: 知识库ID
            
        Returns:
            包含统计信息的字典
        """
        try:
            # 检查集合是否存在
            try:
                collection = self.client.get_collection(name=kb_id)
                exists = True
                
                # 获取集合中的所有数据
                results = collection.get(include=["metadatas"])
                
                if results and results.get('metadatas'):
                    # 统计文档数量（去重）
                    doc_ids = set()
                    for metadata in results['metadatas']:
                        if metadata and 'doc_id' in metadata:
                            doc_ids.add(metadata['doc_id'])
                    
                    document_count = len(doc_ids)
                    segment_count = len(results['metadatas'])
                else:
                    document_count = 0
                    segment_count = 0
                
                logger.info(f"集合 {kb_id} 统计信息: 文档数={document_count}, 片段数={segment_count}")
                
            except Exception:
                # 集合不存在
                exists = False
                document_count = 0
                segment_count = 0
                logger.info(f"集合 {kb_id} 不存在")
            
            return {
                "exists": exists,
                "document_count": document_count,
                "segment_count": segment_count,
                "kb_id": kb_id
            }
        
        except Exception as e:
            logger.error(f"获取集合统计信息失败: {str(e)}")
            raise RuntimeError(f"获取集合统计信息失败: {str(e)}")


# 全局服务实例
retrieval_service = RetrievalService()

