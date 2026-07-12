package com.dorm.backend.controller;

import com.dorm.backend.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @GetMapping("/chat")
    public Result chat(@RequestParam String question) {
        // Simple keyword-based mock for AI assistant
        if (question == null) {
            return Result.success("请输入您的问题");
        }
        
        String q = question.toLowerCase();
        String answer = "对不起，我暂时无法回答这个问题。您可以尝试询问关于“门禁”、“报修”、“调宿”或“水电费”的问题，或者联系您的宿舍管理员。";

        if (q.contains("门禁") || q.contains("晚归")) {
            answer = "宿舍门禁通常为每日 23:30。晚归请及时联系宿管，并按要求完成晚归登记。";
        } else if (q.contains("调宿") || q.contains("换宿舍")) {
            answer = "请在“调宿申请”中填写原因并选择目标房间。宿管审批通过后，系统会同步更新床位和宿舍信息。";
        } else if (q.contains("报修") || q.contains("维修") || q.contains("坏了")) {
            answer = "请在“报修申请”中选择故障类型并描述具体情况。提交后可在同一页面跟踪处理进度。";
        } else if (q.contains("水电费") || q.contains("费用") || q.contains("账单")) {
            answer = "可在“费用查询”中查看当前宿舍账单。线上缴费尚未开放，请按页面指引到服务中心办理。";
        } else if (q.contains("访客") || q.contains("来访")) {
            answer = "请在“访客登记”中填写访客信息。宿管审批通过后，访客方可进入宿舍区。";
        }

        return Result.success(answer);
    }
}
