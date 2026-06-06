<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import Navbar from '@/components/Navbar.vue'
import { 
  getRegisteredActivities, 
  getActivitiesByCreator,
  type Activity 
} from '@/api/index'
import { 
  getChecklistByType, 
  cloneChecklistItems,
  type ChecklistItem,
  type ActivityChecklist 
} from '@/data/checklistConfig'

const CURRENT_USER_ID = 2

const loading = ref(true)
const registeredActivities = ref<Activity[]>([])
const createdActivities = ref<Activity[]>([])
const selectedActivityId = ref<number | null>(null)
const checklistItemMap = ref<Record<number, ChecklistItem[]>>({})

const upcomingActivities = computed(() => {
  const now = new Date()
  const registered = registeredActivities.value
    .filter(a => new Date(a.time) > now)
    .map(a => ({ ...a, source: 'registered' as const }))
  
  const created = createdActivities.value
    .filter(a => new Date(a.time) > now)
    .filter(a => !registered.some(r => r.id === a.id))
    .map(a => ({ ...a, source: 'created' as const }))
  
  const all = [...registered, ...created]
  all.sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime())
  
  console.log('[Checklist] 未来活动总数:', all.length, 
    '报名:', registered.length, 
    '创建:', created.length)
  
  return all
})

const selectedActivity = computed(() => {
  if (!selectedActivityId.value && upcomingActivities.value.length > 0) {
    selectedActivityId.value = upcomingActivities.value[0].id
  }
  return upcomingActivities.value.find(a => a.id === selectedActivityId.value) || null
})

const checklistData = computed<ActivityChecklist | null>(() => {
  if (!selectedActivity.value) return null
  return getChecklistByType(selectedActivity.value.type)
})

const currentItems = computed(() => {
  if (!selectedActivity.value || !checklistData.value) return []
  const id = selectedActivity.value.id
  if (!checklistItemMap.value[id]) {
    checklistItemMap.value[id] = cloneChecklistItems(checklistData.value.items)
  }
  return checklistItemMap.value[id]
})

const itemsByCategory = computed(() => {
  const categories: Record<string, ChecklistItem[]> = {}
  currentItems.value.forEach(item => {
    if (!categories[item.category]) {
      categories[item.category] = []
    }
    categories[item.category].push(item)
  })
  return categories
})

const checkedCount = computed(() => 
  currentItems.value.filter(item => item.checked).length
)

const totalCount = computed(() => currentItems.value.length)

const progressPercent = computed(() => {
  if (totalCount.value === 0) return 0
  return Math.round((checkedCount.value / totalCount.value) * 100)
})

const timeUntilActivity = computed(() => {
  if (!selectedActivity.value) return null
  const now = new Date()
  const activityTime = new Date(selectedActivity.value.time)
  const diffMs = activityTime.getTime() - now.getTime()
  
  if (diffMs <= 0) return { text: '活动已开始', urgent: false }
  
  const diffHours = diffMs / (1000 * 60 * 60)
  const diffDays = Math.floor(diffHours / 24)
  const diffHoursRemain = Math.floor(diffHours % 24)
  const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))
  
  let text = ''
  let urgent = false
  
  if (diffDays > 0) {
    text = `还有 ${diffDays} 天 ${diffHoursRemain} 小时`
  } else if (diffHoursRemain > 0) {
    text = `还有 ${diffHoursRemain} 小时 ${diffMinutes} 分钟`
    urgent = diffHoursRemain < 6
  } else {
    text = `还有 ${diffMinutes} 分钟`
    urgent = true
  }
  
  return { text, urgent }
})

const lateRiskLevel = computed(() => {
  if (!selectedActivity.value) return 'low'
  
  const now = new Date()
  const activityTime = new Date(selectedActivity.value.time)
  const diffHours = (activityTime.getTime() - now.getTime()) / (1000 * 60 * 60)
  
  const dayOfWeek = activityTime.getDay()
  const hour = activityTime.getHours()
  const isWeekday = dayOfWeek >= 1 && dayOfWeek <= 5
  const isRushHour = (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)
  
  if (diffHours < 1) return 'high'
  if (isWeekday && isRushHour && diffHours < 3) return 'high'
  if (isWeekday && isRushHour && diffHours < 6) return 'medium'
  if (diffHours < 3) return 'medium'
  return 'low'
})

