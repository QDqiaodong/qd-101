import { mockActivities, mockRegistrations, mockCreators } from '@/data/mockData'
import type { Activity as MockActivity, CreatorProfile } from '@/types'

const BASE_URL = '/api'

export interface Activity {
  id: number
  title: string
  type: string
  city: string
  location: string
  time: string
  maxParticipants: number
  currentParticipants: number
  description: string
  requirements: string
  image: string
  views: number
  createdAt: string
  creatorId: number
  creatorName: string
  waitlistCount?: number
}

export type RegistrationStatus = 'CONFIRMED' | 'WAITLISTED' | 'CANCELLED' | 'NOT_REGISTERED'

export interface WaitlistUser {
  userId: number
  userName: string
  waitlistPosition: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

function convertMockActivity(mock: MockActivity): Activity {
  return {
    ...mock,
    id: parseInt(mock.id.replace('act-', '')),
    creatorId: parseInt(mock.creatorId.replace('user-', '')),
    creatorName: '活动发起者',
  }
}

function getMockHotActivities(timeRange: string): Activity[] {
  const allActivities = [...mockActivities].map(convertMockActivity)
  
  const now = new Date()
  let filtered = allActivities
  
  if (timeRange === 'realtime') {
    const oneDayAgo = new Date(now.getTime() - 24 * 60 * 60 * 1000)
    filtered = allActivities.filter(a => new Date(a.createdAt) >= oneDayAgo)
  } else if (timeRange === '3days') {
    const threeDaysAgo = new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000)
    filtered = allActivities.filter(a => new Date(a.createdAt) >= threeDaysAgo)
  }
  
  filtered.sort((a, b) => b.currentParticipants - a.currentParticipants)
  
  return filtered.slice(0, 5)
}

export async function getActivities(
  city?: string,
  type?: string,
  sortBy: string = 'newest'
): Promise<Activity[]> {
  try {
    const params = new URLSearchParams()
    if (city) params.set('city', city)
    if (type) params.set('type', type)
    params.set('sortBy', sortBy)
    
    const response = await fetch(`${BASE_URL}/activities?${params}`)
    const result: ApiResponse<Activity[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for activities')
    return mockActivities.map(convertMockActivity)
  }
}

export async function getActivityById(id: number): Promise<Activity> {
  try {
    const response = await fetch(`${BASE_URL}/activities/${id}`)
    const result: ApiResponse<Activity> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for activity detail')
    const mock = mockActivities.find(a => parseInt(a.id.replace('act-', '')) === id) || mockActivities[0]
    return convertMockActivity(mock)
  }
}

export async function getHotActivities(timeRange: string = '7days'): Promise<Activity[]> {
  try {
    const response = await fetch(`${BASE_URL}/activities/hot?timeRange=${timeRange}`)
    const result: ApiResponse<Activity[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for hot activities')
    return getMockHotActivities(timeRange)
  }
}

export async function getActivitiesByCreator(creatorId: number): Promise<Activity[]> {
  try {
    const response = await fetch(`${BASE_URL}/activities/creator/${creatorId}`)
    const result: ApiResponse<Activity[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for creator activities')
    return mockActivities
      .filter(a => parseInt(a.creatorId.replace('user-', '')) === creatorId)
      .map(convertMockActivity)
  }
}

export async function createActivity(data: {
  title: string
  type: string
  city: string
  location: string
  time: string
  maxParticipants: number
  description: string
  requirements?: string
  image?: string
  creatorId: number
}): Promise<Activity> {
  const response = await fetch(`${BASE_URL}/activities`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  })
  
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: '创建活动失败' }))
    throw new Error(errorData.message || `HTTP ${response.status}`)
  }
  
  const result: ApiResponse<Activity> = await response.json()
  if (result.code !== 200) {
    throw new Error(result.message || '创建活动失败')
  }
  
  return result.data
}

export async function registerActivity(activityId: number, userId: number): Promise<void> {
  try {
    const response = await fetch(`${BASE_URL}/registrations`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ activityId, userId }),
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '报名失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }
    
    const result: ApiResponse<void> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '报名失败')
    }
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    console.log('Mock registration successful')
  }
}

