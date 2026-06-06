<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { 
  getActivityById, 
  registerActivity, 
  cancelRegistration, 
  checkRegistration, 
  getRegisteredActivities, 
  getRegistrationStatus,
  getWaitlistPosition,
  getWaitlist,
  type Activity,
  type RegistrationStatus as RegStatus,
  type WaitlistUser
} from '@/api/index'
import { matchDistrictByLocation } from '@/data/locationData'

const route = useRoute()
const router = useRouter()

const CURRENT_USER_ID = 2

const activityId = Number(route.params.id)
const activity = ref<Activity | null>(null)
const isRegistered = ref(false)
const regStatus = ref<RegStatus>('NOT_REGISTERED')
const waitlistPosition = ref<number | null>(null)
const waitlist = ref<WaitlistUser[]>([])
const isFull = ref(false)
const isCreator = ref(false)
const loading = ref(true)
const registeredActivities = ref<Activity[]>([])
const showConflictWarning = ref(false)
const showCloseTimeWarning = ref(false)

interface TimeConflict {
  type: 'overlap' | 'close_time'
  activity: Activity
  message: string
  minutesDiff: number
}

const timeConflicts = computed<TimeConflict[]>(() => {
  if (!activity.value || registeredActivities.value.length === 0) return []
  
  const conflicts: TimeConflict[] = []
  const currentActivityTime = new Date(activity.value.time)
  const currentActivityEnd = new Date(currentActivityTime.getTime() + 2 * 60 * 60 * 1000)
  
  registeredActivities.value.forEach(registered => {
    if (registered.id === activity.value!.id) return
    
    const registeredTime = new Date(registered.time)
    const registeredEnd = new Date(registeredTime.getTime() + 2 * 60 * 60 * 1000)
    
    if (currentActivityTime < registeredEnd && currentActivityEnd > registeredTime) {
      const overlapStart = new Date(Math.max(currentActivityTime.getTime(), registeredTime.getTime()))
      const overlapEnd = new Date(Math.min(currentActivityEnd.getTime(), registeredEnd.getTime()))
      const overlapMinutes = Math.round((overlapEnd.getTime() - overlapStart.getTime()) / (1000 * 60))
      
      conflicts.push({
        type: 'overlap',
        activity: registered,
        message: `与「${registered.title}」时间重叠约 ${overlapMinutes} 分钟`,
        minutesDiff: overlapMinutes
      })
    } else {
      const diffMinutes = Math.abs(currentActivityTime.getTime() - registeredEnd.getTime()) / (1000 * 60)
      if (diffMinutes < 60 && diffMinutes > 0) {
        const isAfter = currentActivityTime > registeredEnd
        conflicts.push({
          type: 'close_time',
          activity: registered,
          message: isAfter 
            ? `「${registered.title}」结束后仅 ${Math.round(diffMinutes)} 分钟开始，返程时间紧张`
            : `开始时间比「${registered.title}」仅早 ${Math.round(diffMinutes)} 分钟`,
          minutesDiff: Math.round(diffMinutes)
        })
      }
    }
  })
  
  return conflicts
})

const hasConflict = computed(() => timeConflicts.value.length > 0)

const district = computed(() => {
  if (!activity.value) return null
  return matchDistrictByLocation(activity.value.location, activity.value.city)
})

const getTypeColor = (type: string) => {
  const colors: Record<string, string> = {
    '聚餐': 'bg-red-100 text-red-600',
    '徒步': 'bg-green-100 text-green-600',
    '打球': 'bg-blue-100 text-blue-600',
    '探店': 'bg-orange-100 text-orange-600',
    '桌游': 'bg-purple-100 text-purple-600',
    '其他': 'bg-gray-100 text-gray-600',
  }
  return colors[type] || colors['其他']
}

