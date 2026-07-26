<template>
  <!-- Trigger Button -->
  <div class="weather-widget">
    <div class="weather-brief" @click="dialogVisible = true" style="cursor: pointer; display: flex; align-items: center; gap: 16px; padding: 12px 20px; background: var(--muted); border-radius: 12px; transition: transform 0.2s;">
      <div style="text-align: center;">
        <div style="font-size: 28px; line-height: 1;">{{ weather.emoji }}</div>
        <div style="font-size: 11px; color: var(--sub); margin-top: 4px;">{{ weather.desc }}</div>
      </div>
      <div style="width: 1px; height: 40px; background: var(--line);"></div>
      <div>
        <div style="display: flex; align-items: baseline; gap: 4px;">
          <div style="font-size: 24px; font-weight: 700; color: var(--text);">{{ weather.temp }}℃</div>
        </div>
        <div style="display: flex; align-items: center; gap: 4px; font-size: 12px; color: var(--ok); margin-top: 2px;">
          <el-icon><component :is="Location" /></el-icon>
          <span>{{ location.name }}</span>
        </div>
      </div>
    </div>
  </div>

  <!-- Weather Dialog — explicitly teleported to body via Vue Teleport -->
  <Teleport to="body">
    <el-dialog v-model="dialogVisible" width="1100px" destroy-on-close :show-close="false" class="weather-dialog-horizontal">
    <template #header="{ close }">
      <div style="display: flex; justify-content: flex-end; width: 100%;">
        <el-icon @click="close" style="cursor: pointer; font-size: 22px; color: var(--sub);"><component :is="X" /></el-icon>
      </div>
    </template>

    <div class="weather-card" v-loading="loadingForecast">
      
      <!-- 第一层：当前地方与当前天气 -->
      <div class="layer-location">
        <div class="loc-header" v-if="!isSearching">
          <div style="display: flex; align-items: center; gap: 16px;">
            <div class="loc-title">
              <el-icon><component :is="Location" /></el-icon>
              <span>{{ location.name }}</span>
            </div>
            <el-button link type="primary" @click="isSearching = true">更换城市</el-button>
          </div>
          <div class="current-weather-display">
            <div class="cw-temp">{{ weather.temp }}<span class="cw-unit">℃</span></div>
            <div class="cw-desc">{{ weather.emoji }} {{ weather.desc }} · {{ weather.wind }}</div>
          </div>
        </div>
        
        <div class="loc-search" v-else>
          <div style="display: flex; gap: 12px; width: 100%; align-items: center;">
            <div style="font-weight: 600; min-width: 80px;">搜索城市:</div>
            <el-input v-model="searchQuery" placeholder="输入城市名称，如：上海" @keyup.enter="searchLocation" style="flex: 1; max-width: 300px;">
              <template #prefix><el-icon><component :is="Search" /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="searchLocation" :loading="searching">搜索</el-button>
            <el-button @click="isSearching = false">取消</el-button>
          </div>
          <div class="search-results-h" v-if="searchResults.length > 0">
            <div v-for="res in searchResults" :key="res.id" @click="selectLocation(res)" class="search-item-h">
              <span style="font-weight: 500;">{{ res.name }}</span>
              <span class="search-meta">{{ res.admin1 || '' }}{{ res.admin1 ? ', ' : '' }}{{ res.country }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 第二层：今天天气的折线图 -->
      <div class="layer-hourly" v-if="hourlyData.length > 0 && !isSearching">
        <div class="layer-title">今日趋势 (未来24小时)</div>
        <div class="chart-container">
          <!-- svg width set to 100% of the 1100px body -->
          <svg width="100%" height="120" viewBox="0 0 1000 120" preserveAspectRatio="none">
            <defs>
              <linearGradient id="lineGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="rgba(59, 130, 246, 0.3)" />
                <stop offset="100%" stop-color="rgba(59, 130, 246, 0)" />
              </linearGradient>
            </defs>
            <polygon :points="areaPoints" fill="url(#lineGrad)" />
            <polyline :points="linePoints" fill="none" stroke="#3b82f6" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" />
            <g v-for="(p, i) in parsedPoints" :key="i">
              <circle :cx="p.x" :cy="p.y" r="4.5" fill="#fff" stroke="#3b82f6" stroke-width="2.5" />
              <text :x="p.x" :y="p.y - 14" fill="var(--text)" font-size="14" text-anchor="middle" font-weight="bold">{{ p.temp }}°</text>
              <text :x="p.x" :y="115" fill="var(--sub)" font-size="12" text-anchor="middle">{{ p.time }}</text>
            </g>
          </svg>
        </div>
      </div>

      <!-- 第三层：近七天的天气简要 -->
      <div class="layer-daily" v-if="!isSearching">
        <div class="layer-title" style="margin-bottom: 12px;">近七天天气简要</div>
        <div class="daily-list-horizontal">
          <div v-for="(day, idx) in dailyForecast" :key="idx" class="daily-item-h">
            <div class="daily-date-h">{{ day.dateLabel }}</div>
            <div class="daily-icon-h">
              <span class="d-emoji-h">{{ day.emoji }}</span> 
              <span class="d-desc-h">{{ day.desc }}</span>
            </div>
            <div class="daily-temp-h">
              <span class="min-temp-h">{{ day.minTemp }}°</span>
              <span class="max-temp-h">{{ day.maxTemp }}°</span>
            </div>
            <div class="temp-bar-bg-h">
              <div class="temp-bar-fill-h" :style="getTempBarStyle(day)"></div>
            </div>
          </div>
        </div>
      </div>
      
    </div>
  </el-dialog>
  </Teleport>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { MapPin as Location, Search, X } from '@lucide/vue'
import { ElMessage } from 'element-plus'

const dialogVisible = ref(false)
const isSearching = ref(false)
const searchQuery = ref('')
const searchResults = ref([])
const searching = ref(false)
const loadingForecast = ref(false)

const location = ref({ name: '北京', lat: 39.90, lon: 116.40 })
const weather = ref({ emoji: '☀️', desc: '获取中', temp: '--', wind: '--' })
const dailyForecast = ref([])
const hourlyData = ref([])

const weekMin = computed(() => dailyForecast.value.length ? Math.min(...dailyForecast.value.map(d => d.minTemp)) : 0)
const weekMax = computed(() => dailyForecast.value.length ? Math.max(...dailyForecast.value.map(d => d.maxTemp)) : 100)

const getTempBarStyle = (day) => {
  const range = weekMax.value - weekMin.value || 1
  const left = ((day.minTemp - weekMin.value) / range) * 100
  const width = ((day.maxTemp - day.minTemp) / range) * 100
  return `left: ${left}%; width: ${width}%;`
}

const parsedPoints = computed(() => {
  if (hourlyData.value.length === 0) return []
  const temps = hourlyData.value.map(d => d.temp)
  const minT = Math.min(...temps)
  const maxT = Math.max(...temps)
  const range = maxT - minT || 1
  
  const width = 1000 
  const height = 120
  const padTop = 28
  const padBottom = 22
  const chartH = height - padTop - padBottom
  
  return hourlyData.value.map((d, i) => {
    // leave margin for edge texts
    const x = 20 + (i / (hourlyData.value.length - 1)) * (width - 40)
    const y = padTop + chartH - ((d.temp - minT) / range) * chartH
    return { x, y, temp: d.temp, time: d.time }
  })
})

const linePoints = computed(() => {
  return parsedPoints.value.map(p => `${p.x},${p.y}`).join(' ')
})

const areaPoints = computed(() => {
  if (parsedPoints.value.length === 0) return ''
  const first = parsedPoints.value[0]
  const last = parsedPoints.value[parsedPoints.value.length - 1]
  return `${first.x},120 ${linePoints.value} ${last.x},120`
})

const getWeatherIcon = (code) => {
  if (code === 0) return { emoji: '☀️', desc: '晴' }
  if (code >= 1 && code <= 3) return { emoji: '🌤️', desc: '多云' }
  if (code >= 45 && code <= 48) return { emoji: '🌫️', desc: '雾' }
  if (code >= 51 && code <= 67) return { emoji: '🌧️', desc: '雨' }
  if (code >= 71 && code <= 77) return { emoji: '❄️', desc: '雪' }
  if (code >= 80 && code <= 82) return { emoji: '🌧️', desc: '阵雨' }
  if (code >= 85 && code <= 86) return { emoji: '❄️', desc: '阵雪' }
  if (code >= 95) return { emoji: '⛈️', desc: '雷暴' }
  return { emoji: '☁️', desc: '阴' }
}

const formatDateLabel = (dateStr, index) => {
  if (index === 0) return '今天'
  if (index === 1) return '明天'
  const date = new Date(dateStr)
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[date.getDay()]
}

const fetchWeather = async () => {
  loadingForecast.value = true
  try {
    const res = await fetch(`https://api.open-meteo.com/v1/forecast?latitude=${location.value.lat}&longitude=${location.value.lon}&current_weather=true&daily=weathercode,temperature_2m_max,temperature_2m_min&hourly=temperature_2m,weathercode&timezone=Asia%2FShanghai`)
    if (res.ok) {
      const data = await res.json()
      
      const cw = data.current_weather
      if (cw) {
        weather.value.temp = Math.round(cw.temperature)
        weather.value.wind = `风速 ${cw.windspeed}km/h`
        const iconInfo = getWeatherIcon(cw.weathercode)
        weather.value.emoji = iconInfo.emoji
        weather.value.desc = iconInfo.desc
      }
      
      const daily = data.daily
      if (daily && daily.time) {
        dailyForecast.value = daily.time.map((timeStr, idx) => {
          const iconInfo = getWeatherIcon(daily.weathercode[idx])
          return {
            dateLabel: formatDateLabel(timeStr, idx),
            emoji: iconInfo.emoji,
            desc: iconInfo.desc,
            maxTemp: Math.round(daily.temperature_2m_max[idx]),
            minTemp: Math.round(daily.temperature_2m_min[idx])
          }
        })
      }
      
      const hourly = data.hourly
      if (hourly && hourly.time && cw) {
        const currentHourStr = cw.time
        let startIndex = hourly.time.findIndex(t => t >= currentHourStr)
        if (startIndex === -1) startIndex = 0
        
        const next24 = []
        for (let i = startIndex; i <= startIndex + 24 && i < hourly.time.length; i++) {
          next24.push({
            time: new Date(hourly.time[i]).getHours() + ':00',
            temp: Math.round(hourly.temperature_2m[i]),
            code: hourly.weathercode[i]
          })
        }
        hourlyData.value = next24
      }
    }
  } catch (e) {
    console.error('Failed to fetch weather', e)
  } finally {
    loadingForecast.value = false
  }
}

const searchLocation = async () => {
  if (!searchQuery.value) return
  searching.value = true
  try {
    const res = await fetch(`https://geocoding-api.open-meteo.com/v1/search?name=${encodeURIComponent(searchQuery.value)}&count=5&language=zh&format=json`)
    if (res.ok) {
      const data = await res.json()
      searchResults.value = data.results || []
      if (searchResults.value.length === 0) {
        ElMessage.warning('未找到该城市，请尝试使用拼音或英文搜索')
      }
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    searching.value = false
  }
}

const selectLocation = (res) => {
  location.value = {
    name: res.name,
    lat: res.latitude,
    lon: res.longitude
  }
  localStorage.setItem('dorm_sys_weather_location', JSON.stringify(location.value))
  searchResults.value = []
  searchQuery.value = ''
  isSearching.value = false
  fetchWeather()
}

onMounted(() => {
  const saved = localStorage.getItem('dorm_sys_weather_location')
  if (saved) {
    try {
      location.value = JSON.parse(saved)
    } catch (e) {}
  }
  fetchWeather()
})
</script>

<style>
/* Override default dialog styles for a horizontal card look */
.weather-dialog-horizontal {
  border-radius: 24px !important;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15) !important;
}
.weather-dialog-horizontal .el-dialog__header {
  padding: 20px 24px 0 !important;
  margin-right: 0 !important;
}
.weather-dialog-horizontal .el-dialog__body {
  padding: 0 28px 28px !important;
}
</style>

<style scoped>
.weather-widget {
  display: inline-block;
}
.weather-brief:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.weather-card {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

/* Layer 1: Location & Current */
.layer-location {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.loc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--bg);
  padding: 16px 24px;
  border-radius: 16px;
}

.loc-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text);
}

