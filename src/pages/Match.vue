<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { getActivities, type Activity } from '@/api/index'
import { mockActivities } from '@/data/mockData'

const router = useRouter()

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const timeSlots = ['上午', '下午', '晚上']
const activityTypes = ['聚餐', '徒步', '打球', '探店', '桌游', '其他']
const distanceOptions = [
  { value: 3, label: '3公里内' },
  { value: 5, label: '5公里内' },
  { value: 10, label: '10公里内' },
  { value: 20, label: '20公里内' },
  { value: 999, label: '不限' }
]

const selectedSlots = ref<Record<string, string[]>>({})
const selectedTypes = ref<string[]>([])
const selectedDistance = ref(10)
const activities = ref<Activity[]>([])
const loading = ref(true)
const showResults = ref(false)

weekDays.forEach(day => {
  selectedSlots.value[day] = []
})

function toggleTimeSlot(day: string, slot: string) {
  const index = selectedSlots.value[day].indexOf(slot)
  if (index > -1) {
    selectedSlots.value[day].splice(index, 1)
  } else {
    selectedSlots.value[day].push(slot)
  }
}

function toggleActivityType(type: string) {
  const index = selectedTypes.value.indexOf(type)
  if (index > -1) {
    selectedTypes.value.splice(index, 1)
  } else {
    selectedTypes.value.push(type)
  }
}

const hasSelectedSlots = computed(() => {
  return Object.values(selectedSlots.value).some(slots => slots.length > 0)
})

const canMatch = computed(() => {
  return hasSelectedSlots.value && selectedTypes.value.length > 0
})

interface MatchCandidate {
  activity: Activity
  urgency: 'tonight' | 'weekend' | 'soon' | 'normal'
  urgencyText: string
  needMore: number
  timeLabel: string
  matchScore: number
}

