"""
数据库连接工具
用于从数据库读取API key等配置信息
"""
import logging
from typing import Optional, Dict

import pymysql

from app.config import settings

logger = logging.getLogger(__name__)


class Database:
    """数据库连接管理"""
    
    def __init__(self):
        self.connection: Optional[pymysql.Connection] = None
    
    def connect(self) -> pymysql.Connection:
        """获取数据库连接"""
        if self.connection is None or not self.connection.open:
            try:
                self.connection = pymysql.connect(
                    host=settings.DB_HOST,
                    port=settings.DB_PORT,
                    user=settings.DB_USER,
                    password=settings.DB_PASSWORD,
                    database=settings.DB_NAME,
                    charset='utf8mb4',
                    cursorclass=pymysql.cursors.DictCursor,
                    autocommit=True
                )
                logger.info("数据库连接成功")
            except Exception as e:
                logger.error(f"数据库连接失败: {str(e)}")
                raise
        return self.connection
    
    def close(self):
        """关闭数据库连接"""
        if self.connection and self.connection.open:
            self.connection.close()
            self.connection = None
    
    def get_provider_config(self, provider_id: str = "prv_zhipu") -> Optional[Dict[str, str]]:
        """
        从数据库获取LLM提供商配置
        
        Args:
            provider_id: 提供商ID，默认为智谱AI (prv_zhipu)
            
        Returns:
            包含 api_key 和 api_base 的字典，如果不存在则返回 None
        """
        try:
            conn = self.connect()
            with conn.cursor() as cursor:
                sql = "SELECT api_key, api_base FROM llm_provider WHERE id = %s AND is_active = 1"
                cursor.execute(sql, (provider_id,))
                result = cursor.fetchone()
                
                if result and result.get('api_key'):
                    return {
                        'api_key': result['api_key'],
                        'api_base': result.get('api_base', 'https://open.bigmodel.cn/api/paas/v4')
                    }
                else:
                    logger.warning(f"提供商 {provider_id} 未找到或未启用")
                    return None
        except Exception as e:
            logger.error(f"查询提供商配置失败: {str(e)}")
            raise


# 全局数据库实例
db = Database()

