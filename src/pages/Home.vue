<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import Navbar from '@/components/Navbar.vue'
import ActivityCard from '@/components/ActivityCard.vue'
import { getActivities, getHotActivities, type Activity } from '@/api/index'
import { allCities } from '@/data/cities'
import { getDistrictsByCity, convenienceOptions, matchDistrictByLocation, type BusinessDistrict } from '@/data/locationData'

const selectedCity = ref('')
const selectedDistrict = ref('')
const selectedDistrictType = ref('')
const selectedConvenience = ref(0)
const selectedType = ref('')
const sortBy = ref('newest')
const activities = ref<Activity[]>([])
const hotActivities = ref<Activity[]>([])
const hotTimeRange = ref('7days')
const loading = ref(true)
const hotLoading = ref(false)

const hotTimeOptions = [
  { value: 'realtime', label: '实时热度' },
  { value: '3days', label: '近3天热度' },
  { value: '7days', label: '近7天热度' },
]

const activityTypes = ['聚餐', '徒步', '打球', '探店', '桌游', '其他']

const availableDistricts = computed(() => {
  if (!selectedCity.value) return []
  return getDistrictsByCity(selectedCity.value)
})

const districtTypes = computed(() => {
  const types = new Set<string>()
  availableDistricts.value.forEach(d => types.add(d.type))
  return Array.from(types)
})

const filteredDistricts = computed(() => {
  if (!selectedDistrictType.value) return availableDistricts.value
  return availableDistricts.value.filter(d => d.type === selectedDistrictType.value)
})

const getActivityDistrict = (activity: Activity): BusinessDistrict | null => {
  return matchDistrictByLocation(activity.location, activity.city)
}

const filteredActivities = computed(() => {
  let result = [...activities.value]
  
  if (selectedCity.value) {
    result = result.filter(a => a.city === selectedCity.value)
  }
  
  if (selectedDistrict.value) {
    result = result.filter(a => {
      const district = getActivityDistrict(a)
      return district?.name === selectedDistrict.value
    })
  }
  
  if (selectedConvenience.value > 0) {
    result = result.filter(a => {
      const district = getActivityDistrict(a)
      return district && district.convenienceScore >= selectedConvenience.value
    })
  }
  
  if (selectedType.value) {
    result = result.filter(a => a.type === selectedType.value)
  }
  
  return result
})

watch(selectedCity, () => {
  selectedDistrict.value = ''
  selectedDistrictType.value = ''
})

async function loadActivities() {
  loading.value = true
  try {
    activities.value = await getActivities(selectedCity.value || undefined, selectedType.value || undefined, sortBy.value)
    hotActivities.value = await getHotActivities(hotTimeRange.value)
  } catch (error) {
    console.error('Failed to load activities:', error)
  } finally {
    loading.value = false
  }
}

async function loadHotActivities() {
  hotLoading.value = true
  try {
    hotActivities.value = await getHotActivities(hotTimeRange.value)
  } catch (error) {
    console.error('Failed to load hot activities:', error)
  } finally {
    hotLoading.value = false
  }
}

onMounted(() => {
  loadActivities()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div class="bg-gradient-to-r from-primary via-orange-400 to-orange-500 py-12">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h1 class="text-4xl font-bold text-white mb-4">发现身边的精彩活动</h1>
        <p class="text-white/90 text-lg mb-8">和志同道合的朋友一起，探索城市的无限可能</p>
        <div class="max-w-4xl mx-auto">
          <div class="flex gap-3 flex-wrap justify-center">
            <select 
              v-model="selectedCity"
              @change="loadActivities"
              class="px-4 py-3 rounded-xl bg-white/95 text-gray-700 focus:outline-none focus:ring-2 focus:ring-white/50 min-w-[140px]"
            >
              <option value="">全部城市</option>
              <option v-for="city in allCities" :key="city" :value="city">{{ city }}</option>
            </select>
            
            <select 
              v-if="selectedCity && districtTypes.length > 0"
              v-model="selectedDistrictType"
              class="px-4 py-3 rounded-xl bg-white/95 text-gray-700 focus:outline-none focus:ring-2 focus:ring-white/50 min-w-[140px]"
            >
              <option value="">全部类型</option>
              <option v-for="type in districtTypes" :key="type" :value="type">{{ type }}</option>
            </select>
            
            <select 
              v-if="selectedCity && filteredDistricts.length > 0"
              v-model="selectedDistrict"
              class="px-4 py-3 rounded-xl bg-white/95 text-gray-700 focus:outline-none focus:ring-2 focus:ring-white/50 min-w-[140px]"
            >
              <option value="">全部商圈/片区</option>
              <option v-for="district in filteredDistricts" :key="district.id" :value="district.name">
                {{ district.name }}
              </option>
            </select>
            
            <select 
              v-if="selectedCity"
              v-model="selectedConvenience"
              class="px-4 py-3 rounded-xl bg-white/95 text-gray-700 focus:outline-none focus:ring-2 focus:ring-white/50 min-w-[140px]"
            >
              <option v-for="option in convenienceOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
            
            <select 
              v-model="sortBy"
              @change="loadActivities"
              class="px-4 py-3 rounded-xl bg-white/95 text-gray-700 focus:outline-none focus:ring-2 focus:ring-white/50 min-w-[140px]"
            >
              <option value="newest">最新发布</option>
              <option value="popular">浏览最多</option>
              <option value="hot">报名最火</option>
            </select>
          </div>
          <div v-if="selectedCity" class="mt-4 flex gap-2 justify-center flex-wrap text-white/80 text-sm">
            <span v-if="selectedDistrictType">📍 类型: {{ selectedDistrictType }}</span>
            <span v-if="selectedDistrict">🏢 区域: {{ selectedDistrict }}</span>
            <span v-if="selectedConvenience > 0">🚇 便利度: {{ convenienceOptions.find(o => o.value === selectedConvenience)?.label }}</span>
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
            <div class="flex gap-1 mb-4 bg-gray-100 p-1 rounded-lg">
              <button
                v-for="option in hotTimeOptions"
                :key="option.value"
                @click="hotTimeRange = option.value; loadHotActivities()"
                :class="[
                  'flex-1 px-2 py-1.5 rounded-md text-xs font-medium transition-all',
                  hotTimeRange === option.value
                    ? 'bg-white text-primary shadow-sm'
                    : 'text-gray-600 hover:text-gray-900'
                ]"
              >
                {{ option.label }}
              </button>
            </div>
            <div v-if="loading || hotLoading" class="text-center py-8">
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
