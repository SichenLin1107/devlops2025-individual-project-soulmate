package com.soulmate.util;

import com.soulmate.common.BusinessException;
import com.soulmate.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具类，用于API Key等敏感数据的AES加密存储
 */
@Slf4j
@Component
public class EncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String IV_SEPARATOR = ":";
    
    @Value("${encryption.secret-key:soulmate-secret-key-32-bytes!}")
    private String secretKey;
    
    /**
     * 加密API Key
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            byte[] keyBytes = ensureKeyLength(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            
            byte[] iv = generateRandomIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            String encodedIV = Base64.getEncoder().encodeToString(iv);
            String encodedEncrypted = Base64.getEncoder().encodeToString(encrypted);
            
            return encodedIV + IV_SEPARATOR + encodedEncrypted;
            
        } catch (Exception e) {
            log.error("API Key加密失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "API Key加密失败");
        }
    }
    
    /**
     * 解密API Key
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        if (!encryptedText.contains(IV_SEPARATOR)) {
            return encryptedText;
        }
        
        try {
            String[] parts = encryptedText.split(IV_SEPARATOR, 2);
            if (parts.length != 2) {
                log.warn("加密数据格式错误，直接返回原文: {}", encryptedText.substring(0, Math.min(10, encryptedText.length())));
                return encryptedText;
            }
            
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            
            byte[] keyBytes = ensureKeyLength(secretKey);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            
            byte[] decrypted = cipher.doFinal(encrypted);
            
            return new String(decrypted, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("API Key解密失败: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "API Key解密失败: " + e.getMessage());
        }
    }
    
    /**
     * 脱敏显示API Key
     */
    public String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "";
        }
        
        if (apiKey.length() <= 6) {
            return "***";
        }
        
        String prefix = apiKey.substring(0, 3);
        String suffix = apiKey.substring(apiKey.length() - 3);
        return prefix + "***" + suffix;
    }
    
    /**
     * 生成随机IV
     */
    private byte[] generateRandomIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
    
    /**
     * 确保密钥长度为32字节（AES-256）
     */
    private byte[] ensureKeyLength(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            for (int i = keyBytes.length; i < 32; i++) {
                paddedKey[i] = keyBytes[i % keyBytes.length];
            }
            return paddedKey;
        } else if (keyBytes.length > 32) {
            byte[] truncatedKey = new byte[32];
            System.arraycopy(keyBytes, 0, truncatedKey, 0, 32);
            return truncatedKey;
        } else {
            return keyBytes;
        }
    }
}