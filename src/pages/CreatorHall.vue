<script setup lang="ts">
import { ref, onMounted } from 'vue'
import Navbar from '@/components/Navbar.vue'
import CreatorCard from '@/components/CreatorCard.vue'
import { getCreators, type Creator } from '@/api/index'

const selectedType = ref('')
const sortBy = ref('popular')
const creators = ref<Creator[]>([])
const loading = ref(true)

const activityTypes = ['', '聚餐', '徒步', '打球', '探店', '桌游', '其他']

const sortOptions = [
  { value: 'popular', label: '最活跃' },
  { value: 'successRate', label: '成局率最高' },
  { value: 'fillSpeed', label: '满员最快' },
]

async function loadCreators() {
  loading.value = true
  try {
    creators.value = await getCreators(selectedType.value || undefined, sortBy.value)
  } catch (error) {
    console.error('Failed to load creators:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCreators()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div class="bg-gradient-to-r from-primary via-orange-400 to-orange-500 py-16">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div class="text-5xl mb-4">🎭</div>
        <h1 class="text-4xl font-bold text-white mb-4">发起人风格名片馆</h1>
        <p class="text-white/90 text-lg mb-8 max-w-2xl mx-auto">
          发现风格各异的活动发起人，了解他们的成局率、常办类型和玩家评价，找到最适合你的那个局
        </p>
      </div>
    </div>
    
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8 -mt-8">
      <div class="bg-white rounded-2xl shadow-sm p-6 mb-8">
        <div class="flex flex-col md:flex-row md:items-center gap-4">
          <div class="flex-1">
            <p class="text-sm text-gray-500 mb-2">按活动类型筛选</p>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="type in activityTypes"
                :key="type || 'all'"
                @click="selectedType = type; loadCreators()"
                :class="[
                  'px-4 py-2 rounded-full text-sm font-medium transition-all',
                  selectedType === type
                    ? 'bg-primary text-white shadow-md'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                ]"
              >
                {{ type || '全部类型' }}
              </button>
            </div>
          </div>
          
          <div class="md:w-48">
            <p class="text-sm text-gray-500 mb-2">排序方式</p>
            <select 
              v-model="sortBy"
              @change="loadCreators()"
              class="w-full px-4 py-2 rounded-xl bg-gray-100 text-gray-700 focus:outline-none focus:ring-2 focus:ring-primary/30"
            >
              <option v-for="option in sortOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </div>
        </div>
      </div>
      
      <div v-if="loading" class="text-center py-20">
        <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
        <p class="mt-4 text-gray-500">加载中...</p>
      </div>
      
      <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        <CreatorCard 
          v-for="creator in creators" 
          :key="creator.id"
          :creator="creator"
        />
      </div>
      
      <div v-if="!loading && creators.length === 0" class="text-center py-20">
        <div class="text-gray-300 text-6xl mb-4">🔍</div>
        <p class="text-gray-500">暂无符合条件的发起人</p>
        <p class="text-gray-400 text-sm mt-2">试试调整筛选条件吧</p>
      </div>
    </div>
  </div>
</template>
