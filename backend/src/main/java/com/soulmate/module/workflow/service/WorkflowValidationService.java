package com.soulmate.module.workflow.service;

import com.soulmate.module.workflow.dto.NodesConfig;

import java.util.List;

/**
 * 工作流验证服务
 * 验证工作流配置是否正确
 */
public interface WorkflowValidationService {
    
    /**
     * 验证工作流配置
     * 
     * @param nodesConfig 工作流节点配置
     * @return 验证结果，包含错误和警告列表
     */
    ValidationResult validate(NodesConfig nodesConfig);
    
    /**
     * 验证结果
     */
    class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private List<String> warnings;
        
        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, List.of(), List.of());
        }
        
        public static ValidationResult error(List<String> errors) {
            return new ValidationResult(false, errors, List.of());
        }
        
        public static ValidationResult withWarnings(List<String> warnings) {
            return new ValidationResult(true, List.of(), warnings);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public List<String> getWarnings() {
            return warnings;
        }
    }
}

