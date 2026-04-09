<template>
  <section class="weather-section" aria-label="天气信息">
    <el-card shadow="never" class="weather-card glass-panel">
      <div class="weather-container">
        <div class="weather-main">
          <div class="weather-icon-wrapper">
            <el-icon :size="48" class="weather-icon-dynamic">
              <Sunny v-if="weatherData.weather && weatherData.weather.includes('晴')" />
              <Cloudy v-else-if="weatherData.weather && weatherData.weather.includes('云')" />
              <Pouring v-else-if="weatherData.weather && weatherData.weather.includes('雨')" />
              <PartlyCloudy v-else />
            </el-icon>
          </div>
          <div class="weather-info">
            <div class="location-row">
              <el-icon><Location /></el-icon>
              <span 
                v-if="!isEditingCity" 
                @click="startEditCity" 
                class="city-name" 
                :title="weatherData.city ? '点击切换城市' : '点击手动输入城市'"
                tabindex="0"
                role="button"
                @keydown.enter="startEditCity"
              >{{ weatherData.city || '点击输入城市' }}</span>
              <el-input 
                v-else 
                v-model="inputCity" 
                placeholder="输入城市" 
                size="small" 
                class="city-input"
                @blur="handleCitySubmit" 
                @keyup.enter="handleCitySubmit" 
                ref="cityInputRef"
                aria-label="输入城市名称"
              />
            </div>
            <div class="temp-row" v-if="weatherData.weather">
              <span class="temp">{{ weatherData.temperature }}°</span>
              <span class="cond">{{ weatherData.weather }}</span>
            </div>
            <div class="meta-row" v-if="weatherData.weather">
              <span class="meta-item">湿度 {{ weatherData.humidity }}%</span>
              <span class="meta-divider">|</span>
              <span class="meta-item">更新 {{ formatTimeSimple(weatherData.reportTime) }}</span>
            </div>
            <div v-else class="loading-text">{{ statusText }}</div>
          </div>
        </div>
        
        <div class="weather-tips-wrapper">
          <div class="tip-card">
            <el-icon class="tip-icon"><ChatDotRound /></el-icon>
            <p class="tip-text">{{ weatherTip }}</p>
          </div>
        </div>
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { Location, Sunny, Cloudy, Pouring, PartlyCloudy, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  weatherData: {
    type: Object,
    required: true,
    default: () => ({
      city: '',
      weather: '',
      temperature: '',
      humidity: '',
      reportTime: ''
    })
  },
  statusText: {
    type: String,
    default: '正在获取位置...'
  },
  amapKey: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['city-change'])

const isEditingCity = ref(false)
const inputCity = ref('')
const cityInputRef = ref(null)

const weatherTip = computed(() => {
  if (!props.weatherData.weather) return '今天也要记得照顾好植物哦~'
  if (props.weatherData.weather.includes('雨')) return '今天下雨，户外植物记得避雨，室内注意空气流通。'
  if (props.weatherData.weather.includes('晴') || props.weatherData.weather.includes('多云')) return '天气不错，适合给植物晒晒太阳！'
  if (props.weatherData.weather.includes('雪')) return '下雪啦，注意给植物保暖。'
  if (props.weatherData.weather.includes('阴')) return '阴天光照不足，喜阳植物可能没精神。'
  return '根据天气调整植物养护计划吧！'
})

const formatTimeSimple = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.split(' ')[1] || timeStr
}

const startEditCity = () => {
  isEditingCity.value = true
  inputCity.value = props.weatherData.city
  nextTick(() => {
    cityInputRef.value?.focus()
  })
}

const handleCitySubmit = async () => {
  if (!inputCity.value) {
    isEditingCity.value = false
    return
  }

  try {
    const geoRes = await fetch(`https://restapi.amap.com/v3/geocode/geo?address=${inputCity.value}&key=${props.amapKey}`)
    const geoData = await geoRes.json()
    if (geoData.status === '1' && geoData.geocodes && geoData.geocodes.length > 0) {
      const adcode = geoData.geocodes[0].adcode
      emit('city-change', adcode)
      isEditingCity.value = false
    } else {
      ElMessage.warning('找不到该城市')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/responsive.scss';

.weather-section {
  margin-bottom: var(--spacing-xl);
}

.weather-card {
  border: none;
  background: linear-gradient(120deg, var(--color-primary-light-9) 0%, #fff 100%);
  position: relative;
  overflow: hidden;
  
  &::after {
    content: '';
    position: absolute;
    right: -20px;
    top: -20px;
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, var(--color-primary-light-7) 0%, transparent 70%);
    opacity: 0.5;
    border-radius: 50%;
  }
}

.weather-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 2;
  flex-wrap: wrap;
  gap: var(--spacing-lg);
}

.weather-main {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
  
  .weather-icon-dynamic {
    color: var(--color-warning);
    animation: float 3s ease-in-out infinite;
  }
  
  .weather-info {
    display: flex;
    flex-direction: column;
    
    .location-row {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 16px;
      color: var(--color-text-secondary);
      
      .city-name {
        cursor: pointer;
        border-bottom: 1px dashed transparent;
        transition: all 0.3s;
        
        &:hover,
        &:focus-visible {
          color: var(--color-primary);
          border-bottom-color: var(--color-primary);
        }
      }
      
      .city-input {
        width: 120px;
      }
    }
    
    .temp-row {
      display: flex;
      align-items: baseline;
      gap: var(--spacing-md);
      margin-top: 4px;
      
      .temp {
        font-size: 42px;
        font-weight: 700;
        color: var(--color-text-main);
        line-height: 1;
      }
      
      .cond {
        font-size: 20px;
        color: var(--color-text-secondary);
      }
    }
    
    .meta-row {
      margin-top: var(--spacing-sm);
      font-size: 13px;
      color: var(--color-text-secondary);
      
      .meta-divider {
        margin: 0 var(--spacing-sm);
        opacity: 0.5;
      }
    }
  }
}

.weather-tips-wrapper {
  flex: 1;
  min-width: 260px;
  display: flex;
  justify-content: flex-end;
  
  .tip-card {
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(4px);
    padding: var(--spacing-sm) var(--spacing-lg);
    border-radius: var(--border-radius-base);
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    max-width: 100%;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    
    .tip-icon {
      color: var(--color-primary);
      font-size: 20px;
    }
    
    .tip-text {
      margin: 0;
      font-size: 14px;
      color: var(--color-text-main);
    }
  }
}

@keyframes float {
  0% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
  100% { transform: translateY(0); }
}

@include respond-to(mobile) {
  .weather-container {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .weather-tips-wrapper {
    width: 100%;
    justify-content: flex-start;
  }
  
  .weather-main {
    .temp-row {
      .temp {
        font-size: 32px;
      }
    }
  }
}
</style>