export async function cancelRegistration(activityId: number, userId: number): Promise<void> {
  try {
    const response = await fetch(`${BASE_URL}/registrations?activityId=${activityId}&userId=${userId}`, {
      method: 'DELETE',
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '取消报名失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }
    
    const result: ApiResponse<void> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '取消报名失败')
    }
  } catch (error) {
    if (error instanceof Error) {
      throw error
    }
    console.log('Mock cancellation successful')
  }
}

export async function checkRegistration(activityId: number, userId: number): Promise<boolean> {
  try {
    const response = await fetch(`${BASE_URL}/registrations/check?activityId=${activityId}&userId=${userId}`)
    const result: ApiResponse<boolean> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for registration check')
    return mockRegistrations.some(
      r => parseInt(r.activityId.replace('act-', '')) === activityId && 
           parseInt(r.userId.replace('user-', '')) === userId
    )
  }
}

export async function getRegistrationStatus(activityId: number, userId: number): Promise<RegistrationStatus> {
  try {
    const response = await fetch(`${BASE_URL}/registrations/status?activityId=${activityId}&userId=${userId}`)
    const result: ApiResponse<RegistrationStatus> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for registration status')
    const isReg = mockRegistrations.some(
      r => parseInt(r.activityId.replace('act-', '')) === activityId && 
           parseInt(r.userId.replace('user-', '')) === userId
    )
    return isReg ? 'CONFIRMED' : 'NOT_REGISTERED'
  }
}

export async function getWaitlistPosition(activityId: number, userId: number): Promise<number | null> {
  try {
    const response = await fetch(`${BASE_URL}/registrations/waitlist-position?activityId=${activityId}&userId=${userId}`)
    const result: ApiResponse<number | null> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for waitlist position')
    return null
  }
}

export async function getWaitlist(activityId: number): Promise<WaitlistUser[]> {
  try {
    const response = await fetch(`${BASE_URL}/registrations/waitlist/${activityId}`)
    const result: ApiResponse<WaitlistUser[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for waitlist')
    return []
  }
}

export async function getRegisteredActivities(userId: number): Promise<Activity[]> {
  try {
    const response = await fetch(`${BASE_URL}/registrations/user/${userId}`)
    const result: ApiResponse<Activity[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for registered activities')
    const registeredIds = mockRegistrations
      .filter(r => parseInt(r.userId.replace('user-', '')) === userId)
      .map(r => r.activityId)
    return mockActivities
      .filter(a => registeredIds.includes(a.id))
      .map(convertMockActivity)
  }
}

export interface Creator {
  id: number
  name: string
  avatar: string
  bio: string
  totalActivities: number
  successRate: number
  avgFillSpeedHours: number
  commonTypes: { type: string; count: number }[]
  commonAreas: { name: string; count: number }[]
  reviewTags: { tag: string; count: number }[]
  styleTags: string[]
}

function convertMockCreator(mock: CreatorProfile): Creator {
  return {
    ...mock,
    id: parseInt(mock.id.replace('user-', '')),
  }
}

export async function getCreators(
  type?: string,
  sortBy: string = 'popular'
): Promise<Creator[]> {
  try {
    const params = new URLSearchParams()
    if (type) params.set('type', type)
    params.set('sortBy', sortBy)
    
    const response = await fetch(`${BASE_URL}/creators?${params}`)
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    
    const result: ApiResponse<Creator[]> = await response.json()
    
    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || 'Invalid response')
    }
    
    return result.data
  } catch (error) {
    console.log('Using mock data for creators:', error)
    let creators = [...mockCreators].map(convertMockCreator)
    
    if (type) {
      creators = creators.filter(c => 
        c.commonTypes.some(t => t.type === type)
      )
    }
    
    if (sortBy === 'popular') {
      creators.sort((a, b) => b.totalActivities - a.totalActivities)
    } else if (sortBy === 'successRate') {
      creators.sort((a, b) => b.successRate - a.successRate)
    } else if (sortBy === 'fillSpeed') {
      creators.sort((a, b) => a.avgFillSpeedHours - b.avgFillSpeedHours)
    }
    
    return creators
  }
}

export async function getCreatorById(id: number): Promise<Creator> {
  try {
    const response = await fetch(`${BASE_URL}/creators/${id}`)
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    
    const result: ApiResponse<Creator> = await response.json()
    
    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || 'Invalid response')
    }
    
    return result.data
  } catch (error) {
    console.log('Using mock data for creator detail:', error)
    const mock = mockCreators.find(c => parseInt(c.id.replace('user-', '')) === id) || mockCreators[0]
    return convertMockCreator(mock)
  }
}

