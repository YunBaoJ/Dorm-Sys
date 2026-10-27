package com.dorm.backend.service;

import com.dorm.backend.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AiServiceTest {

    @Test
    public void testFallbackAnswerDormRules() {
        AiService aiService = new AiService();

        // 验证门禁规则
        String doorAnswer = aiService.fallbackAnswer("请问宿舍几点关门？");
        assertNotNull(doorAnswer);
        assertTrue(doorAnswer.contains("23:30"));

        // 验证调宿申请
        String transferAnswer = aiService.fallbackAnswer("如何申请调换宿舍？");
        assertNotNull(transferAnswer);
        assertTrue(transferAnswer.contains("调宿申请"));

        // 验证报修
        String repairAnswer = aiService.fallbackAnswer("寝室水管坏了漏水怎么办");
        assertNotNull(repairAnswer);
        assertTrue(repairAnswer.contains("报修"));

        // 验证空输入处理
        String emptyAnswer = aiService.chat("");
        assertNotNull(emptyAnswer);
    }

    @Test
    public void testRoleDisplayNameAndAddressing() {
        AiService aiService = new AiService();

        // 1. 学生身份测试：某某同学
        User student = new User();
        student.setRole("student");
        student.setName("李华");
        student.setGender("男");
        assertEquals("李华同学", aiService.getRoleDisplayName(student));

        // 2. 宿管身份测试：男宿管为王叔，女宿管为张阿姨
        User maleManager = new User();
        maleManager.setRole("dormmanager");
        maleManager.setName("王建国");
        maleManager.setGender("男");
        assertEquals("王叔", aiService.getRoleDisplayName(maleManager));

        User femaleManager = new User();
        femaleManager.setRole("dormmanager");
        femaleManager.setName("张秀英");
        femaleManager.setGender("女");
        assertEquals("张阿姨", aiService.getRoleDisplayName(femaleManager));

        // 3. 管理员身份测试：管理员大大
        User admin = new User();
        admin.setRole("admin");
        admin.setName("超级管理员");
        assertEquals("管理员大大", aiService.getRoleDisplayName(admin));

        // 4. 身份特定问候测试
        String adminReply = aiService.fallbackAnswer("你好", "管理员大大", admin);
        assertTrue(adminReply.contains("管理员大大"));

        String managerReply = aiService.fallbackAnswer("你好", "王叔", maleManager);
        assertTrue(managerReply.contains("王叔"));
        assertTrue(managerReply.contains("巡查"));

        String studentReply = aiService.fallbackAnswer("你好", "李华同学", student);
        assertTrue(studentReply.contains("李华同学"));
    }
}
