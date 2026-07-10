import { reactive, watch } from 'vue'

const INITIAL_DATA = {
  users: [
    { id: 1, name: "张伟", role: "student", studentId: "2022010001", class: "计科2201", dorm: "明德楼101", bed: "1号床", email: "stu001@stu.edu.cn" },
    { id: 2, name: "李明", role: "student", studentId: "2022010002", class: "计科2201", dorm: "明德楼101", bed: "2号床", email: "stu002@stu.edu.cn" },
    { id: 3, name: "王芳", role: "student", studentId: "2022010003", class: "计科2201", dorm: "明德楼101", bed: "3号床", email: "stu003@stu.edu.cn" },
    { id: 4, name: "刘洋", role: "student", studentId: "2022010004", class: "计科2201", dorm: "明德楼101", bed: "4号床", email: "stu004@stu.edu.cn" },
    { id: 5, name: "陈欣", role: "student", studentId: "2023020001", class: "软工2301", dorm: "至善楼102", bed: "1号床", email: "stu005@stu.edu.cn" },
    { id: 6, name: "赵丽", role: "student", studentId: "2023020002", class: "软工2301", dorm: "至善楼102", bed: "2号床", email: "stu006@stu.edu.cn" },
    { id: 7, name: "周强", role: "dormmanager", studentId: "MG001", class: "-", dorm: "明德楼", bed: "-", email: "zhou@stu.edu.cn" },
    { id: 8, name: "吴芳", role: "dormmanager", studentId: "MG002", class: "-", dorm: "至善楼", bed: "-", email: "wu@stu.edu.cn" }
  ],
  buildings: [
    { id: 1, name: "明德楼", rooms: 20, beds: 40, occupied: 12 },
    { id: 2, name: "至善楼", rooms: 12, beds: 24, occupied: 4 },
    { id: 3, name: "书院楼", rooms: 8, beds: 16, occupied: 0 }
  ],
  repairs: [
    { id: 1, room: "至善楼101", issue: "卫生间水龙头漏水", userId: 5, submitTime: "2026-01-05 09:00", type: "水管", status: "pending" },
    { id: 2, room: "明德楼101", issue: "网络信号时断时续，无法正常上网", userId: 2, submitTime: "2025-12-10 14:30", type: "网络", status: "processing" },
    { id: 3, room: "明德楼101", issue: "空调不制冷，开机后只有风扇转动", userId: 1, submitTime: "2025-12-01 10:00", type: "电器", status: "completed" },
    { id: 4, room: "明德楼102", issue: "窗户关不严，有风漏进来", userId: 4, submitTime: "2025-11-20 11:00", type: "门窗", status: "completed" }
  ],
  bills: [
    { id: 1, dorm: "明德楼101", type: "电费", amount: 92.30, status: "unpaid", period: "2025-12" },
    { id: 2, dorm: "明德楼101", type: "水费", amount: 28.50, status: "unpaid", period: "2025-12" },
    { id: 3, dorm: "明德楼101", type: "电费", amount: 85.50, status: "paid", period: "2025-11", payTime: "2025-12-05 10:00" },
    { id: 4, dorm: "明德楼101", type: "水费", amount: 25.00, status: "paid", period: "2025-11", payTime: "2025-12-05 10:05" }
  ],
  notices: [
    { id: 1, title: "2026年寒假放假通知", date: "2026-01-10", pinned: true },
    { id: 2, title: "关于开展宿舍卫生大检查的通知", date: "2025-12-10", pinned: false },
    { id: 3, title: "宿舍用电安全须知", date: "2025-12-01", pinned: false },
    { id: 4, title: "校园网络维护通知", date: "2025-11-25", pinned: false }
  ],
  visitors: [
    { id: 1, name: "张三", targetUser: "张伟", purpose: "探望", status: "approved", date: "2026-03-20" },
    { id: 2, name: "李四", targetUser: "张伟", purpose: "送物品", status: "pending", date: "2026-03-25" },
    { id: 3, name: "王五", targetUser: "李明", purpose: "送生活用品", status: "rejected", date: "2026-03-18" }
  ]
};

const stored = localStorage.getItem("dorm-data");
const initial = stored ? JSON.parse(stored) : INITIAL_DATA;

export const dataStore = reactive(initial);

watch(dataStore, (newVal) => {
  localStorage.setItem("dorm-data", JSON.stringify(newVal));
}, { deep: true });

export function resetData() {
  Object.assign(dataStore, INITIAL_DATA);
}
