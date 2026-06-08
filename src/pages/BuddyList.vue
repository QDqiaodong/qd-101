<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { getBuddyRequests, type BuddyRequest } from '@/api/index'
import { allCities } from '@/data/cities'

const router = useRouter()

const CURRENT_USER_ID = 2

const buddyTypes = ['饭搭子', '球搭子', '探店搭子', '健身搭子', '户外运动', '其他']
const statusOptions = [
  { value: '', label: '全部状态' },
  { value: 'OPEN', label: '征集中' },
  { value: 'MATCHING', label: '配对中' },
  { value: 'MATCHED', label: '已配对' },
  { value: 'CONVERTED', label: '已转活动' },
  { value: 'CLOSED', label: '已关闭' },
]

const selectedCity = ref('')
const selectedType = ref('')
const selectedStatus = ref('')
const sortBy = ref('newest')
const buddyRequests = ref<BuddyRequest[]>([])
const loading = ref(true)

async function loadBuddyRequests() {
  loading.value = true
  try {
    buddyRequests.value = await getBuddyRequests(
      selectedCity.value || undefined,
      selectedType.value || undefined,
      selectedStatus.value || undefined,
      sortBy.value
    )
  } catch (error) {
    console.error('Failed to load buddy requests:', error)
  } finally {
    loading.value = false
  }
}

function getTypeColor(type: string) {
  const colors: Record<string, string> = {
    '饭搭子': 'bg-red-100 text-red-600',
    '球搭子': 'bg-blue-100 text-blue-600',
    '探店搭子': 'bg-orange-100 text-orange-600',
    '健身搭子': 'bg-green-100 text-green-600',
    '户外运动': 'bg-teal-100 text-teal-600',
    '其他': 'bg-gray-100 text-gray-600',
  }
  return colors[type] || colors['其他']
}

function getStatusInfo(status: string) {
  const map: Record<string, { label: string; color: string }> = {
    'OPEN': { label: '征集中', color: 'bg-green-100 text-green-600' },
    'MATCHING': { label: '配对中', color: 'bg-yellow-100 text-yellow-600' },
    'MATCHED': { label: '已配对', color: 'bg-blue-100 text-blue-600' },
    'CONVERTED': { label: '已转活动', color: 'bg-purple-100 text-purple-600' },
    'CLOSED': { label: '已关闭', color: 'bg-gray-100 text-gray-600' },
  }
  return map[status] || map['OPEN']
}

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const progressPercent = computed(() => (item: BuddyRequest) => {
  return Math.min((item.currentCount / item.targetCount) * 100, 100)
})

onMounted(() => {
  loadBuddyRequests()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="text-2xl font-bold text-gray-900">找搭子</h1>
          <p class="text-gray-500 mt-1">轻量征集，快速配对，找到志同道合的小伙伴</p>
        </div>
        <button
          @click="router.push('/buddies/publish')"
          class="px-6 py-3 bg-gradient-to-r from-primary to-orange-400 text-white font-medium rounded-xl hover:shadow-lg transition-all duration-300"
        >
          + 发布搭子征集
        </button>
      </div>

      <div class="bg-white rounded-xl shadow-sm p-6 mb-6">
        <div class="flex flex-wrap gap-4 items-end">
          <div class="flex-1 min-w-[150px]">
            <label class="block text-sm font-medium text-gray-700 mb-2">城市</label>
            <select
              v-model="selectedCity"
              @change="loadBuddyRequests"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            >
              <option value="">全部城市</option>
              <option v-for="city in allCities" :key="city" :value="city">{{ city }}</option>
            </select>
          </div>
          <div class="flex-1 min-w-[150px]">
            <label class="block text-sm font-medium text-gray-700 mb-2">类型</label>
            <select
              v-model="selectedType"
              @change="loadBuddyRequests"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            >
              <option value="">全部类型</option>
              <option v-for="type in buddyTypes" :key="type" :value="type">{{ type }}</option>
            </select>
          </div>
          <div class="flex-1 min-w-[150px]">
            <label class="block text-sm font-medium text-gray-700 mb-2">状态</label>
            <select
              v-model="selectedStatus"
              @change="loadBuddyRequests"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            >
              <option v-for="opt in statusOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
            </select>
          </div>
          <div class="flex-1 min-w-[150px]">
            <label class="block text-sm font-medium text-gray-700 mb-2">排序</label>
            <select
              v-model="sortBy"
              @change="loadBuddyRequests"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            >
              <option value="newest">最新发布</option>
              <option value="popular">最热门</option>
            </select>
          </div>
        </div>
      </div>

      <div v-if="loading" class="text-center py-16">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary border-t-transparent"></div>
        <p class="text-gray-500 mt-4">加载中...</p>
      </div>

      <div v-else-if="buddyRequests.length === 0" class="text-center py-16 bg-white rounded-xl shadow-sm">
        <div class="text-6xl mb-4">🔍</div>
        <h3 class="text-lg font-medium text-gray-900 mb-2">暂无搭子征集</h3>
        <p class="text-gray-500 mb-6">快来发布第一个搭子征集吧~</p>
        <button
          @click="router.push('/buddies/publish')"
          class="px-6 py-2.5 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors"
        >
          立即发布
        </button>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="item in buddyRequests"
          :key="item.id"
          class="bg-white rounded-xl shadow-sm hover:shadow-lg transition-all duration-300 cursor-pointer overflow-hidden"
          @click="router.push(`/buddy/${item.id}`)"
        >
          <div class="p-5">
            <div class="flex items-start justify-between mb-3">
              <span :class="['px-3 py-1 rounded-full text-sm font-medium', getTypeColor(item.type)]">
                {{ item.type }}
              </span>
              <span :class="['px-3 py-1 rounded-full text-xs font-medium', getStatusInfo(item.status).color]">
                {{ getStatusInfo(item.status).label }}
              </span>
            </div>

            <h3 class="font-semibold text-lg text-gray-900 mb-2 line-clamp-2">{{ item.title }}</h3>

            <p class="text-sm text-gray-500 mb-4 line-clamp-2">{{ item.description }}</p>

            <div class="flex items-center gap-3 text-sm text-gray-500 mb-4">
              <div class="flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                </svg>
                <span>{{ item.city }}</span>
              </div>
              <div class="flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>{{ formatTime(item.createdAt) }}</span>
              </div>
            </div>

            <div class="flex items-center gap-3">
              <img
                :src="item.creatorAvatar"
                :alt="item.creatorName"
                class="w-8 h-8 rounded-full object-cover"
              />
              <span class="text-sm text-gray-600">{{ item.creatorName }}</span>
            </div>

            <div class="mt-4">
              <div class="flex items-center justify-between text-sm mb-2">
                <span class="text-gray-500">已配对 {{ item.currentCount }}/{{ item.targetCount }} 人</span>
                <span class="text-primary font-medium">{{ Math.round(progressPercent(item)) }}%</span>
              </div>
              <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
                <div
                  class="h-full bg-gradient-to-r from-primary to-orange-400 rounded-full transition-all duration-500"
                  :style="{ width: `${progressPercent(item)}%` }"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
