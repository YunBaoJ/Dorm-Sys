package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

public class UploadControllerTest {

    @Test
    public void testUploadValidImage() {
        UploadController uploadController = new UploadController();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-photo.png",
                "image/png",
                "dummy image content".getBytes()
        );

        Result<String> result = uploadController.uploadFile(file);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData().startsWith("/uploads/"));
        assertTrue(result.getData().endsWith(".png"));
    }

    @Test
    public void testUploadInvalidExtension() {
        UploadController uploadController = new UploadController();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "malicious".getBytes()
        );

        Result<String> result = uploadController.uploadFile(file);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("不支持的文件类型"));
    }

    @Test
    public void testUploadEmptyFile() {
        UploadController uploadController = new UploadController();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        Result<String> result = uploadController.uploadFile(file);
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("不能为空"));
    }
}
