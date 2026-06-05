<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Navbar from '@/components/Navbar.vue'
import { nightActivities, type Activity } from '@/data/mockData'
import { allCities } from '@/data/cities'
import { getDistrictsByCity, convenienceOptions, matchDistrictByLocation, type BusinessDistrict } from '@/data/locationData'

const selectedCity = ref('北京')
const selectedScene = ref('')
const selectedStartTime = ref('')
const selectedEndTime = ref('')
const selectedConvenience = ref(0)
const sortBy = ref('time')

const nightScenes = [
  { key: '', label: '全部场景', icon: '🌙' },
  { key: '夜宵', label: '夜宵美食', icon: '🍜' },
  { key: '桌游', label: '深夜桌游', icon: '🎲' },
  { key: '小聚', label: '下班后小聚', icon: '🍻' },
  { key: '夜跑', label: '夜跑运动', icon: '🏃' },
]

const startTimeOptions = [
  { value: '', label: '开始时间不限' },
  { value: '18', label: '18:00 后' },
  { value: '19', label: '19:00 后' },
  { value: '20', label: '20:00 后' },
  { value: '21', label: '21:00 后' },
  { value: '22', label: '22:00 后' },
]

const endTimeOptions = [
  { value: '', label: '结束时间不限' },
  { value: '20', label: '20:00 前结束' },
  { value: '21', label: '21:00 前结束' },
  { value: '22', label: '22:00 前结束' },
  { value: '23', label: '23:00 前结束' },
]

const sortOptions = [
  { value: 'time', label: '按时间排序' },
  { value: 'popular', label: '报名最多' },
  { value: 'views', label: '浏览最多' },
  { value: 'convenience', label: '便利度优先' },
]

const getActivityDistrict = (activity: Activity): BusinessDistrict | null => {
  return matchDistrictByLocation(activity.location, activity.city)
}

const getActivityHour = (activity: Activity): number => {
  const timeStr = activity.time.split(' ')[1] || ''
  const hour = parseInt(timeStr.split(':')[0] || '0')
  return hour
}

const getSceneType = (activity: Activity): string => {
  if (activity.type === '桌游') return '桌游'
  if (activity.type === '徒步') return '夜跑'
  if (activity.type === '探店' && (activity.title.includes('清吧') || activity.title.includes('酒') || activity.title.includes('微醺'))) return '小聚'
  if (activity.type === '聚餐' && (activity.title.includes('夜宵') || activity.title.includes('深夜') || activity.title.includes('撸串'))) return '夜宵'
  if (activity.type === '聚餐') return '小聚'
  if (activity.type === '探店') return '夜宵'
  return ''
}

const filteredActivities = computed(() => {
  let result = [...nightActivities]

  if (selectedCity.value) {
    result = result.filter(a => a.city === selectedCity.value)
  }

  if (selectedScene.value) {
    result = result.filter(a => getSceneType(a) === selectedScene.value)
  }

  if (selectedStartTime.value) {
    const startHour = parseInt(selectedStartTime.value)
    result = result.filter(a => getActivityHour(a) >= startHour)
  }

  if (selectedEndTime.value) {
    const endHour = parseInt(selectedEndTime.value)
    result = result.filter(a => getActivityHour(a) < endHour)
  }

  if (selectedConvenience.value > 0) {
    result = result.filter(a => {
      const district = getActivityDistrict(a)
      return district && district.convenienceScore >= selectedConvenience.value
    })
  }

  if (sortBy.value === 'time') {
    result.sort((a, b) => getActivityHour(a) - getActivityHour(b))
  } else if (sortBy.value === 'popular') {
    result.sort((a, b) => b.currentParticipants - a.currentParticipants)
  } else if (sortBy.value === 'views') {
    result.sort((a, b) => b.views - a.views)
  } else if (sortBy.value === 'convenience') {
    result.sort((a, b) => {
      const distA = getActivityDistrict(a)?.convenienceScore || 0
      const distB = getActivityDistrict(b)?.convenienceScore || 0
      return distB - distA
    })
  }

  return result
})

const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    '聚餐': 'bg-rose-500/20 text-rose-300 border-rose-500/30',
    '徒步': 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
    '打球': 'bg-blue-500/20 text-blue-300 border-blue-500/30',
    '探店': 'bg-amber-500/20 text-amber-300 border-amber-500/30',
    '桌游': 'bg-purple-500/20 text-purple-300 border-purple-500/30',
    '其他': 'bg-gray-500/20 text-gray-300 border-gray-500/30',
  }
  return colors[type] || colors['其他']
}

const getConvenienceTag = (activity: Activity) => {
  const district = getActivityDistrict(activity)
  if (!district) return null
  const score = district.convenienceScore
  if (score >= 90) return { text: '极便利', color: 'text-emerald-400 bg-emerald-500/20' }
  if (score >= 80) return { text: '很便利', color: 'text-cyan-400 bg-cyan-500/20' }
  if (score >= 70) return { text: '较便利', color: 'text-yellow-400 bg-yellow-500/20' }
  return null
}

