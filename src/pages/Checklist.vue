<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Navbar from '@/components/Navbar.vue'
import { getRegisteredActivities, type Activity } from '@/api/index'

const CURRENT_USER_ID = 2

const loading = ref(true)
const registeredActivities = ref<Activity[]>([])
const selectedActivityId = ref<number | null>(null)

interface ChecklistItem {
  name: string
  icon: string
  checked: boolean
  category: string
}

interface TransportOption {
  type: string
  icon: string
  duration: string
  risk: 'low' | 'medium' | 'high'
  description: string
}

interface ActivityChecklist {
  items: ChecklistItem[]
  transportOptions: TransportOption[]
  tips: string[]
}

const typeChecklistConfig: Record<string, ActivityChecklist> = {
  '徒步': {
    items: [
      { name: '运动鞋/登山鞋', icon: '👟', checked: false, category: '穿着' },
      { name: '运动服装', icon: '👕', checked: false, category: '穿着' },
      { name: '遮阳帽/太阳镜', icon: '🕶️', checked: false, category: '穿着' },
      { name: '防晒霜', icon: '🧴', checked: false, category: '防护' },
      { name: '驱蚊液', icon: '🦟', checked: false, category: '防护' },
      { name: '充电宝', icon: '🔋', checked: false, category: '电子' },
      { name: '手机', icon: '📱', checked: false, category: '电子' },
      { name: '饮用水（至少500ml）', icon: '💧', checked: false, category: '补给' },
      { name: '能量棒/小零食', icon: '🍫', checked: false, category: '补给' },
      { name: '纸巾/湿巾', icon: '🧻', checked: false, category: '其他' },
      { name: '垃圾袋', icon: '🗑️', checked: false, category: '其他' },
      { name: '急救包（创可贴等）', icon: '🩹', checked: false, category: '其他' },
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约45分钟', risk: 'low', description: '准时可靠，推荐' },
      { type: '公交', icon: '🚌', duration: '约60分钟', risk: 'medium', description: '可能堵车，预留时间' },
      { type: '打车', icon: '🚕', duration: '约30分钟', risk: 'low', description: '费用较高，可拼车' },
      { type: '自驾', icon: '🚗', duration: '约25分钟', risk: 'medium', description: '停车可能紧张' },
    ],
    tips: [
      '提前15分钟到达集合点，热身准备',
      '沿途注意补水，少量多次',
      '跟随队伍，不要擅自离队',
      '下山时注意膝盖保护',
    ],
  },
  '打球': {
    items: [
      { name: '运动服', icon: '👕', checked: false, category: '穿着' },
      { name: '运动鞋', icon: '👟', checked: false, category: '穿着' },
      { name: '运动手环/护具', icon: '⌚', checked: false, category: '装备' },
      { name: '球拍（如有）', icon: '🏸', checked: false, category: '装备' },
      { name: '运动毛巾', icon: '🧣', checked: false, category: '装备' },
      { name: '换洗衣物', icon: '👔', checked: false, category: '其他' },
      { name: '饮用水/运动饮料', icon: '💧', checked: false, category: '补给' },
      { name: '香蕉/能量棒', icon: '🍌', checked: false, category: '补给' },
      { name: '手机', icon: '📱', checked: false, category: '电子' },
      { name: '充电宝', icon: '🔋', checked: false, category: '电子' },
      { name: '纸巾/湿巾', icon: '🧻', checked: false, category: '其他' },
      { name: '洗发水/沐浴露', icon: '🧴', checked: false, category: '其他' },
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约40分钟', risk: 'low', description: '准时可靠' },
      { type: '公交', icon: '🚌', duration: '约55分钟', risk: 'medium', description: '可能堵车' },
      { type: '打车', icon: '🚕', duration: '约25分钟', risk: 'low', description: '携带装备方便' },
      { type: '自驾', icon: '🚗', duration: '约20分钟', risk: 'low', description: '有停车场，方便' },
    ],
    tips: [
      '运动前做好热身，避免拉伤',
      '运动中注意补充水分',
      '运动后做好拉伸放松',
      '记得带换洗衣物，运动后可洗澡',
    ],
  },
  '桌游': {
    items: [
      { name: '手机+充电宝', icon: '📱', checked: false, category: '电子' },
      { name: '身份证', icon: '🪪', checked: false, category: '证件' },
      { name: '口罩（可选）', icon: '😷', checked: false, category: '防护' },
      { name: '纸巾', icon: '🧻', checked: false, category: '其他' },
      { name: '口香糖/薄荷糖', icon: '🍬', checked: false, category: '其他' },
      { name: '雨伞（看天气）', icon: '☂️', checked: false, category: '其他' },
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约35分钟', risk: 'low', description: '推荐，不堵车' },
      { type: '公交', icon: '🚌', duration: '约50分钟', risk: 'medium', description: '晚高峰可能堵车' },
      { type: '打车', icon: '🚕', duration: '约20分钟', risk: 'low', description: '方便快捷' },
      { type: '骑行', icon: '🚲', duration: '约25分钟', risk: 'medium', description: '锻炼身体，注意安全' },
    ],
    tips: [
      '提前了解桌游规则，快速上手',
      '桌游吧一般有饮料，可自带水杯',
      '玩到深夜注意安全，结伴回家',
      '保持手机电量充足',
    ],
  },
  '聚餐': {
    items: [
      { name: '手机+充电宝', icon: '📱', checked: false, category: '电子' },
      { name: '身份证', icon: '🪪', checked: false, category: '证件' },
      { name: '口罩（可选）', icon: '😷', checked: false, category: '防护' },
      { name: '纸巾/湿巾', icon: '🧻', checked: false, category: '其他' },
      { name: '口香糖/薄荷糖', icon: '🍬', checked: false, category: '其他' },
      { name: '雨伞（看天气）', icon: '☂️', checked: false, category: '其他' },
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约40分钟', risk: 'low', description: '不堵车，推荐' },
      { type: '公交', icon: '🚌', duration: '约55分钟', risk: 'high', description: '晚高峰大概率堵车' },
      { type: '打车', icon: '🚕', duration: '约25分钟', risk: 'medium', description: '晚高峰难打车' },
      { type: '骑行', icon: '🚲', duration: '约30分钟', risk: 'medium', description: '注意交通安全' },
    ],
    tips: [
      '聚餐一般AA制，带好手机支付',
      '如有忌口提前告知组织者',
      '适量饮酒，切勿贪杯',
      '饭后注意安全回家',
    ],
  },
  '探店': {
    items: [
      { name: '手机+充电宝', icon: '📱', checked: false, category: '电子' },
      { name: '相机（可选）', icon: '📷', checked: false, category: '电子' },
      { name: '口罩（可选）', icon: '😷', checked: false, category: '防护' },
      { name: '纸巾/湿巾', icon: '🧻', checked: false, category: '其他' },
      { name: '口香糖/薄荷糖', icon: '🍬', checked: false, category: '其他' },
      { name: '雨伞（看天气）', icon: '☂️', checked: false, category: '其他' },
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约35分钟', risk: 'low', description: '推荐，商圈附近都有地铁' },
      { type: '公交', icon: '🚌', duration: '约50分钟', risk: 'medium', description: '可能堵车' },
      { type: '打车', icon: '🚕', duration: '约20分钟', risk: 'medium', description: '商圈附近打车方便' },
      { type: '步行', icon: '🚶', duration: '约15分钟', risk: 'low', description: '住得近可步行' },
    ],
    tips: [
      '提前了解店铺营业时间',
      '网红店可能需要排队，早点到',
      '拍照注意礼貌，不要影响其他顾客',
      '探店后可以写评价分享',
    ],
  },
  '其他': {
    items: [
      { name: '手机+充电宝', icon: '📱', checked: false, category: '电子' },
      { name: '身份证', icon: '🪪', checked: false, category: '证件' },
      { name: '口罩（可选）', icon: '😷', checked: false, category: '防护' },
      { name: '纸巾', icon: '🧻', checked: false, category: '其他' },
      { name: '雨伞（看天气）', icon: '☂️', checked: false, category: '其他' },
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约40分钟', risk: 'low', description: '准时可靠' },
      { type: '公交', icon: '🚌', duration: '约55分钟', risk: 'medium', description: '可能堵车' },
      { type: '打车', icon: '🚕', duration: '约25分钟', risk: 'low', description: '方便快捷' },
    ],
    tips: [
      '提前了解活动详情',
      '准时到达集合地点',
      '保持手机畅通',
      '有问题及时联系组织者',
    ],
  },
}

