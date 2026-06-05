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

export async function getActivities(
  city?: string,
  type?: string,
  sortBy: string = 'newest'
): Promise<Activity[]> {
  const params = new URLSearchParams()
  if (city) params.set('city', city)
  if (type) params.set('type', type)
  params.set('sortBy', sortBy)
  
  const response = await fetch(`${BASE_URL}/activities?${params}`)
  const result: ApiResponse<Activity[]> = await response.json()
  return result.data
}

export async function getActivityById(id: number): Promise<Activity> {
  const response = await fetch(`${BASE_URL}/activities/${id}`)
  const result: ApiResponse<Activity> = await response.json()
  return result.data
}

export async function getHotActivities(): Promise<Activity[]> {
  const response = await fetch(`${BASE_URL}/activities/hot`)
  const result: ApiResponse<Activity[]> = await response.json()
  return result.data
}

export async function getActivitiesByCreator(creatorId: number): Promise<Activity[]> {
  const response = await fetch(`${BASE_URL}/activities/creator/${creatorId}`)
  const result: ApiResponse<Activity[]> = await response.json()
  return result.data
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
  const result: ApiResponse<Activity> = await response.json()
  return result.data
}

export async function registerActivity(activityId: number, userId: number): Promise<void> {
  await fetch(`${BASE_URL}/registrations`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ activityId, userId }),
  })
}

export async function cancelRegistration(activityId: number, userId: number): Promise<void> {
  await fetch(`${BASE_URL}/registrations?activityId=${activityId}&userId=${userId}`, {
    method: 'DELETE',
  })
}

export async function checkRegistration(activityId: number, userId: number): Promise<boolean> {
  const response = await fetch(`${BASE_URL}/registrations/check?activityId=${activityId}&userId=${userId}`)
  const result: ApiResponse<boolean> = await response.json()
  return result.data
}

export async function getRegisteredActivities(userId: number): Promise<Activity[]> {
  const response = await fetch(`${BASE_URL}/registrations/user/${userId}`)
  const result: ApiResponse<Activity[]> = await response.json()
  return result.data
}
