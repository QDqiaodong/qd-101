<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
</script>

<template>
  <nav class="bg-white shadow-sm sticky top-0 z-50">
    <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between items-center h-16">
        <div class="flex items-center gap-8">
          <div class="flex items-center cursor-pointer" @click="router.push('/')">
            <div class="w-8 h-8 bg-gradient-to-br from-primary to-orange-400 rounded-lg flex items-center justify-center">
              <span class="text-white text-lg font-bold">聚</span>
            </div>
            <span class="ml-2 text-xl font-bold text-gray-900">同城活动</span>
          </div>
        </div>
        
        <div class="flex items-center gap-4">
          <button
            @click="router.push('/')"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              route.name === 'home' 
                ? 'bg-primary text-white' 
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            首页
          </button>
          <button
            @click="router.push('/nightlife')"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              route.name === 'nightlife' 
                ? 'bg-primary text-white' 
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            🌙 夜生活
          </button>
          <button
            @click="router.push('/match')"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              route.name === 'match' 
                ? 'bg-primary text-white' 
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            🎯 智能组队
          </button>
          <button
            @click="router.push('/creators')"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              route.name === 'creators' || route.name === 'creator'
                ? 'bg-primary text-white' 
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            🎭 名片馆
          </button>
          <button
            @click="router.push('/checklist')"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              route.name === 'checklist' 
                ? 'bg-primary text-white' 
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            📋 行前清单
          </button>
          <button
            @click="router.push('/buddies')"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              route.name === 'buddies' || route.name === 'buddy-detail' || route.name === 'buddy-publish'
                ? 'bg-primary text-white' 
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            👋 找搭子
          </button>
          <button
            @click="router.push('/publish')"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              route.name === 'publish' 
                ? 'bg-primary text-white' 
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            发布活动
          </button>
          <button
            @click="router.push('/profile')"
            :class="[
              'px-4 py-2 rounded-lg transition-colors',
              route.name === 'profile' 
                ? 'bg-primary text-white' 
                : 'text-gray-600 hover:bg-gray-100'
            ]"
          >
            我的
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>
