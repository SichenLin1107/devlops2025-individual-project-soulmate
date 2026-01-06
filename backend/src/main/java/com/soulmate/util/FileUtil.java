package com.soulmate.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传工具类
 */
@Slf4j
public class FileUtil {
    
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 上传头像文件
     */
    public static String uploadAvatar(MultipartFile file, String uploadPath) throws IOException {
        validateImageFile(file);
        
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
            log.info("创建上传目录: {}", uploadPath);
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = generateUniqueFilename(extension);
        
        Path filePath = Paths.get(uploadPath, filename);
        Files.write(filePath, file.getBytes());
        
        log.info("头像上传成功: filename={}, size={} bytes", filename, file.getSize());
        
        return "/uploads/avatars/" + filename;
    }
    
    /**
     * 验证图片文件
     */
    private static void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }
        
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new IllegalArgumentException("只支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg";
    }
    
    /**
     * 生成唯一文件名
     */
    private static String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }
    
    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("文件删除成功: {}", filePath);
                }
                return deleted;
            }
            return false;
        } catch (Exception e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
}

