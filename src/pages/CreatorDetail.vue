<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import ActivityCard from '@/components/ActivityCard.vue'
import { getCreatorById, getCreatorActivities, type Creator, type Activity } from '@/api/index'

const route = useRoute()
const creatorId = computed(() => parseInt(route.params.id as string))

const creator = ref<Creator | null>(null)
const activities = ref<Activity[]>([])
const loading = ref(true)
const activitiesLoading = ref(true)

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

const formatFillSpeed = (hours: number) => {
  if (hours < 24) return `${hours}小时`
  return `${(hours / 24).toFixed(1)}天`
}

const maxTypeCount = computed(() => {
  if (!creator.value) return 0
  return Math.max(...creator.value.commonTypes.map(t => t.count))
})

const maxAreaCount = computed(() => {
  if (!creator.value) return 0
  return Math.max(...creator.value.commonAreas.map(a => a.count))
})

const maxReviewCount = computed(() => {
  if (!creator.value) return 0
  return Math.max(...creator.value.reviewTags.map(r => r.count))
})

async function loadCreator() {
  loading.value = true
  try {
    creator.value = await getCreatorById(creatorId.value)
  } catch (error) {
    console.error('Failed to load creator:', error)
  } finally {
    loading.value = false
  }
}

async function loadActivities() {
  activitiesLoading.value = true
  try {
    activities.value = await getCreatorActivities(creatorId.value)
  } catch (error) {
    console.error('Failed to load creator activities:', error)
  } finally {
    activitiesLoading.value = false
  }
}

onMounted(() => {
  loadCreator()
  loadActivities()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div v-if="loading" class="text-center py-20">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
      <p class="mt-4 text-gray-500">加载中...</p>
    </div>
    
    <div v-else-if="creator">
      <div class="bg-gradient-to-br from-primary via-orange-400 to-orange-500 pt-16 pb-24">
        <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex flex-col md:flex-row items-center md:items-start gap-6">
            <div class="relative">
              <img 
                :src="creator.avatar" 
                :alt="creator.name"
                class="w-28 h-28 rounded-2xl object-cover border-4 border-white shadow-xl"
              />
              <div class="absolute -bottom-2 left-1/2 -translate-x-1/2 bg-white text-primary text-sm font-bold px-3 py-1 rounded-full shadow">
                {{ creator.totalActivities }} 场活动
              </div>
            </div>
            <div class="text-center md:text-left flex-1">
              <h1 class="text-3xl font-bold text-white">{{ creator.name }}</h1>
              <p class="text-white/90 mt-2 max-w-xl">{{ creator.bio }}</p>
              <div class="flex flex-wrap gap-2 mt-4 justify-center md:justify-start">
                <span 
                  v-for="tag in creator.styleTags" 
                  :key="tag"
                  class="px-3 py-1 bg-white/20 text-white rounded-full text-sm font-medium backdrop-blur-sm"
                >
                  {{ tag }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 -mt-16">
        <div class="bg-white rounded-2xl shadow-lg p-6 mb-6">
          <div class="grid grid-cols-3 divide-x divide-gray-100">
            <div class="text-center">
              <p class="text-4xl font-bold text-primary">{{ creator.successRate }}%</p>
              <p class="text-sm text-gray-500 mt-2">成局率</p>
              <p class="text-xs text-gray-400 mt-1">活动成功举办比例</p>
            </div>
            <div class="text-center">
              <p class="text-4xl font-bold text-orange-500">{{ formatFillSpeed(creator.avgFillSpeedHours) }}</p>
              <p class="text-sm text-gray-500 mt-2">平均满员速度</p>
              <p class="text-xs text-gray-400 mt-1">发布后平均多久报满</p>
            </div>
            <div class="text-center">
              <p class="text-4xl font-bold text-green-500">{{ creator.totalActivities }}</p>
              <p class="text-sm text-gray-500 mt-2">累计举办</p>
              <p class="text-xs text-gray-400 mt-1">历史活动总数</p>
            </div>
          </div>
        </div>
        
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
          <div class="bg-white rounded-2xl shadow-sm p-6">
            <h3 class="font-bold text-lg text-gray-900 mb-4 flex items-center gap-2">
              <span class="text-xl">🎯</span>
              常办活动类型
            </h3>
            <div class="space-y-3">
              <div 
                v-for="item in creator.commonTypes" 
                :key="item.type"
                class="flex items-center gap-3"
              >
                <span :class="['px-3 py-1 rounded-full text-xs font-medium w-20 text-center', getTypeColor(item.type)]">
                  {{ item.type }}
                </span>
                <div class="flex-1 h-6 bg-gray-100 rounded-full overflow-hidden">
                  <div 
                    class="h-full bg-gradient-to-r from-primary to-orange-400 rounded-full transition-all duration-500"
                    :style="{ width: `${(item.count / maxTypeCount) * 100}%` }"
                  ></div>
                </div>
                <span class="text-sm font-medium text-gray-600 w-12 text-right">{{ item.count }}次</span>
              </div>
            </div>
          </div>
          
          <div class="bg-white rounded-2xl shadow-sm p-6">
            <h3 class="font-bold text-lg text-gray-900 mb-4 flex items-center gap-2">
              <span class="text-xl">📍</span>
              常见集合区域
            </h3>
            <div class="space-y-3">
              <div 
                v-for="item in creator.commonAreas" 
                :key="item.name"
                class="flex items-center gap-3"
              >
                <span class="w-20 text-sm text-gray-600 truncate">{{ item.name }}</span>
                <div class="flex-1 h-6 bg-gray-100 rounded-full overflow-hidden">
                  <div 
                    class="h-full bg-gradient-to-r from-blue-400 to-cyan-400 rounded-full transition-all duration-500"
                    :style="{ width: `${(item.count / maxAreaCount) * 100}%` }"
                  ></div>
                </div>
                <span class="text-sm font-medium text-gray-600 w-12 text-right">{{ item.count }}次</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-2xl shadow-sm p-6 mb-6">
          <h3 class="font-bold text-lg text-gray-900 mb-4 flex items-center gap-2">
            <span class="text-xl">💬</span>
            参与者评价倾向
          </h3>
          <div class="space-y-3">
            <div 
              v-for="item in creator.reviewTags" 
              :key="item.tag"
              class="flex items-center gap-3"
            >
              <span class="w-28 text-sm text-gray-600">👍 {{ item.tag }}</span>
              <div class="flex-1 h-6 bg-gray-100 rounded-full overflow-hidden">
                <div 
                  class="h-full bg-gradient-to-r from-yellow-400 to-orange-400 rounded-full transition-all duration-500"
                  :style="{ width: `${(item.count / maxReviewCount) * 100}%` }"
                ></div>
              </div>
              <span class="text-sm font-medium text-gray-600 w-12 text-right">{{ item.count }}人</span>
            </div>
          </div>
        </div>
        
        <div class="bg-white rounded-2xl shadow-sm p-6">
          <h3 class="font-bold text-lg text-gray-900 mb-4 flex items-center gap-2">
            <span class="text-xl">🎉</span>
            TA 发起的活动
          </h3>
          
          <div v-if="activitiesLoading" class="text-center py-12">
            <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
            <p class="mt-3 text-gray-500 text-sm">加载中...</p>
          </div>
          
          <div v-else-if="activities.length > 0" class="grid md:grid-cols-2 gap-4">
            <ActivityCard 
              v-for="activity in activities" 
              :key="activity.id"
              :activity="activity"
            />
          </div>
          
          <div v-else class="text-center py-12">
            <div class="text-gray-300 text-5xl mb-3">📭</div>
            <p class="text-gray-500">暂无活动</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