.current-weather-display {
  display: flex;
  align-items: center;
  gap: 16px;
}

.cw-temp {
  font-size: 36px;
  font-weight: 800;
  color: var(--text);
  line-height: 1;
}

.cw-unit {
  font-size: 20px;
  font-weight: 500;
  color: var(--sub);
  margin-left: 2px;
}

.cw-desc {
  font-size: 16px;
  color: var(--text);
  font-weight: 500;
}

.search-results-h {
  margin-top: 12px; 
  border: 1px solid var(--line); 
  border-radius: 12px; 
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px;
  background: var(--bg);
}

.search-item-h {
  padding: 8px 16px; 
  cursor: pointer; 
  display: flex; 
  gap: 8px;
  align-items: center;
  background: white;
  border: 1px solid var(--line);
  border-radius: 20px;
  transition: all 0.2s;
}

.search-item-h:hover {
  background-color: var(--primary-2);
  border-color: var(--primary);
  color: var(--primary);
}

.search-meta {
  color: var(--sub); 
  font-size: 12px;
}

/* Layer 2: Hourly Chart */
.layer-hourly {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.layer-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text);
}

.chart-container {
  width: 100%;
  height: 120px;
  background: var(--bg);
  border-radius: 16px;
  overflow: hidden;
}

/* Layer 3: Daily Forecast (Horizontal) */
.layer-daily {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.daily-list-horizontal {
  display: flex;
  gap: 12px;
  justify-content: space-between;
}

.daily-item-h {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 8px;
  background: var(--bg);
  border-radius: 16px;
  transition: transform 0.2s;
}
.daily-item-h:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.05);
}

.daily-date-h {
  font-size: 14px;
  font-weight: 600;
  color: var(--text);
}

.daily-icon-h {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.d-emoji-h {
  font-size: 28px;
  line-height: 1;
}
.d-desc-h {
  font-size: 12px;
  color: var(--sub);
}

.daily-temp-h {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
}
.min-temp-h { color: var(--sub); }
.max-temp-h { color: var(--text); }

.temp-bar-bg-h {
  width: 100%;
  height: 4px;
  background: var(--line);
  border-radius: 2px;
  position: relative;
  overflow: hidden;
  margin-top: 4px;
}
.temp-bar-fill-h {
  position: absolute;
  top: 0;
  height: 100%;
  background: linear-gradient(90deg, #60a5fa, #f59e0b);
  border-radius: 2px;
}
</style>
