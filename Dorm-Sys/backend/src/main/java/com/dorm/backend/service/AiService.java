package com.dorm.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dorm.backend.dto.AiChatRequest;
import com.dorm.backend.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${ai.enabled:true}")
    private boolean enabled;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.base-url:https://api.siliconflow.cn/v1}")
    private String baseUrl;

    @Value("${ai.model:Qwen/Qwen2.5-7B-Instruct}")
    private String defaultModel;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final AiConfigService aiConfigService;
    private final UserService userService;
    private final BedService bedService;
    private final RoomService roomService;
    private final BuildingService buildingService;

    public AiService() {
        this.aiConfigService = null;
        this.userService = null;
        this.bedService = null;
        this.roomService = null;
        this.buildingService = null;
    }

    @Autowired
    public AiService(AiConfigService aiConfigService,
                     UserService userService,
                     BedService bedService,
                     RoomService roomService,
                     BuildingService buildingService) {
        this.aiConfigService = aiConfigService;
        this.userService = userService;
        this.bedService = bedService;
        this.roomService = roomService;
        this.buildingService = buildingService;
    }

    private static final String DORM_REGULATIONS = """
            【本校核心宿舍管理规则】
            1. 门禁规定：宿舍每晚 23:30 关闭大门，晚归同学需在一楼值班室联系宿管登记；
            2. 调宿申请：可在本系统“调宿申请”模块提交理由并挑选意向空床位，由楼栋宿管在线审核；
            3. 故障报修：可在“报修申请”页面选择故障类型并拍照上传描述，宿管将安排专人派工上门维修；
            4. 账单费用：可在“费用查询”查看每月水电公摊明细，支持到宿管中心办理；
            5. 访客接待：外来访客须实名登记，每晚 22:00 前离开宿舍区；
            6. 用电安全：严禁使用 800W 以上电磁炉、电热毯、热得快等大功率违规电器，每周四下午常规联检。
            """;

    public String chat(String question) {
        AiChatRequest request = new AiChatRequest();
        request.setQuestion(question);
        return chat(null, request);
    }

    public String chat(Long currentUserId, AiChatRequest request) {
        String question = request != null ? request.getQuestion() : null;
        User user = (currentUserId != null && userService != null) ? userService.getById(currentUserId) : null;
        String displayName = getRoleDisplayName(user);

        if ((question == null || question.trim().isEmpty()) && (request == null || request.getHistory() == null || request.getHistory().isEmpty())) {
            if (user != null && "admin".equalsIgnoreCase(user.getRole())) {
                return "管理员大大好！我是您的系统全局智能助理小寝。今天全校宿舍运行平稳，随时听候您的差遣！有什么可以协助您？";
            } else if (user != null && isManagerRole(user.getRole())) {
                return displayName + "您好呀！今天楼栋巡查辛苦啦！有什么我可以协助您处理的宿管事务吗？无论晚归排查还是报修派工，随时都可以吩咐我哦！";
            } else {
                return displayName + "你好呀！有什么我可以帮你的吗？无论是宿舍事务、生活日常还是各种百科问题，都可以随时问我哦！";
            }
        }

        AiConfig config = aiConfigService != null ? aiConfigService.getOrCreateConfig() : new AiConfig();
        String effectiveApiKey = (config.getApiKey() != null && !config.getApiKey().isBlank()) ? config.getApiKey() : apiKey;

        // 1. 如果配置了有效的 API Key 且开启了 AI，尝试调用真实大模型接口
        if (enabled && effectiveApiKey != null && !effectiveApiKey.isBlank() && !effectiveApiKey.startsWith("sk-xxx") && !effectiveApiKey.contains("your_siliconflow")) {
            try {
                String llmAnswer = callLlmApi(currentUserId, config, request, effectiveApiKey);
                if (llmAnswer != null && !llmAnswer.isBlank()) {
                    return llmAnswer;
                }
            } catch (Exception e) {
                log.warn("大模型接口调用失败或超时，自动切换为本地专业知识库兜底: {}", e.getMessage());
            }
        }

        // 2. 本地专业规则知识库优雅降级（保证无网或无 API Key 场景下的绝对可用）
        String targetQ = (question != null && !question.isBlank()) ? question : "你好";
        return fallbackAnswer(targetQ.trim(), displayName, user);
    }

    private String callLlmApi(Long currentUserId, AiConfig config, AiChatRequest chatRequest, String effectiveApiKey) throws Exception {
        String endpoint = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";

        // 1. 组装灵动的 System Prompt
        String botName = config.getBotName() != null ? config.getBotName() : "小寝管家";
        String persona = getPersonaPrompt(config.getPersonaType(), botName);
        String userContext = buildUserContext(currentUserId);
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss E"));

        StringBuilder systemPromptBuilder = new StringBuilder();
        systemPromptBuilder.append(persona).append("\n");
        if (!userContext.isEmpty()) {
            systemPromptBuilder.append(userContext).append("\n");
        }
        systemPromptBuilder.append("【当前服务器精准时间】").append(currentTime).append("\n");
        systemPromptBuilder.append(DORM_REGULATIONS).append("\n");
        if (config.getCustomPrompt() != null && !config.getCustomPrompt().isBlank()) {
            systemPromptBuilder.append("【管理员补充特别设定】").append(config.getCustomPrompt()).append("\n");
        }
        log.info("构建的 AI System Prompt: {}", systemPromptBuilder.toString());

        // 2. 组装多轮历史上下文 (Sliding Window，保留最近 6 条历史消息)
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPromptBuilder.toString()));

        List<Map<String, String>> history = chatRequest != null ? chatRequest.getHistory() : null;
        if (history != null && !history.isEmpty()) {
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> m = history.get(i);
                if (m != null) {
                    String role = m.get("role");
                    String content = m.get("content");
                    if (("user".equals(role) || "assistant".equals(role)) && content != null && !content.isBlank()) {
                        messages.add(Map.of("role", role, "content", content));
                    }
                }
            }
        }

        // 如果用户本次的问题尚未包含在历史最后一条，则作为最新一条 user 消息追加
        String q = chatRequest != null ? chatRequest.getQuestion() : null;
        if (q != null && !q.isBlank()) {
            boolean alreadyAppended = !messages.isEmpty()
                    && "user".equals(messages.get(messages.size() - 1).get("role"))
                    && q.trim().equals(messages.get(messages.size() - 1).get("content").trim());
            if (!alreadyAppended) {
                messages.add(Map.of("role", "user", "content", q.trim()));
            }
        }

        // 3. 构建请求 Payload
        String modelName = (config.getModel() != null && !config.getModel().isBlank()) ? config.getModel() : defaultModel;
        Double temperature = config.getTemperature() != null ? config.getTemperature() : 0.85;

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("model", modelName);
        requestPayload.put("messages", messages);
        requestPayload.put("temperature", temperature);
        requestPayload.put("max_tokens", 800);

        String jsonBody = objectMapper.writeValueAsString(requestPayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + effectiveApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .timeout(Duration.ofSeconds(35))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonNode root = objectMapper.readTree(response.body());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).path("message");
                return message.path("content").asText();
            }
        } else {
            log.error("大模型 API 响应非200状态码: {} - {}", response.statusCode(), response.body());
        }
        return null;
    }

    private String getPersonaPrompt(String personaType, String botName) {
        if ("gentle".equalsIgnoreCase(personaType)) {
            return String.format("""
                    你是%s，一位温柔体贴、善解人意的高校后勤管家。
                    你说话温和有耐心，声音像冬日暖阳，擅长倾听并体贴解答各类问题。
                    根据不同用户身份（同学、宿管长辈、管理员大大），适度切换最亲切得体的称呼与口吻。
                    """, botName);
        } else if ("humorous".equalsIgnoreCase(personaType)) {
            return String.format("""
                    你是%s，一位幽默风趣、反应敏捷、接地气的校园专属AI智囊伙伴。
                    你聊天自带幽默感，会恰到好处地开玩笑、抛梗，说话活泼有趣、绝不呆板，但遇到正经问题时依然靠谱给力。
                    面对不同身份（某某同学、王叔/张阿姨、管理员大大），你总能用最舒服贴切的方式互动。
                    """, botName);
        } else if ("formal".equalsIgnoreCase(personaType)) {
            return String.format("""
                    你是%s，高校智慧后勤管理系统的专业智能顾问。
                    你态度严谨、逻辑缜密、条理分明、用词规范大方，能够高效解答各类后勤业务与日常百科咨询。
                    """, botName);
        } else if ("custom".equalsIgnoreCase(personaType)) {
            return String.format("你是%s，请根据管理员设定的定制要求，生动自然地为师生与管理人员提供解答。", botName);
        } else {
            // 默认 vibrant (灵动活泼 / 推荐)
            return String.format("""
                    你是%s，一个充满灵气、高情商、活泼亲切且知识渊博的高校全能AI管家与得力助手。
                    【灵动沟通原则】
                    1. 说话生动有趣、有温度、接地气，适度带点活泼的语气词和表情符号（如 😊、✨、💡、🍕、🛌 等），绝不呆板机械！
                    2. 面对任何日常闲聊、学习建议、后勤管理、科技百科、情感吐槽，都能机智对答，带来陪伴感与实际帮助；
                    3. 严格遵循身份识别规则，对不同角色使用专门的亲切称呼，融入对方的工作与生活场景！
                    """, botName);
        }
    }

    private String buildUserContext(Long userId) {
        if (userId == null || userService == null) return "";
        try {
            User user = userService.getById(userId);
            if (user == null) return "";

            String role = user.getRole();
            String name = (user.getName() != null && !user.getName().isBlank()) ? user.getName() : user.getUsername();
            String roleDisplayName = getRoleDisplayName(user);

            StringBuilder sb = new StringBuilder();
            sb.append("【当前向你咨询的用户画像与身份指令】\n");
            sb.append("- 用户真实姓名：").append(name);
            if (user.getGender() != null) sb.append("（").append(user.getGender()).append("）");
            sb.append("，系统身份角色：").append(getRoleChineseName(role)).append("。\n");

            if ("admin".equalsIgnoreCase(role)) {
                sb.append("- 核心称呼与态度要求：对方是掌控整个高校宿舍系统的最高超级管理员！在交流中请务必崇敬、机敏、专业又略带活泼地尊称对方为“管理员大大”！例如：“管理员大大好！”、“收到，管理员大大，这就为您查询！”。全力协助管理员大大掌控全校宿舍宏观房源入住率、批量调度、权限分配、报修监控、审计日志排查以及AI系统配置等全局运维管理工作。");
            } else if (isManagerRole(role)) {
                String managedBuildings = "负责楼栋巡查与管理";
                try {
                    if (buildingService != null) {
                        QueryWrapper<Building> bw = new QueryWrapper<>();
                        bw.like("manager", name);
                        List<Building> bList = buildingService.list(bw);
                        if (bList != null && !bList.isEmpty()) {
                            StringBuilder bbs = new StringBuilder();
                            for (int i = 0; i < bList.size(); i++) {
                                if (i > 0) bbs.append("、");
                                bbs.append(bList.get(i).getName());
                            }
                            managedBuildings = bbs.toString();
                        }
                    }
                } catch (Exception ignored) {}
                sb.append("- 管辖楼宇：").append(managedBuildings).append("。\n");
                sb.append("- 核心称呼与态度要求：对方是负责宿舍楼宇日常管理的长辈老师！请在交流中亲切、尊敬地称呼对方为“").append(roleDisplayName).append("”（宿管男性长辈称“王叔”，女性长辈称“张阿姨”等长辈尊称）！像贴心、懂事的晚辈小助手一样，全力协助王叔/张阿姨处理楼宇门禁晚归、故障报修派工催办、公摊水电抄表、违规用电排查与调宿审批等日常宿管业务。");
            } else {
                // 学生
                if (user.getClassName() != null) sb.append("- 班级：").append(user.getClassName()).append("，");
                if (bedService != null) {
                    QueryWrapper<Bed> bqw = new QueryWrapper<>();
                    bqw.eq("student_id", userId);
                    Bed bed = bedService.getOne(bqw);
                    if (bed != null) {
                        Room room = (roomService != null) ? roomService.getById(bed.getRoomId()) : null;
                        Building building = (room != null && buildingService != null) ? buildingService.getById(room.getBuildingId()) : null;
                        sb.append("宿舍：");
                        if (building != null) sb.append(building.getName()).append(" ");
                        if (room != null) sb.append(room.getRoomNumber()).append("室 ");
                        sb.append(bed.getBedNumber()).append("号床。");
                    } else {
                        sb.append("宿舍：目前尚未分配宿舍床位。");
                    }
                }
                sb.append("\n- 核心称呼与态度要求：对方是在校同学！请在交流中亲切称呼对方为“").append(roleDisplayName).append("”（如“").append(name).append("同学”）；当他询问“你知道我是谁吗”、“我在哪个宿舍”等身份问题时，务必准确、热情地确认他的名字、班级和所在宿舍房间床位，让他感受到专属智能管家的贴心！");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("构建用户信息画像上下文失败: {}", e.getMessage());
            return "";
        }
    }

    public String getUserDisplayName(Long userId) {
        if (userId == null) return "同学";
        try {
            if (userService != null) {
                User user = userService.getById(userId);
                if (user != null) {
                    return getRoleDisplayName(user);
                }
            }
        } catch (Exception ignored) {}
        return "同学";
    }

    public String getRoleDisplayName(User user) {
        if (user == null) return "同学";
        String role = user.getRole();
        String name = user.getName() != null && !user.getName().isBlank() ? user.getName().trim() : "";
        String gender = user.getGender() != null ? user.getGender().trim() : "";

        if ("admin".equalsIgnoreCase(role)) {
            return "管理员大大";
        } else if (isManagerRole(role)) {
            // 宿管长辈称谓：王叔或者张阿姨
            if (name.contains("叔") || name.contains("阿姨") || name.contains("伯")) {
                return name;
            }
            String surname = !name.isEmpty() ? name.substring(0, 1) : "";
            if ("女".equals(gender)) {
                return !surname.isEmpty() ? surname + "阿姨" : "张阿姨";
            } else if ("男".equals(gender)) {
                return !surname.isEmpty() ? surname + "叔" : "王叔";
            } else {
                if ("张".equals(surname)) return "张阿姨";
                if ("王".equals(surname)) return "王叔";
                return !surname.isEmpty() ? surname + "叔" : "王叔";
            }
        } else {
            // 学生：某某同学
            return !name.isEmpty() ? name + "同学" : "同学";
        }
    }

    public boolean isManagerRole(String role) {
        if (role == null) return false;
        String r = role.toLowerCase();
        return r.equals("dormmanager") || r.equals("manager") || r.equals("dorm_manager");
    }

    public String getRoleChineseName(String role) {
        if ("admin".equalsIgnoreCase(role)) return "系统超级管理员";
        if (isManagerRole(role)) return "楼栋宿管长辈";
        return "在校学生";
    }

    public String fallbackAnswer(String question) {
        return fallbackAnswer(question, "同学", null);
    }

    public String fallbackAnswer(String question, String displayName) {
        return fallbackAnswer(question, displayName, null);
    }

    public String fallbackAnswer(String question, Long userId) {
        User user = (userId != null && userService != null) ? userService.getById(userId) : null;
        String displayName = getRoleDisplayName(user);
        return fallbackAnswer(question, displayName, user);
    }

    public String fallbackAnswer(String question, String displayName, User user) {
        String q = question.toLowerCase();
        String role = user != null ? user.getRole() : "student";
        boolean isAdmin = "admin".equalsIgnoreCase(role);
        boolean isManager = isManagerRole(role);

        // 1. 询问身份 "我是谁" / "你知道我是谁吗"
        if (q.contains("我是谁") || q.contains("知道我是谁") || q.contains("我的名字") || q.contains("我在哪")) {
            if (isAdmin) {
                return "您是掌控我们宿舍管理系统全局运转的超级管理员大大呀！今天全校系统各项指标运行良好，随时听候您的差遣！";
            } else if (isManager) {
                return String.format("您好呀！您是我们宿舍楼栋辛勤负责的%s呀！今天巡楼辛苦啦，有什么宿管工作需要我协助处理的吗？", displayName);
            } else {
                return String.format("您好呀！您是我们的%s呀！有什么关于宿舍生活、报修或调宿的问题，都可以随时问我哦！", displayName);
            }
        }

        // 2. 询问时间
        if ((q.contains("时间") || q.contains("几点") || q.contains("现在是")) && !q.contains("关门") && !q.contains("开门") && !q.contains("锁门") && !q.contains("门禁")) {
            String nowStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (isAdmin) {
                return "管理员大大，当前服务器精准系统时间为：" + nowStr + "。";
            } else if (isManager) {
                return String.format("%s，现在服务器时间是：%s。今晚 23:30 记得准时锁楼栋大门哦！", displayName, nowStr);
            } else {
                return displayName + "，现在服务器时间是：" + nowStr + "。";
            }
        }

        // 3. 门禁 / 晚归
        if (q.contains("门禁") || q.contains("晚归") || q.contains("锁门") || q.contains("关门")) {
            if (isAdmin) {
                return "管理员大大，全校宿舍门禁标准时间统一为每晚 23:30 闭门。各楼栋晚归学生情况由宿管老师在线登记，汇总于审计日志与数据看板中供您随时审查。";
            } else if (isManager) {
                return String.format("%s，宿舍大门每晚 23:30 准时关闭。若有晚归学生，请在一楼值班室“晚归登记”模块录入学号与事由留痕备查，方便次日班级通报哦。", displayName);
            } else {
                return "宿舍大门每晚 23:30 关闭。如有特殊情况需晚归，请提前或到达时联系楼栋宿管老师，并在一楼值班室按要求完成实名登记哦。";
            }
        }

        // 4. 报修 / 维修
        if (q.contains("报修") || q.contains("维修") || q.contains("坏了") || q.contains("坏掉") || q.contains("漏水") || q.contains("灯不亮")) {
            if (isAdmin) {
                return "管理员大大，全校报修监控可在左侧【报修监控】模块中实时巡查各楼栋派工进度、故障类型分布及完工耗时，支持超时工单督办。";
            } else if (isManager) {
                return String.format("%s，本楼栋学生提交的报修可在左侧【报修处理】中直接查看派工，指派师傅上门修好后点击验收即可完成销单。", displayName);
            } else {
                return "请进入“报修申请”页面，选择具体的故障类型并上传照片。提交后宿管将进行派工维修，您可在同一页面实时追踪处理进度。";
            }
        }

        // 5. 调宿申请
        if (q.contains("调宿") || q.contains("换宿") || q.contains("换宿舍") || q.contains("换寝室")) {
            if (isAdmin) {
                return "管理员大大，学生调宿由学生端发起、所属楼栋宿管审批。管理员可在【房间管理】中直观掌控全校房源空满状态，必要时执行跨楼栋批量调度。";
            } else if (isManager) {
                return String.format("%s，学生申请调宿后会进入您的【调宿审批】列表。请核实意向房间空闲床位后点击通过，系统会自动完成床位数据同步。", displayName);
            } else {
                return "您可以在系统左侧菜单点击“调宿申请”，填写申请理由并选定意向空闲房间。提交后由所属楼栋宿管在线审核，审批通过后系统将自动同步变更床位与个人信息。";
            }
        }

        // 6. 水电账单 / 费用
        if (q.contains("水电") || q.contains("电费") || q.contains("水费") || q.contains("费用") || q.contains("账单")) {
            if (isAdmin) {
                return "管理员大大，全校水电大盘数据可在【数据报表】中查看，支持按月度统计与导出各楼栋公摊能耗。";
            } else if (isManager) {
                return String.format("%s，您可在【水电计费】中录入楼栋当月用电用水数据生成账单，并在一楼服务中心为学生办理线下核销。", displayName);
            } else {
                return "您可在“费用查询”模块中查看当前宿舍的月度水电公摊账单及缴费状态。目前支持到一楼宿管服务中心统一办理缴费。";
            }
        }

        // 7. 访客接待
        if (q.contains("访客") || q.contains("来访") || q.contains("朋友") || q.contains("家长")) {
            if (isAdmin) {
                return "管理员大大，各楼栋外来人员登记情况可在全校访客列表中统一监管，严格执行夜间 22:00 清楼离访规范。";
            } else if (isManager) {
                return String.format("%s，外来访客须在【访客登记】中如实填报身份信息，经您审批后方可入楼，且须在每晚 22:00 前离开楼栋。", displayName);
            } else {
                return "外来访客进入宿舍区前须进行实名登记。请在“访客登记”中提交访客信息，宿管审批通过后方可进入，且访客须在每晚 22:00 前离开楼栋。";
            }
        }

        // 默认兜底问候
        if (isAdmin) {
            return "管理员大大好！我是您的系统全局智能助理小寝。今天全校宿舍运行正常，随时听候您的差遣！您可以向我咨询全校入住率、报修监控、权限设置或系统运维分析哦！";
        } else if (isManager) {
            return String.format("%s您好呀！今天楼栋巡查辛苦啦！我是您的智能宿管助手小寝。有什么需要我协助的吗？无论晚归排查还是报修派工，随时都可以吩咐我哦！", displayName);
        } else {
            return String.format("%s你好呀！我是你的智慧校园 AI 助手小寝管家。无论宿舍事务、生活日常还是各种百科闲聊，都可以随时问我哦！", displayName);
        }
    }
}
