<script setup lang="ts">
import type { ActivityFootprint } from '@/api/index'
import { useRouter } from 'vue-router'

defineProps<{
  footprints: ActivityFootprint[]
  loading?: boolean
}>()

const router = useRouter()

function getEventTypeInfo(eventType: string) {
  const map: Record<string, { icon: string; label: string; color: string; bgColor: string }> = {
    PUBLISHED: { icon: '📝', label: '发布活动', color: 'text-blue-600', bgColor: 'bg-blue-100' },
    REGISTERED: { icon: '✅', label: '报名活动', color: 'text-green-600', bgColor: 'bg-green-100' },
    CANCELLED: { icon: '❌', label: '取消报名', color: 'text-red-600', bgColor: 'bg-red-100' },
    FULL: { icon: '🎉', label: '活动满员', color: 'text-orange-600', bgColor: 'bg-orange-100' },
    CONFIRMED: { icon: '👏', label: '活动成局', color: 'text-purple-600', bgColor: 'bg-purple-100' },
    EXPIRED: { icon: '⏰', label: '活动结束', color: 'text-gray-600', bgColor: 'bg-gray-100' }
  }
  return map[eventType] || { icon: '📌', label: eventType, color: 'text-gray-600', bgColor: 'bg-gray-100' }
}

function formatDate(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (24 * 60 * 60 * 1000))
  const hours = Math.floor(diff / (60 * 60 * 1000))
  const minutes = Math.floor(diff / (60 * 1000))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return `${date.getMonth() + 1}月${date.getDate()}日`
}

function formatActivityTime(dateStr: string) {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

function goToDetail(activityId: number) {
  router.push(`/activity/${activityId}`)
}
</script>

<template>
  <div class="activity-timeline">
    <div v-if="loading" class="text-center py-16">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
      <p class="mt-4 text-gray-500">加载中...</p>
    </div>

    <div v-else-if="footprints.length === 0" class="text-center py-16">
      <div class="text-gray-300 text-6xl mb-4">📅</div>
      <p class="text-gray-500">还没有活动足迹</p>
      <p class="text-gray-400 text-sm mt-2">去发现有趣的活动，开启你的社交轨迹</p>
    </div>

    <div v-else class="relative">
      <div class="absolute left-6 top-0 bottom-0 w-0.5 bg-gray-200"></div>

      <div 
        v-for="footprint in footprints" 
        :key="footprint.id"
        class="relative pl-16 pb-8 last:pb-0"
      >
        <div 
          :class="[
            'absolute left-4 w-5 h-5 rounded-full flex items-center justify-center text-xs ring-4 ring-white z-10',
            getEventTypeInfo(footprint.eventType).bgColor
          ]"
        >
          <span class="text-sm">{{ getEventTypeInfo(footprint.eventType).icon }}</span>
        </div>

        <div 
          class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow cursor-pointer"
          @click="goToDetail(footprint.activityId)"
        >
          <div class="p-4">
            <div class="flex items-start justify-between mb-2">
              <span 
                :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
                  getEventTypeInfo(footprint.eventType).bgColor,
                  getEventTypeInfo(footprint.eventType).color
                ]"
              >
                {{ getEventTypeInfo(footprint.eventType).label }}
              </span>
              <span class="text-xs text-gray-400">{{ formatDate(footprint.eventTime) }}</span>
            </div>

            <p class="text-gray-800 font-medium mb-2 line-clamp-1">{{ footprint.description }}</p>

            <div class="flex items-center gap-3">
              <img 
                :src="footprint.image" 
                :alt="footprint.title"
                class="w-12 h-12 rounded-lg object-cover flex-shrink-0"
              />
              <div class="flex-1 min-w-0">
                <h4 class="text-sm font-medium text-gray-900 line-clamp-1">{{ footprint.title }}</h4>
                <p class="text-xs text-gray-500 mt-0.5 flex items-center gap-1">
                  <span>📍</span>
                  <span class="truncate">{{ footprint.location }}</span>
                </p>
                <p class="text-xs text-gray-400 mt-0.5 flex items-center gap-1">
                  <span>🕐</span>
                  <span>{{ formatActivityTime(footprint.activityTime) }}</span>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
