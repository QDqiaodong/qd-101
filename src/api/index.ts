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
    const result: ApiResponse<Creator[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for creators')
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
    const result: ApiResponse<Creator> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for creator detail')
    const mock = mockCreators.find(c => parseInt(c.id.replace('user-', '')) === id) || mockCreators[0]
    return convertMockCreator(mock)
  }
}

export async function getCreatorActivities(creatorId: number): Promise<Activity[]> {
  try {
    const response = await fetch(`${BASE_URL}/creators/${creatorId}/activities`)
    const result: ApiResponse<Activity[]> = await response.json()
    return result.data
  } catch (error) {
    console.log('Using mock data for creator activities')
    return mockActivities
      .filter(a => parseInt(a.creatorId.replace('user-', '')) === creatorId)
      .map(convertMockActivity)
  }
}
