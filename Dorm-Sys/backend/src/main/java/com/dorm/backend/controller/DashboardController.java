package com.dorm.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.common.Result;
import com.dorm.backend.entity.*;
import com.dorm.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
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
        
        // 5. Power Chart (Pseudo-random based on room id)
        List<Integer> powerHistory = new ArrayList<>();
        int baseSeed = myRoomId != null ? myRoomId.hashCode() : 42;
        Random rand = new Random(baseSeed + Calendar.getInstance().get(Calendar.MONTH));
        int totalPower = 0;
        for (int i = 0; i < 12; i++) {
            int val = 30 + rand.nextInt(50);
            powerHistory.add(val);
            totalPower += val;
        }
        data.put("powerHistory", powerHistory);
        data.put("totalPower", totalPower);
        
        return Result.success(data);
    }

    @GetMapping("/buildings")
    public Result<List<Map<String, Object>>> getBuildingStats() {
        List<Building> buildings = buildingService.list();
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
            
            long occupied = bBeds.stream().filter(bed -> bed.getStudentId() != null && "OCCUPIED".equals(bed.getStatus())).count();
            
            stat.put("totalRooms", bRooms.size());
            stat.put("totalBeds", bBeds.size());
            stat.put("occupiedBeds", occupied);
            
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
            return Result.error("未分配宿舍");
        }
        Long myRoomId = myBed.getRoomId();
        
        Room room = roomService.getById(myRoomId);
        Building building = room != null ? buildingService.getById(room.getBuildingId()) : null;
        
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
            String type = r.getType();
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
