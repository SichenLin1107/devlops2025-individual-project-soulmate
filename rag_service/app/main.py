"""
RAG 服务主应用
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.api import rag
import sys
import os
import uvicorn

# 创建 FastAPI 应用
app = FastAPI(
    title="RAG Service",
    description="向量检索和知识库管理服务",
    version="1.0.0"
)

# 配置 CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应限制具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(rag.router)


@app.get("/")
async def root():
    """根路径"""
    return {
        "service": "RAG Service",
        "version": "1.0.0",
        "status": "running"
    }


if __name__ == "__main__":
    
    # 添加项目根目录到 Python 路径
    current_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(current_dir)
    if project_root not in sys.path:
        sys.path.insert(0, project_root)
    
    # 验证配置
    settings.validate()
    uvicorn.run(
        "app.main:app",
        host=settings.SERVICE_HOST,
        port=settings.SERVICE_PORT,
        reload=settings.DEBUG
    )

