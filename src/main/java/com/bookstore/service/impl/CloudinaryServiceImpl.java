package com.bookstore.service.impl;

import com.bookstore.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    private final Cloudinary cloudinary;
    private final boolean isCloudinaryConfigured;

    public CloudinaryServiceImpl(
            @Value("${cloudinary.cloud_name:}") String cloudName,
            @Value("${cloudinary.api_key:}") String apiKey,
            @Value("${cloudinary.api_secret:}") String apiSecret
    ) {
        if (cloudName != null && !cloudName.isBlank() &&
            apiKey != null && !apiKey.isBlank() &&
            apiSecret != null && !apiSecret.isBlank() &&
            !apiSecret.equalsIgnoreCase("your_cloudinary_api_secret")) {
            this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,
                    "api_key", apiKey,
                    "api_secret", apiSecret,
                    "secure", true
            ));
            this.isCloudinaryConfigured = true;
            log.info("Cloudinary Service initialized with cloud_name: {}", cloudName);
        } else {
            this.cloudinary = null;
            this.isCloudinaryConfigured = false;
            log.warn("Cloudinary credentials are not configured or using default placeholders. Fallback local storage enabled.");
        }
    }

    @Override
    public UploadResult uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Tệp tải lên không được rỗng");
        }

        String publicId = (folder != null ? folder + "/" : "") + UUID.randomUUID();

        if (isCloudinaryConfigured && cloudinary != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                        "folder", folder != null ? folder : "bookstore/book_items",
                        "resource_type", "auto"
                ));
                String secureUrl = (String) uploadResult.get("secure_url");
                String cPublicId = (String) uploadResult.get("public_id");
                log.info("Uploaded successfully to Cloudinary: {}", secureUrl);
                return new UploadResult(secureUrl, cPublicId);
            } catch (Exception ex) {
                log.error("Cloudinary upload failed: {}. Falling back to local storage.", ex.getMessage());
            }
        }

        // Fallback local storage
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                extension = ".jpg";
            }

            String localFilename = UUID.randomUUID() + extension;
            Path uploadDir = Paths.get("uploads", folder != null ? folder : "book_items");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path targetPath = uploadDir.resolve(localFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            String localUrl = "/uploads/" + (folder != null ? folder + "/" : "book_items/") + localFilename;
            log.info("File saved locally: {}", localUrl);
            return new UploadResult(localUrl, "local_" + localFilename);
        } catch (IOException e) {
            log.error("Failed to save file locally: {}", e.getMessage(), e);
            // Default safe placeholder image
            return new UploadResult(
                    "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500&q=80",
                    "placeholder_" + UUID.randomUUID()
            );
        }
    }

    @Override
    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank() || publicId.startsWith("local_") || publicId.startsWith("placeholder_")) {
            return;
        }

        if (isCloudinaryConfigured && cloudinary != null) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Deleted image from Cloudinary: {}", publicId);
            } catch (Exception ex) {
                log.error("Failed to delete Cloudinary image: {}", ex.getMessage());
            }
        }
    }
}