export async function getCreatorActivities(creatorId: number): Promise<Activity[]> {
  try {
    const response = await fetch(`${BASE_URL}/creators/${creatorId}/activities`)
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    
    const result: ApiResponse<Activity[]> = await response.json()
    
    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || 'Invalid response')
    }
    
    return result.data
  } catch (error) {
    console.log('Using mock data for creator activities:', error)
    return mockActivities
      .filter(a => parseInt(a.creatorId.replace('user-', '')) === creatorId)
      .map(convertMockActivity)
  }
}

export interface ActivityFootprint {
  id: number
  activityId: number
  title: string
  activityType: string
  city: string
  location: string
  image: string
  activityTime: string
  eventTime: string
  eventType: 'PUBLISHED' | 'REGISTERED' | 'CANCELLED' | 'FULL' | 'CONFIRMED' | 'EXPIRED'
  description: string
}

function getMockFootprints(): ActivityFootprint[] {
  const now = new Date()
  return [
    {
      id: 1,
      activityId: 3,
      title: '周末篮球友谊赛',
      activityType: '打球',
      city: '北京',
      location: '洛克公园篮球场',
      image: 'https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=300&fit=crop',
      activityTime: new Date(now.getTime() + 6 * 24 * 60 * 60 * 1000).toISOString(),
      eventTime: new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString(),
      eventType: 'PUBLISHED',
      description: '你发布了活动 "周末篮球友谊赛"'
    },
    {
      id: 2,
      activityId: 1,
      title: '周末CBD美食探店小分队',
      activityType: '探店',
      city: '北京',
      location: '朝阳区CBD商圈',
      image: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop',
      activityTime: new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000).toISOString(),
      eventTime: new Date(now.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString(),
      eventType: 'REGISTERED',
      description: '你报名了活动 "周末CBD美食探店小分队"'
    },
    {
      id: 3,
      activityId: 2,
      title: '香山徒步登山活动',
      activityType: '徒步',
      city: '北京',
      location: '香山公园东门集合',
      image: 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=300&fit=crop',
      activityTime: new Date(now.getTime() + 5 * 24 * 60 * 60 * 1000).toISOString(),
      eventTime: new Date(now.getTime() - 3 * 24 * 60 * 60 * 1000).toISOString(),
      eventType: 'REGISTERED',
      description: '你报名了活动 "香山徒步登山活动"'
    },
    {
      id: 4,
      activityId: 4,
      title: '桌游之夜：狼人杀+剧本杀',
      activityType: '桌游',
      city: '北京',
      location: '三里屯某某桌游吧',
      image: 'https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400&h=300&fit=crop',
      activityTime: new Date(now.getTime() + 4 * 24 * 60 * 60 * 1000).toISOString(),
      eventTime: new Date(now.getTime() - 4 * 24 * 60 * 60 * 1000).toISOString(),
      eventType: 'REGISTERED',
      description: '你报名了活动 "桌游之夜：狼人杀+剧本杀"'
    },
    {
      id: 5,
      activityId: 4,
      title: '桌游之夜：狼人杀+剧本杀',
      activityType: '桌游',
      city: '北京',
      location: '三里屯某某桌游吧',
      image: 'https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400&h=300&fit=crop',
      activityTime: new Date(now.getTime() + 4 * 24 * 60 * 60 * 1000).toISOString(),
      eventTime: new Date(now.getTime() - 3.5 * 24 * 60 * 60 * 1000).toISOString(),
      eventType: 'CANCELLED',
      description: '你取消了活动 "桌游之夜：狼人杀+剧本杀" 的报名'
    }
  ]
}

export async function getUserActivityFootprints(userId: number): Promise<ActivityFootprint[]> {
  try {
    const response = await fetch(`${BASE_URL}/users/${userId}/footprints`)
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    
    const result: ApiResponse<ActivityFootprint[]> = await response.json()
    
    if (result.code !== 200 || !result.data) {
      throw new Error(result.message || 'Invalid response')
    }
    
    return result.data
  } catch (error) {
    console.log('Using mock data for activity footprints:', error)
    return getMockFootprints()
  }
}

