package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.common.AuthUtils;
import com.dorm.backend.entity.*;
import com.dorm.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private FeeBillService feeBillService;
    
    @Autowired
    private RepairRequestService repairRequestService;
    
    @Autowired
    private HygieneRecordService hygieneRecordService;
    
    @Autowired
    private VisitorRecordService visitorRecordService;
    
    @Autowired
    private BedService bedService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private BuildingService buildingService;

    @Autowired
    private UserService userService;

    @Autowired
    private DormManagerScopeService managerScopeService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("studentCount", userService.count(new QueryWrapper<User>().eq("role", "student")));
        stats.put("managerCount", userService.count(new QueryWrapper<User>().eq("role", "dormmanager")));
        stats.put("buildingCount", buildingService.count());
        stats.put("roomCount", roomService.count());
        return Result.success(stats);
    }

    @GetMapping("/alerts")
    public Result<List<Map<String, Object>>> getAlerts() {
        List<Map<String, Object>> alerts = new ArrayList<>();
        long pendingRepairs = repairRequestService.count(new QueryWrapper<RepairRequest>()
            .in("status", List.of("PENDING", "PROCESSING")));
        if (pendingRepairs > 0) {
            alerts.add(alert("当前有 " + pendingRepairs + " 条报修工单待处理", "warning", "查看工单", "/admin/repairs"));
        }
        long maintenanceRooms = roomService.count(new QueryWrapper<Room>().eq("status", "MAINTENANCE"));
        if (maintenanceRooms > 0) {
            alerts.add(alert("当前有 " + maintenanceRooms + " 间宿舍处于维护状态", "error", "查看房间", "/admin/resources/rooms"));
        }
        return Result.success(alerts);
    }

    private Map<String, Object> alert(String title, String type, String action, String url) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("title", title);
        alert.put("type", type);
        alert.put("action", action);
        alert.put("url", url);
        return alert;
    }

    @GetMapping("/student")
    public Result<Map<String, Object>> getStudentDashboard() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) return Result.error("未登录");
        Object idObj = attr.getRequest().getAttribute("currentUserId");
        if (idObj == null) return Result.error("未登录");
        
        Long userId = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
        
        Map<String, Object> data = new HashMap<>();
        
        // Find user's room
        Bed myBed = bedService.getOne(new QueryWrapper<Bed>().eq("student_id", userId));
        Long myRoomId = myBed != null ? myBed.getRoomId() : null;
        
        // 1. Account Balance (Sum of UNPAID fee bills for the room)
        BigDecimal unpaidAmount = BigDecimal.ZERO;
        if (myRoomId != null) {
            List<FeeBill> bills = feeBillService.list(new QueryWrapper<FeeBill>()
                .eq("room_id", myRoomId)
                .eq("status", "UNPAID"));
            for (FeeBill bill : bills) {
                unpaidAmount = unpaidAmount.add(bill.getAmount());
            }
        }
        data.put("unpaidAmount", unpaidAmount);
        
        // 2. Pending Repair Count (for the user)
        long pendingRepairs = repairRequestService.count(new QueryWrapper<RepairRequest>()
            .eq("submitter_id", userId)
            .eq("status", "PENDING"));
        data.put("pendingRepairs", pendingRepairs);
        
        // 3. Hygiene Score (Latest for the room)
        Integer latestHygieneScore = null;
        if (myRoomId != null) {
            HygieneRecord hr = hygieneRecordService.getOne(new QueryWrapper<HygieneRecord>()
                .eq("room_id", myRoomId)
                .orderByDesc("check_date")
                .last("LIMIT 1"));
            if (hr != null) latestHygieneScore = hr.getScore();
        }
        data.put("hygieneScore", latestHygieneScore != null ? latestHygieneScore : "--");
        
        // 4. Todo List Items
        List<Map<String, Object>> todoList = new ArrayList<>();
        
        // Todo: Unpaid bills
        if (myRoomId != null) {
            List<FeeBill> bills = feeBillService.list(new QueryWrapper<FeeBill>()
                .eq("room_id", myRoomId)
                .eq("status", "UNPAID")
                .orderByDesc("create_time"));
            for (FeeBill bill : bills) {
                Map<String, Object> todo = new HashMap<>();
                todo.put("id", "fee_" + bill.getId());
                todo.put("type", "fee");
                todo.put("title", "缴纳" + bill.getMonth() + (bill.getType().equals("ELECTRICITY") ? "电费" : "水费"));
                todo.put("meta1", "金额：¥ " + bill.getAmount());
                todo.put("meta2", "尽快缴纳");
                todo.put("statusColor", "var(--warn)");
                todo.put("actionLabel", "去缴纳");
                todo.put("route", "/student/fees");
                todoList.add(todo);
            }
        }
        
        // Todo: Processing Repairs
        List<RepairRequest> processingRepairs = repairRequestService.list(new QueryWrapper<RepairRequest>()
            .eq("submitter_id", userId)
            .eq("status", "PROCESSING"));
        for (RepairRequest r : processingRepairs) {
            Map<String, Object> todo = new HashMap<>();
            todo.put("id", "repair_" + r.getId());
            todo.put("type", "repair");
            todo.put("title", "报修处理中");
            todo.put("meta1", r.getType() + " - " + r.getDescription());
            todo.put("meta2", "师傅正在处理");
            todo.put("statusColor", "var(--primary)");
            todo.put("actionLabel", "查看详情");
            todo.put("route", "/student/repair");
            todoList.add(todo);
        }
        
        // Todo: Pending Visitor Requests
        List<VisitorRecord> pendingVisitors = visitorRecordService.list(new QueryWrapper<VisitorRecord>()
            .eq("student_id", userId)
            .eq("status", "PENDING"));
        for (VisitorRecord v : pendingVisitors) {
            Map<String, Object> todo = new HashMap<>();
            todo.put("id", "visitor_" + v.getId());
            todo.put("type", "visitor");
            todo.put("title", "访客预约审批中");
            SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日");
            String vDate = v.getVisitTime() != null ? sdf.format(v.getVisitTime()) : "";
            todo.put("meta1", v.getVisitorName() + " · " + vDate + "来访");
            todo.put("meta2", "待宿管审批");
            todo.put("statusColor", "var(--sub)");
            todo.put("actionLabel", "查看");
            todo.put("route", "/student/visitor");
            todoList.add(todo);
        }
        
        data.put("todoList", todoList);
        
        List<BigDecimal> electricityFeeHistory = new ArrayList<>();
        List<String> electricityFeeMonths = new ArrayList<>();
        BigDecimal totalElectricityFee = BigDecimal.ZERO;
        Map<String, BigDecimal> electricityFeesByMonth = new HashMap<>();
        if (myRoomId != null) {
            List<FeeBill> electricityBills = feeBillService.list(new QueryWrapper<FeeBill>()
                .eq("room_id", myRoomId)
                .eq("type", "ELECTRICITY"));
            for (FeeBill bill : electricityBills) {
                if (bill.getMonth() != null && bill.getAmount() != null) {
                    electricityFeesByMonth.merge(bill.getMonth(), bill.getAmount(), BigDecimal::add);
                }
            }
        }
        YearMonth currentMonth = YearMonth.now();
        for (int offset = 11; offset >= 0; offset--) {
            YearMonth month = currentMonth.minusMonths(offset);
            BigDecimal amount = electricityFeesByMonth.getOrDefault(month.toString(), BigDecimal.ZERO);
            electricityFeeMonths.add(month.getMonthValue() + "月");
            electricityFeeHistory.add(amount);
            totalElectricityFee = totalElectricityFee.add(amount);
        }
        data.put("electricityFeeMonths", electricityFeeMonths);
        data.put("electricityFeeHistory", electricityFeeHistory);
        data.put("totalElectricityFee", totalElectricityFee);
        
        return Result.success(data);
    }

    @GetMapping("/buildings")
    public Result<List<Map<String, Object>>> getBuildingStats() {
        List<Building> buildings = buildingService.list();
        if ("dormmanager".equals(AuthUtils.getCurrentUserRole())) {
            List<Long> managedBuildingIds = managerScopeService.managedBuildingIds(AuthUtils.getCurrentUserId());
            buildings = buildings.stream().filter(building -> managedBuildingIds.contains(building.getId())).toList();
        }
        List<Room> allRooms = roomService.list();
        List<Bed> allBeds = bedService.list();
        
        List<Map<String, Object>> res = new ArrayList<>();
        
        for (Building b : buildings) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("id", b.getId());
            stat.put("name", b.getName());
            
            List<Room> bRooms = allRooms.stream().filter(r -> r.getBuildingId().equals(b.getId())).collect(Collectors.toList());
            List<Long> bRoomIds = bRooms.stream().map(Room::getId).collect(Collectors.toList());
            
            List<Bed> bBeds = allBeds.stream().filter(bed -> bRoomIds.contains(bed.getRoomId())).collect(Collectors.toList());
            
            long occupied = bBeds.stream().filter(bed -> bed.getStudentId() != null).count();
            int percentage = bBeds.isEmpty()
                ? 0
                : (int) Math.round(occupied * 100.0 / bBeds.size());
            
            stat.put("totalRooms", bRooms.size());
            stat.put("totalBeds", bBeds.size());
            stat.put("occupiedBeds", occupied);
            stat.put("percentage", percentage);
            stat.put("status", percentage >= 100 ? "已满" : percentage >= 80 ? "紧张" : "正常");
            
            res.add(stat);
        }
        
        return Result.success(res);
    }
    
    @GetMapping("/dorm")
    public Result<Map<String, Object>> getDormDashboard() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) return Result.error("未登录");
        Object idObj = attr.getRequest().getAttribute("currentUserId");
        if (idObj == null) return Result.error("未登录");
        
        Long userId = idObj instanceof Number ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
        
        Map<String, Object> data = new HashMap<>();
        
        Bed myBed = bedService.getOne(new QueryWrapper<Bed>().eq("student_id", userId));
        if (myBed == null || myBed.getRoomId() == null) {
            data.put("myBed", null);
            data.put("room", null);
            data.put("building", null);
            data.put("roommates", List.of());
            return Result.success(data);
        }
        Long myRoomId = myBed.getRoomId();
        
        Room room = roomService.getById(myRoomId);
        Building building = room != null ? buildingService.getById(room.getBuildingId()) : null;

        data.put("myBed", myBed);
        data.put("room", room);
        data.put("building", building);

        List<Bed> roomBeds = bedService.list(new QueryWrapper<Bed>()
            .eq("room_id", myRoomId)
            .isNotNull("student_id"));
        List<Long> roommateIds = roomBeds.stream()
            .map(Bed::getStudentId)
            .filter(Objects::nonNull)
            .filter(id -> !id.equals(userId))
            .toList();
        Map<Long, User> roommateUsers = roommateIds.isEmpty()
            ? Collections.emptyMap()
            : userService.list(new QueryWrapper<User>().in("id", roommateIds)).stream()
                .collect(Collectors.toMap(User::getId, user -> user));
        List<Map<String, Object>> roommates = new ArrayList<>();
        for (Bed bed : roomBeds) {
            if (bed.getStudentId() == null || bed.getStudentId().equals(userId)) continue;
            User user = roommateUsers.get(bed.getStudentId());
            if (user == null) continue;
            Map<String, Object> roommate = new HashMap<>();
            roommate.put("id", user.getId());
            roommate.put("name", user.getName());
            roommate.put("username", user.getUsername());
            roommate.put("avatar", user.getAvatar());
            roommate.put("bedNumber", bed.getBedNumber());
            roommates.add(roommate);
        }
        data.put("roommates", roommates);
        
        data.put("campus", building != null ? building.getLocation() : "主校区");
        
        // Facilities status (Lighting, AC, Network, Water)
        // If there's an active (PENDING/PROCESSING) repair request containing keywords, mark as warning
        List<RepairRequest> activeRepairs = repairRequestService.list(new QueryWrapper<RepairRequest>()
            .eq("room_id", myRoomId)
            .in("status", Arrays.asList("PENDING", "PROCESSING")));
            
        boolean lightIssue = false;
        boolean acIssue = false;
        boolean netIssue = false;
        boolean waterIssue = false;
        
        for (RepairRequest r : activeRepairs) {
            String type = r.getType() == null ? "" : r.getType();
            if (type.contains("灯") || type.contains("电")) lightIssue = true;
            if (type.contains("空调")) acIssue = true;
            if (type.contains("网")) netIssue = true;
            if (type.contains("水")) waterIssue = true;
        }
        
        data.put("lightIssue", lightIssue);
        data.put("acIssue", acIssue);
        data.put("netIssue", netIssue);
        data.put("waterIssue", waterIssue);
        
        // Hygiene Metrics
        List<HygieneRecord> hRecords = hygieneRecordService.list(new QueryWrapper<HygieneRecord>()
            .eq("room_id", myRoomId)
            .orderByDesc("check_date"));
            
        int currentScore = hRecords.isEmpty() ? 100 : hRecords.get(0).getScore();
        double avgScore = 100;
        if (!hRecords.isEmpty()) {
            avgScore = hRecords.stream().mapToInt(HygieneRecord::getScore).average().orElse(100);
        }
        
        // Calculate rank among all rooms in the same building
        int rank = 1;
        if (building != null) {
            List<Room> allRoomsInBuilding = roomService.list(new QueryWrapper<Room>().eq("building_id", building.getId()));
            List<Long> roomIds = allRoomsInBuilding.stream().map(Room::getId).collect(Collectors.toList());
            if (!roomIds.isEmpty()) {
                List<HygieneRecord> allRecords = hygieneRecordService.list(new QueryWrapper<HygieneRecord>().in("room_id", roomIds));
                Map<Long, Double> roomAvg = allRecords.stream().collect(
                    Collectors.groupingBy(HygieneRecord::getRoomId, Collectors.averagingInt(HygieneRecord::getScore))
                );
                
                double myAvg = roomAvg.getOrDefault(myRoomId, avgScore);
                for (Double a : roomAvg.values()) {
                    if (a > myAvg) rank++;
                }
            }
        }
        
        data.put("hygieneCurrentScore", currentScore);
        data.put("hygieneAverageScore", Math.round(avgScore));
        data.put("hygieneRank", rank);
        
        return Result.success(data);
    }
}
