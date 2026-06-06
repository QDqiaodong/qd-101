<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Navbar from '@/components/Navbar.vue'
import ActivityCard from '@/components/ActivityCard.vue'
import ActivityTimeline from '@/components/ActivityTimeline.vue'
import { getActivitiesByCreator, getRegisteredActivities, getUserActivityFootprints, type Activity, type ActivityFootprint } from '@/api/index'

const CURRENT_USER_ID = 2

const activeTab = ref('timeline')
const myCreatedActivities = ref<Activity[]>([])
const myJoinedActivities = ref<Activity[]>([])
const activityFootprints = ref<ActivityFootprint[]>([])
const loading = ref(true)

async function loadActivities() {
  loading.value = true
  try {
    const [created, joined, footprints] = await Promise.all([
      getActivitiesByCreator(CURRENT_USER_ID),
      getRegisteredActivities(CURRENT_USER_ID),
      getUserActivityFootprints(CURRENT_USER_ID)
    ])
    myCreatedActivities.value = created
    myJoinedActivities.value = joined
    activityFootprints.value = footprints
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
    
    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
        <div class="bg-gradient-to-r from-primary to-orange-400 p-8 text-center">
          <div class="w-24 h-24 bg-white/20 rounded-full mx-auto flex items-center justify-center">
            <span class="text-4xl">👤</span>
          </div>
          <h1 class="text-2xl font-bold text-white mt-4">城市探索者</h1>
          <p class="text-white/80 mt-1">热爱生活，探索城市</p>
        </div>
        
        <div class="grid grid-cols-3 divide-x divide-gray-100">
          <div class="p-6 text-center">
            <p class="text-3xl font-bold text-primary">{{ myCreatedActivities.length }}</p>
            <p class="text-sm text-gray-500 mt-1">发布活动</p>
          </div>
          <div class="p-6 text-center">
            <p class="text-3xl font-bold text-primary">{{ myJoinedActivities.length }}</p>
            <p class="text-sm text-gray-500 mt-1">参与活动</p>
          </div>
          <div class="p-6 text-center">
            <p class="text-3xl font-bold text-primary">
              {{ myCreatedActivities.reduce((sum, a) => sum + a.currentParticipants, 0) }}
            </p>
            <p class="text-sm text-gray-500 mt-1">认识伙伴</p>
          </div>
        </div>
      </div>
      
      <div class="mt-6 bg-white rounded-xl shadow-sm overflow-hidden">
        <div class="flex border-b border-gray-100">
          <button
            @click="activeTab = 'timeline'"
            :class="[
              'flex-1 py-4 text-center font-medium transition-colors',
              activeTab === 'timeline'
                ? 'text-primary border-b-2 border-primary'
                : 'text-gray-500 hover:text-gray-700'
            ]"
          >
            活动足迹
          </button>
          <button
            @click="activeTab = 'joined'"
            :class="[
              'flex-1 py-4 text-center font-medium transition-colors',
              activeTab === 'joined'
                ? 'text-primary border-b-2 border-primary'
                : 'text-gray-500 hover:text-gray-700'
            ]"
          >
            我报名的活动
          </button>
          <button
            @click="activeTab = 'created'"
            :class="[
              'flex-1 py-4 text-center font-medium transition-colors',
              activeTab === 'created'
                ? 'text-primary border-b-2 border-primary'
                : 'text-gray-500 hover:text-gray-700'
            ]"
          >
            我发布的活动
          </button>
        </div>
        
        <div class="p-6">
          <div v-if="loading" class="text-center py-16">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto"></div>
            <p class="mt-4 text-gray-500">加载中...</p>
          </div>
          
          <div v-else-if="activeTab === 'timeline'">
            <ActivityTimeline :footprints="activityFootprints" />
          </div>
          
          <div v-else-if="activeTab === 'joined'">
            <div v-if="myJoinedActivities.length > 0" class="grid md:grid-cols-2 gap-6">
              <ActivityCard 
                v-for="activity in myJoinedActivities" 
                :key="activity.id"
                :activity="activity"
              />
            </div>
            <div v-else class="text-center py-12">
              <div class="text-gray-300 text-6xl mb-4">🎯</div>
              <p class="text-gray-500">还没有报名任何活动</p>
              <p class="text-gray-400 text-sm mt-2">去首页发现有趣的活动吧</p>
            </div>
          </div>
          
          <div v-else-if="activeTab === 'created'">
            <div v-if="myCreatedActivities.length > 0" class="grid md:grid-cols-2 gap-6">
              <ActivityCard 
                v-for="activity in myCreatedActivities" 
                :key="activity.id"
                :activity="activity"
              />
            </div>
            <div v-else class="text-center py-12">
              <div class="text-gray-300 text-6xl mb-4">✨</div>
              <p class="text-gray-500">还没有发布任何活动</p>
              <p class="text-gray-400 text-sm mt-2">发起一个活动，认识新朋友</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
