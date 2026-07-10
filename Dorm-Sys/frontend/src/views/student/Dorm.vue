<template>
  <div>
    <!-- Standard Hero -->
    <div class="hero">
      <div class="hero-title">
        <div class="hero-icon">
          <el-icon :size="28"><component :is="Home" /></el-icon>
        </div>
        <div>
          <h1>我的宿舍</h1>
          <p class="subtitle">住宿信息 · 室友 · 生活服务</p>
        </div>
      </div>
    </div>

    <div class="grid">
      <!-- Left Stack -->
      <div class="left-stack">
        <!-- Room Stats -->
        <div class="card">
          <div class="card-head"><h2>寝室信息</h2></div>
          <div class="card-body stats">
            <div class="stat">
              <div class="tile-icon"><el-icon :size="22"><component :is="Home" /></el-icon></div>
              <div>
                <span class="muted" style="font-size:13px">楼栋房间</span>
                <span class="num" style="font-size:18px">明德楼 101</span>
              </div>
            </div>
            <div class="stat">
              <div class="tile-icon"><el-icon :size="22"><component :is="BedDouble" /></el-icon></div>
              <div>
                <span class="muted" style="font-size:13px">床位</span>
                <span class="num" style="font-size:18px">1号床</span>
              </div>
            </div>
            <div class="stat">
              <div class="tile-icon"><el-icon :size="22"><component :is="Calendar" /></el-icon></div>
              <div>
                <span class="muted" style="font-size:13px">入住日期</span>
                <span class="num" style="font-size:18px">2025/09/01</span>
              </div>
            </div>
            <div class="stat">
              <div class="tile-icon"><el-icon :size="22"><component :is="Medal" /></el-icon></div>
              <div>
                <span class="muted" style="font-size:13px">卫生评分</span>
                <span class="num" style="font-size:18px">92 分</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Roommates List -->
        <div class="card">
          <div class="card-head"><h2>室友信息</h2></div>
          <div class="card-body list">
            <div class="row">
              <div style="width: 48px; height: 48px; border-radius: 50%; background: linear-gradient(145deg, #334155, #94a3b8); display: grid; place-items: center; color: white; font-size: 18px; font-weight: bold;">张</div>
              <div class="row-main">
                <div class="row-title">
                  <span style="font-size:16px">张伟</span>
                  <span class="tag info">自己</span>
                </div>
                <div class="row-meta">
                  <span>学号：2022010001</span>
                  <span>计科2201</span>
                  <span>1号床</span>
                </div>
              </div>
              <button class="ghost-btn">编辑</button>
            </div>
            <div class="row" v-for="(rm, i) in roommates" :key="i">
              <div style="width: 48px; height: 48px; border-radius: 50%; background: var(--muted-2); display: grid; place-items: center; font-weight: bold; color: var(--text-secondary); font-size: 18px;">{{ rm.name[0] }}</div>
              <div class="row-main">
                <div class="row-title">
                  <span style="font-size:16px">{{ rm.name }}</span>
                  <span v-if="rm.role === 'dormhead'" class="tag warn">宿舍长</span>
                </div>
                <div class="row-meta">
                  <span>学号：{{ rm.sno }}</span>
                  <span>计科2201</span>
                  <span>{{ i + 2 }}号床</span>
                </div>
              </div>
              <button class="ghost-btn">联系</button>
            </div>
          </div>
        </div>

        <!-- Devices List -->
        <div class="card">
          <div class="card-head"><h2>设备状态</h2></div>
          <div class="card-body list">
            <div class="row" style="min-height:56px;border:1px solid var(--line);">
              <span class="tile-icon" style="background:var(--ok-2);color:var(--ok)"><el-icon :size="20"><component :is="Zap" /></el-icon></span>
              <div class="row-main">
                <div class="row-title">空调</div>
                <div class="row-meta"><span>运行正常</span></div>
              </div>
              <span class="status-dot" style="width:8px;height:8px;border-radius:50%;background:var(--ok);"></span>
            </div>
            <div class="row" style="min-height:56px;border:1px solid var(--line);">
              <span class="tile-icon" style="background:var(--ok-2);color:var(--ok)"><el-icon :size="20"><component :is="Wifi" /></el-icon></span>
              <div class="row-main">
                <div class="row-title">网络</div>
                <div class="row-meta"><span>信号良好</span></div>
              </div>
              <span class="status-dot" style="width:8px;height:8px;border-radius:50%;background:var(--ok);"></span>
            </div>
            <div class="row" style="min-height:56px;border:1px solid var(--line);">
              <span class="tile-icon" style="background:var(--warn-2);color:var(--warn)"><el-icon :size="20"><component :is="Lightbulb" /></el-icon></span>
              <div class="row-main">
                <div class="row-title">热水器</div>
                <div class="row-meta"><span>加热中</span></div>
              </div>
              <span class="status-dot" style="width:8px;height:8px;border-radius:50%;background:var(--warn);"></span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Stack -->
      <div class="right-stack">
        <!-- Side Stats -->
        <div class="card">
          <div class="card-head"><h2>寝室状态</h2></div>
          <div class="card-body list">
            <div class="side-stat">
              <el-icon :size="24" color="var(--primary)"><component :is="Users" /></el-icon>
              <div>
                <span class="muted" style="font-size:13px">当前人数</span>
                <strong>4 人</strong>
              </div>
            </div>
            <div class="side-stat">
              <el-icon :size="24" color="var(--primary)"><component :is="WalletCards" /></el-icon>
              <div>
                <span class="muted" style="font-size:13px">待缴费用</span>
                <strong>120.80 元</strong>
              </div>
            </div>
            <div class="side-stat">
              <el-icon :size="24" color="var(--primary)"><component :is="Settings" /></el-icon>
              <div>
                <span class="muted" style="font-size:13px">待处理报修</span>
                <strong>1 件</strong>
              </div>
            </div>
            <div class="side-stat">
              <el-icon :size="24" color="var(--primary)"><component :is="Trophy" /></el-icon>
              <div>
                <span class="muted" style="font-size:13px">卫生排名</span>
                <strong>第 3 名</strong>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Services -->
        <div class="card">
          <div class="card-head"><h2>常用服务</h2></div>
          <div class="card-body quick-grid">
            <button @click="router.push('/student/repair')">
              <el-icon :size="24"><component :is="Settings" /></el-icon>
              <span>申请报修</span>
            </button>
            <button @click="router.push('/student/visitor')">
              <el-icon :size="24"><component :is="UserRoundCheck" /></el-icon>
              <span>访客预约</span>
            </button>
            <button @click="router.push('/student/fees')">
              <el-icon :size="24"><component :is="WalletCards" /></el-icon>
              <span>费用查询</span>
            </button>
            <button>
              <el-icon :size="24"><component :is="Phone" /></el-icon>
              <span>联系宿管</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { 
  Home, BedDouble, Calendar, Medal,
  Zap, Wifi, Lightbulb, Users, WalletCards, Settings, Trophy, UserRoundCheck, Phone
} from '@lucide/vue'

const router = useRouter()
const roommates = [
  { name: '李明', sno: '2022010002', role: 'dormhead' },
  { name: '王芳', sno: '2022010003', role: 'member' },
  { name: '刘洋', sno: '2022010004', role: 'member' }
]
</script>