const matchCandidates = computed<MatchCandidate[]>(() => {
  if (!showResults.value) return []
  
  const today = new Date()
  const candidates: MatchCandidate[] = []
  
  activities.value.forEach(activity => {
    if (selectedTypes.value.length > 0 && !selectedTypes.value.includes(activity.type)) {
      return
    }
    
    const activityDate = new Date(activity.time)
    const dayOfWeek = activityDate.getDay()
    const hour = activityDate.getHours()
    
    const dayIndex = dayOfWeek === 0 ? 6 : dayOfWeek - 1
    const dayName = weekDays[dayIndex]
    
    let timeSlot = ''
    if (hour >= 6 && hour < 12) timeSlot = '上午'
    else if (hour >= 12 && hour < 18) timeSlot = '下午'
    else timeSlot = '晚上'
    
    if (!selectedSlots.value[dayName]?.includes(timeSlot)) {
      return
    }
    
    const needMore = activity.maxParticipants - activity.currentParticipants
    if (needMore <= 0) return
    
    const daysDiff = Math.ceil((activityDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
    const isToday = daysDiff === 0 || (daysDiff === 1 && hour < 6)
    const isThisWeekend = dayOfWeek === 0 || dayOfWeek === 6
    const isSoon = daysDiff <= 3
    
    let urgency: 'tonight' | 'weekend' | 'soon' | 'normal' = 'normal'
    let urgencyText = ''
    
    if (isToday && timeSlot === '晚上') {
      urgency = 'tonight'
      urgencyText = '🔥 今晚可约'
    } else if (isThisWeekend && needMore <= 3) {
      urgency = 'weekend'
      urgencyText = `👥 本周末差${needMore}人开局`
    } else if (isSoon) {
      urgency = 'soon'
      urgencyText = '⏰ 即将开始'
    } else if (needMore <= 2) {
      urgency = 'soon'
      urgencyText = `还差${needMore}人满员`
    }
    
    let matchScore = 0
    if (urgency === 'tonight') matchScore += 100
    if (urgency === 'weekend') matchScore += 80
    if (urgency === 'soon') matchScore += 60
    matchScore += (activity.currentParticipants / activity.maxParticipants) * 40
    
    const timeStr = activityDate.toLocaleString('zh-CN', { 
      month: 'numeric', 
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      weekday: 'short'
    })
    
    candidates.push({
      activity,
      urgency,
      urgencyText,
      needMore,
      timeLabel: timeStr,
      matchScore
    })
  })
  
  return candidates.sort((a, b) => b.matchScore - a.matchScore)
})

const tonightCandidates = computed(() => 
  matchCandidates.value.filter(c => c.urgency === 'tonight')
)

const weekendCandidates = computed(() => 
  matchCandidates.value.filter(c => c.urgency === 'weekend')
)

const otherCandidates = computed(() => 
  matchCandidates.value.filter(c => c.urgency !== 'tonight' && c.urgency !== 'weekend')
)

async function loadActivities() {
  loading.value = true
  try {
    activities.value = await getActivities()
  } catch (error) {
    console.error('Failed to load activities from API, using mock data:', error)
    activities.value = mockActivities as unknown as Activity[]
  } finally {
    loading.value = false
  }
}

function findMatches() {
  showResults.value = true
}

function resetSelection() {
  weekDays.forEach(day => {
    selectedSlots.value[day] = []
  })
  selectedTypes.value = []
  selectedDistance.value = 10
  showResults.value = false
}

onMounted(() => {
  loadActivities()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="text-center mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">🎯 智能组队</h1>
        <p class="text-gray-500">勾选你的空闲时间，发现可以一起玩的伙伴</p>
      </div>
      
      <div class="bg-white rounded-2xl shadow-sm p-6 mb-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <span class="text-xl">📅</span>
          本周可出行时段
        </h2>
        <div class="grid grid-cols-7 gap-2">
          <div 
            v-for="day in weekDays" 
            :key="day"
            class="text-center"
          >
            <p class="text-sm font-medium text-gray-600 mb-2">{{ day }}</p>
            <div class="space-y-2">
              <button
                v-for="slot in timeSlots"
                :key="slot"
                @click="toggleTimeSlot(day, slot)"
                :class="[
                  'w-full py-2 px-2 text-xs rounded-lg transition-all',
                  selectedSlots[day].includes(slot)
                    ? 'bg-primary text-white shadow-md'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                ]"
              >
                {{ slot }}
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="bg-white rounded-2xl shadow-sm p-6 mb-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <span class="text-xl">🎮</span>
          偏好活动
        </h2>
        <div class="flex flex-wrap gap-3">
          <button
            v-for="type in activityTypes"
            :key="type"
            @click="toggleActivityType(type)"
            :class="[
              'px-5 py-2.5 rounded-full transition-all text-sm font-medium',
              selectedTypes.includes(type)
                ? 'bg-primary text-white shadow-md'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            {{ type }}
          </button>
        </div>
      </div>
      
      <div class="bg-white rounded-2xl shadow-sm p-6 mb-6">
        <h2 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
          <span class="text-xl">📍</span>
          可接受距离
        </h2>
        <div class="flex flex-wrap gap-3">
          <button
            v-for="option in distanceOptions"
            :key="option.value"
            @click="selectedDistance = option.value"
            :class="[
              'px-5 py-2.5 rounded-full transition-all text-sm font-medium',
              selectedDistance === option.value
                ? 'bg-primary text-white shadow-md'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            ]"
          >
            {{ option.label }}
          </button>
        </div>
      </div>
      
      <div class="flex gap-4 mb-8">
        <button
          @click="findMatches"
          :disabled="!canMatch"
          :class="[
            'flex-1 py-4 rounded-xl font-semibold text-white transition-all',
            canMatch
              ? 'bg-gradient-to-r from-primary to-orange-400 hover:shadow-lg hover:scale-[1.02]'
              : 'bg-gray-300 cursor-not-allowed'
          ]"
        >
          🔍 发现组队机会
        </button>
        <button
          @click="resetSelection"
          class="px-6 py-4 rounded-xl font-medium bg-gray-100 text-gray-600 hover:bg-gray-200 transition-all"
        >
          重置
        </button>
      </div>
      
      <div v-if="showResults" class="space-y-6">
        <div v-if="tonightCandidates.length > 0" class="bg-gradient-to-br from-orange-50 to-red-50 rounded-2xl p-6 border border-orange-200">
          <h3 class="text-lg font-bold text-orange-600 mb-4 flex items-center gap-2">
            <span class="text-2xl animate-pulse">🔥</span>
            今晚可约
            <span class="text-sm font-normal text-orange-500">（{{ tonightCandidates.length }}个机会）</span>
          </h3>
          <div class="space-y-3">
            <div 
              v-for="candidate in tonightCandidates" 
              :key="candidate.activity.id"
              class="bg-white rounded-xl p-4 shadow-sm cursor-pointer hover:shadow-md transition-all"
              @click="router.push(`/activity/${candidate.activity.id}`)"
            >
              <div class="flex gap-4">
                <img 
                  :src="candidate.activity.image" 
                  :alt="candidate.activity.title"
                  class="w-20 h-20 rounded-lg object-cover"
                />
                <div class="flex-1">
                  <div class="flex items-start justify-between">
                    <h4 class="font-semibold text-gray-900 line-clamp-1">{{ candidate.activity.title }}</h4>
                    <span class="bg-red-100 text-red-600 text-xs px-2 py-1 rounded-full whitespace-nowrap">
                      {{ candidate.urgencyText }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-500 mt-1">{{ candidate.timeLabel }}</p>
                  <div class="flex items-center gap-3 mt-2">
                    <span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">{{ candidate.activity.type }}</span>
                    <span class="text-xs text-gray-500">{{ candidate.activity.location }}</span>
                  </div>
                  <div class="flex items-center justify-between mt-2">
                    <div class="flex items-center gap-1">
                      <span class="text-sm text-gray-500">👥</span>
                      <span class="text-sm text-primary font-medium">
                        {{ candidate.activity.currentParticipants }}/{{ candidate.activity.maxParticipants }}人
                      </span>
                    </div>
                    <span class="text-sm text-orange-500 font-medium">
                      还差{{ candidate.needMore }}人
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="weekendCandidates.length > 0" class="bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl p-6 border border-blue-200">
          <h3 class="text-lg font-bold text-blue-600 mb-4 flex items-center gap-2">
            <span class="text-2xl">🎉</span>
            本周末开局
            <span class="text-sm font-normal text-blue-500">（{{ weekendCandidates.length }}个活动）</span>
          </h3>
          <div class="space-y-3">
            <div 
              v-for="candidate in weekendCandidates" 
              :key="candidate.activity.id"
              class="bg-white rounded-xl p-4 shadow-sm cursor-pointer hover:shadow-md transition-all"
              @click="router.push(`/activity/${candidate.activity.id}`)"
            >
              <div class="flex gap-4">
                <img 
                  :src="candidate.activity.image" 
                  :alt="candidate.activity.title"
                  class="w-20 h-20 rounded-lg object-cover"
                />
                <div class="flex-1">
                  <div class="flex items-start justify-between">
                    <h4 class="font-semibold text-gray-900 line-clamp-1">{{ candidate.activity.title }}</h4>
                    <span class="bg-blue-100 text-blue-600 text-xs px-2 py-1 rounded-full whitespace-nowrap">
                      {{ candidate.urgencyText }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-500 mt-1">{{ candidate.timeLabel }}</p>
                  <div class="flex items-center gap-3 mt-2">
                    <span class="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded">{{ candidate.activity.type }}</span>
                    <span class="text-xs text-gray-500">{{ candidate.activity.location }}</span>
                  </div>
                  <div class="flex items-center justify-between mt-2">
                    <div class="flex items-center gap-1">
                      <span class="text-sm text-gray-500">👥</span>
                      <span class="text-sm text-primary font-medium">
                        {{ candidate.activity.currentParticipants }}/{{ candidate.activity.maxParticipants }}人
                      </span>
                    </div>
                    <div class="w-24 bg-gray-200 rounded-full h-2">
                      <div 
                        class="bg-blue-500 h-2 rounded-full transition-all"
                        :style="{ width: `${(candidate.activity.currentParticipants / candidate.activity.maxParticipants) * 100}%` }"
                      ></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="otherCandidates.length > 0" class="bg-white rounded-2xl shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <span class="text-xl">📋</span>
            其他匹配活动
            <span class="text-sm font-normal text-gray-500">（{{ otherCandidates.length }}个）</span>
          </h3>
          <div class="grid md:grid-cols-2 gap-4">
            <div 
              v-for="candidate in otherCandidates" 
              :key="candidate.activity.id"
              class="border border-gray-100 rounded-xl p-4 cursor-pointer hover:shadow-md hover:border-primary/30 transition-all"
              @click="router.push(`/activity/${candidate.activity.id}`)"
            >
              <div class="flex gap-3">
                <img 
                  :src="candidate.activity.image" 
                  :alt="candidate.activity.title"
                  class="w-16 h-16 rounded-lg object-cover"
                />
                <div class="flex-1 min-w-0">
                  <h4 class="font-medium text-gray-900 line-clamp-1 text-sm">{{ candidate.activity.title }}</h4>
                  <p class="text-xs text-gray-500 mt-1">{{ candidate.timeLabel }}</p>
                  <div class="flex items-center justify-between mt-2">
                    <span class="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded">{{ candidate.activity.type }}</span>
                    <span class="text-xs text-primary">
                      {{ candidate.activity.currentParticipants }}/{{ candidate.activity.maxParticipants }}人
                    </span>
                  </div>
                  <p v-if="candidate.urgencyText" class="text-xs text-orange-500 mt-1">
                    {{ candidate.urgencyText }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div v-if="matchCandidates.length === 0" class="bg-white rounded-2xl shadow-sm p-12 text-center">
          <div class="text-6xl mb-4">🔍</div>
          <h3 class="text-lg font-semibold text-gray-900 mb-2">暂无匹配的组队机会</h3>
          <p class="text-gray-500 mb-6">试试调整你的时间或活动偏好</p>
          <button
            @click="resetSelection"
            class="px-6 py-3 bg-primary text-white rounded-xl hover:shadow-md transition-all"
          >
            重新选择
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
