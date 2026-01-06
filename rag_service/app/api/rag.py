"""
RAG API 路由
"""
from typing import List, Optional
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from app.services.embedding import embedding_service
from app.services.retrieval import retrieval_service

router = APIRouter(prefix="/api/rag", tags=["RAG"])


# 请求模型
class EmbedRequest(BaseModel):
    """向量化请求"""
    text: Optional[str] = Field(None, description="单个文本（单个模式）")
    texts: Optional[List[str]] = Field(None, description="文本列表（批量模式）")
    
    class Config:
        json_schema_extra = {
            "example": {
                "text": "这是要向量化的文本",
                "texts": ["文本1", "文本2", "文本3"]
            }
        }


# 响应模型
class EmbedResponse(BaseModel):
    """向量化响应"""
    embedding: Optional[List[float]] = Field(None, description="单个向量（单个模式）")
    embeddings: Optional[List[List[float]]] = Field(None, description="向量列表（批量模式）")


class HealthResponse(BaseModel):
    """健康检查响应"""
    status: str
    service: str
    version: str


# 索引请求模型
class IndexRequest(BaseModel):
    """索引文档切片请求"""
    kb_id: str = Field(..., description="知识库ID")
    doc_id: int = Field(..., description="文档ID", gt=0)
    segments: List[str] = Field(..., description="文本片段列表", min_items=1)
    
    class Config:
        json_schema_extra = {
            "example": {
                "kb_id": "kb_psychology",
                "doc_id": 1,
                "segments": [
                    "这是第一个文本片段",
                    "这是第二个文本片段"
                ]
            }
        }


# 索引响应模型
class IndexResponse(BaseModel):
    """索引文档切片响应"""
    success: bool
    count: int = Field(..., description="成功索引的片段数量")
    message: str


# 检索请求模型
class SearchRequest(BaseModel):
    """向量检索请求"""
    kb_id: str = Field(..., description="知识库ID")
    query: str = Field(..., description="查询文本", min_length=1)
    top_k: int = Field(3, description="返回结果数量", gt=0, le=20)
    
    class Config:
        json_schema_extra = {
            "example": {
                "kb_id": "kb_psychology",
                "query": "如何缓解焦虑情绪",
                "top_k": 3
            }
        }


# 检索结果模型
class SearchResult(BaseModel):
    """单个检索结果"""
    text: str = Field(..., description="文档片段内容")
    score: float = Field(..., description="相似度分数（0-1之间，越大越相似）")
    doc_id: int = Field(..., description="文档ID")
    segment_id: str = Field(..., description="片段ID")
    metadata: dict = Field(..., description="元数据")


# 检索响应模型
class SearchResponse(BaseModel):
    """向量检索响应"""
    results: List[SearchResult] = Field(..., description="检索结果列表")
    query: str = Field(..., description="查询文本")
    kb_id: str = Field(..., description="知识库ID")
    total: int = Field(..., description="返回结果数量")


# 删除响应模型
class DeleteResponse(BaseModel):
    """删除操作响应"""
    success: bool
    message: str
    count: int = Field(0, description="删除的向量数量")


# 集合统计响应模型
class CollectionStatsResponse(BaseModel):
    """集合统计信息响应"""
    kb_id: str = Field(..., description="知识库ID")
    document_count: int = Field(..., description="文档数量")
    segment_count: int = Field(..., description="片段总数")
    exists: bool = Field(..., description="集合是否存在")


@router.post("/embed", response_model=EmbedResponse)
async def embed_text(request: EmbedRequest):
    """
    文本向量化接口
    
    支持两种模式：
    1. 单个模式：传入 text 参数
    2. 批量模式：传入 texts 参数
    
    注意：text 和 texts 只能传入其中一个
    """
    # 验证参数
    if request.text and request.texts:
        raise HTTPException(
            status_code=400,
            detail="不能同时传入 text 和 texts 参数，请选择其中一种模式"
        )
    
    if not request.text and not request.texts:
        raise HTTPException(
            status_code=400,
            detail="必须传入 text 或 texts 参数"
        )
    
    try:
        # 单个模式
        if request.text:
            embedding = embedding_service.embed(request.text)
            return EmbedResponse(embedding=embedding)
        
        # 批量模式
        if request.texts:
            embeddings = embedding_service.embed_batch(request.texts)
            return EmbedResponse(embeddings=embeddings)
    
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"向量化失败: {str(e)}")


@router.get("/health", response_model=HealthResponse)
async def health_check():
    """
    健康检查接口
    """
    return HealthResponse(
        status="healthy",
        service="RAG Service",
        version="1.0.0"
    )


def parse_api_error(error_msg: str) -> dict:
    """解析API错误类型，返回包含错误类型的详细信息"""
    error_type = "UNKNOWN"
    
    if "401" in error_msg or "authentication" in error_msg.lower() or "api key" in error_msg.lower() or "令牌" in error_msg or "验证" in error_msg:
        error_type = "AUTH_ERROR"
    elif "402" in error_msg or "quota" in error_msg.lower() or "余额" in error_msg or "额度" in error_msg or "insufficient" in error_msg.lower():
        error_type = "QUOTA_ERROR"
    elif "429" in error_msg or "rate" in error_msg.lower() or "频率" in error_msg or "too many" in error_msg.lower():
        error_type = "RATE_LIMIT"
    elif "timeout" in error_msg.lower() or "超时" in error_msg:
        error_type = "TIMEOUT"
    elif "connect" in error_msg.lower() or "连接" in error_msg or "network" in error_msg.lower():
        error_type = "CONNECTION_ERROR"
    elif "model" in error_msg.lower() and ("not found" in error_msg.lower() or "不存在" in error_msg):
        error_type = "MODEL_ERROR"
    
    return {"message": error_msg, "error_type": error_type}


