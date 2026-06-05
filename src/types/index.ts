export type ActivityType = '聚餐' | '徒步' | '打球' | '探店' | '桌游' | '其他'

export interface Activity {
  id: string
  title: string
  type: ActivityType
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
  creatorId: string
}

export interface User {
  id: string
  name: string
  avatar: string
}

export interface Registration {
  id: string
  activityId: string
  userId: string
  registeredAt: string
}
