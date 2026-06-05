<script setup lang="ts">
import { useRouter } from 'vue-router'
import type { Activity } from '@/api/index'

const props = defineProps<{
  activity: Activity
}>()

const router = useRouter()

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
</script>

<template>
  <div 
    class="bg-white rounded-xl shadow-sm hover:shadow-lg transition-all duration-300 cursor-pointer overflow-hidden group"
    @click="router.push(`/activity/${activity.id}`)"
  >
    <div class="relative h-48 overflow-hidden">
      <img 
        :src="activity.image" 
        :alt="activity.title"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      <div class="absolute top-3 left-3">
        <span :class="['px-3 py-1 rounded-full text-sm font-medium', getTypeColor(activity.type)]">
          {{ activity.type }}
        </span>
      </div>
      <div class="absolute top-3 right-3 bg-black/50 text-white px-2 py-1 rounded-lg text-sm flex items-center gap-1">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
        </svg>
        {{ activity.views }}
      </div>
    </div>
    
    <div class="p-4">
      <h3 class="font-semibold text-lg text-gray-900 mb-2 line-clamp-2">{{ activity.title }}</h3>
      
      <div class="space-y-2 text-sm text-gray-500">
        <div class="flex items-center gap-2">
          <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span>{{ activity.city }} · {{ activity.location }}</span>
        </div>
        
        <div class="flex items-center gap-2">
          <svg class="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <span>{{ new Date(activity.time).toLocaleString('zh-CN') }}</span>
        </div>
      </div>
      
      <div class="mt-4 flex items-center justify-between">
        <div class="flex items-center gap-1">
          <div class="flex -space-x-2">
            <div class="w-6 h-6 bg-primary/20 rounded-full flex items-center justify-center text-xs text-primary font-medium">
              {{ activity.currentParticipants }}
            </div>
          </div>
          <span class="text-sm text-gray-500">
            {{ activity.currentParticipants }}/{{ activity.maxParticipants }}人
          </span>
        </div>
        
        <div class="w-20 h-2 bg-gray-200 rounded-full overflow-hidden">
          <div 
            class="h-full bg-gradient-to-r from-primary to-orange-400 rounded-full transition-all duration-500"
            :style="{ width: `${(activity.currentParticipants / activity.maxParticipants) * 100}%` }"
          ></div>
        </div>
      </div>
    </div>
  </div>
</template>
