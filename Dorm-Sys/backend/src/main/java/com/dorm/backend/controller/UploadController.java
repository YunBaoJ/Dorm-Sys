package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private static final String UPLOAD_DIR = "uploads";
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @PostMapping
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error(400, "文件格式不正确，缺少扩展名");
        }

        String ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return Result.error(400, "不支持的文件类型，仅允许上传图片（jpg, png, gif, webp）");
        }

        // 确保上传目录存在
        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            boolean created = uploadFolder.mkdirs();
            if (!created) {
                log.error("创建上传目录失败: {}", uploadFolder.getAbsolutePath());
                return Result.error(500, "服务器存储目录初始化失败");
            }
        }

        String uniqueName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path targetPath = Paths.get(UPLOAD_DIR, uniqueName);

        try {
            Files.copy(file.getInputStream(), targetPath);
            String fileUrl = "/uploads/" + uniqueName;
            log.info("文件上传成功: {}", fileUrl);
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("保存上传文件发生异常: {}", e.getMessage(), e);
            return Result.error(500, "文件保存失败: " + e.getMessage());
        }
    }
}
