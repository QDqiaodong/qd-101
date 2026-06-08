<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Navbar from '@/components/Navbar.vue'
import { 
  getActivityById, 
  registerActivity, 
  cancelRegistration, 
  checkRegistration, 
  getRegisteredActivities, 
  getRegistrationStatus,
  getWaitlistPosition,
  getWaitlist,
  getComments,
  getCommentCategoryStats,
  createComment,
  likeComment,
  confirmAttendance,
  getConfirmedRegistrations,
  COMMENT_CATEGORIES,
  type Activity,
  type RegistrationStatus as RegStatus,
  type WaitlistUser,
  type Comment,
  type CommentCategoryStats,
  type AttendanceStatus,
  type RegistrationUser,
} from '@/api/index'
import { matchDistrictByLocation } from '@/data/locationData'

const route = useRoute()
const router = useRouter()

const CURRENT_USER_ID = 2

const activityId = Number(route.params.id)
const activity = ref<Activity | null>(null)
const isRegistered = ref(false)
const regStatus = ref<RegStatus>('NOT_REGISTERED')
const waitlistPosition = ref<number | null>(null)
const waitlist = ref<WaitlistUser[]>([])
const isFull = ref(false)
const isCreator = ref(false)
const loading = ref(true)
const registeredActivities = ref<Activity[]>([])
const showConflictWarning = ref(false)
const showCloseTimeWarning = ref(false)

const confirmedUsers = ref<RegistrationUser[]>([])
const myAttendanceStatus = ref<AttendanceStatus>('PENDING')

const activeCommentTab = ref<'all' | 'qa'>('all')
const selectedCategory = ref<string>('')
const comments = ref<Comment[]>([])
const commentStats = ref<CommentCategoryStats[]>([])
const commentContent = ref('')
const replyToCommentId = ref<number | null>(null)
const replyToUserId = ref<number | null>(null)
const replyToUserName = ref<string>('')
const replyContent = ref('')
const commentLoading = ref(false)
const showCommentInput = ref(false)

const qaCategories = COMMENT_CATEGORIES

interface TimeConflict {
  type: 'overlap' | 'close_time'
  activity: Activity
  message: string
  minutesDiff: number
}

const timeConflicts = computed<TimeConflict[]>(() => {
  if (!activity.value || registeredActivities.value.length === 0) return []
  
  const conflicts: TimeConflict[] = []
  const currentActivityTime = new Date(activity.value.time)
  const currentActivityEnd = new Date(currentActivityTime.getTime() + 2 * 60 * 60 * 1000)
  
  registeredActivities.value.forEach(registered => {
    if (registered.id === activity.value!.id) return
    
    const registeredTime = new Date(registered.time)
    const registeredEnd = new Date(registeredTime.getTime() + 2 * 60 * 60 * 1000)
    
    if (currentActivityTime < registeredEnd && currentActivityEnd > registeredTime) {
      const overlapStart = new Date(Math.max(currentActivityTime.getTime(), registeredTime.getTime()))
      const overlapEnd = new Date(Math.min(currentActivityEnd.getTime(), registeredEnd.getTime()))
      const overlapMinutes = Math.round((overlapEnd.getTime() - overlapStart.getTime()) / (1000 * 60))
      
      conflicts.push({
        type: 'overlap',
        activity: registered,
        message: `与「${registered.title}」时间重叠约 ${overlapMinutes} 分钟`,
        minutesDiff: overlapMinutes
      })
    } else {
      const diffMinutes = Math.abs(currentActivityTime.getTime() - registeredEnd.getTime()) / (1000 * 60)
      if (diffMinutes < 60 && diffMinutes > 0) {
        const isAfter = currentActivityTime > registeredEnd
        conflicts.push({
          type: 'close_time',
          activity: registered,
          message: isAfter 
            ? `「${registered.title}」结束后仅 ${Math.round(diffMinutes)} 分钟开始，返程时间紧张`
            : `开始时间比「${registered.title}」仅早 ${Math.round(diffMinutes)} 分钟`,
          minutesDiff: Math.round(diffMinutes)
        })
      }
    }
  })
  
  return conflicts
})

const hasConflict = computed(() => timeConflicts.value.length > 0)

