<template>
  <div>
    <div class="grid col-2-right">
      <!-- Left Column: Chat Window -->
      <div class="card chat-shell" style="padding: 0; display: grid; grid-template-rows: 1fr auto; overflow: hidden; height: 100%; border: none;">
        <!-- Chat Area (Empty State) -->
        <div class="chat-empty" style="background: var(--surface);">
          <div class="bulb">
            <el-icon :size="44"><component :is="Lightbulb" /></el-icon>
          </div>
          <h2 style="margin-bottom: 8px;">您好，我是您的智慧宿舍助手</h2>
          <p class="subtitle" style="margin-bottom: 24px;">您可以询问我任何关于宿舍生活、规定、费用或报修的问题</p>
          
          <div class="prompt-grid">
            <button>
              <span class="tile-icon" style="background: var(--info-2); color: var(--info)"><el-icon><component :is="Clock" /></el-icon></span>
              <span>门禁查询</span>
            </button>
            <button>
              <span class="tile-icon" style="background: var(--primary-2); color: var(--primary)"><el-icon><component :is="Repeat" /></el-icon></span>
              <span>调宿流程</span>
            </button>
            <button>
              <span class="tile-icon" style="background: var(--orange-2); color: var(--orange)"><el-icon><component :is="Settings" /></el-icon></span>
              <span>报修指引</span>
            </button>
            <button>
              <span class="tile-icon" style="background: var(--ok-2); color: var(--ok)"><el-icon><component :is="Wallet" /></el-icon></span>
              <span>缴费咨询</span>
            </button>
          </div>
        </div>

        <!-- Chat Input -->
        <div style="padding: 24px; border-top: 1px solid var(--line); background: var(--surface);">
          <div style="display: flex; gap: 12px; align-items: center; background: var(--bg); border-radius: 28px; padding: 6px 6px 6px 20px; border: 1px solid var(--line);">
            <input type="text" placeholder="询问我关于宿舍的任何问题..." style="flex: 1; border: none; background: transparent; outline: none; color: var(--text); font-size: 15px;" />
            <button class="primary-btn" style="width: 44px; height: 44px; border-radius: 50%; padding: 0; display: grid; place-items: center; cursor: pointer; flex-shrink: 0; border: none;">
              <el-icon :size="20"><component :is="Send" /></el-icon>
            </button>
          </div>
          <div style="text-align: center; font-size: 12px; color: var(--sub); margin-top: 16px;">由智慧宿舍大模型提供技术支持</div>
        </div>
      </div>

      <!-- Right Column: Sidebar -->
      <div style="display: grid; gap: 24px; align-content: start;">
        <!-- AI Tips Accordion -->
        <div class="card">
          <div class="card-head" style="display: flex; justify-content: space-between; align-items: center;">
            <h2>智慧锦囊</h2>
            <el-icon color="var(--primary)"><component :is="BookOpen" /></el-icon>
          </div>
          <div class="card-body list">
             <div class="row" style="align-items: flex-start;">
               <span class="tile-icon" style="background: var(--warn-2); color: var(--warn)"><el-icon><component :is="AlertCircle" /></el-icon></span>
               <div class="row-main">
                 <div class="row-title" style="margin-bottom: 8px;">核心管理规定</div>
                 <div style="font-size: 13px; color: var(--sub); line-height: 1.8;">
                   <div>• 门禁: 每日 23:30 关闭</div>
                   <div>• 用电: 严禁 800W 以上电器</div>
                   <div>• 访客: 22:00 前离开楼栋</div>
                   <div>• 卫生: 每周四下午检查</div>
                 </div>
               </div>
             </div>
             <div class="row" style="cursor: pointer;">
               <span class="tile-icon"><el-icon><component :is="Settings" /></el-icon></span>
               <div class="row-main"><div class="row-title">报修小贴士</div></div>
               <el-icon color="var(--sub)"><component :is="ChevronRight" /></el-icon>
             </div>
             <div class="row" style="cursor: pointer;">
               <span class="tile-icon"><el-icon><component :is="Zap" /></el-icon></span>
               <div class="row-main"><div class="row-title">用电安全指南</div></div>
               <el-icon color="var(--sub)"><component :is="ChevronRight" /></el-icon>
             </div>
          </div>
        </div>

        <!-- Hot Questions -->
        <div class="card">
          <div class="card-head">
            <h2>热门咨询</h2>
          </div>
          <div class="card-body list">
             <div class="row" style="cursor: pointer;">
               <div class="row-main"><div class="row-title" style="color: var(--primary); font-weight: 500;">水电费账单查询</div></div>
               <el-icon color="var(--sub)"><component :is="ChevronRight" /></el-icon>
             </div>
             <div class="row" style="cursor: pointer;">
               <div class="row-main"><div class="row-title">遥控器遗失处理</div></div>
               <el-icon color="var(--sub)"><component :is="ChevronRight" /></el-icon>
             </div>
             <div class="row" style="cursor: pointer;">
               <div class="row-main"><div class="row-title">调宿申请资格</div></div>
               <el-icon color="var(--sub)"><component :is="ChevronRight" /></el-icon>
             </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { MessageCircle, Plus, Lightbulb, Clock, Repeat, Settings, Wallet, Send, BookOpen, AlertCircle, ChevronDown, ChevronRight, Zap } from '@lucide/vue'
</script>

<style scoped>
/* Scoped styles are mostly unnecessary now, relying on global style.css */
</style>
