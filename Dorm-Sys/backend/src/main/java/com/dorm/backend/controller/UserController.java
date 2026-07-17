package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import com.dorm.backend.entity.User;
import com.dorm.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.entity.StudentInfo;
import com.dorm.backend.entity.ManagerInfo;
import com.dorm.backend.entity.AdminInfo;
import com.dorm.backend.service.StudentInfoService;
import com.dorm.backend.service.ManagerInfoService;
import com.dorm.backend.service.AdminInfoService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.service.BedService;
import com.dorm.backend.entity.Bed;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private StudentInfoService studentInfoService;
    
    @Autowired
    private ManagerInfoService managerInfoService;
    
    @Autowired
    private AdminInfoService adminInfoService;

    @Autowired
    private BedService bedService;

    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> users = userService.list();
        List<StudentInfo> students = studentInfoService.list();
        Map<Long, StudentInfo> studentMap = students.stream()
            .collect(Collectors.toMap(StudentInfo::getUserId, s -> s));
            
        for (User user : users) {
            hidePassword(user);
            if ("student".equals(user.getRole())) {
                StudentInfo info = studentMap.get(user.getId());
                if (info != null) {
                    user.setClassName(info.getClassName());
                    user.setMajor(info.getMajor());
                }
            }
        }
        return Result.success(users);
    }

    @GetMapping("/unassigned")
    public Result<List<User>> getUnassignedStudents(@RequestParam(required = false) Integer gender) {
        List<Long> occupiedIds = bedService.list().stream()
            .filter(b -> b.getStudentId() != null)
            .map(Bed::getStudentId)
            .collect(Collectors.toList());
            
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("role", "student");
        if (gender != null) {
            qw.eq("gender", gender);
        }
        if (!occupiedIds.isEmpty()) {
            qw.notIn("id", occupiedIds);
        }
        
        List<User> users = userService.list(qw);
        for(User u : users) hidePassword(u);
        return Result.success(users);
    }

    @PostMapping("/save")
    public Result<Boolean> save(@RequestBody User user) {
        if (!"admin".equals(AuthUtils.getCurrentUserRole())) {
            Long userId = AuthUtils.getCurrentUserId();
            User existing = userService.getById(userId);
            if (existing == null) {
                return Result.error(404, "用户不存在");
            }
            existing.setName(user.getName());
            existing.setAvatar(user.getAvatar());
            existing.setClassName(user.getClassName());
            existing.setEmail(user.getEmail());
            existing.setPhone(user.getPhone());
            user = existing;
        }
        // Simple mock encryption for prototype (in production use BCrypt)
        if (user.getId() == null && user.getPassword() == null) {
            user.setPassword("123456"); // Default password
        } else if (user.getId() != null && (user.getPassword() == null || user.getPassword().isBlank())) {
            user.setPassword(null);
        }
        boolean res = userService.saveOrUpdate(user);
        
        if (res) {
            Long userId = user.getId();
            String role = user.getRole();
            if ("student".equals(role)) {
                QueryWrapper<StudentInfo> qw = new QueryWrapper<>();
                qw.eq("user_id", userId);
                StudentInfo info = studentInfoService.getOne(qw);
                if (info == null) {
                    info = new StudentInfo();
                    info.setUserId(userId);
                }
                info.setClassName(user.getClassName());
                info.setMajor(user.getMajor());
                studentInfoService.saveOrUpdate(info);
            } else if ("dormmanager".equals(role)) {
                QueryWrapper<ManagerInfo> qw = new QueryWrapper<>();
                qw.eq("user_id", userId);
                ManagerInfo info = managerInfoService.getOne(qw);
                if (info == null) {
                    info = new ManagerInfo();
                    info.setUserId(userId);
                    managerInfoService.save(info);
                }
            } else if ("admin".equals(role)) {
                QueryWrapper<AdminInfo> qw = new QueryWrapper<>();
                qw.eq("user_id", userId);
                AdminInfo info = adminInfoService.getOne(qw);
                if (info == null) {
                    info = new AdminInfo();
                    info.setUserId(userId);
                    adminInfoService.save(info);
                }
            }
        }
        return Result.success(res);
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        if (!"admin".equals(AuthUtils.getCurrentUserRole())) {
            return Result.error(403, "仅管理员可以删除用户");
        }
        return Result.success(userService.removeById(id));
    }

    private void hidePassword(User user) {
        user.setPassword(null);
    }        return null;
    }
}
