"""
配置管理
"""
import os
from typing import Optional
from dotenv import load_dotenv

# 根据环境变量 ENV 加载对应的配置文件
# 开发环境：ENV=dev -> 加载 .env.dev
# 生产环境：ENV=prod -> 加载 .env.prod
# 如果未设置 ENV，默认加载 .env（开发环境）
env_mode = os.getenv("ENV", "").lower()

if env_mode == "dev":
    if os.path.exists(".env.dev"):
        load_dotenv(".env.dev")
    else:
        load_dotenv()  # 如果 .env.dev 不存在，尝试加载 .env
elif env_mode == "prod":
    if os.path.exists(".env.prod"):
        load_dotenv(".env.prod")
    else:
        load_dotenv()  # 如果 .env.prod 不存在，尝试加载 .env
else:
    # 未设置 ENV 环境变量，默认加载 .env（作为开发环境）
    load_dotenv()


class Settings:
    """应用配置"""
    
    # 服务配置
    SERVICE_HOST: str = os.getenv("SERVICE_HOST", "0.0.0.0")
    SERVICE_PORT: int = int(os.getenv("SERVICE_PORT", "8000"))
    DEBUG: bool = os.getenv("DEBUG", "true").lower() == "true"
    
    # 数据库配置（用于读取API key）
    DB_HOST: str = os.getenv("DB_HOST", "localhost")
    DB_PORT: int = int(os.getenv("DB_PORT", "3306"))
    DB_USER: str = os.getenv("DB_USER", "root")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "")
    DB_NAME: str = os.getenv("DB_NAME", "soulmate_db")
    
    # Embedding 配置（智谱 AI）
    # 优先从数据库读取，如果数据库读取失败，则使用环境变量
    ZHIPU_API_KEY: Optional[str] = os.getenv("ZHIPU_API_KEY")
    ZHIPU_API_BASE: str = os.getenv("ZHIPU_API_BASE", "https://open.bigmodel.cn/api/paas/v4")
    EMBEDDING_MODEL: str = os.getenv("EMBEDDING_MODEL", "embedding-2")
    
    # ChromaDB 配置（后续使用）
    CHROMA_PERSIST_PATH: str = os.getenv("CHROMA_PERSIST_PATH", "./data/chroma")
    
    @classmethod
    def validate(cls):
        """验证配置"""
        # 数据库配置是必需的（用于读取API key）
        if not cls.DB_HOST or not cls.DB_NAME:
            raise ValueError("数据库配置不完整，请设置 DB_HOST 和 DB_NAME")
        
        # API key可以从数据库读取，也可以从环境变量读取
        # 这里不强制要求环境变量，因为会从数据库读取

# 全局配置实例
settings = Settings()

