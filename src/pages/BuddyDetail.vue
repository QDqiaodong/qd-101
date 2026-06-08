<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import {
  getBuddyRequestById,
  getBuddyApplicationsByRequest,
  applyForBuddy,
  acceptBuddyApplication,
  rejectBuddyApplication,
  closeBuddyRequest,
  convertBuddyToActivity,
  type BuddyRequest,
  type BuddyApplication,
} from '@/api/index'

const route = useRoute()
const router = useRouter()

const CURRENT_USER_ID = 2

const buddyRequest = ref<BuddyRequest | null>(null)
const applications = ref<BuddyApplication[]>([])
const loading = ref(true)
const applyMessage = ref('')
const showApplyModal = ref(false)
const showConvertModal = ref(false)
const isSubmitting = ref(false)
const isConverting = ref(false)

const convertForm = ref({
  location: '',
  time: '',
  requirements: '',
  image: 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=300&fit=crop',
})

const isCreator = computed(() => {
  return buddyRequest.value?.creatorId === CURRENT_USER_ID
})

const canConvert = computed(() => {
  if (!buddyRequest.value) return false
  if (buddyRequest.value.status === 'CONVERTED' || buddyRequest.value.status === 'CLOSED') return false
  return buddyRequest.value.status === 'MATCHED' ||
    buddyRequest.value.currentCount >= buddyRequest.value.targetCount
})

const hasApplied = computed(() => {
  return applications.value.some(
    a => a.applicantId === CURRENT_USER_ID &&
      (a.status === 'PENDING' || a.status === 'ACCEPTED')
  )
})

const myApplication = computed(() => {
  return applications.value.find(a => a.applicantId === CURRENT_USER_ID)
})

