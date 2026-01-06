/**
 * 后端错误码与前端提示的映射
 * 错误码定义参考：backend/src/main/java/com/soulmate/common/ErrorCode.java
 */

// RAG相关错误 (5xxx)
export const RAG_ERROR_CODES = {
  5007: { type: 'error', title: 'RAG服务错误', message: 'RAG服务调用失败，请稍后重试' },
  5008: { type: 'error', title: 'RAG服务不可用', message: 'RAG服务暂时不可用，请检查服务状态' },
  5009: { type: 'warning', title: 'API认证失败', message: 'Embedding API Key无效或已过期，请在系统设置中更新智谱AI的API Key' },
  5010: { type: 'warning', title: 'API额度不足', message: 'Embedding API额度已用完，请充值后重试' },
  5011: { type: 'warning', title: '请求频率超限', message: 'Embedding API请求过于频繁，请稍后重试' },
  5012: { type: 'error', title: '请求超时', message: 'Embedding API调用超时，请检查网络后重试' },
  5013: { type: 'error', title: '向量化失败', message: '文本向量化处理失败，请检查文档内容后重试' },
}

// LLM相关错误 (8xxx)
export const LLM_ERROR_CODES = {
  8001: { type: 'error', title: '提供商不存在', message: 'LLM提供商配置不存在，请检查系统设置' },
  8002: { type: 'error', title: '模型不存在', message: 'LLM模型不存在或已被删除' },
  8003: { type: 'error', title: 'LLM调用失败', message: 'LLM API调用失败，请稍后重试' },
  8004: { type: 'warning', title: 'API认证失败', message: 'LLM API Key无效或已过期，请在系统设置中更新对应提供商的API Key' },
  8005: { type: 'warning', title: 'API额度不足', message: 'LLM API额度已用完，请充值后重试' },
  8006: { type: 'warning', title: '请求频率超限', message: 'LLM API请求过于频繁，请稍后重试' },
  8007: { type: 'error', title: '请求超时', message: 'LLM API调用超时，请检查网络后重试' },
  8008: { type: 'error', title: '模型不可用', message: 'LLM模型不可用或配置错误，请检查模型设置' },
}

// 知识库相关错误 (5xxx)
export const KNOWLEDGE_ERROR_CODES = {
  5001: { type: 'error', title: '知识库不存在', message: '请求的知识库不存在或已被删除' },
  5002: { type: 'error', title: '文档不存在', message: '请求的文档不存在或已被删除' },
  5003: { type: 'info', title: '文档处理中', message: '文档正在处理中，请稍候' },
  5004: { type: 'error', title: '上传失败', message: '文件上传失败，请检查文件格式和大小' },
  5005: { type: 'error', title: '参数无效', message: '请求参数无效，请检查输入' },
  5006: { type: 'error', title: '无效操作', message: '当前操作无效，请刷新后重试' },
}

// 合并所有错误码
export const ALL_ERROR_CODES = {
  ...RAG_ERROR_CODES,
  ...LLM_ERROR_CODES,
  ...KNOWLEDGE_ERROR_CODES,
}

/**
 * 根据错误码获取错误信息
 * @param {number} code - 错误码
 * @param {string} fallbackMessage - 后备消息（来自后端）
 * @returns {{ type: string, title: string, message: string }}
 */
export function getErrorInfo(code, fallbackMessage = '操作失败') {
  const errorInfo = ALL_ERROR_CODES[code]
  if (errorInfo) {
    return {
      ...errorInfo,
      // 如果后端有更具体的消息，使用后端的消息
      detail: fallbackMessage !== errorInfo.message ? fallbackMessage : null
    }
  }
  return {
    type: 'error',
    title: '操作失败',
    message: fallbackMessage,
    detail: null
  }
}

/**
 * 判断错误是否为API Key相关错误
 * @param {number} code - 错误码
 * @returns {boolean}
 */
export function isApiKeyError(code) {
  return [5009, 8004].includes(code)
}

/**
 * 判断错误是否为额度相关错误
 * @param {number} code - 错误码
 * @returns {boolean}
 */
export function isQuotaError(code) {
  return [5010, 8005].includes(code)
}

/**
 * 判断错误是否为频率限制错误
 * @param {number} code - 错误码
 * @returns {boolean}
 */
export function isRateLimitError(code) {
  return [5011, 8006].includes(code)
}

/**
 * 判断错误是否为超时错误
 * @param {number} code - 错误码
 * @returns {boolean}
 */
export function isTimeoutError(code) {
  return [5012, 8007].includes(code)
}

/**
 * 获取错误的建议操作
 * @param {number} code - 错误码
 * @returns {string|null}
 */
export function getErrorSuggestion(code) {
  if (isApiKeyError(code)) {
    return '请联系管理员更新API Key配置'
  }
  if (isQuotaError(code)) {
    return '请联系管理员充值API额度'
  }
  if (isRateLimitError(code)) {
    return '请等待几秒后重试'
  }
  if (isTimeoutError(code)) {
    return '请检查网络连接后重试'
  }
  return null
}