const selectedActivity = computed(() => {
  if (!selectedActivityId.value && upcomingActivities.value.length > 0) {
    selectedActivityId.value = upcomingActivities.value[0].id
  }
  return upcomingActivities.value.find(a => a.id === selectedActivityId.value) || null
})

const upcomingActivities = computed(() => {
  const now = new Date()
  return registeredActivities.value
    .filter(a => new Date(a.time) > now)
    .sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime())
})

const checklistData = computed(() => {
  if (!selectedActivity.value) return null
  const type = selectedActivity.value.type
  return typeChecklistConfig[type] || typeChecklistConfig['其他']
})

const itemsByCategory = computed(() => {
  if (!checklistData.value) return {}
  const categories: Record<string, ChecklistItem[]> = {}
  checklistData.value.items.forEach(item => {
    if (!categories[item.category]) {
      categories[item.category] = []
    }
    categories[item.category].push(item)
  })
  return categories
})

const checkedCount = computed(() => {
  if (!checklistData.value) return 0
  return checklistData.value.items.filter(item => item.checked).length
})

const totalCount = computed(() => {
  if (!checklistData.value) return 0
  return checklistData.value.items.length
})

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
    registeredActivities.value = await getRegisteredActivities(CURRENT_USER_ID)
    if (upcomingActivities.value.length > 0) {
      selectedActivityId.value = upcomingActivities.value[0].id
    }
  } catch (error) {
    console.error('Failed to load activities:', error)
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
      
      <div v-if="loading" class="flex items-center justify-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
        <p class="mt-4 text-gray-500">加载中...</p>
      </div>
      
      <div v-else-if="upcomingActivities.length === 0" class="text-center py-20">
        <div class="text-6xl mb-4">🎉</div>
        <h3 class="text-xl font-semibold text-gray-700 mb-2">暂无即将开始的活动</h3>
        <p class="text-gray-500">去首页发现有趣的活动吧</p>
      </div>
      
      <div v-else class="grid lg:grid-cols-3 gap-6">
        <div class="lg:col-span-1">
          <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
            <div class="p-4 border-b border-gray-100">
              <h3 class="font-semibold text-gray-900">我报名的活动</h3>
              <p class="text-sm text-gray-500 mt-1">共 {{ upcomingActivities.length }} 个活动</p>
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
                    <span :class="['inline-block px-2 py-0.5 rounded-full text-xs font-medium mb-1', getTypeColor(activity.type)]">
                      {{ activity.type }}
                    </span>
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
                <span :class="['inline-block px-3 py-1 rounded-full text-sm font-medium mb-2', getTypeColor(selectedActivity.type)]">
                  {{ selectedActivity.type }}
                </span>
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
