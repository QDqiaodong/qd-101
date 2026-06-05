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

const nightActivityImages = [
  'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1478145046317-39f10e56b5e9?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1552664730-d307ca884978?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1505751172876-fa1923c5c528?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1559339352-11d035aa65de?w=400&h=300&fit=crop',
  'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&h=300&fit=crop',
]

const tonight6 = new Date(today)
tonight6.setHours(18, 0, 0, 0)

const tonight630 = new Date(today)
tonight630.setHours(18, 30, 0, 0)

const tonight7 = new Date(today)
tonight7.setHours(19, 0, 0, 0)

const tonight730 = new Date(today)
tonight730.setHours(19, 30, 0, 0)

const tonight8 = new Date(today)
tonight8.setHours(20, 0, 0, 0)

const tonight830 = new Date(today)
tonight830.setHours(20, 30, 0, 0)

const tonight9 = new Date(today)
tonight9.setHours(21, 0, 0, 0)

const tonight930 = new Date(today)
tonight930.setHours(21, 30, 0, 0)

const tonight10 = new Date(today)
tonight10.setHours(22, 0, 0, 0)

const tonight1030 = new Date(today)
tonight1030.setHours(22, 30, 0, 0)

const tomorrowNight = new Date(today)
tomorrowNight.setDate(today.getDate() + 1)
tomorrowNight.setHours(19, 0, 0, 0)

const fridayNight = new Date(today)
fridayNight.setDate(today.getDate() + ((5 - today.getDay() + 7) % 7))
fridayNight.setHours(19, 30, 0, 0)

