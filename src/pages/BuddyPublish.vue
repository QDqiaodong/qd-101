<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { createBuddyRequest } from '@/api/index'
import { allCities } from '@/data/cities'

const router = useRouter()

const CURRENT_USER_ID = 2

const buddyTypes = ['饭搭子', '球搭子', '探店搭子', '健身搭子', '户外运动', '其他']

const form = ref({
  title: '',
  type: '',
  city: '',
  description: '',
  targetCount: 1,
})

const isSubmitting = ref(false)

const handleSubmit = async () => {
  if (!form.value.title || !form.value.type || !form.value.city || !form.value.description) {
    alert('请填写完整信息')
    return
  }

  if (form.value.targetCount < 1) {
    alert('目标人数至少为1人')
    return
  }

  isSubmitting.value = true

  try {
    const result = await createBuddyRequest({
      title: form.value.title,
      type: form.value.type,
      city: form.value.city,
      description: form.value.description,
      targetCount: form.value.targetCount,
      creatorId: CURRENT_USER_ID,
    })

    alert('发布成功！')
    router.push(`/buddy/${result.id}`)
  } catch (error) {
    const message = error instanceof Error ? error.message : '发布失败，请稍后重试'
    alert(message)
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
        <h1 class="text-2xl font-bold text-gray-900 mb-2">发布搭子征集</h1>
        <p class="text-gray-500 mb-8">轻量发布，快速找到志同道合的小伙伴</p>
        
        <div class="space-y-6">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">征集标题 *</label>
            <input
              v-model="form.title"
              type="text"
              placeholder="例如：找个饭搭子，一起吃火锅去！"
              maxlength="50"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            />
            <p class="text-xs text-gray-400 mt-1">{{ form.title.length }}/50</p>
          </div>
          
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-2">搭子类型 *</label>
              <select
                v-model="form.type"
                class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
              >
                <option value="">请选择</option>
                <option v-for="type in buddyTypes" :key="type" :value="type">{{ type }}</option>
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
            <label class="block text-sm font-medium text-gray-700 mb-2">想找几位搭子？</label>
            <div class="flex items-center gap-4">
              <button
                type="button"
                @click="form.targetCount = Math.max(1, form.targetCount - 1)"
                class="w-10 h-10 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-lg font-medium text-gray-600 transition-colors"
              >
                -
              </button>
              <div class="text-2xl font-bold text-gray-900 w-12 text-center">{{ form.targetCount }}</div>
              <button
                type="button"
                @click="form.targetCount = Math.min(10, form.targetCount + 1)"
                class="w-10 h-10 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center text-lg font-medium text-gray-600 transition-colors"
              >
                +
              </button>
              <span class="text-sm text-gray-500">人</span>
            </div>
          </div>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">详细描述 *</label>
            <textarea
              v-model="form.description"
              placeholder="介绍一下你自己，说说想找什么样的搭子..."
              rows="5"
              maxlength="500"
              class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary resize-none"
            ></textarea>
            <p class="text-xs text-gray-400 mt-1 text-right">{{ form.description.length }}/500</p>
          </div>
          
          <div class="bg-blue-50 rounded-xl p-4">
            <div class="flex gap-3">
              <div class="text-2xl">💡</div>
              <div>
                <h4 class="font-medium text-gray-900 mb-1">温馨提示</h4>
                <ul class="text-sm text-gray-600 space-y-1">
                  <li>• 发布后其他用户可以申请加入，你可以选择接受或拒绝</li>
                  <li>• 达到目标人数后，状态变为"已配对"</li>
                  <li>• 配对成功后，可以一键转化为正式活动</li>
                  <li>• 请文明交友，注意人身安全</li>
                </ul>
              </div>
            </div>
          </div>
          
          <div class="flex gap-4 pt-4">
            <button
              @click="router.back()"
              class="flex-1 px-6 py-3 border border-gray-200 text-gray-600 font-medium rounded-xl hover:bg-gray-50 transition-colors"
            >
              取消
            </button>
            <button
              @click="handleSubmit"
              :disabled="isSubmitting"
              class="flex-1 px-6 py-3 bg-gradient-to-r from-primary to-orange-400 text-white font-medium rounded-xl hover:shadow-lg transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ isSubmitting ? '发布中...' : '立即发布' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