const getTimeLabel = (activity: Activity) => {
  const hour = getActivityHour(activity)
  if (hour >= 22) return { text: '深夜场', color: 'bg-purple-500/30 text-purple-300' }
  if (hour >= 20) return { text: '晚间场', color: 'bg-blue-500/30 text-blue-300' }
  if (hour >= 18) return { text: '傍晚场', color: 'bg-orange-500/30 text-orange-300' }
  return null
}

const formatTimeDisplay = (timeStr: string) => {
  const parts = timeStr.split(' ')
  if (parts.length < 2) return timeStr
  const time = parts[1]
  const today = new Date().toISOString().split('T')[0]
  const dateStr = parts[0]
  if (dateStr === today) return `今晚 ${time}`
  return timeStr
}

onMounted(() => {
})
</script>

<template>
  <div class="min-h-screen bg-slate-900 text-white">
    <Navbar />
    
    <div class="relative overflow-hidden">
      <div class="absolute inset-0 bg-gradient-to-br from-indigo-900 via-purple-900 to-slate-900"></div>
      <div class="absolute top-0 left-1/4 w-96 h-96 bg-purple-500/20 rounded-full blur-3xl"></div>
      <div class="absolute bottom-0 right-1/4 w-96 h-96 bg-blue-500/20 rounded-full blur-3xl"></div>
      
      <div class="relative max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div class="text-center mb-10">
          <div class="inline-flex items-center gap-2 px-4 py-2 bg-white/10 rounded-full text-sm mb-4">
            <span class="text-xl">🌙</span>
            <span class="text-purple-200">城市夜生活雷达</span>
          </div>
          <h1 class="text-4xl md:text-5xl font-bold mb-4 bg-gradient-to-r from-purple-300 via-pink-300 to-amber-300 bg-clip-text text-transparent">
            发现夜晚的无限可能
          </h1>
          <p class="text-lg text-gray-300 max-w-2xl mx-auto">
            夜宵、桌游、小聚、夜跑...下班后还能参加的同城局，一键直达
          </p>
        </div>

        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3 mb-8">
          <button
            v-for="scene in nightScenes"
            :key="scene.key || 'all'"
            @click="selectedScene = scene.key"
            :class="[
              'flex flex-col items-center gap-2 p-4 rounded-2xl transition-all border',
              selectedScene === scene.key
                ? 'bg-purple-500/30 border-purple-400/50 shadow-lg shadow-purple-500/20'
                : 'bg-white/5 border-white/10 hover:bg-white/10 hover:border-white/20'
            ]"
          >
            <span class="text-3xl">{{ scene.icon }}</span>
            <span class="text-sm font-medium">{{ scene.label }}</span>
          </button>
        </div>

        <div class="bg-white/5 backdrop-blur-sm rounded-2xl p-6 border border-white/10">
          <div class="flex flex-wrap gap-4">
            <select
              v-model="selectedCity"
              class="flex-1 min-w-[140px] px-4 py-3 rounded-xl bg-white/10 text-white border border-white/20 focus:outline-none focus:ring-2 focus:ring-purple-400/50 appearance-none cursor-pointer"
            >
              <option value="" class="bg-slate-800">全部城市</option>
              <option v-for="city in allCities" :key="city" :value="city" class="bg-slate-800">{{ city }}</option>
            </select>

            <select
              v-model="selectedStartTime"
              class="flex-1 min-w-[140px] px-4 py-3 rounded-xl bg-white/10 text-white border border-white/20 focus:outline-none focus:ring-2 focus:ring-purple-400/50 appearance-none cursor-pointer"
            >
              <option v-for="opt in startTimeOptions" :key="opt.value" :value="opt.value" class="bg-slate-800">{{ opt.label }}</option>
            </select>

            <select
              v-model="selectedEndTime"
              class="flex-1 min-w-[140px] px-4 py-3 rounded-xl bg-white/10 text-white border border-white/20 focus:outline-none focus:ring-2 focus:ring-purple-400/50 appearance-none cursor-pointer"
            >
              <option v-for="opt in endTimeOptions" :key="opt.value" :value="opt.value" class="bg-slate-800">{{ opt.label }}</option>
            </select>

            <select
              v-model="selectedConvenience"
              class="flex-1 min-w-[140px] px-4 py-3 rounded-xl bg-white/10 text-white border border-white/20 focus:outline-none focus:ring-2 focus:ring-purple-400/50 appearance-none cursor-pointer"
            >
              <option v-for="option in convenienceOptions" :key="option.value" :value="option.value" class="bg-slate-800">
                {{ option.value > 0 ? `🚇 ${option.label}` : '返程便利度不限' }}
              </option>
            </select>

            <select
              v-model="sortBy"
              class="flex-1 min-w-[140px] px-4 py-3 rounded-xl bg-white/10 text-white border border-white/20 focus:outline-none focus:ring-2 focus:ring-purple-400/50 appearance-none cursor-pointer"
            >
              <option v-for="opt in sortOptions" :key="opt.value" :value="opt.value" class="bg-slate-800">{{ opt.label }}</option>
            </select>
          </div>

          <div class="mt-4 flex flex-wrap gap-3 text-sm text-gray-400">
            <span v-if="selectedScene" class="px-3 py-1 bg-purple-500/20 text-purple-300 rounded-full">
              {{ nightScenes.find(s => s.key === selectedScene)?.icon }} {{ nightScenes.find(s => s.key === selectedScene)?.label }}
            </span>
            <span v-if="selectedStartTime" class="px-3 py-1 bg-blue-500/20 text-blue-300 rounded-full">
              ⏰ {{ startTimeOptions.find(o => o.value === selectedStartTime)?.label }}
            </span>
            <span v-if="selectedEndTime" class="px-3 py-1 bg-amber-500/20 text-amber-300 rounded-full">
              🌙 {{ endTimeOptions.find(o => o.value === selectedEndTime)?.label }}
            </span>
            <span v-if="selectedConvenience > 0" class="px-3 py-1 bg-emerald-500/20 text-emerald-300 rounded-full">
              🚇 {{ convenienceOptions.find(o => o.value === selectedConvenience)?.label }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex items-center justify-between mb-6">
        <h2 class="text-xl font-semibold flex items-center gap-2">
          <span class="text-2xl">✨</span>
          今晚活动
          <span class="text-sm font-normal text-gray-400">({{ filteredActivities.length }}个)</span>
        </h2>
      </div>

      <div v-if="filteredActivities.length === 0" class="text-center py-16">
        <div class="text-6xl mb-4">🌃</div>
        <p class="text-gray-400 text-lg">暂无符合条件的夜间活动</p>
        <p class="text-gray-500 text-sm mt-2">试试调整筛选条件，或换个城市看看</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        <div
          v-for="activity in filteredActivities"
          :key="activity.id"
          class="group bg-slate-800/50 rounded-2xl overflow-hidden border border-white/10 hover:border-purple-400/30 hover:shadow-xl hover:shadow-purple-500/10 transition-all duration-300 cursor-pointer"
          @click="$router.push(`/activity/${activity.id.replace('night-', '')}`)"
        >
          <div class="relative h-48 overflow-hidden">
            <img
              :src="activity.image"
              :alt="activity.title"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            />
            <div class="absolute inset-0 bg-gradient-to-t from-slate-900/80 via-transparent to-transparent"></div>
            
            <div class="absolute top-3 left-3 flex flex-wrap gap-2">
              <span :class="['px-3 py-1 rounded-full text-xs font-medium border', getTypeColor(activity.type)]">
                {{ activity.type }}
              </span>
              <span v-if="getTimeLabel(activity)" :class="['px-3 py-1 rounded-full text-xs font-medium', getTimeLabel(activity)?.color]">
                {{ getTimeLabel(activity)?.text }}
              </span>
            </div>

            <div class="absolute top-3 right-3 flex gap-2">
              <div v-if="getConvenienceTag(activity)" :class="['px-2 py-1 rounded-lg text-xs font-medium flex items-center gap-1', getConvenienceTag(activity)?.color]">
                <span>🚇</span>
                <span>{{ getConvenienceTag(activity)?.text }}</span>
              </div>
            </div>

            <div class="absolute bottom-3 left-3 right-3">
              <div class="flex items-center gap-2 text-white/90 text-sm">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span class="font-medium">{{ formatTimeDisplay(activity.time) }}</span>
              </div>
            </div>
          </div>

          <div class="p-4">
            <h3 class="font-semibold text-lg text-white mb-2 line-clamp-2 group-hover:text-purple-300 transition-colors">
              {{ activity.title }}
            </h3>

            <div class="space-y-2 text-sm text-gray-400 mb-4">
              <div class="flex items-center gap-2">
                <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
                <span>{{ activity.city }} · {{ activity.location }}</span>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="flex -space-x-2">
                  <div class="w-7 h-7 bg-gradient-to-br from-purple-500 to-pink-500 rounded-full flex items-center justify-center text-xs font-bold text-white">
                    {{ activity.currentParticipants }}
                  </div>
                </div>
                <span class="text-sm text-gray-400">
                  {{ activity.currentParticipants }}/{{ activity.maxParticipants }}人
                </span>
              </div>

              <div class="w-24 h-2 bg-slate-700 rounded-full overflow-hidden">
                <div
                  class="h-full bg-gradient-to-r from-purple-500 to-pink-500 rounded-full transition-all duration-500"
                  :style="{ width: `${(activity.currentParticipants / activity.maxParticipants) * 100}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-12 text-center">
        <div class="inline-block p-6 bg-gradient-to-r from-purple-500/20 to-pink-500/20 rounded-2xl border border-purple-400/30">
          <p class="text-lg text-purple-200 mb-4">有好玩的夜间活动？分享给大家！</p>
          <button
            @click="$router.push('/publish')"
            class="px-6 py-3 bg-gradient-to-r from-purple-500 to-pink-500 rounded-xl font-medium hover:from-purple-600 hover:to-pink-600 transition-all shadow-lg shadow-purple-500/30 hover:shadow-purple-500/50"
          >
            🎉 发布夜生活活动
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