export type BuddyRequestStatus = 'OPEN' | 'MATCHING' | 'MATCHED' | 'CONVERTED' | 'CLOSED'
export type BuddyApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED'

export interface BuddyRequest {
  id: number
  title: string
  type: string
  city: string
  description: string
  targetCount: number
  currentCount: number
  status: BuddyRequestStatus
  convertedActivityId?: number
  createdAt: string
  updatedAt: string
  creatorId: number
  creatorName: string
  creatorAvatar: string
  applicationCount: number
}

export interface BuddyApplication {
  id: number
  requestId: number
  requestTitle: string
  requestType: string
  requestCity: string
  applicantId: number
  applicantName: string
  applicantAvatar: string
  message: string
  status: BuddyApplicationStatus
  createdAt: string
  updatedAt: string
}

const mockBuddyRequests: BuddyRequest[] = [
  {
    id: 1,
    title: '找个饭搭子，一起吃火锅去！',
    type: '饭搭子',
    city: '北京',
    description: '最近想吃火锅，一个人吃太无聊了，找个同样爱吃火锅的小伙伴一起~ 男女不限，AA制。',
    targetCount: 1,
    currentCount: 1,
    status: 'OPEN',
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
    creatorId: 2,
    creatorName: '美食探险家小王',
    creatorAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop',
    applicationCount: 0,
  },
  {
    id: 2,
    title: '周末羽毛球搭子，有人一起吗？',
    type: '球搭子',
    city: '北京',
    description: '周末想打羽毛球，水平一般，纯属娱乐健身。找个水平差不多的球友一起打，场地可以商量。',
    targetCount: 2,
    currentCount: 1,
    status: 'OPEN',
    createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    creatorId: 7,
    creatorName: '羽球小王子',
    creatorAvatar: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=200&h=200&fit=crop',
    applicationCount: 1,
  },
  {
    id: 3,
    title: '一起探店！寻找城市里的宝藏咖啡馆',
    type: '探店搭子',
    city: '北京',
    description: '喜欢探店拍照，特别是有特色的咖啡馆和小店。周末可以一起去探索，互相拍照~',
    targetCount: 1,
    currentCount: 1,
    status: 'MATCHING',
    createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
    creatorId: 6,
    creatorName: '野餐达人小楠',
    creatorAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=200&h=200&fit=crop',
    applicationCount: 2,
  },
  {
    id: 4,
    title: '健身搭子，互相监督一起瘦！',
    type: '健身搭子',
    city: '北京',
    description: '想找个健身搭子，互相监督打卡。我一般晚上下班后去健身房，有一起的吗？',
    targetCount: 1,
    currentCount: 1,
    status: 'OPEN',
    createdAt: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString(),
    creatorId: 4,
    creatorName: '运动达人阿杰',
    creatorAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&h=200&fit=crop',
    applicationCount: 0,
  },
  {
    id: 5,
    title: '周末爬山搭子，香山走起~',
    type: '户外运动',
    city: '北京',
    description: '这周末想去香山徒步，有一起的小伙伴吗？路线轻松，主要是锻炼身体呼吸新鲜空气。',
    targetCount: 3,
    currentCount: 1,
    status: 'OPEN',
    createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
    creatorId: 3,
    creatorName: '户外领队-大山',
    creatorAvatar: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=200&h=200&fit=crop',
    applicationCount: 0,
  },
  {
    id: 6,
    title: '找个一起吃晚饭的饭搭子',
    type: '饭搭子',
    city: '上海',
    description: '刚来上海工作，一个人吃饭太寂寞了，找个附近的饭搭子，工作日晚餐可以一起吃~',
    targetCount: 1,
    currentCount: 1,
    status: 'OPEN',
    createdAt: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 8 * 60 * 60 * 1000).toISOString(),
    creatorId: 1,
    creatorName: '城市探索者',
    creatorAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop',
    applicationCount: 0,
  },
]

