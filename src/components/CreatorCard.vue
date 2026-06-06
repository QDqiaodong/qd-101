<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Creator } from '@/api/index'

const props = defineProps<{
  creator: Creator
}>()

const router = useRouter()

const topTypes = computed(() => {
  return props.creator.commonTypes.slice(0, 2)
})

const topAreas = computed(() => {
  return props.creator.commonAreas.slice(0, 2)
})

const topReviews = computed(() => {
  return props.creator.reviewTags.slice(0, 3)
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

const formatFillSpeed = (hours: number) => {
  if (hours < 24) return `${hours}小时`
  return `${(hours / 24).toFixed(1)}天`
}
</script>

<template>
  <div 
    class="bg-white rounded-2xl shadow-sm hover:shadow-xl transition-all duration-300 cursor-pointer overflow-hidden group"
    @click="router.push(`/creator/${creator.id}`)"
  >
    <div class="bg-gradient-to-br from-primary/10 to-orange-400/10 p-6 pb-4">
      <div class="flex items-start gap-4">
        <div class="relative">
          <img 
            :src="creator.avatar" 
            :alt="creator.name"
            class="w-16 h-16 rounded-full object-cover border-2 border-white shadow-md group-hover:scale-105 transition-transform duration-300"
          />
          <div class="absolute -bottom-1 -right-1 bg-primary text-white text-xs font-medium px-2 py-0.5 rounded-full">
            {{ creator.totalActivities }}局
          </div>
        </div>
        <div class="flex-1 min-w-0">
          <h3 class="font-bold text-lg text-gray-900 group-hover:text-primary transition-colors">
            {{ creator.name }}
          </h3>
          <p class="text-sm text-gray-500 mt-1 line-clamp-2">{{ creator.bio }}</p>
        </div>
      </div>
      
      <div class="flex flex-wrap gap-1.5 mt-3">
        <span 
          v-for="tag in creator.styleTags.slice(0, 3)" 
          :key="tag"
          class="px-2.5 py-1 bg-white/80 text-primary text-xs font-medium rounded-full"
        >
          {{ tag }}
        </span>
      </div>
    </div>
    
    <div class="grid grid-cols-3 divide-x divide-gray-100 py-4 px-2">
      <div class="text-center">
        <p class="text-2xl font-bold text-gray-900">{{ creator.successRate }}%</p>
        <p class="text-xs text-gray-500 mt-1">成局率</p>
      </div>
      <div class="text-center">
        <p class="text-2xl font-bold text-gray-900">{{ formatFillSpeed(creator.avgFillSpeedHours) }}</p>
        <p class="text-xs text-gray-500 mt-1">平均满员</p>
      </div>
      <div class="text-center">
        <p class="text-2xl font-bold text-gray-900">{{ creator.totalActivities }}</p>
        <p class="text-xs text-gray-500 mt-1">举办活动</p>
      </div>
    </div>
    
    <div class="px-5 pb-5 space-y-4">
      <div>
        <p class="text-xs text-gray-400 font-medium mb-2">常办活动类型</p>
        <div class="flex flex-wrap gap-2">
          <span 
            v-for="item in topTypes" 
            :key="item.type"
            :class="['px-3 py-1 rounded-full text-xs font-medium', getTypeColor(item.type)]"
          >
            {{ item.type }} · {{ item.count }}次
          </span>
        </div>
      </div>
      
      <div>
        <p class="text-xs text-gray-400 font-medium mb-2">常见集合区域</p>
        <div class="flex flex-wrap gap-2">
          <span 
            v-for="item in topAreas" 
            :key="item.name"
            class="px-3 py-1 bg-gray-100 text-gray-600 rounded-full text-xs font-medium"
          >
            📍 {{ item.name }}
          </span>
        </div>
      </div>
      
      <div>
        <p class="text-xs text-gray-400 font-medium mb-2">参与者评价</p>
        <div class="flex flex-wrap gap-1.5">
          <span 
            v-for="item in topReviews" 
            :key="item.tag"
            class="px-2 py-1 bg-yellow-50 text-yellow-700 rounded-md text-xs"
          >
            👍 {{ item.tag }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