@router.post("/index", response_model=IndexResponse)
async def index_document(request: IndexRequest):
    """
    索引文档切片到 ChromaDB
    
    接收知识库ID、文档ID和文本片段列表，批量向量化后存入 ChromaDB
    """
    try:
        result = retrieval_service.index_segments(
            kb_id=request.kb_id,
            doc_id=request.doc_id,
            segments=request.segments
        )
        
        return IndexResponse(
            success=True,
            count=result["count"],
            message=f"成功索引 {result['count']} 个片段到知识库 {request.kb_id}"
        )
    
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        error_detail = parse_api_error(str(e))
        raise HTTPException(status_code=500, detail=error_detail)
    except Exception as e:
        error_detail = parse_api_error(f"索引失败: {str(e)}")
        raise HTTPException(status_code=500, detail=error_detail)


def parse_api_error(error_msg: str) -> dict:
    """解析API错误类型，返回包含错误类型的详细信息"""
    error_type = "UNKNOWN"
    
    if "401" in error_msg or "authentication" in error_msg.lower() or "api key" in error_msg.lower() or "令牌" in error_msg or "验证" in error_msg:
        error_type = "AUTH_ERROR"
    elif "402" in error_msg or "quota" in error_msg.lower() or "余额" in error_msg or "额度" in error_msg or "insufficient" in error_msg.lower():
        error_type = "QUOTA_ERROR"
    elif "429" in error_msg or "rate" in error_msg.lower() or "频率" in error_msg or "too many" in error_msg.lower():
        error_type = "RATE_LIMIT"
    elif "timeout" in error_msg.lower() or "超时" in error_msg:
        error_type = "TIMEOUT"
    elif "connect" in error_msg.lower() or "连接" in error_msg or "network" in error_msg.lower():
        error_type = "CONNECTION_ERROR"
    elif "model" in error_msg.lower() and ("not found" in error_msg.lower() or "不存在" in error_msg):
        error_type = "MODEL_ERROR"
    
    return {"message": error_msg, "error_type": error_type}


@router.post("/search", response_model=SearchResponse)
async def search_documents(request: SearchRequest):
    """
    向量检索接口
    
    在指定知识库中检索与查询文本相关的文档片段
    返回按相似度排序的结果
    """
    try:
        results = retrieval_service.search(
            kb_id=request.kb_id,
            query=request.query,
            top_k=request.top_k
        )
        
        # 转换为响应模型
        search_results = [
            SearchResult(
                text=result["text"],
                score=result["score"],
                doc_id=result["doc_id"],
                segment_id=result["segment_id"],
                metadata=result["metadata"]
            )
            for result in results
        ]
        
        return SearchResponse(
            results=search_results,
            query=request.query,
            kb_id=request.kb_id,
            total=len(search_results)
        )
    
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        error_detail = parse_api_error(str(e))
        raise HTTPException(status_code=500, detail=error_detail)
    except Exception as e:
        error_detail = parse_api_error(f"检索失败: {str(e)}")
        raise HTTPException(status_code=500, detail=error_detail)


@router.delete("/collection/{kb_id}", response_model=DeleteResponse)
async def delete_collection(kb_id: str):
    """
    删除知识库的向量集合
    
    删除指定知识库的所有向量数据和集合
    """
    try:
        success = retrieval_service.delete_collection(kb_id)
        
        if success:
            return DeleteResponse(
                success=True,
                message=f"成功删除知识库 {kb_id} 的向量集合"
            )
        else:
            raise HTTPException(status_code=500, detail="删除集合失败")
    
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"删除集合失败: {str(e)}")


@router.delete("/document/{kb_id}/{doc_id}", response_model=DeleteResponse)
async def delete_document_vectors(kb_id: str, doc_id: int):
    """
    删除单个文档的向量数据
    
    删除指定文档的所有向量数据，但保留集合
    """
    try:
        count = retrieval_service.delete_document_vectors(kb_id, doc_id)
        
        return DeleteResponse(
            success=True,
            message=f"成功删除文档 {doc_id} 的向量数据",
            count=count
        )
    
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"删除文档向量失败: {str(e)}")


@router.get("/collection/{kb_id}/stats", response_model=CollectionStatsResponse)
async def get_collection_stats(kb_id: str):
    """
    获取知识库集合的统计信息
    
    返回集合中的文档数量、片段数量等统计信息
    """
    try:
        stats = retrieval_service.get_collection_stats(kb_id)
        
        return CollectionStatsResponse(
            kb_id=kb_id,
            document_count=stats["document_count"],
            segment_count=stats["segment_count"],
            exists=stats["exists"]
        )
    
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except RuntimeError as e:
        raise HTTPException(status_code=500, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"获取集合统计信息失败: {str(e)}")