const mockBuddyApplications: BuddyApplication[] = [
  {
    id: 1,
    requestId: 3,
    requestTitle: '一起探店！寻找城市里的宝藏咖啡馆',
    requestType: '探店搭子',
    requestCity: '北京',
    applicantId: 2,
    applicantName: '美食探险家小王',
    applicantAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&h=200&fit=crop',
    message: '我也特别喜欢探店咖啡馆！我知道几家超有特色的小众店，可以一起去~',
    status: 'PENDING',
    createdAt: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 6 * 60 * 60 * 1000).toISOString(),
  },
  {
    id: 2,
    requestId: 3,
    requestTitle: '一起探店！寻找城市里的宝藏咖啡馆',
    requestType: '探店搭子',
    requestCity: '北京',
    applicantId: 5,
    applicantName: '桌游女王Luna',
    applicantAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&h=200&fit=crop',
    message: '周末有空，可以一起去！',
    status: 'PENDING',
    createdAt: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(),
    updatedAt: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(),
  },
]

export async function getBuddyRequests(
  city?: string,
  type?: string,
  status?: string,
  sortBy: string = 'newest'
): Promise<BuddyRequest[]> {
  try {
    const params = new URLSearchParams()
    if (city) params.set('city', city)
    if (type) params.set('type', type)
    if (status) params.set('status', status)
    params.set('sortBy', sortBy)

    const response = await fetch(`${BASE_URL}/buddies/requests?${params}`)
    const result: ApiResponse<BuddyRequest[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for buddy requests')
    let result = [...mockBuddyRequests]
    if (city) result = result.filter(r => r.city === city)
    if (type) result = result.filter(r => r.type === type)
    if (status) result = result.filter(r => r.status === status)
    if (sortBy === 'popular') {
      result.sort((a, b) => b.currentCount - a.currentCount)
    } else {
      result.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    }
    return result
  }
}

export async function getBuddyRequestById(id: number): Promise<BuddyRequest> {
  try {
    const response = await fetch(`${BASE_URL}/buddies/requests/${id}`)
    const result: ApiResponse<BuddyRequest> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for buddy request detail')
    const mock = mockBuddyRequests.find(r => r.id === id) || mockBuddyRequests[0]
    return { ...mock }
  }
}

export async function getBuddyRequestsByCreator(creatorId: number): Promise<BuddyRequest[]> {
  try {
    const response = await fetch(`${BASE_URL}/buddies/requests/creator/${creatorId}`)
    const result: ApiResponse<BuddyRequest[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for creator buddy requests')
    return mockBuddyRequests.filter(r => r.creatorId === creatorId)
  }
}

export async function getBuddyRecommendations(
  userId: number,
  city?: string
): Promise<BuddyRequest[]> {
  try {
    const params = new URLSearchParams()
    params.set('userId', String(userId))
    if (city) params.set('city', city)

    const response = await fetch(`${BASE_URL}/buddies/recommendations?${params}`)
    const result: ApiResponse<BuddyRequest[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for buddy recommendations')
    return mockBuddyRequests
      .filter(r => r.creatorId !== userId)
      .filter(r => r.status === 'OPEN' || r.status === 'MATCHING')
      .slice(0, 5)
  }
}

export async function createBuddyRequest(data: {
  title: string
  type: string
  city: string
  description: string
  targetCount: number
  creatorId: number
}): Promise<BuddyRequest> {
  try {
    const response = await fetch(`${BASE_URL}/buddies/requests`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '发布失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }

    const result: ApiResponse<BuddyRequest> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '发布失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error) throw error
    const newRequest: BuddyRequest = {
      id: mockBuddyRequests.length + 1,
      title: data.title,
      type: data.type,
      city: data.city,
      description: data.description,
      targetCount: data.targetCount,
      currentCount: 1,
      status: 'OPEN',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      creatorId: data.creatorId,
      creatorName: '城市探索者',
      creatorAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop',
      applicationCount: 0,
    }
    mockBuddyRequests.unshift(newRequest)
    return newRequest
  }
}

export async function applyForBuddy(data: {
  requestId: number
  applicantId: number
  message: string
}): Promise<BuddyApplication> {
  try {
    const response = await fetch(`${BASE_URL}/buddies/applications`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '申请失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }

    const result: ApiResponse<BuddyApplication> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '申请失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error) throw error
    const request = mockBuddyRequests.find(r => r.id === data.requestId)
    const application: BuddyApplication = {
      id: mockBuddyApplications.length + 1,
      requestId: data.requestId,
      requestTitle: request?.title || '',
      requestType: request?.type || '',
      requestCity: request?.city || '',
      applicantId: data.applicantId,
      applicantName: '城市探索者',
      applicantAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop',
      message: data.message,
      status: 'PENDING',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    }
    mockBuddyApplications.push(application)
    if (request) {
      request.applicationCount++
      if (request.status === 'OPEN') request.status = 'MATCHING'
    }
    return application
  }
}

export async function getBuddyApplicationsByRequest(requestId: number): Promise<BuddyApplication[]> {
  try {
    const response = await fetch(`${BASE_URL}/buddies/applications/request/${requestId}`)
    const result: ApiResponse<BuddyApplication[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for buddy applications')
    return mockBuddyApplications.filter(a => a.requestId === requestId)
  }
}

export async function getBuddyApplicationsByApplicant(applicantId: number): Promise<BuddyApplication[]> {
  try {
    const response = await fetch(`${BASE_URL}/buddies/applications/applicant/${applicantId}`)
    const result: ApiResponse<BuddyApplication[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for applicant buddy applications')
    return mockBuddyApplications.filter(a => a.applicantId === applicantId)
  }
}

export async function acceptBuddyApplication(
  applicationId: number,
  creatorId: number
): Promise<BuddyApplication> {
  try {
    const response = await fetch(
      `${BASE_URL}/buddies/applications/${applicationId}/accept?creatorId=${creatorId}`,
      { method: 'POST' }
    )

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '操作失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }

    const result: ApiResponse<BuddyApplication> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '操作失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error) throw error
    const app = mockBuddyApplications.find(a => a.id === applicationId)
    if (app) {
      app.status = 'ACCEPTED'
      app.updatedAt = new Date().toISOString()
      const request = mockBuddyRequests.find(r => r.id === app.requestId)
      if (request) {
        request.currentCount++
        if (request.currentCount >= request.targetCount) {
          request.status = 'MATCHED'
        }
      }
    }
    return app!
  }
}

export async function rejectBuddyApplication(
  applicationId: number,
  creatorId: number,
  reason?: string
): Promise<BuddyApplication> {
  try {
    const params = new URLSearchParams()
    params.set('creatorId', String(creatorId))
    if (reason) params.set('reason', reason)

    const response = await fetch(
      `${BASE_URL}/buddies/applications/${applicationId}/reject?${params}`,
      { method: 'POST' }
    )

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '操作失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }

    const result: ApiResponse<BuddyApplication> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '操作失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error) throw error
    const app = mockBuddyApplications.find(a => a.id === applicationId)
    if (app) {
      app.status = 'REJECTED'
      app.updatedAt = new Date().toISOString()
    }
    return app!
  }
}

export async function cancelBuddyApplication(
  applicationId: number,
  applicantId: number
): Promise<BuddyApplication> {
  try {
    const response = await fetch(
      `${BASE_URL}/buddies/applications/${applicationId}/cancel?applicantId=${applicantId}`,
      { method: 'POST' }
    )

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '操作失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }

    const result: ApiResponse<BuddyApplication> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '操作失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error) throw error
    const app = mockBuddyApplications.find(a => a.id === applicationId)
    if (app) {
      app.status = 'CANCELLED'
      app.updatedAt = new Date().toISOString()
    }
    return app!
  }
}

export async function closeBuddyRequest(
  requestId: number,
  creatorId: number
): Promise<BuddyRequest> {
  try {
    const response = await fetch(
      `${BASE_URL}/buddies/requests/${requestId}/close?creatorId=${creatorId}`,
      { method: 'POST' }
    )

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '操作失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }

    const result: ApiResponse<BuddyRequest> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '操作失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error) throw error
    const request = mockBuddyRequests.find(r => r.id === requestId)
    if (request) {
      request.status = 'CLOSED'
      request.updatedAt = new Date().toISOString()
    }
    return request!
  }
}

export async function convertBuddyToActivity(data: {
  requestId: number
  creatorId: number
  location: string
  time: string
  requirements?: string
  image?: string
}): Promise<Activity> {
  try {
    const response = await fetch(`${BASE_URL}/buddies/convert`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '转换失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }

    const result: ApiResponse<Activity> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '转换失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error) throw error
    const request = mockBuddyRequests.find(r => r.id === data.requestId)
    if (!request) throw new Error('征集帖不存在')

    if (request.status !== 'MATCHED' && request.currentCount < request.targetCount) {
      throw new Error('需达到目标人数或状态为已配对才能转换为正式活动')
    }

    if (request.status === 'CONVERTED') throw new Error('该征集已转换为活动')

    const newActivity: Activity = {
      id: 100 + Math.floor(Math.random() * 1000),
      title: request.title,
      type: request.type,
      city: request.city,
      location: data.location,
      time: data.time,
      maxParticipants: request.targetCount,
      currentParticipants: request.currentCount,
      description: `【${request.type}搭子活动】\n${request.description}\n\n本活动由搭子征集帖转化而来，已成功配对${request.currentCount}人。`,
      requirements: data.requirements || '',
      image: data.image || 'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=300&fit=crop',
      views: 0,
      createdAt: new Date().toISOString(),
      creatorId: data.creatorId,
      creatorName: request.creatorName,
      waitlistCount: 0,
    }

    request.status = 'CONVERTED'
    request.convertedActivityId = newActivity.id
    request.updatedAt = new Date().toISOString()

    return newActivity
  }
}

export interface Comment {
  id: number
  activityId: number
  userId: number
  userName: string
  userAvatar: string
  content: string
  parentId?: number
  replyToUserId?: number
  replyToUserName?: string
  category?: string
  likes: number
  isPinned: boolean
  createdAt: string
  replies?: Comment[]
}

export interface CommentCategoryStats {
  category: string
  count: number
}

export const COMMENT_CATEGORIES = [
  { key: 'MEETING_POINT', label: '集合点', icon: '📍' },
  { key: 'FEE', label: '费用', icon: '💰' },
  { key: 'EQUIPMENT', label: '装备', icon: '🎒' },
  { key: 'BEGINNER_FRIENDLY', label: '新手友好', icon: '🌱' },
  { key: 'OTHER', label: '其他', icon: '💬' },
]

const mockComments: Comment[] = [
  {
    id: 1,
    activityId: 1,
    userId: 3,
    userName: '户外爱好者小明',
    userAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop',
    content: '请问集合点具体在香山公园东门的哪个位置？有明显的标志物吗？大概需要提前多久到？',
    category: 'MEETING_POINT',
    likes: 5,
    isPinned: false,
    createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString(),
    replies: [
      {
        id: 2,
        activityId: 1,
        userId: 1,
        userName: '活动发起人',
        userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
        content: '东门进去有个大石碑，就在那里集合～建议提前10分钟到，我们会准时出发的！',
        parentId: 1,
        replyToUserId: 3,
        replyToUserName: '户外爱好者小明',
        likes: 3,
        isPinned: false,
        createdAt: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000 + 60 * 60 * 1000).toISOString(),
      },
    ],
  },
  {
    id: 3,
    activityId: 1,
    userId: 5,
    userName: '新手小白',
    userAvatar: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&h=100&fit=crop',
    content: '请问这个活动对新手友好吗？我平时很少运动，会不会跟不上大部队？',
    category: 'BEGINNER_FRIENDLY',
    likes: 8,
    isPinned: true,
    createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(),
    replies: [
      {
        id: 4,
        activityId: 1,
        userId: 1,
        userName: '活动发起人',
        userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
        content: '完全没问题！这条路线是入门级的，全程都是修好的步道，我们会控制节奏，大家相互照应～',
        parentId: 3,
        replyToUserId: 5,
        replyToUserName: '新手小白',
        likes: 6,
        isPinned: false,
        createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000 + 2 * 60 * 60 * 1000).toISOString(),
      },
      {
        id: 5,
        activityId: 1,
        userId: 7,
        userName: '羽球小王子',
        userAvatar: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100&h=100&fit=crop',
        content: '我也是新手，上周参加过一次，完全跟得上，领队人超好的！',
        parentId: 3,
        replyToUserId: 5,
        replyToUserName: '新手小白',
        likes: 2,
        isPinned: false,
        createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000 + 3 * 60 * 60 * 1000).toISOString(),
      },
    ],
  },
  {
    id: 6,
    activityId: 1,
    userId: 6,
    userName: '野餐达人小楠',
    userAvatar: 'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=100&h=100&fit=crop',
    content: '请问费用大概是多少呀？门票是AA还是组织者统一买？',
    category: 'FEE',
    likes: 4,
    isPinned: false,
    createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
    replies: [
      {
        id: 7,
        activityId: 1,
        userId: 1,
        userName: '活动发起人',
        userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
        content: '门票10块钱自己买哈，下山后聚餐AA，人均大概50左右，丰俭由人～',
        parentId: 6,
        replyToUserId: 6,
        replyToUserName: '野餐达人小楠',
        likes: 3,
        isPinned: false,
        createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000 + 30 * 60 * 1000).toISOString(),
      },
    ],
  },
  {
    id: 8,
    activityId: 1,
    userId: 4,
    userName: '运动达人阿杰',
    userAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&h=100&fit=crop',
    content: '需要带什么装备吗？有没有强制要求的？',
    category: 'EQUIPMENT',
    likes: 3,
    isPinned: false,
    createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
    replies: [
      {
        id: 9,
        activityId: 1,
        userId: 1,
        userName: '活动发起人',
        userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
        content: '建议穿舒适的运动鞋，带瓶水就行～有登山杖可以带上，没有也完全没问题。',
        parentId: 8,
        replyToUserId: 4,
        replyToUserName: '运动达人阿杰',
        likes: 2,
        isPinned: false,
        createdAt: new Date(Date.now() - 4 * 60 * 60 * 1000).toISOString(),
      },
    ],
  },
  {
    id: 10,
    activityId: 1,
    userId: 2,
    userName: '美食探险家小王',
    userAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&h=100&fit=crop',
    content: '期待！上次一起徒步超开心的，这次还能认识新朋友～',
    likes: 1,
    isPinned: false,
    createdAt: new Date(Date.now() - 30 * 60 * 1000).toISOString(),
  },
]

export async function getComments(
  activityId: number,
  category?: string
): Promise<Comment[]> {
  try {
    const params = new URLSearchParams()
    if (category) params.set('category', category)

    const response = await fetch(`${BASE_URL}/comments/activity/${activityId}?${params}`)
    const result: ApiResponse<Comment[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for comments')
    let comments = [...mockComments].filter(c => c.activityId === activityId)
    if (category) {
      comments = comments.filter(c => c.category === category)
    }
    return comments
  }
}

export async function getCommentCategoryStats(activityId: number): Promise<CommentCategoryStats[]> {
  try {
    const response = await fetch(`${BASE_URL}/comments/activity/${activityId}/stats`)
    const result: ApiResponse<CommentCategoryStats[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for comment stats')
    const stats: Record<string, number> = {}
    mockComments
      .filter(c => c.activityId === activityId && c.category)
      .forEach(c => {
        const cat = c.category!
        stats[cat] = (stats[cat] || 0) + 1
      })
    return Object.entries(stats).map(([category, count]) => ({ category, count }))
  }
}

export async function createComment(data: {
  activityId: number
  userId: number
  content: string
  parentId?: number
  replyToUserId?: number
  category?: string
}): Promise<Comment> {
  try {
    const response = await fetch(`${BASE_URL}/comments`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '发布失败' }))
      throw new Error(errorData.message || `HTTP ${response.status}`)
    }

    const result: ApiResponse<Comment> = await response.json()
    if (result.code !== 200) {
      throw new Error(result.message || '发布失败')
    }

    return result.data
  } catch (error) {
    if (error instanceof Error) throw error
    
    const newComment: Comment = {
      id: mockComments.length + 100,
      activityId: data.activityId,
      userId: data.userId,
      userName: '城市探索者',
      userAvatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
      content: data.content,
      parentId: data.parentId,
      replyToUserId: data.replyToUserId,
      category: data.category,
      likes: 0,
      isPinned: false,
      createdAt: new Date().toISOString(),
    }

    if (data.parentId) {
      const parent = mockComments.find(c => c.id === data.parentId)
      if (parent && parent.replies) {
        parent.replies.push(newComment)
      }
    } else {
      mockComments.unshift(newComment)
    }

    return newComment
  }
}

export async function likeComment(commentId: number, userId: number): Promise<Comment> {
  try {
    const response = await fetch(`${BASE_URL}/comments/${commentId}/like?userId=${userId}`, {
      method: 'POST',
    })
    const result: ApiResponse<Comment> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for like comment')
    const comment = findCommentById(commentId)
    if (comment) {
      comment.likes++
    }
    return comment!
  }
}

function findCommentById(id: number): Comment | undefined {
  for (const c of mockComments) {
    if (c.id === id) return c
    if (c.replies) {
      const reply = c.replies.find(r => r.id === id)
      if (reply) return reply
    }
  }
  return undefined
}
