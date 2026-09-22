package com.bookstore.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    record UploadResult(String imageUrl, String publicId) {}

    UploadResult uploadImage(MultipartFile file, String folder);

    void deleteImage(String publicId);
}