const totalComments = computed(() => {
  let count = comments.value.length
  comments.value.forEach(c => {
    if (c.replies) count += c.replies.length
  })
  return count
})

const getCategoryInfo = (categoryKey: string) => {
  return COMMENT_CATEGORIES.find(c => c.key === categoryKey)
}

const getCategoryCount = (categoryKey: string) => {
  const stat = commentStats.value.find(s => s.category === categoryKey)
  return stat?.count || 0
}

const getCategoryTagClass = (categoryKey: string) => {
  const classes: Record<string, string> = {
    'MEETING_POINT': 'bg-blue-50 text-blue-600',
    'FEE': 'bg-green-50 text-green-600',
    'EQUIPMENT': 'bg-purple-50 text-purple-600',
    'BEGINNER_FRIENDLY': 'bg-yellow-50 text-yellow-600',
    'OTHER': 'bg-gray-50 text-gray-600',
  }
  return classes[categoryKey] || classes['OTHER']
}

const district = computed(() => {
  if (!activity.value) return null
  return matchDistrictByLocation(activity.value.location, activity.value.city)
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

async function loadActivity() {
  loading.value = true
  try {
    activity.value = await getActivityById(activityId)
    isRegistered.value = await checkRegistration(activityId, CURRENT_USER_ID)
    regStatus.value = await getRegistrationStatus(activityId, CURRENT_USER_ID)
    waitlistPosition.value = await getWaitlistPosition(activityId, CURRENT_USER_ID)
    waitlist.value = await getWaitlist(activityId)
    isFull.value = activity.value.currentParticipants >= activity.value.maxParticipants
    isCreator.value = activity.value.creatorId === CURRENT_USER_ID
    registeredActivities.value = await getRegisteredActivities(CURRENT_USER_ID)
    confirmedUsers.value = await getConfirmedRegistrations(activityId)
    
    const myReg = confirmedUsers.value.find(u => u.userId === CURRENT_USER_ID)
    if (myReg) {
      myAttendanceStatus.value = myReg.attendanceStatus
    }
    
    await loadComments()
    await loadCommentStats()
  } catch (error) {
    console.error('Failed to load activity:', error)
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const category = activeCommentTab.value === 'qa' && selectedCategory.value ? selectedCategory.value : undefined
    comments.value = await getComments(activityId, category)
  } catch (error) {
    console.error('Failed to load comments:', error)
  }
}

async function loadCommentStats() {
  try {
    commentStats.value = await getCommentCategoryStats(activityId)
  } catch (error) {
    console.error('Failed to load comment stats:', error)
  }
}

function switchCommentTab(tab: 'all' | 'qa') {
  activeCommentTab.value = tab
  selectedCategory.value = ''
  loadComments()
}

function selectCategory(categoryKey: string) {
  if (selectedCategory.value === categoryKey) {
    selectedCategory.value = ''
  } else {
    selectedCategory.value = categoryKey
  }
  loadComments()
}

async function handlePostComment() {
  if (!commentContent.value.trim()) return
  
  commentLoading.value = true
  try {
    const category = activeCommentTab.value === 'qa' && selectedCategory.value ? selectedCategory.value : undefined
    await createComment({
      activityId,
      userId: CURRENT_USER_ID,
      content: commentContent.value.trim(),
      category,
    })
    commentContent.value = ''
    showCommentInput.value = false
    await loadComments()
    if (activeCommentTab.value === 'qa') {
      await loadCommentStats()
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '发布失败，请稍后重试'
    alert(message)
  } finally {
    commentLoading.value = false
  }
}

function startReply(comment: Comment) {
  replyToCommentId.value = comment.id
  replyToUserId.value = comment.userId
  replyToUserName.value = comment.userName
  replyContent.value = ''
}

function cancelReply() {
  replyToCommentId.value = null
  replyToUserId.value = null
  replyToUserName.value = ''
  replyContent.value = ''
}

async function handleReply(parentComment: Comment) {
  if (!replyContent.value.trim()) return
  
  commentLoading.value = true
  try {
    await createComment({
      activityId,
      userId: CURRENT_USER_ID,
      content: replyContent.value.trim(),
      parentId: parentComment.id,
      replyToUserId: replyToUserId.value || undefined,
    })
    cancelReply()
    await loadComments()
  } catch (error) {
    const message = error instanceof Error ? error.message : '回复失败，请稍后重试'
    alert(message)
  } finally {
    commentLoading.value = false
  }
}

async function handleLikeComment(commentId: number) {
  try {
    await likeComment(commentId, CURRENT_USER_ID)
    await loadComments()
  } catch (error) {
    console.error('Failed to like comment:', error)
  }
}

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString('zh-CN')
}

const handleRegister = async () => {
  try {
    await registerActivity(activityId, CURRENT_USER_ID)
    await loadActivity()
    if (regStatus.value === 'WAITLISTED') {
      alert(`候补报名成功！您当前是第 ${waitlistPosition.value} 位候补，有人取消时会自动补上。`)
    } else {
      alert('报名成功！')
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '报名失败，请稍后重试'
    alert(message)
  }
}

const handleCancel = async () => {
  try {
    await cancelRegistration(activityId, CURRENT_USER_ID)
    alert('已取消报名')
    await loadActivity()
  } catch (error) {
    const message = error instanceof Error ? error.message : '取消报名失败，请稍后重试'
    alert(message)
  }
}

const handleConfirmAttendance = async () => {
  try {
    await confirmAttendance(activityId, CURRENT_USER_ID, 'CONFIRMED')
    alert('已确认出席！')
    await loadActivity()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败，请稍后重试'
    alert(message)
  }
}

const handleDeclineAttendance = async () => {
  if (!confirm('确定确认不出席吗？这将帮助组织者提前调整安排。')) {
    return
  }
  try {
    await confirmAttendance(activityId, CURRENT_USER_ID, 'DECLINED')
    alert('已提交不出席确认')
    await loadActivity()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败，请稍后重试'
    alert(message)
  }
}

const getAttendanceStatusText = (status: AttendanceStatus) => {
  const texts: Record<AttendanceStatus, string> = {
    PENDING: '待确认',
    CONFIRMED: '确认出席',
    DECLINED: '确认不出席',
  }
  return texts[status]
}

const getAttendanceStatusClass = (status: AttendanceStatus) => {
  const classes: Record<AttendanceStatus, string> = {
    PENDING: 'bg-yellow-100 text-yellow-700',
    CONFIRMED: 'bg-green-100 text-green-700',
    DECLINED: 'bg-red-100 text-red-700',
  }
  return classes[status]
}

onMounted(() => {
  loadActivity()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <Navbar />
    
    <div v-if="loading" class="flex items-center justify-center min-h-screen">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
    </div>
    
    <div v-else-if="activity" class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <button @click="router.back()" class="flex items-center gap-2 text-gray-500 hover:text-gray-700 mb-6">
        <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
        返回
      </button>
      
      <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
        <div class="relative h-64 md:h-80">
          <img :src="activity.image" :alt="activity.title" class="w-full h-full object-cover" />
          <div class="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent"></div>
          <div class="absolute bottom-6 left-6 right-6">
            <div class="flex flex-wrap gap-2">
              <span :class="['px-4 py-1.5 rounded-full text-sm font-medium', getTypeColor(activity.type)]">
                {{ activity.type }}
              </span>
              <span v-if="district" class="px-4 py-1.5 rounded-full text-sm font-medium bg-white/90 text-gray-700">
                {{ district.name }} ({{ district.type }})
              </span>
            </div>
            <h1 class="text-2xl md:text-3xl font-bold text-white mt-3">{{ activity.title }}</h1>
          </div>
        </div>
        
        <div v-if="hasConflict && !isRegistered" class="p-6 bg-gradient-to-r from-red-50 to-orange-50 border-b border-red-100">
          <div class="flex items-start gap-3">
            <div class="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center flex-shrink-0">
              <svg class="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <div class="flex-1">
              <h3 class="font-semibold text-red-800 mb-2">⚠️ 时间冲突提醒</h3>
              <p class="text-sm text-red-700 mb-3">您已报名的活动与该活动存在以下时间冲突：</p>
              <div class="space-y-2">
                <div 
                  v-for="(conflict, index) in timeConflicts" 
                  :key="index"
                  :class="[
                    'p-3 rounded-lg text-sm flex items-center justify-between',
                    conflict.type === 'overlap' ? 'bg-red-100' : 'bg-orange-100'
                  ]"
                >
                  <div class="flex-1">
                    <span :class="conflict.type === 'overlap' ? 'text-red-700' : 'text-orange-700'">
                      {{ conflict.type === 'overlap' ? '⏰ 时间重叠' : '⚡ 返程紧张' }}
                    </span>
                    <p class="text-gray-700 mt-1">{{ conflict.message }}</p>
                    <p class="text-gray-500 text-xs mt-1">
                      {{ conflict.activity.title }} · {{ new Date(conflict.activity.time).toLocaleString('zh-CN') }}
                    </p>
                  </div>
                  <button 
                    @click="$router.push(`/activity/${conflict.activity.id}`)"
                    class="ml-4 text-xs text-primary hover:underline flex-shrink-0"
                  >
                    查看详情
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="p-6 md:p-8">
          <div class="grid md:grid-cols-3 gap-6 mb-8">
            <div class="flex items-start gap-3">
              <div class="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center flex-shrink-0">
                <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">地点</p>
                <p class="font-medium text-gray-900">{{ activity.city }} · {{ activity.location }}</p>
              </div>
            </div>
            
            <div class="flex items-start gap-3">
              <div class="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center flex-shrink-0">
                <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">时间</p>
                <p class="font-medium text-gray-900">{{ new Date(activity.time).toLocaleString('zh-CN') }}</p>
              </div>
            </div>
            
            <div class="flex items-start gap-3">
              <div class="w-10 h-10 bg-primary/10 rounded-xl flex items-center justify-center flex-shrink-0">
                <svg class="w-5 h-5 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
              </div>
              <div>
                <p class="text-sm text-gray-500">报名人数</p>
                <p class="font-medium text-gray-900">{{ activity.currentParticipants }}/{{ activity.maxParticipants }}人</p>
                <p v-if="activity.waitlistCount && activity.waitlistCount > 0" class="text-xs text-orange-500 mt-1">
                  候补 {{ activity.waitlistCount }} 人
                </p>
              </div>
            </div>
          </div>
          
          <div class="space-y-6">
            <div v-if="regStatus === 'WAITLISTED'" class="p-4 bg-orange-50 border border-orange-200 rounded-xl">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0">
                  <svg class="w-5 h-5 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <p class="font-medium text-orange-800">您已加入候补队列</p>
                  <p class="text-sm text-orange-600 mt-1">
                    当前是第 <span class="font-bold">{{ waitlistPosition }}</span> 位候补，有人取消报名时会自动按顺序补上
                  </p>
                </div>
              </div>
            </div>
            
            <div>
              <h2 class="text-lg font-semibold text-gray-900 mb-3">活动介绍</h2>
              <p class="text-gray-600 leading-relaxed">{{ activity.description }}</p>
            </div>
            
            <div v-if="activity.requirements">
              <h2 class="text-lg font-semibold text-gray-900 mb-3">报名要求</h2>
              <div class="bg-gray-50 rounded-xl p-4">
                <p class="text-gray-600">{{ activity.requirements }}</p>
              </div>
            </div>
            
            <div v-if="waitlist.length > 0" class="mt-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-3">
                候补队列 
                <span class="text-sm font-normal text-gray-500">({{ waitlist.length }}人)</span>
              </h2>
              <div class="bg-gray-50 rounded-xl p-4">
                <div class="space-y-3">
                  <div 
                    v-for="(user, index) in waitlist" 
                    :key="user.userId"
                    class="flex items-center gap-3"
                  >
                    <div class="w-8 h-8 bg-orange-100 rounded-full flex items-center justify-center flex-shrink-0">
                      <span class="text-sm font-medium text-orange-600">{{ user.waitlistPosition }}</span>
                    </div>
                    <span class="text-gray-700">{{ user.userName }}</span>
                    <span v-if="index === 0" class="ml-auto text-xs text-green-600 bg-green-50 px-2 py-1 rounded-full">
                      下一位补位
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="regStatus === 'CONFIRMED' || isCreator" class="mt-6">
              <h2 class="text-lg font-semibold text-gray-900 mb-3">
                出席确认
                <span class="text-sm font-normal text-gray-500 ml-2">
                  活动开始前确认是否到场，提前暴露掉队风险
                </span>
              </h2>
              
              <div class="grid grid-cols-3 gap-3 mb-4">
                <div class="bg-green-50 rounded-xl p-4 text-center">
                  <p class="text-2xl font-bold text-green-600">{{ activity.attendanceStats?.attendanceConfirmed || 0 }}</p>
                  <p class="text-sm text-green-700 mt-1">确认出席</p>
                </div>
                <div class="bg-yellow-50 rounded-xl p-4 text-center">
                  <p class="text-2xl font-bold text-yellow-600">{{ activity.attendanceStats?.attendancePending || 0 }}</p>
                  <p class="text-sm text-yellow-700 mt-1">待确认</p>
                </div>
                <div class="bg-red-50 rounded-xl p-4 text-center">
                  <p class="text-2xl font-bold text-red-500">{{ activity.attendanceStats?.attendanceDeclined || 0 }}</p>
                  <p class="text-sm text-red-600 mt-1">确认不出席</p>
                </div>
              </div>

              <div v-if="regStatus === 'CONFIRMED'" class="bg-gradient-to-r from-blue-50 to-purple-50 rounded-xl p-4 mb-4">
                <div class="flex items-start gap-3">
                  <div class="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                    <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div class="flex-1">
                    <p class="font-medium text-gray-800">我的出席状态</p>
                    <p class="text-sm text-gray-600 mt-1">
                      请在活动开始前确认您的出席状态，帮助组织者更好地安排活动～
                    </p>
                  </div>
                  <span :class="['px-3 py-1 text-sm font-medium rounded-full', getAttendanceStatusClass(myAttendanceStatus)]">
                    {{ getAttendanceStatusText(myAttendanceStatus) }}
                  </span>
                </div>
              </div>

              <div v-if="regStatus === 'CONFIRMED'" class="flex gap-3 mb-6">
                <button
                  @click="handleConfirmAttendance"
                  :class="[
                    'flex-1 py-3 rounded-xl font-medium transition-all',
                    myAttendanceStatus === 'CONFIRMED'
                      ? 'bg-green-500 text-white shadow-md'
                      : 'bg-green-100 text-green-700 hover:bg-green-200'
                  ]"
                >
                  ✓ 确认出席
                </button>
                <button
                  @click="handleDeclineAttendance"
                  :class="[
                    'flex-1 py-3 rounded-xl font-medium transition-all',
                    myAttendanceStatus === 'DECLINED'
                      ? 'bg-red-500 text-white shadow-md'
                      : 'bg-red-100 text-red-700 hover:bg-red-200'
                  ]"
                >
                  ✗ 确认不出席
                </button>
              </div>

              <div v-if="confirmedUsers.length > 0" class="bg-gray-50 rounded-xl p-4">
                <h3 class="font-medium text-gray-800 mb-3">
                  报名名单 
                  <span class="text-sm font-normal text-gray-500">({{ confirmedUsers.length }}人)</span>
                </h3>
                <div class="space-y-2">
                  <div 
                    v-for="user in confirmedUsers" 
                    :key="user.userId"
                    class="flex items-center gap-3 py-2"
                  >
                    <img :src="user.userAvatar" :alt="user.userName" class="w-8 h-8 rounded-full object-cover flex-shrink-0" />
                    <div class="flex-1 min-w-0">
                      <div class="flex items-center gap-2">
                        <span class="text-gray-800 font-medium">{{ user.userName }}</span>
                        <span v-if="user.userId === activity.creatorId" class="px-1.5 py-0.5 bg-primary/10 text-primary text-xs rounded-full">
                          发起人
                        </span>
                      </div>
                      <p class="text-xs text-gray-500">
                        {{ new Date(user.registeredAt).toLocaleDateString('zh-CN') }} 报名
                      </p>
                    </div>
                    <span :class="['px-2 py-1 text-xs rounded-full', getAttendanceStatusClass(user.attendanceStatus)]">
                      {{ getAttendanceStatusText(user.attendanceStatus) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="mt-8 pt-6 border-t border-gray-100">
            <div class="flex flex-col sm:flex-row items-center justify-between gap-4">
              <div class="flex items-center gap-4">
                <div class="flex items-center gap-2 text-gray-500">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  <span>{{ activity.views }} 次浏览</span>
                </div>
              </div>
              
              <div class="flex gap-3 w-full sm:w-auto">
                <button
                  v-if="isCreator"
                  disabled
                  class="flex-1 sm:flex-none px-8 py-3 bg-gray-200 text-gray-500 rounded-xl cursor-not-allowed"
                >
                  我发布的活动
                </button>
                <button
                  v-else-if="regStatus === 'CONFIRMED'"
                  @click="handleCancel"
                  class="flex-1 sm:flex-none px-8 py-3 border-2 border-primary text-primary rounded-xl hover:bg-primary/5 transition-colors"
                >
                  取消报名
                </button>
                <button
                  v-else-if="regStatus === 'WAITLISTED'"
                  @click="handleCancel"
                  class="flex-1 sm:flex-none px-8 py-3 border-2 border-orange-400 text-orange-500 rounded-xl hover:bg-orange-50 transition-colors"
                >
                  取消候补 (第{{ waitlistPosition }}位)
                </button>
                <button
                  v-else-if="isFull"
                  @click="handleRegister"
                  class="flex-1 sm:flex-none px-8 py-3 bg-gradient-to-r from-orange-400 to-yellow-400 text-white rounded-xl hover:from-orange-500 hover:to-yellow-500 transition-all font-medium shadow-lg shadow-orange-200"
                >
                  候补报名
                </button>
                <button
                  v-else
                  @click="handleRegister"
                  class="flex-1 sm:flex-none px-8 py-3 bg-gradient-to-r from-primary to-orange-400 text-white rounded-xl hover:from-primary/90 hover:to-orange-500/90 transition-all font-medium shadow-lg shadow-orange-200"
                >
                  立即报名
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="mt-8 bg-white rounded-2xl shadow-sm overflow-hidden">
        <div class="border-b border-gray-100">
          <div class="flex">
            <button
              @click="switchCommentTab('all')"
              :class="[
                'flex-1 py-4 px-6 text-center font-medium transition-colors relative',
                activeCommentTab === 'all' ? 'text-primary' : 'text-gray-500 hover:text-gray-700'
              ]"
            >
              <span class="flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
                全部评论
                <span class="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded-full">{{ totalComments }}</span>
              </span>
              <div v-if="activeCommentTab === 'all'" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-primary rounded-full"></div>
            </button>
            <button
              @click="switchCommentTab('qa')"
              :class="[
                'flex-1 py-4 px-6 text-center font-medium transition-colors relative',
                activeCommentTab === 'qa' ? 'text-primary' : 'text-gray-500 hover:text-gray-700'
              ]"
            >
              <span class="flex items-center justify-center gap-2">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                问答区
              </span>
              <div v-if="activeCommentTab === 'qa'" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-12 h-0.5 bg-primary rounded-full"></div>
            </button>
          </div>
        </div>

        <div v-if="activeCommentTab === 'qa'" class="px-6 py-4 border-b border-gray-100 bg-gray-50">
          <div class="flex flex-wrap gap-2">
            <button
              v-for="cat in qaCategories"
              :key="cat.key"
              @click="selectCategory(cat.key)"
              :class="[
                'flex items-center gap-1.5 px-4 py-2 rounded-full text-sm font-medium transition-all',
                selectedCategory === cat.key
                  ? 'bg-primary text-white shadow-md'
                  : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'
              ]"
            >
              <span>{{ cat.icon }}</span>
              <span>{{ cat.label }}</span>
              <span
                :class="[
                  'text-xs px-1.5 py-0.5 rounded-full',
                  selectedCategory === cat.key ? 'bg-white/20 text-white' : 'bg-gray-100 text-gray-500'
                ]"
              >
                {{ getCategoryCount(cat.key) }}
              </span>
            </button>
          </div>
          <p v-if="selectedCategory" class="text-xs text-gray-500 mt-3">
            正在查看「{{ getCategoryInfo(selectedCategory)?.label }}」相关问题，点击分类可取消筛选
          </p>
        </div>

        <div class="p-6">
          <div v-if="!showCommentInput" class="mb-6">
            <button
              @click="showCommentInput = true"
              class="w-full p-4 text-left bg-gray-50 rounded-xl text-gray-400 hover:bg-gray-100 transition-colors border-2 border-dashed border-gray-200 hover:border-primary/30"
            >
              <span v-if="activeCommentTab === 'qa'">
                {{ selectedCategory ? '提问关于' + getCategoryInfo(selectedCategory)?.label + '的问题...' : '有问题？来问问大家吧...' }}
              </span>
              <span v-else>说点什么吧...</span>
            </button>
          </div>

          <div v-else class="mb-6">
            <div v-if="activeCommentTab === 'qa' && !selectedCategory" class="mb-3">
              <p class="text-sm text-gray-500 mb-2">选择问题分类（可选）：</p>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="cat in qaCategories"
                  :key="cat.key"
                  @click="selectedCategory = selectedCategory === cat.key ? '' : cat.key"
                  :class="[
                    'flex items-center gap-1 px-3 py-1.5 rounded-lg text-sm transition-all',
                    selectedCategory === cat.key
                      ? 'bg-primary/10 text-primary border border-primary/30'
                      : 'bg-gray-50 text-gray-600 border border-gray-200 hover:border-gray-300'
                  ]"
                >
                  <span>{{ cat.icon }}</span>
                  <span>{{ cat.label }}</span>
                </button>
              </div>
            </div>
            <textarea
              v-model="commentContent"
              :placeholder="activeCommentTab === 'qa' ? (selectedCategory ? '关于' + getCategoryInfo(selectedCategory)?.label + '的问题...' : '描述你的问题，让大家帮你解答...') : '分享你的想法...'"
              class="w-full p-4 bg-gray-50 rounded-xl text-gray-700 placeholder-gray-400 resize-none focus:outline-none focus:ring-2 focus:ring-primary/30 focus:bg-white transition-all"
              rows="3"
            ></textarea>
            <div class="flex items-center justify-between mt-3">
              <p class="text-xs text-gray-400">
                {{ activeCommentTab === 'qa' ? '💡 好问题会被置顶，帮助更多小伙伴' : '友善发言，理性讨论' }}
              </p>
              <div class="flex gap-2">
                <button
                  @click="showCommentInput = false; commentContent = ''"
                  class="px-4 py-2 text-sm text-gray-500 hover:text-gray-700 transition-colors"
                >
                  取消
                </button>
                <button
                  @click="handlePostComment"
                  :disabled="!commentContent.trim() || commentLoading"
                  :class="[
                    'px-6 py-2 text-sm font-medium rounded-lg transition-all',
                    commentContent.trim() && !commentLoading
                      ? 'bg-gradient-to-r from-primary to-orange-400 text-white hover:from-primary/90 hover:to-orange-500/90 shadow-md'
                      : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                  ]"
                >
                  {{ commentLoading ? '发布中...' : (activeCommentTab === 'qa' ? '发布问题' : '发布评论') }}
                </button>
              </div>
            </div>
          </div>

          <div class="space-y-6">
            <div
              v-for="comment in comments"
              :key="comment.id"
              :class="[
                'relative',
                comment.isPinned ? 'bg-gradient-to-r from-yellow-50 to-orange-50 -mx-2 px-4 py-4 rounded-xl border border-yellow-200' : ''
              ]"
            >
              <div v-if="comment.isPinned" class="absolute -top-2 left-4">
                <span class="px-2 py-0.5 bg-gradient-to-r from-yellow-400 to-orange-400 text-white text-xs font-medium rounded-full shadow-sm">
                  📌 置顶
                </span>
              </div>
              
              <div class="flex gap-3">
                <img
                  :src="comment.userAvatar"
                  :alt="comment.userName"
                  class="w-10 h-10 rounded-full object-cover flex-shrink-0"
                />
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <span class="font-medium text-gray-900">{{ comment.userName }}</span>
                    <span v-if="comment.userId === activity?.creatorId" class="px-2 py-0.5 bg-primary/10 text-primary text-xs rounded-full font-medium">
                      发起人
                    </span>
                    <span v-if="comment.category && activeCommentTab === 'all'" :class="['px-2 py-0.5 text-xs rounded-full', getCategoryTagClass(comment.category)]">
                      {{ getCategoryInfo(comment.category)?.icon }} {{ getCategoryInfo(comment.category)?.label }}
                    </span>
                    <span class="text-xs text-gray-400">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  
                  <p class="mt-2 text-gray-700 leading-relaxed whitespace-pre-wrap">{{ comment.content }}</p>
                  
                  <div class="mt-3 flex items-center gap-4">
                    <button
                      @click="handleLikeComment(comment.id)"
                      class="flex items-center gap-1 text-sm text-gray-400 hover:text-red-500 transition-colors group"
                    >
                      <svg class="w-4 h-4 group-hover:scale-110 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                      </svg>
                      <span>{{ comment.likes }}</span>
                    </button>
                    <button
                      @click="startReply(comment)"
                      class="flex items-center gap-1 text-sm text-gray-400 hover:text-primary transition-colors"
                    >
                      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
                      </svg>
                      回复
                    </button>
                  </div>

                  <div v-if="replyToCommentId === comment.id" class="mt-4 bg-gray-50 rounded-xl p-4">
                    <p class="text-sm text-gray-500 mb-2">
                      回复 <span class="text-primary font-medium">@{{ replyToUserName }}</span>
                    </p>
                    <textarea
                      v-model="replyContent"
                      placeholder="写下你的回复..."
                      class="w-full p-3 bg-white rounded-lg text-gray-700 placeholder-gray-400 resize-none focus:outline-none focus:ring-2 focus:ring-primary/30 text-sm border border-gray-200"
                      rows="2"
                    ></textarea>
                    <div class="flex justify-end gap-2 mt-2">
                      <button
                        @click="cancelReply"
                        class="px-3 py-1.5 text-sm text-gray-500 hover:text-gray-700 transition-colors"
                      >
                        取消
                      </button>
                      <button
                        @click="handleReply(comment)"
                        :disabled="!replyContent.trim() || commentLoading"
                        :class="[
                          'px-4 py-1.5 text-sm font-medium rounded-lg transition-all',
                          replyContent.trim() && !commentLoading
                            ? 'bg-primary text-white hover:bg-primary/90'
                            : 'bg-gray-200 text-gray-400 cursor-not-allowed'
                        ]"
                      >
                        发送
                      </button>
                    </div>
                  </div>

                  <div v-if="comment.replies && comment.replies.length > 0" class="mt-4 pl-4 border-l-2 border-gray-100 space-y-4">
                    <div
                      v-for="reply in comment.replies"
                      :key="reply.id"
                      class="flex gap-3"
                    >
                      <img
                        :src="reply.userAvatar"
                        :alt="reply.userName"
                        class="w-8 h-8 rounded-full object-cover flex-shrink-0"
                      />
                      <div class="flex-1 min-w-0">
                        <div class="flex items-center gap-2 flex-wrap">
                          <span class="font-medium text-gray-900 text-sm">{{ reply.userName }}</span>
                          <span v-if="reply.userId === activity?.creatorId" class="px-1.5 py-0.5 bg-primary/10 text-primary text-xs rounded-full">
                            发起人
                          </span>
                          <span v-if="reply.replyToUserName" class="text-xs text-gray-400">
                            回复
                            <span class="text-gray-600">@{{ reply.replyToUserName }}</span>
                          </span>
                          <span class="text-xs text-gray-400">{{ formatTime(reply.createdAt) }}</span>
                        </div>
                        <p class="mt-1 text-gray-700 text-sm leading-relaxed">{{ reply.content }}</p>
                        <div class="mt-2 flex items-center gap-4">
                          <button
                            @click="handleLikeComment(reply.id)"
                            class="flex items-center gap-1 text-xs text-gray-400 hover:text-red-500 transition-colors"
                          >
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                            </svg>
                            <span>{{ reply.likes }}</span>
                          </button>
                          <button
                            @click="() => { replyToCommentId = comment.id; replyToUserId = reply.userId; replyToUserName = reply.userName; replyContent = ''; }"
                            class="text-xs text-gray-400 hover:text-primary transition-colors"
                          >
                            回复
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="comments.length === 0" class="text-center py-12">
              <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg class="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path v-if="activeCommentTab === 'qa'" stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
              <p class="text-gray-500 font-medium">
                {{ activeCommentTab === 'qa' ? '还没有问题' : '还没有评论' }}
              </p>
              <p class="text-gray-400 text-sm mt-1">
                {{ activeCommentTab === 'qa' ? '来提第一个问题，让大家帮你解答吧～' : '快来发表第一条评论吧～' }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