async function loadData() {
  const id = parseInt(route.params.id as string)
  loading.value = true
  try {
    buddyRequest.value = await getBuddyRequestById(id)
    applications.value = await getBuddyApplicationsByRequest(id)
  } catch (error) {
    console.error('Failed to load data:', error)
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
  const map: Record<string, { label: string; color: string; bgColor: string }> = {
    'OPEN': { label: '征集中', color: 'text-green-600', bgColor: 'bg-green-100' },
    'MATCHING': { label: '配对中', color: 'text-yellow-600', bgColor: 'bg-yellow-100' },
    'MATCHED': { label: '已配对', color: 'text-blue-600', bgColor: 'bg-blue-100' },
    'CONVERTED': { label: '已转活动', color: 'text-purple-600', bgColor: 'bg-purple-100' },
    'CLOSED': { label: '已关闭', color: 'text-gray-600', bgColor: 'bg-gray-100' },
  }
  return map[status] || map['OPEN']
}

function getAppStatusInfo(status: string) {
  const map: Record<string, { label: string; color: string }> = {
    'PENDING': { label: '待审核', color: 'text-yellow-600 bg-yellow-50' },
    'ACCEPTED': { label: '已通过', color: 'text-green-600 bg-green-50' },
    'REJECTED': { label: '已拒绝', color: 'text-red-600 bg-red-50' },
    'CANCELLED': { label: '已取消', color: 'text-gray-600 bg-gray-50' },
  }
  return map[status] || map['PENDING']
}

function formatTime(dateStr: string) {
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const progressPercent = computed(() => {
  if (!buddyRequest.value) return 0
  return Math.min((buddyRequest.value.currentCount / buddyRequest.value.targetCount) * 100, 100)
})

async function handleApply() {
  if (!applyMessage.value.trim()) {
    alert('请写点什么吧，让发起人更了解你~')
    return
  }

  isSubmitting.value = true
  try {
    await applyForBuddy({
      requestId: buddyRequest.value!.id,
      applicantId: CURRENT_USER_ID,
      message: applyMessage.value,
    })
    alert('申请成功！等待发起人审核~')
    showApplyModal.value = false
    applyMessage.value = ''
    await loadData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '申请失败，请稍后重试'
    alert(message)
  } finally {
    isSubmitting.value = false
  }
}

async function handleAccept(appId: number) {
  if (!confirm('确定接受这位搭子的申请吗？')) return

  try {
    await acceptBuddyApplication(appId, CURRENT_USER_ID)
    alert('已接受申请')
    await loadData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    alert(message)
  }
}

async function handleReject(appId: number) {
  if (!confirm('确定拒绝这位搭子的申请吗？')) return

  try {
    await rejectBuddyApplication(appId, CURRENT_USER_ID, '抱歉，不太合适')
    alert('已拒绝申请')
    await loadData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    alert(message)
  }
}

async function handleClose() {
  if (!confirm('确定关闭这个搭子征集吗？')) return

  try {
    await closeBuddyRequest(buddyRequest.value!.id, CURRENT_USER_ID)
    alert('已关闭征集')
    await loadData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    alert(message)
  }
}

async function handleConvert() {
  if (!convertForm.value.location || !convertForm.value.time) {
    alert('请填写地点和时间')
    return
  }

  isConverting.value = true
  try {
    const activity = await convertBuddyToActivity({
      requestId: buddyRequest.value!.id,
      creatorId: CURRENT_USER_ID,
      location: convertForm.value.location,
      time: new Date(convertForm.value.time).toISOString(),
      requirements: convertForm.value.requirements,
      image: convertForm.value.image,
    })
    alert('成功转化为正式活动！')
    showConvertModal.value = false
    router.push(`/activity/${activity.id}`)
  } catch (error) {
    const message = error instanceof Error ? error.message : '转换失败'
    alert(message)
  } finally {
    isConverting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />

    <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div v-if="loading" class="text-center py-20">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-4 border-primary border-t-transparent"></div>
        <p class="text-gray-500 mt-4">加载中...</p>
      </div>

      <template v-else-if="buddyRequest">
        <button
          @click="router.back()"
          class="flex items-center gap-2 text-gray-600 hover:text-primary mb-6 transition-colors"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          返回列表
        </button>

        <div class="bg-white rounded-xl shadow-sm overflow-hidden">
          <div class="p-6 border-b border-gray-100">
            <div class="flex items-start justify-between mb-4">
              <div class="flex items-center gap-3">
                <span :class="['px-3 py-1.5 rounded-full text-sm font-medium', getTypeColor(buddyRequest.type)]">
                  {{ buddyRequest.type }}
                </span>
                <span :class="['px-3 py-1.5 rounded-full text-sm font-medium', getStatusInfo(buddyRequest.status).bgColor, getStatusInfo(buddyRequest.status).color]">
                  {{ getStatusInfo(buddyRequest.status).label }}
                </span>
              </div>
              <span class="text-sm text-gray-400">
                发布于 {{ formatTime(buddyRequest.createdAt) }}
              </span>
            </div>

            <h1 class="text-2xl font-bold text-gray-900 mb-4">{{ buddyRequest.title }}</h1>

            <div class="flex items-center gap-4 mb-6">
              <img
                :src="buddyRequest.creatorAvatar"
                :alt="buddyRequest.creatorName"
                class="w-12 h-12 rounded-full object-cover"
              />
              <div>
                <p class="font-medium text-gray-900">{{ buddyRequest.creatorName }}</p>
                <p class="text-sm text-gray-500">发起人</p>
              </div>
            </div>

            <div class="bg-gray-50 rounded-xl p-5 mb-6">
              <div class="flex items-center justify-between mb-3">
                <span class="text-gray-700 font-medium">配对进度</span>
                <span class="text-primary font-bold">{{ buddyRequest.currentCount }}/{{ buddyRequest.targetCount }}人</span>
              </div>
              <div class="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                <div
                  class="h-full bg-gradient-to-r from-primary to-orange-400 rounded-full transition-all duration-500"
                  :style="{ width: `${progressPercent}%` }"
                ></div>
              </div>
              <p class="text-sm text-gray-500 mt-2">
                {{ buddyRequest.city }} · 还需要 {{ buddyRequest.targetCount - buddyRequest.currentCount }} 位搭子
              </p>
            </div>

            <div class="prose max-w-none">
              <h3 class="text-lg font-semibold text-gray-900 mb-3">征集说明</h3>
              <p class="text-gray-600 whitespace-pre-line">{{ buddyRequest.description }}</p>
            </div>
          </div>

          <div v-if="isCreator" class="p-6 border-b border-gray-100 bg-gray-50">
            <div class="flex items-center justify-between mb-4">
              <h3 class="text-lg font-semibold text-gray-900">申请列表</h3>
              <span class="text-sm text-gray-500">共 {{ applications.length }} 个申请</span>
            </div>

            <div v-if="applications.length === 0" class="text-center py-8">
              <div class="text-4xl mb-2">📭</div>
              <p class="text-gray-500">暂无申请，耐心等待哦~</p>
            </div>

            <div v-else class="space-y-3">
              <div
                v-for="app in applications"
                :key="app.id"
                class="bg-white rounded-lg p-4 flex items-center gap-4"
              >
                <img
                  :src="app.applicantAvatar"
                  :alt="app.applicantName"
                  class="w-12 h-12 rounded-full object-cover"
                />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2">
                    <p class="font-medium text-gray-900">{{ app.applicantName }}</p>
                    <span :class="['px-2 py-0.5 rounded text-xs font-medium', getAppStatusInfo(app.status).color]">
                      {{ getAppStatusInfo(app.status).label }}
                    </span>
                  </div>
                  <p class="text-sm text-gray-600 mt-1 line-clamp-2">{{ app.message }}</p>
                  <p class="text-xs text-gray-400 mt-1">{{ formatTime(app.createdAt) }}</p>
                </div>
                <div v-if="app.status === 'PENDING'" class="flex gap-2">
                  <button
                    @click="handleAccept(app.id)"
                    class="px-4 py-2 bg-green-500 text-white text-sm rounded-lg hover:bg-green-600 transition-colors"
                  >
                    接受
                  </button>
                  <button
                    @click="handleReject(app.id)"
                    class="px-4 py-2 bg-gray-100 text-gray-600 text-sm rounded-lg hover:bg-gray-200 transition-colors"
                  >
                    拒绝
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="p-6">
            <div class="flex flex-col sm:flex-row gap-3">
              <template v-if="isCreator">
                <button
                  v-if="buddyRequest.status !== 'CONVERTED' && buddyRequest.status !== 'CLOSED'"
                  @click="showConvertModal = true"
                  :disabled="!canConvert"
                  :class="[
                    'flex-1 px-6 py-3 font-medium rounded-xl transition-all duration-300',
                    canConvert
                      ? 'bg-gradient-to-r from-primary to-orange-400 text-white hover:shadow-lg'
                      : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                  ]"
                >
                  {{ canConvert ? '🎉 转化为正式活动' : '需达到目标人数后可转化' }}
                </button>
                <button
                  v-if="buddyRequest.status !== 'CONVERTED' && buddyRequest.status !== 'CLOSED'"
                  @click="handleClose"
                  class="px-6 py-3 border border-gray-200 text-gray-600 font-medium rounded-xl hover:bg-gray-50 transition-colors"
                >
                  关闭征集
                </button>
                <button
                  v-if="buddyRequest.status === 'CONVERTED' && buddyRequest.convertedActivityId"
                  @click="router.push(`/activity/${buddyRequest.convertedActivityId}`)"
                  class="flex-1 px-6 py-3 bg-purple-500 text-white font-medium rounded-xl hover:bg-purple-600 transition-colors"
                >
                  查看已转化的活动 →
                </button>
              </template>

              <template v-else>
                <button
                  v-if="!hasApplied && buddyRequest.status !== 'CLOSED' && buddyRequest.status !== 'CONVERTED'"
                  @click="showApplyModal = true"
                  class="flex-1 px-6 py-3 bg-gradient-to-r from-primary to-orange-400 text-white font-medium rounded-xl hover:shadow-lg transition-all duration-300"
                >
                  🙋 申请成为搭子
                </button>
                <div
                  v-else-if="myApplication"
                  class="flex-1 px-6 py-3 text-center font-medium rounded-xl"
                  :class="getAppStatusInfo(myApplication.status).color"
                >
                  申请状态：{{ getAppStatusInfo(myApplication.status).label }}
                </div>
                <button
                  v-else
                  disabled
                  class="flex-1 px-6 py-3 bg-gray-200 text-gray-400 font-medium rounded-xl cursor-not-allowed"
                >
                  {{ buddyRequest.status === 'CLOSED' ? '征集已关闭' : '已转化为活动' }}
                </button>
              </template>
            </div>
          </div>
        </div>
      </template>
    </div>

    <div v-if="showApplyModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-md p-6">
        <h3 class="text-xl font-bold text-gray-900 mb-4">申请成为搭子</h3>
        <p class="text-gray-600 mb-4">向发起人介绍一下自己吧，让TA更快了解你~</p>

        <textarea
          v-model="applyMessage"
          placeholder="例如：我也特别喜欢吃火锅，每周都会去探店，口味偏辣..."
          rows="4"
          maxlength="200"
          class="w-full px-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary resize-none mb-2"
        ></textarea>
        <p class="text-xs text-gray-400 text-right mb-6">{{ applyMessage.length }}/200</p>

        <div class="flex gap-3">
          <button
            @click="showApplyModal = false"
            class="flex-1 px-6 py-3 border border-gray-200 text-gray-600 font-medium rounded-xl hover:bg-gray-50 transition-colors"
          >
            取消
          </button>
          <button
            @click="handleApply"
            :disabled="isSubmitting"
            class="flex-1 px-6 py-3 bg-gradient-to-r from-primary to-orange-400 text-white font-medium rounded-xl hover:shadow-lg transition-all duration-300 disabled:opacity-50"
          >
            {{ isSubmitting ? '提交中...' : '提交申请' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showConvertModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-2xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto">
        <h3 class="text-xl font-bold text-gray-900 mb-2">转化为正式活动</h3>
        <p class="text-gray-500 mb-6">补充活动信息，一键创建正式活动</p>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">活动地点 *</label>
            <input
              v-model="convertForm.location"
              type="text"
              placeholder="例如：朝阳区某某火锅店"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">活动时间 *</label>
            <input
              v-model="convertForm.time"
              type="datetime-local"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">活动要求</label>
            <textarea
              v-model="convertForm.requirements"
              placeholder="对参与者有什么要求吗？"
              rows="3"
              class="w-full px-4 py-2.5 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary resize-none"
            ></textarea>
          </div>
        </div>

        <div class="flex gap-3 mt-6">
          <button
            @click="showConvertModal = false"
            class="flex-1 px-6 py-3 border border-gray-200 text-gray-600 font-medium rounded-xl hover:bg-gray-50 transition-colors"
          >
            取消
          </button>
          <button
            @click="handleConvert"
            :disabled="isConverting"
            class="flex-1 px-6 py-3 bg-gradient-to-r from-primary to-orange-400 text-white font-medium rounded-xl hover:shadow-lg transition-all duration-300 disabled:opacity-50"
          >
            {{ isConverting ? '转化中...' : '确认转化' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
