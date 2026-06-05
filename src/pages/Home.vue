<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Navbar from '@/components/Navbar.vue'
import ActivityCard from '@/components/ActivityCard.vue'
import { getActivities, getHotActivities, type Activity } from '@/api/index'
import { allCities } from '@/data/cities'

const selectedCity = ref('')
const selectedType = ref('')
const sortBy = ref('newest')
const activities = ref<Activity[]>([])
const hotActivities = ref<Activity[]>([])
const loading = ref(true)

const activityTypes = ['聚餐', '徒步', '打球', '探店', '桌游', '其他']

const filteredActivities = computed(() => {
  let result = [...activities.value]
  
  if (selectedCity.value) {
    result = result.filter(a => a.city === selectedCity.value)
  }
  
  if (selectedType.value) {
    result = result.filter(a => a.type === selectedType.value)
  }
  
  return result
})

async function loadActivities() {
  loading.value = true
  try {
    activities.value = await getActivities(selectedCity.value || undefined, selectedType.value || undefined, sortBy.value)
    hotActivities.value = await getHotActivities()
  } catch (error) {
    console.error('Failed to load activities:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadActivities()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div class="bg-gradient-to-r from-primary via-orange-400 to-orange-500 py-16">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h1 class="text-4xl font-bold text-white mb-4">发现身边的精彩活动</h1>
        <p class="text-white/90 text-lg mb-8">和志同道合的朋友一起，探索城市的无限可能</p>
        <div class="max-w-2xl mx-auto">
          <div class="flex gap-3 flex-wrap justify-center">
            <select 
              v-model="selectedCity"
              @change="loadActivities"
              class="px-4 py-3 rounded-xl bg-white/95 text-gray-700 focus:outline-none focus:ring-2 focus:ring-white/50 min-w-[150px]"
            >
              <option value="">全部城市</option>
              <option v-for="city in allCities" :key="city" :value="city">{{ city }}</option>
            </select>
            
            <select 
              v-model="sortBy"
              @change="loadActivities"
              class="px-4 py-3 rounded-xl bg-white/95 text-gray-700 focus:outline-none focus:ring-2 focus:ring-white/50 min-w-[150px]"
            >
              <option value="newest">最新发布</option>
              <option value="popular">浏览最多</option>
              <option value="hot">报名最火</option>
            </select>
          </div>
        </div>
      </div>
    </div>
    
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex flex-col lg:flex-row gap-8">
        <div class="flex-1">
          <div class="flex gap-2 mb-6 overflow-x-auto pb-2">
            <button
              v-for="type in ['', ...activityTypes]"
              :key="type || 'all'"
              @click="selectedType = type; loadActivities()"
              :class="[
                'px-4 py-2 rounded-full whitespace-nowrap transition-all',
                selectedType === type
                  ? 'bg-primary text-white shadow-md'
                  : 'bg-white text-gray-600 hover:bg-gray-100'
              ]"
            >
              {{ type || '全部类型' }}
            </button>
          </div>
          
          <div v-if="loading" class="text-center py-16">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
            <p class="mt-4 text-gray-500">加载中...</p>
          </div>
          
          <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
            <ActivityCard 
              v-for="activity in filteredActivities" 
              :key="activity.id"
              :activity="activity"
            />
          </div>
          
          <div v-if="!loading && filteredActivities.length === 0" class="text-center py-16">
            <div class="text-gray-400 text-6xl mb-4">🎉</div>
            <p class="text-gray-500">暂无符合条件的活动</p>
            <p class="text-gray-400 text-sm mt-2">试试调整筛选条件吧</p>
          </div>
        </div>
        
        <div class="lg:w-80">
          <div class="bg-white rounded-xl shadow-sm p-6 sticky top-24">
            <h3 class="text-lg font-semibold text-gray-900 mb-4 flex items-center gap-2">
              <span class="text-2xl">🔥</span>
              热门活动榜
            </h3>
            <div v-if="loading" class="text-center py-8">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
            </div>
            <div v-else class="space-y-4">
              <div 
                v-for="(activity, index) in hotActivities"
                :key="activity.id"
                class="flex items-start gap-3 cursor-pointer hover:bg-gray-50 p-2 rounded-lg transition-colors"
                @click="$router.push(`/activity/${activity.id}`)"
              >
                <div 
                  :class="[
                    'w-6 h-6 rounded-full flex items-center justify-center text-sm font-bold text-white',
                    index === 0 ? 'bg-yellow-400' :
                    index === 1 ? 'bg-gray-300' :
                    index === 2 ? 'bg-orange-400' : 'bg-gray-200 text-gray-600'
                  ]"
                >
                  {{ index + 1 }}
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium text-gray-900 line-clamp-2">{{ activity.title }}</p>
                  <p class="text-xs text-gray-500 mt-1">{{ activity.city }} · {{ activity.currentParticipants }}人报名</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
