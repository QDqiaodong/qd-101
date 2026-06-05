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

const today = new Date()
const formatDate = (date: Date) => date.toISOString().split('T')[0]

const tonight = new Date(today)
tonight.setHours(19, 0, 0, 0)

const tonightLate = new Date(today)
tonightLate.setHours(20, 30, 0, 0)

const saturday = new Date(today)
saturday.setDate(today.getDate() + ((6 - today.getDay() + 7) % 7))
saturday.setHours(14, 0, 0, 0)

const saturdayNight = new Date(saturday)
saturdayNight.setHours(19, 0, 0, 0)

const sunday = new Date(today)
sunday.setDate(today.getDate() + ((7 - today.getDay() + 7) % 7))
sunday.setHours(10, 0, 0, 0)

const wednesday = new Date(today)
wednesday.setDate(today.getDate() + ((3 - today.getDay() + 7) % 7))
wednesday.setHours(18, 30, 0, 0)

const thursday = new Date(today)
thursday.setDate(today.getDate() + ((4 - today.getDay() + 7) % 7))
thursday.setHours(19, 0, 0, 0)

export const mockActivities: Activity[] = [
  {
    id: 'act-1',
    title: '今晚CBD美食探店小分队',
    type: '探店',
    city: '北京',
    location: '朝阳区CBD商圈',
    time: `${formatDate(tonight)} 19:00`,
    maxParticipants: 6,
    currentParticipants: 4,
    description: '今晚一起去探索CBD新开的网红餐厅吧！主打融合菜，听说环境超棒，适合拍照打卡。',
    requirements: '热爱美食，性格开朗，不挑食',
    image: activityImages[3],
    views: 328,
    createdAt: '2026-06-04T10:30:00',
    creatorId: 'user-2',
  },
  {
    id: 'act-2',
    title: '周六香山徒步登山活动',
    type: '徒步',
    city: '北京',
    location: '香山公园东门集合',
    time: `${formatDate(saturday)} 09:00`,
    maxParticipants: 15,
    currentParticipants: 13,
    description: '周末登山活动！路线从东门到鬼见愁，全程约3小时，难度适中，适合新手。',
    requirements: '穿着运动鞋，自带饮用水',
    image: activityImages[0],
    views: 512,
    createdAt: '2026-06-03T14:20:00',
    creatorId: 'user-3',
  },
  {
    id: 'act-3',
    title: '周末篮球友谊赛',
    type: '打球',
    city: '北京',
    location: '洛克公园篮球场',
    time: `${formatDate(saturday)} 14:00`,
    maxParticipants: 12,
    currentParticipants: 10,
    description: '周末放松一下，来场3v3友谊赛！不分水平，重在参与，锻炼身体结交朋友。',
    requirements: '带好运动装备，注意安全',
    image: activityImages[1],
    views: 245,
    createdAt: '2026-06-02T08:45:00',
    creatorId: 'user-4',
  },
  {
    id: 'act-4',
    title: '周六桌游之夜：狼人杀+剧本杀',
    type: '桌游',
    city: '北京',
    location: '朝阳区某某桌游吧',
    time: `${formatDate(saturdayNight)} 19:00`,
    maxParticipants: 10,
    currentParticipants: 8,
    description: '周六来场烧脑的桌游派对！狼人杀、剧本杀、uno都有，场地已预定好，就等你了！',
    requirements: '喜欢逻辑推理，放得开玩',
    image: activityImages[4],
    views: 403,
    createdAt: '2026-06-01T20:10:00',
    creatorId: 'user-5',
  },
  {
    id: 'act-5',
    title: '周日公园野餐聚会',
    type: '聚餐',
    city: '北京',
    location: '朝阳公园',
    time: `${formatDate(sunday)} 12:00`,
    maxParticipants: 12,
    currentParticipants: 9,
    description: '周日阳光正好，来公园野餐吧！每人带一道菜，分享美食和故事。',
    requirements: '自带一道菜品，不挑食',
    image: activityImages[5],
    views: 287,
    createdAt: '2026-06-03T12:00:00',
    creatorId: 'user-6',
  },
  {
    id: 'act-6',
    title: '周三下班后羽毛球局',
    type: '打球',
    city: '北京',
    location: '李宁羽毛球馆',
    time: `${formatDate(wednesday)} 18:30`,
    maxParticipants: 8,
    currentParticipants: 6,
    description: '周三下班放松一下，来打羽毛球！场地已订好，球拍可借。',
    requirements: '穿着运动服，注意安全',
    image: activityImages[2],
    views: 176,
    createdAt: '2026-06-02T09:30:00',
    creatorId: 'user-7',
  },
  {
    id: 'act-7',
    title: '周四晚撸串小聚',
    type: '聚餐',
    city: '北京',
    location: '簋街某烧烤店',
    time: `${formatDate(thursday)} 19:00`,
    maxParticipants: 8,
    currentParticipants: 5,
    description: '周四晚上来撸串！聊聊工作生活，放松一下心情~',
    requirements: '能吃辣，AA制',
    image: activityImages[3],
    views: 198,
    createdAt: '2026-06-04T15:00:00',
    creatorId: 'user-8',
  },
  {
    id: 'act-8',
    title: '周日晨跑+早餐',
    type: '徒步',
    city: '北京',
    location: '奥森公园南门',
    time: `${formatDate(sunday)} 07:00`,
    maxParticipants: 10,
    currentParticipants: 3,
    description: '迎着清晨的阳光，绕奥森跑一圈，然后一起去吃早餐！',
    requirements: '有跑步基础，起得来床',
    image: activityImages[0],
    views: 156,
    createdAt: '2026-06-03T08:00:00',
    creatorId: 'user-9',
  },
  {
    id: 'act-9',
    title: '今晚深夜狼人杀',
    type: '桌游',
    city: '北京',
    location: '五道口桌游吧',
    time: `${formatDate(tonightLate)} 20:30`,
    maxParticipants: 12,
    currentParticipants: 9,
    description: '今晚来场深夜狼人杀！高手云集，就等你了~',
    requirements: '熟悉狼人杀规则，能玩到深夜',
    image: activityImages[4],
    views: 234,
    createdAt: '2026-06-05T14:00:00',
    creatorId: 'user-10',
  },
  {
    id: 'act-10',
    title: '周日咖啡品鉴会',
    type: '探店',
    city: '北京',
    location: '三里屯某精品咖啡店',
    time: `${formatDate(sunday)} 15:00`,
    maxParticipants: 8,
    currentParticipants: 5,
    description: '一起品尝来自世界各地的精品咖啡，了解咖啡文化~',
    requirements: '喜欢咖啡，不迟到',
    image: activityImages[1],
    views: 189,
    createdAt: '2026-06-04T10:00:00',
    creatorId: 'user-11',
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
  {
    id: 'reg-3',
    activityId: 'act-9',
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