async function loadActivity() {
  loading.value = true
  try {
    activity.value = await getActivityById(activityId)
    isRegistered.value = await checkRegistration(activityId, CURRENT_USER_ID)
    regStatus.value = await getRegistrationStatus(activityId, CURRENT_USER_ID)
    waitlistPosition.value = await getWaitlistPosition(activityId, CURRENT_USER_ID)
    waitlist.value = await getWaitlist(activityId)
    isFull.value = activity.value.currentParticipants >= activity.value.maxParticipants
    isCreator.value = activity.value.creatorId === CURRENT_USER_ID
    registeredActivities.value = await getRegisteredActivities(CURRENT_USER_ID)
  } catch (error) {
    console.error('Failed to load activity:', error)
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerActivity(activityId, CURRENT_USER_ID)
    await loadActivity()
    if (regStatus.value === 'WAITLISTED') {
      alert(`候补报名成功！您当前是第 ${waitlistPosition.value} 位候补，有人取消时会自动补上。`)
    } else {
      alert('报名成功！')
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '报名失败，请稍后重试'
    alert(message)
  }
}

const handleCancel = async () => {
  try {
    await cancelRegistration(activityId, CURRENT_USER_ID)
    alert('已取消报名')
    await loadActivity()
  } catch (error) {
    const message = error instanceof Error ? error.message : '取消报名失败，请稍后重试'
    alert(message)
  }
}

onMounted(() => {
  loadActivity()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
    </div>
    
    <div v-else-if="activity" class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <button @click="router.back()" class="flex items-center gap-2 text-gray-500 hover:text-gray-700 mb-6">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        返回
      </button>
      
      <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
        <div class="relative h-64 md:h-80">
          <img :src="activity.image" :alt="activity.title" class="w-full h-full object-cover" />
          <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
          <div class="absolute bottom-6 left-6 right-6">
            <div class="flex flex-wrap gap-2">
              <span :class="['px-4 py-1.5 rounded-full text-sm font-medium', getTypeColor(activity.type)]">
                {{ activity.type }}
              </span>
              <span v-if="district" class="px-4 py-1.5 rounded-full text-sm font-medium bg-white/90 text-gray-700">
                {{ district.name }} ({{ district.type }})
              </span>
            </div>
            <h1 class="text-2xl md:text-3xl font-bold text-white mt-3">{{ activity.title }}</h1>
          </div>
        </div>
        
        <div v-if="hasConflict && !isRegistered" class="p-6 bg-gradient-to-r from-red-50 to-orange-50 border-b border-red-100">
          <div class="flex items-start gap-3">
            <div class="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <div class="flex-1">
              <h3 class="font-semibold text-red-800 mb-2">⚠️ 时间冲突提醒</h3>
              <p class="text-sm text-red-700 mb-3">您已报名的活动与该活动存在以下时间冲突：</p>
              <div class="space-y-2">
                <div 
                  v-for="(conflict, index) in timeConflicts" 
                  :key="index"
                  :class="[
                    'p-3 rounded-lg text-sm flex items-center justify-between',
                    conflict.type === 'overlap' ? 'bg-red-100' : 'bg-orange-100'
                  ]"
                >
                  <div class="flex-1">
                    <span :class="conflict.type === 'overlap' ? 'text-red-700' : 'text-orange-700'">
                      {{ conflict.type === 'overlap' ? '⏰ 时间重叠' : '⚡ 返程紧张' }}
                    </span>
                    <p class="text-gray-700 mt-1">{{ conflict.message }}</p>
                    <p class="text-gray-500 text-xs mt-1">
                      {{ conflict.activity.title }} · {{ new Date(conflict.activity.time).toLocaleString('zh-CN') }}
                    </p>
                  </div>
                  <button 
                    @click="$router.push(`/activity/${conflict.activity.id}`)"
                    class="ml-4 text-xs text-primary hover:underline flex-shrink-0"
                  >
                    查看详情
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="p-6 md:p-8">
          <div class="grid md:grid-cols-3 gap-6 mb-8">
            <div class="flex items-start gap-3">
              <div class="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center flex-shrink-0">
                <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">地点</p>
                <p class="font-medium text-gray-900">{{ activity.city }} · {{ activity.location }}</p>
              </div>
            </div>
            
            <div class="flex items-start gap-3">
              <div class="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center flex-shrink-0">
                <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">时间</p>
                <p class="font-medium text-gray-900">{{ new Date(activity.time).toLocaleString('zh-CN') }}</p>
              </div>
            </div>
            
            <div class="flex items-start gap-3">
              <div class="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center flex-shrink-0">
                <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">报名人数</p>
                <p class="font-medium text-gray-900">{{ activity.currentParticipants }}/{{ activity.maxParticipants }}人</p>
                <p v-if="activity.waitlistCount && activity.waitlistCount > 0" class="text-xs text-orange-500 mt-1">
                  候补 {{ activity.waitlistCount }} 人
                </p>
              </div>
            </div>
          </div>
          
          <div class="space-y-6">
            <div v-if="regStatus === 'WAITLISTED'" class="p-4 bg-orange-50 border border-orange-200 rounded-xl">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0">
                  <svg class="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <p class="font-medium text-orange-800">您已加入候补队列</p>
                  <p class="text-sm text-orange-600 mt-1">
                    当前是第 <span class="font-bold">{{ waitlistPosition }}</span> 位候补，有人取消报名时会自动按顺序补上
                  </p>
                </div>
              </div>
            </div>
            
            <div>
              <h2 class="text-lg font-semibold text-gray-900 mb-3">活动介绍</h2>
              <p class="text-gray-600 leading-relaxed">{{ activity.description }}</p>
            </div>
            
            <div v-if="activity.requirements">
              <h2 class="text-lg font-semibold text-gray-900 mb-3">报名要求</h2>
              <div class="bg-gray-50 rounded-xl p-4">
                <p class="text-gray-600">{{ activity.requirements }}</p>
              </div>
            </div>
            
            <div v-if="waitlist.length > 0" class="mt-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-3">
                候补队列 
                <span class="text-sm font-normal text-gray-500">({{ waitlist.length }}人)</span>
              </h2>
              <div class="bg-gray-50 rounded-xl p-4">
                <div class="space-y-3">
                  <div 
                    v-for="(user, index) in waitlist" 
                    :key="user.userId"
                    class="flex items-center gap-3"
                  >
                    <div class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0">
                      <span class="text-sm font-medium text-orange-600">{{ user.waitlistPosition }}</span>
                    </div>
                    <span class="text-gray-700">{{ user.userName }}</span>
                    <span v-if="index === 0" class="ml-auto text-xs text-green-600 bg-green-50 px-2 py-1 rounded-full">
                      下一位补位
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="mt-8 pt-6 border-t border-gray-100">
            <div class="flex flex-col sm:flex-row items-center justify-between gap-4">
              <div class="flex items-center gap-4">
                <div class="flex items-center gap-2 text-gray-500">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <span>{{ activity.views }} 次浏览</span>
                </div>
              </div>
              
              <div class="flex gap-3 w-full sm:w-auto">
                <button
                  v-if="isCreator"
                  disabled
                  class="flex-1 sm:flex-none px-8 py-3 bg-gray-200 text-gray-500 rounded-xl cursor-not-allowed"
                >
                  我发布的活动
                </button>
                <button
                  v-else-if="regStatus === 'CONFIRMED'"
                  @click="handleCancel"
                  class="flex-1 sm:flex-none px-8 py-3 border-2 border-primary text-primary rounded-xl hover:bg-primary/5 transition-colors"
                >
                  取消报名
                </button>
                <button
                  v-else-if="regStatus === 'WAITLISTED'"
                  @click="handleCancel"
                  class="flex-1 sm:flex-none px-8 py-3 border-2 border-orange-400 text-orange-500 rounded-xl hover:bg-orange-50 transition-colors"
                >
                  取消候补 (第{{ waitlistPosition }}位)
                </button>
                <button
                  v-else-if="isFull"
                  @click="handleRegister"
                  class="flex-1 sm:flex-none px-8 py-3 bg-gradient-to-r from-orange-400 to-yellow-400 text-white rounded-xl hover:from-orange-500 hover:to-yellow-500 transition-all font-medium shadow-lg shadow-orange-200"
                >
                  候补报名
                </button>
                <button
                  v-else
                  @click="handleRegister"
                  class="flex-1 sm:flex-none px-8 py-3 bg-gradient-to-r from-primary to-orange-400 text-white rounded-xl hover:from-primary/90 hover:to-orange-500/90 transition-all font-medium shadow-lg shadow-orange-200"
                >
                  立即报名
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
