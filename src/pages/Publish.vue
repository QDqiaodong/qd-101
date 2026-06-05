<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { createActivity } from '@/api/index'
import { allCities } from '@/data/cities'

const router = useRouter()

const CURRENT_USER_ID = 2

const activityTypes = ['聚餐', '徒步', '打球', '探店', '桌游', '其他']

const form = ref({
  title: '',
  type: '',
  city: '',
  location: '',
  time: '',
  maxParticipants: 10,
  description: '',
  requirements: '',
  image: 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=300&fit=crop',
})

const isSubmitting = ref(false)

const handleSubmit = async () => {
  if (!form.value.title || !form.value.type || !form.value.city || !form.value.location || !form.value.time || !form.value.description) {
    alert('请填写完整信息')
    return
  }

  isSubmitting.value = true

  try {
    await createActivity({
      title: form.value.title,
      type: form.value.type,
      city: form.value.city,
      location: form.value.location,
      time: new Date(form.value.time).toISOString(),
      maxParticipants: form.value.maxParticipants,
      description: form.value.description,
      requirements: form.value.requirements,
      image: form.value.image,
      creatorId: CURRENT_USER_ID,
    })
    
    alert('发布成功！')
    router.push('/')
  } catch (error) {
    alert('发布失败，请稍后重试')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-xl shadow-sm p-8">
        <h1 class="text-2xl font-bold text-gray-900 mb-8">发布新活动</h1>
        
        <div class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">活动标题 *</label>
            <input
              v-model="form.title"
              type="text"
              placeholder="给你的活动起个吸引人的名字"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            />
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">活动类型 *</label>
              <select
                v-model="form.type"
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
              >
                <option value="">请选择</option>
                <option v-for="type in activityTypes" :key="type" :value="type">{{ type }}</option>
              </select>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">城市 *</label>
              <select
                v-model="form.city"
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
              >
                <option value="">请选择</option>
                <option v-for="city in allCities" :key="city" :value="city">{{ city }}</option>
              </select>
            </div>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">具体地点 *</label>
            <input
              v-model="form.location"
              type="text"
              placeholder="例如：朝阳区某某咖啡厅"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            />
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">活动时间 *</label>
              <input
                v-model="form.time"
                type="datetime-local"
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
              />
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">人数上限</label>
              <input
                v-model.number="form.maxParticipants"
                type="number"
                min="2"
                max="100"
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
              />
            </div>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">活动描述 *</label>
            <textarea
              v-model="form.description"
              placeholder="详细介绍一下你的活动吧..."
              rows="4"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary resize-none"
            ></textarea>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">报名要求</label>
            <textarea
              v-model="form.requirements"
              placeholder="对参与者有什么要求吗？"
              rows="3"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary resize-none"
            ></textarea>
          </div>
          
          <div class="flex gap-4 pt-4">
            <button
              @click="router.back()"
              class="flex-1 px-6 py-3 border border-gray-200 text-gray-600 rounded-xl hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button
              @click="handleSubmit"
              :disabled="isSubmitting"
              class="flex-1 px-6 py-3 bg-gradient-to-r from-primary to-orange-400 text-white rounded-xl hover:from-primary/90 hover:to-orange-500/90 transition-all disabled:opacity-50 disabled:cursor-not-allowed font-medium"
            >
              {{ isSubmitting ? '发布中...' : '发布活动' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