export const nightActivities: Activity[] = [
  {
    id: 'night-1',
    title: '今晚簋街小龙虾夜宵局',
    type: '聚餐',
    city: '北京',
    location: '簋街胡大饭馆',
    time: `${formatDate(tonight630)} 18:30`,
    maxParticipants: 8,
    currentParticipants: 5,
    description: '下班直奔簋街，小龙虾+烤串+冰啤酒，麻辣鲜香，深夜食堂走起！',
    requirements: '能吃辣，不挑食，AA制',
    image: nightActivityImages[0],
    views: 256,
    createdAt: '2026-06-05T14:00:00',
    creatorId: 'user-12',
  },
  {
    id: 'night-2',
    title: '深夜桌游吧·狼人杀剧本杀通宵场',
    type: '桌游',
    city: '北京',
    location: '三里屯某某桌游吧',
    time: `${formatDate(tonight8)} 20:00`,
    maxParticipants: 12,
    currentParticipants: 9,
    description: '周五晚上来场烧脑桌游！狼人杀、剧本杀、uno全都有，玩到嗨！',
    requirements: '喜欢逻辑推理，放得开玩，能玩到深夜',
    image: nightActivityImages[3],
    views: 389,
    createdAt: '2026-06-04T20:00:00',
    creatorId: 'user-13',
  },
  {
    id: 'night-3',
    title: 'CBD下班后小聚·清吧聊天',
    type: '聚餐',
    city: '北京',
    location: 'CBD商圈某某清吧',
    time: `${formatDate(tonight7)} 19:00`,
    maxParticipants: 6,
    currentParticipants: 3,
    description: '下班不想直接回家？来清吧小酌一杯，聊聊工作生活，认识新朋友。',
    requirements: '性格开朗，不酗酒',
    image: nightActivityImages[1],
    views: 178,
    createdAt: '2026-06-05T10:00:00',
    creatorId: 'user-14',
  },
  {
    id: 'night-4',
    title: '奥森夜跑5公里·夜猫子跑团',
    type: '徒步',
    city: '北京',
    location: '奥森公园南门',
    time: `${formatDate(tonight830)} 20:30`,
    maxParticipants: 15,
    currentParticipants: 10,
    description: '晚上跑步更凉快！绕奥森南园跑一圈5公里，配速6分30秒，新手友好。',
    requirements: '有跑步基础，穿运动鞋，带水',
    image: nightActivityImages[6],
    views: 234,
    createdAt: '2026-06-04T16:00:00',
    creatorId: 'user-15',
  },
  {
    id: 'night-5',
    title: '深夜食堂·日式居酒屋探店',
    type: '探店',
    city: '北京',
    location: '望京某某居酒屋',
    time: `${formatDate(tonight9)} 21:00`,
    maxParticipants: 6,
    currentParticipants: 4,
    description: '藏在望京的宝藏居酒屋，烧鸟+清酒+日剧氛围，深夜的治愈时刻。',
    requirements: '热爱美食，喜欢日料',
    image: nightActivityImages[2],
    views: 167,
    createdAt: '2026-06-05T12:00:00',
    creatorId: 'user-16',
  },
  {
    id: 'night-6',
    title: '五道口深夜狼人杀·高手局',
    type: '桌游',
    city: '北京',
    location: '五道口桌游吧',
    time: `${formatDate(tonight930)} 21:30`,
    maxParticipants: 10,
    currentParticipants: 7,
    description: '五道口狼人杀高手局，逻辑流玩家聚集地，玩到凌晨不是事！',
    requirements: '熟悉狼人杀规则，逻辑清晰',
    image: nightActivityImages[7],
    views: 298,
    createdAt: '2026-06-03T22:00:00',
    creatorId: 'user-17',
  },
  {
    id: 'night-7',
    title: '朝阳公园夜跑团·减肥打卡',
    type: '徒步',
    city: '北京',
    location: '朝阳公园西门',
    time: `${formatDate(tonight730)} 19:30`,
    maxParticipants: 20,
    currentParticipants: 12,
    description: '每天晚上朝阳公园夜跑，3-5公里任选，跑完一起拉伸打卡。',
    requirements: '想运动减肥，有基本运动能力',
    image: nightActivityImages[11],
    views: 312,
    createdAt: '2026-06-02T09:00:00',
    creatorId: 'user-18',
  },
  {
    id: 'night-8',
    title: '撸串啤酒·深夜解忧杂货店',
    type: '聚餐',
    city: '北京',
    location: '双井某烧烤店',
    time: `${formatDate(tonight10)} 22:00`,
    maxParticipants: 8,
    currentParticipants: 6,
    description: '深夜的烧烤摊，是城市的解忧杂货店。烤串+啤酒+聊天，烦恼全忘掉。',
    requirements: '性格开朗，能吃能聊',
    image: nightActivityImages[4],
    views: 245,
    createdAt: '2026-06-05T15:00:00',
    creatorId: 'user-19',
  },
  {
    id: 'night-9',
    title: '下班后的微醺时光·鸡尾酒品鉴',
    type: '探店',
    city: '北京',
    location: '三里屯某某鸡尾酒吧',
    time: `${formatDate(tonight6)} 18:00`,
    maxParticipants: 6,
    currentParticipants: 2,
    description: '下班后来杯鸡尾酒放松一下，专业调酒师教你认识各种基酒。',
    requirements: '喜欢鸡尾酒，不酗酒',
    image: nightActivityImages[9],
    views: 145,
    createdAt: '2026-06-05T11:00:00',
    creatorId: 'user-20',
  },
  {
    id: 'night-10',
    title: '深夜剧本杀·情感本哭哭局',
    type: '桌游',
    city: '北京',
    location: '朝阳剧本杀推理馆',
    time: `${formatDate(tomorrowNight)} 19:00`,
    maxParticipants: 6,
    currentParticipants: 4,
    description: '明天晚上高分情感本，沉浸式体验，准备好纸巾，哭就完事了。',
    requirements: '喜欢情感本，代入感强',
    image: nightActivityImages[5],
    views: 201,
    createdAt: '2026-06-04T14:00:00',
    creatorId: 'user-21',
  },
  {
    id: 'night-11',
    title: '后海夜跑+酒吧小坐',
    type: '徒步',
    city: '北京',
    location: '后海地铁站',
    time: `${formatDate(fridayNight)} 19:30`,
    maxParticipants: 10,
    currentParticipants: 5,
    description: '周五晚后海夜跑，沿着湖边跑3公里，跑完去酒吧小坐聊聊天。',
    requirements: '喜欢运动，性格开朗',
    image: nightActivityImages[8],
    views: 189,
    createdAt: '2026-06-03T18:00:00',
    creatorId: 'user-22',
  },
  {
    id: 'night-12',
    title: '深夜火锅局·越夜越热闹',
    type: '聚餐',
    city: '北京',
    location: '国贸海底捞',
    time: `${formatDate(tonight1030)} 22:30`,
    maxParticipants: 8,
    currentParticipants: 3,
    description: '深夜火锅最治愈！海底捞服务好味道棒，聊到天亮都可以。',
    requirements: '爱吃火锅，不挑食',
    image: nightActivityImages[10],
    views: 156,
    createdAt: '2026-06-05T16:00:00',
    creatorId: 'user-23',
  },
]

export let activities = [...mockActivities, ...nightActivities]
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
