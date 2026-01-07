"""
向量化服务 - 使用智谱 AI Embedding API
"""
import logging
from typing import List, Optional

from openai import OpenAI

from app.config import settings
from app.db import db

logger = logging.getLogger(__name__)


class EmbeddingService:
    """向量化服务"""
    
    def __init__(self):
        """初始化 OpenAI 客户端（兼容智谱 AI）"""
        self._client: Optional[OpenAI] = None
        self._api_key: Optional[str] = None
        self._api_base: Optional[str] = None
        self.model = settings.EMBEDDING_MODEL
    
    def _get_client(self) -> OpenAI:
        """获取OpenAI客户端，如果未初始化则从数据库读取API key并初始化"""
        if self._client is None:
            # 尝试从数据库读取API key
            try:
                provider_config = db.get_provider_config("prv_zhipu")
                if provider_config:
                    self._api_key = provider_config['api_key']
                    self._api_base = provider_config.get('api_base', settings.ZHIPU_API_BASE)
                    logger.info("从数据库读取智谱AI API配置成功")
                else:
                    # 如果数据库读取失败，尝试使用环境变量
                    if settings.ZHIPU_API_KEY:
                        self._api_key = settings.ZHIPU_API_KEY
                        self._api_base = settings.ZHIPU_API_BASE
                        logger.info("使用环境变量中的智谱AI API配置")
                    else:
                        raise ValueError("无法获取智谱AI API key，请检查数据库配置或环境变量")
            except Exception as e:
                logger.error(f"读取API配置失败: {str(e)}")
                # 如果数据库读取失败，尝试使用环境变量作为后备
                if settings.ZHIPU_API_KEY:
                    self._api_key = settings.ZHIPU_API_KEY
                    self._api_base = settings.ZHIPU_API_BASE
                    logger.warning("数据库读取失败，使用环境变量配置")
                else:
                    raise ValueError(f"无法获取智谱AI API key: {str(e)}")
            
            # 初始化客户端
            if not self._api_key:
                raise ValueError("API key为空，无法初始化客户端")
            
            self._client = OpenAI(
                api_key=self._api_key,
                base_url=self._api_base
            )
        
        return self._client
    
    def _refresh_client(self):
        """刷新客户端（重新从数据库读取配置）"""
        self._client = None
        self._api_key = None
        self._api_base = None
        return self._get_client()
    
    def embed(self, text: str) -> List[float]:
        """
        向量化单个文本
        
        Args:
            text: 待向量化的文本
            
        Returns:
            向量列表
        """
        if not text or not text.strip():
            raise ValueError("文本不能为空")
        
        try:
            client = self._get_client()
            response = client.embeddings.create(
                model=self.model,
                input=text
            )
            return response.data[0].embedding
        except Exception as e:
            # 如果是认证错误，尝试刷新客户端
            if "authentication" in str(e).lower() or "api key" in str(e).lower():
                logger.warning("API认证失败，尝试刷新配置")
                client = self._refresh_client()
                response = client.embeddings.create(
                    model=self.model,
                    input=text
                )
                return response.data[0].embedding
            raise RuntimeError(f"向量化失败: {str(e)}")
    
    def embed_batch(self, texts: List[str]) -> List[List[float]]:
        """
        批量向量化文本
        
        Args:
            texts: 待向量化的文本列表
            
        Returns:
            向量列表的列表
        """
        if not texts:
            raise ValueError("文本列表不能为空")
        
        # 过滤空文本
        valid_texts = [text for text in texts if text and text.strip()]
        if not valid_texts:
            raise ValueError("文本列表中不包含有效文本")
        
        try:
            client = self._get_client()
            response = client.embeddings.create(
                model=self.model,
                input=valid_texts
            )
            # 按输入顺序返回向量
            embeddings_dict = {item.index: item.embedding for item in response.data}
            return [embeddings_dict[i] for i in range(len(valid_texts))]
        except Exception as e:
            # 如果是认证错误，尝试刷新客户端
            if "authentication" in str(e).lower() or "api key" in str(e).lower():
                logger.warning("API认证失败，尝试刷新配置")
                client = self._refresh_client()
                response = client.embeddings.create(
                    model=self.model,
                    input=valid_texts
                )
                embeddings_dict = {item.index: item.embedding for item in response.data}
                return [embeddings_dict[i] for i in range(len(valid_texts))]
            raise RuntimeError(f"批量向量化失败: {str(e)}")


# 全局服务实例
embedding_service = EmbeddingService()

