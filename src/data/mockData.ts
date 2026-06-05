import type { Activity, User, Registration } from '@/types'

export const mockUser: User = {
  id: 'user-1',
  name: '城市探索者',
  avatar: 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=100&h=100&fit=crop',
}

const activityImages = [
  'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1551632811-561732d1e306?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1514525253440-b393452e8d26?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400&h=300&fit=crop',
]

export const mockActivities: Activity[] = [
  {
    id: 'act-1',
    title: '周末CBD美食探店小分队',
    type: '探店',
    city: '北京',
    location: '朝阳区CBD商圈',
    time: '2025-01-20 18:00',
    maxParticipants: 8,
    currentParticipants: 5,
    description: '这周末一起去探索CBD新开的网红餐厅吧！主打融合菜，听说环境超棒，适合拍照打卡。',
    requirements: '热爱美食，性格开朗，不挑食',
    image: activityImages[3],
    views: 328,
    createdAt: '2025-01-15T10:30:00',
    creatorId: 'user-2',
  },
  {
    id: 'act-2',
    title: '香山徒步登山活动',
    type: '徒步',
    city: '北京',
    location: '香山公园东门集合',
    time: '2025-01-21 09:00',
    maxParticipants: 15,
    currentParticipants: 12,
    description: '新年第一次登山活动！路线从东门到鬼见愁，全程约3小时，难度适中，适合新手。',
    requirements: '穿着运动鞋，自带饮用水',
    image: activityImages[0],
    views: 512,
    createdAt: '2025-01-14T14:20:00',
    creatorId: 'user-3',
  },
  {
    id: 'act-3',
    title: '周末篮球友谊赛',
    type: '打球',
    city: '上海',
    location: '洛克公园篮球场',
    time: '2025-01-22 14:00',
    maxParticipants: 12,
    currentParticipants: 8,
    description: '下班后放松一下，来场3v3友谊赛！不分水平，重在参与，锻炼身体结交朋友。',
    requirements: '带好运动装备，注意安全',
    image: activityImages[1],
    views: 245,
    createdAt: '2025-01-16T08:45:00',
    creatorId: 'user-4',
  },
  {
    id: 'act-4',
    title: '桌游之夜：狼人杀+剧本杀',
    type: '桌游',
    city: '广州',
    location: '天河区某某桌游吧',
    time: '2025-01-20 19:00',
    maxParticipants: 10,
    currentParticipants: 7,
    description: '周末来场烧脑的桌游派对！狼人杀、剧本杀、uno都有，场地已预定好，就等你了！',
    requirements: '喜欢逻辑推理，放得开玩',
    image: activityImages[4],
    views: 403,
    createdAt: '2025-01-13T20:10:00',
    creatorId: 'user-5',
  },
  {
    id: 'act-5',
    title: '年夜饭预热聚餐',
    type: '聚餐',
    city: '深圳',
    location: '福田区某湘菜馆',
    time: '2025-01-23 18:30',
    maxParticipants: 12,
    currentParticipants: 9,
    description: '年前最后一次聚餐，选了家超正宗的湘菜馆，无辣不欢的朋友赶紧报名！',
    requirements: '能吃辣，AA制',
    image: activityImages[5],
    views: 287,
    createdAt: '2025-01-17T12:00:00',
    creatorId: 'user-6',
  },
  {
    id: 'act-6',
    title: '西湖晨跑+早餐',
    type: '徒步',
    city: '杭州',
    location: '西湖断桥集合',
    time: '2025-01-21 07:00',
    maxParticipants: 10,
    currentParticipants: 4,
    description: '迎着清晨的阳光，绕西湖跑一圈，然后一起去吃杭州特色早餐！',
    requirements: '有跑步基础，起得来床',
    image: activityImages[2],
    views: 176,
    createdAt: '2025-01-18T09:30:00',
    creatorId: 'user-7',
  },
]

export let mockRegistrations: Registration[] = [
  {
    id: 'reg-1',
    activityId: 'act-1',
    userId: mockUser.id,
    registeredAt: '2025-01-16T15:30:00',
  },
  {
    id: 'reg-2',
    activityId: 'act-3',
    userId: mockUser.id,
    registeredAt: '2025-01-17T10:00:00',
  },
]

export let activities = [...mockActivities]
export let registrations = [...mockRegistrations]

export function saveToStorage() {
  localStorage.setItem('activities', JSON.stringify(activities))
  localStorage.setItem('registrations', JSON.stringify(registrations))
}

export function loadFromStorage() {
  const savedActivities = localStorage.getItem('activities')
  const savedRegistrations = localStorage.getItem('registrations')
  
  if (savedActivities) {
    activities = JSON.parse(savedActivities)
  }
  if (savedRegistrations) {
    registrations = JSON.parse(savedRegistrations)
  }
}

loadFromStorage()