const lateRiskText = computed(() => {
  const levels: Record<string, { text: string; color: string; bg: string }> = {
    low: { text: '迟到风险低', color: 'text-green-600', bg: 'bg-green-100' },
    medium: { text: '迟到风险中等', color: 'text-yellow-600', bg: 'bg-yellow-100' },
    high: { text: '迟到风险高！', color: 'text-red-600', bg: 'bg-red-100' },
  }
  return levels[lateRiskLevel.value]
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

const getRiskColor = (risk: string) => {
  const colors: Record<string, string> = {
    low: 'bg-green-100 text-green-600',
    medium: 'bg-yellow-100 text-yellow-600',
    high: 'bg-red-100 text-red-600',
  }
  return colors[risk] || colors['low']
}

const getRiskText = (risk: string) => {
  const texts: Record<string, string> = {
    low: '准时',
    medium: '可能延误',
    high: '易迟到',
  }
  return texts[risk] || '准时'
}

async function loadActivities() {
  loading.value = true
  try {
    const [registered, created] = await Promise.all([
      getRegisteredActivities(CURRENT_USER_ID),
      getActivitiesByCreator(CURRENT_USER_ID),
    ])
    registeredActivities.value = registered
    createdActivities.value = created
    
    console.log('[Checklist] 数据加载完成 - 报名活动数:', registered.length, 
      '创建活动数:', created.length)
    
    if (upcomingActivities.value.length > 0) {
      selectedActivityId.value = upcomingActivities.value[0].id
    }
  } catch (error) {
    console.error('[Checklist] 加载活动失败:', error)
  } finally {
    loading.value = false
  }
}

function toggleItem(item: ChecklistItem) {
  item.checked = !item.checked
}

function selectActivity(id: number) {
  selectedActivityId.value = id
}

watch(selectedActivityId, () => {
  if (selectedActivity.value && checklistData.value) {
    const id = selectedActivity.value.id
    if (!checklistItemMap.value[id]) {
      checklistItemMap.value[id] = cloneChecklistItems(checklistData.value.items)
    }
  }
})

onMounted(() => {
  loadActivities()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-gray-900">📋 行前清单</h1>
        <p class="text-gray-500 mt-1">出发前，检查一下你都准备好了吗？</p>
      </div>
      
      <div v-if="loading" class="flex flex-col items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
        <p class="mt-4 text-gray-500">加载中...</p>
      </div>
      
      <div v-else-if="upcomingActivities.length === 0" class="text-center py-16">
        <div class="text-6xl mb-4">🎉</div>
        <h3 class="text-xl font-semibold text-gray-700 mb-2">暂无即将开始的活动</h3>
        <p class="text-gray-500 mb-6">去首页发现有趣的活动吧</p>
        <button 
          @click="$router.push('/')"
          class="px-6 py-2.5 bg-gradient-to-r from-primary to-orange-400 text-white rounded-xl hover:from-primary/90 hover:to-orange-500/90 transition-all font-medium"
        >
          去发现活动
        </button>
        
        <div class="mt-12 text-left">
          <p class="text-sm text-gray-400 mb-4 text-center">💡 先看看不同类型活动的准备清单</p>
          <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
            <button
              v-for="(config, type) in {徒步: null, 打球: null, 桌游: null, 聚餐: null, 探店: null, 其他: null}"
              :key="type"
              @click="() => {}"
              :class="[
                'p-4 rounded-xl bg-white shadow-sm hover:shadow transition-all text-center',
              ]"
            >
              <div class="text-3xl mb-2">
                {{ type === '徒步' ? '🥾' : type === '打球' ? '🏀' : type === '桌游' ? '🎲' : type === '聚餐' ? '🍻' : type === '探店' ? '☕' : '📌' }}
              </div>
              <p class="text-sm font-medium text-gray-700">{{ type }}</p>
            </button>
          </div>
        </div>
      </div>
      
      <div v-else class="grid lg:grid-cols-3 gap-6">
        <div class="lg:col-span-1">
          <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
            <div class="p-4 border-b border-gray-100">
              <h3 class="font-semibold text-gray-900">即将开始的活动</h3>
              <p class="text-sm text-gray-500 mt-1">共 {{ upcomingActivities.length }} 个</p>
            </div>
            <div class="divide-y divide-gray-50 max-h-96 overflow-y-auto">
              <button
                v-for="activity in upcomingActivities"
                :key="activity.id"
                @click="selectActivity(activity.id)"
                :class="[
                  'w-full p-4 text-left transition-colors hover:bg-gray-50',
                  selectedActivityId === activity.id ? 'bg-primary/5 border-l-4 border-primary' : ''
                ]"
              >
                <div class="flex items-start gap-3">
                  <div class="w-12 h-12 rounded-xl overflow-hidden flex-shrink-0">
                    <img :src="activity.image" :alt="activity.title" class="w-full h-full object-cover" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1">
                      <span :class="['inline-block px-2 py-0.5 rounded-full text-xs font-medium', getTypeColor(activity.type)]">
                        {{ activity.type }}
                      </span>
                      <span 
                        v-if="activity.source === 'created'" 
                        class="text-xs text-gray-400"
                      >
                        我发布的
                      </span>
                    </div>
                    <h4 class="font-medium text-gray-900 truncate">{{ activity.title }}</h4>
                    <p class="text-sm text-gray-500 mt-1">
                      {{ new Date(activity.time).toLocaleString('zh-CN', { month: 'short', day: 'numeric', weekday: 'short', hour: '2-digit', minute: '2-digit' }) }}
                    </p>
                  </div>
                </div>
              </button>
            </div>
          </div>
        </div>
        
        <div class="lg:col-span-2 space-y-6">
          <div v-if="selectedActivity" class="bg-white rounded-2xl shadow-sm overflow-hidden">
            <div class="relative h-40">
              <img :src="selectedActivity.image" :alt="selectedActivity.title" class="w-full h-full object-cover" />
              <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
              <div class="absolute bottom-4 left-6 right-6">
                <div class="flex items-center gap-2 mb-2">
                  <span :class="['inline-block px-3 py-1 rounded-full text-sm font-medium', getTypeColor(selectedActivity.type)]">
                    {{ selectedActivity.type }}
                  </span>
                  <span 
                    v-if="(selectedActivity as any).source === 'created'" 
                    class="inline-block px-2 py-1 rounded-full text-xs font-medium bg-white/30 text-white"
                  >
                    我发布的
                  </span>
                </div>
                <h2 class="text-xl font-bold text-white">{{ selectedActivity.title }}</h2>
              </div>
            </div>
            
            <div class="p-6">
              <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                <div class="bg-gray-50 rounded-xl p-4 text-center">
                  <div class="text-2xl mb-1">⏰</div>
                  <p class="text-xs text-gray-500">集合时间</p>
                  <p class="font-semibold text-gray-900 text-sm mt-1">
                    {{ new Date(selectedActivity.time).toLocaleString('zh-CN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }) }}
                  </p>
                </div>
                <div class="bg-gray-50 rounded-xl p-4 text-center">
                  <div class="text-2xl mb-1">📍</div>
                  <p class="text-xs text-gray-500">集合地点</p>
                  <p class="font-semibold text-gray-900 text-sm mt-1 truncate" :title="selectedActivity.location">
                    {{ selectedActivity.location }}
                  </p>
                </div>
                <div class="bg-gray-50 rounded-xl p-4 text-center">
                  <div class="text-2xl mb-1">⏳</div>
                  <p class="text-xs text-gray-500">距离开始</p>
                  <p :class="['font-semibold text-sm mt-1', timeUntilActivity?.urgent ? 'text-red-600' : 'text-gray-900']">
                    {{ timeUntilActivity?.text }}
                  </p>
                </div>
                <div class="bg-gray-50 rounded-xl p-4 text-center">
                  <div class="text-2xl mb-1">⚠️</div>
                  <p class="text-xs text-gray-500">迟到风险</p>
                  <p :class="['font-semibold text-sm mt-1', lateRiskText.color]">
                    {{ lateRiskText.text }}
                  </p>
                </div>
              </div>
              
              <div class="mb-6">
                <div class="flex items-center justify-between mb-2">
                  <h3 class="font-semibold text-gray-900">🎒 携带物品清单</h3>
                  <span class="text-sm text-gray-500">
                    已完成 {{ checkedCount }}/{{ totalCount }}
                  </span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-2 mb-4">
                  <div 
                    class="bg-gradient-to-r from-primary to-orange-400 h-2 rounded-full transition-all duration-300"
                    :style="{ width: `${progressPercent}%` }"
                  ></div>
                </div>
                
                <div v-if="progressPercent === 100" class="bg-green-50 border border-green-200 rounded-xl p-4 mb-4">
                  <div class="flex items-center gap-3">
                    <div class="text-2xl">✅</div>
                    <div>
                      <p class="font-semibold text-green-700">太棒了！全部准备就绪</p>
                      <p class="text-sm text-green-600">所有物品都已准备好，放心出发吧</p>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="space-y-6">
                <div v-for="(items, category) in itemsByCategory" :key="category">
                  <h4 class="text-sm font-medium text-gray-700 mb-3 flex items-center gap-2">
                    <span class="w-1 h-4 bg-primary rounded-full"></span>
                    {{ category }}
                  </h4>
                  <div class="grid grid-cols-1 sm:grid-cols-2 gap-2">
                    <button
                      v-for="item in items"
                      :key="item.name"
                      @click="toggleItem(item)"
                      :class="[
                        'flex items-center gap-3 p-3 rounded-xl border-2 transition-all text-left',
                        item.checked 
                          ? 'border-primary bg-primary/5' 
                          : 'border-gray-100 hover:border-gray-200 hover:bg-gray-50'
                      ]"
                    >
                      <span class="text-xl">{{ item.icon }}</span>
                      <span :class="['flex-1 text-sm', item.checked ? 'text-gray-400 line-through' : 'text-gray-700']">
                        {{ item.name }}
                      </span>
                      <div :class="[
                        'w-5 h-5 rounded-full border-2 flex items-center justify-center flex-shrink-0 transition-colors',
                        item.checked ? 'bg-primary border-primary' : 'border-gray-300'
                      ]">
                        <svg v-if="item.checked" class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7" />
                        </svg>
                      </div>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="checklistData" class="bg-white rounded-2xl shadow-sm p-6">
            <h3 class="font-semibold text-gray-900 mb-4">🚇 交通方式推荐</h3>
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
              <div 
                v-for="transport in checklistData.transportOptions" 
                :key="transport.type"
                class="bg-gray-50 rounded-xl p-4 text-center hover:bg-gray-100 transition-colors"
              >
                <div class="text-3xl mb-2">{{ transport.icon }}</div>
                <p class="font-medium text-gray-900">{{ transport.type }}</p>
                <p class="text-sm text-gray-500 mt-1">{{ transport.duration }}</p>
                <span :class="['inline-block mt-2 px-2 py-0.5 rounded-full text-xs font-medium', getRiskColor(transport.risk)]">
                  {{ getRiskText(transport.risk) }}
                </span>
                <p class="text-xs text-gray-400 mt-2">{{ transport.description }}</p>
              </div>
            </div>
          </div>
          
          <div v-if="checklistData" class="bg-white rounded-2xl shadow-sm p-6">
            <h3 class="font-semibold text-gray-900 mb-4">💡 温馨提示</h3>
            <div class="space-y-3">
              <div 
                v-for="(tip, index) in checklistData.tips" 
                :key="index"
                class="flex items-start gap-3 p-3 bg-yellow-50 rounded-xl"
              >
                <span class="text-lg flex-shrink-0">💡</span>
                <p class="text-sm text-yellow-800">{{ tip }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
