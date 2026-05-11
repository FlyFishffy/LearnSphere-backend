package com.flyfish.learnsphere.controller;

import com.flyfish.learnsphere.annotaion.AuthCheck;
import com.flyfish.learnsphere.exception.BusinessException;
import com.flyfish.learnsphere.model.entity.Result;
import com.flyfish.learnsphere.model.enums.ErrorCode;
import com.flyfish.learnsphere.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

/**
 * Video upload controller — stores video files to local filesystem
 * @Author: FlyFish
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Value("${file.upload.video-path:./uploads/videos/}")
    private String videoUploadPath;

    @Value("${file.upload.image-path:./uploads/images/}")
    private String imageUploadPath;

    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024L; // 500MB
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L; // 10MB

    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            "mp4", "webm", "ogg", "mov", "avi", "mkv"
    );

    private static final java.util.Set<String> ALLOWED_IMAGE_EXTENSIONS = java.util.Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg"
    );

    /**
     * Upload an image file and return its accessible URL
     */
    @PostMapping("/upload/image")
    @AuthCheck(value = {"Teacher", "Admin"})
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "File is empty.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Image size exceeds 10MB limit.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "File name is null.");
        }

        // Validate file extension
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    "Unsupported image format. Allowed: jpg, jpeg, png, gif, webp, bmp, svg");
        }

        try {
            // Ensure upload directory exists
            Path uploadDir = Paths.get(imageUploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generate unique filename
            String newFilename = UUID.randomUUID().toString() + "." + extension;
            Path targetPath = uploadDir.resolve(newFilename);

            // Save file
            file.transferTo(targetPath.toFile());
            log.info("Image uploaded successfully: {} -> {}", originalFilename, targetPath);

            // Return the accessible URL
            String imageUrl = "/api/uploads/images/" + newFilename;
            Map<String, String> result = Map.of(
                    "imageUrl", imageUrl,
                    "originalFilename", originalFilename
            );
            return ResultUtils.success(result);
        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to upload image: " + e.getMessage());
        }
    }
}
