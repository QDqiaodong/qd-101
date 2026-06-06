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

export interface CreatorActivityType {
  type: ActivityType
  count: number
}

export interface CreatorCommonArea {
  name: string
  count: number
}

export interface CreatorReviewTendency {
  tag: string
  count: number
}

export interface CreatorProfile {
  id: string
  name: string
  avatar: string
  bio: string
  totalActivities: number
  successRate: number
  avgFillSpeedHours: number
  commonTypes: CreatorActivityType[]
  commonAreas: CreatorCommonArea[]
  reviewTags: CreatorReviewTendency[]
  styleTags: string[]
}
