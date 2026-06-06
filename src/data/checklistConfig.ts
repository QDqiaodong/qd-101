export interface ChecklistItem {
  name: string
  icon: string
  checked: boolean
  category: string
}

export interface TransportOption {
  type: string
  icon: string
  duration: string
  risk: 'low' | 'medium' | 'high'
  description: string
}

export interface ActivityChecklist {
  items: ChecklistItem[]
  transportOptions: TransportOption[]
  tips: string[]
}

const createItem = (name: string, icon: string, category: string): ChecklistItem => ({
  name,
  icon,
  checked: false,
  category,
})

export const typeChecklistConfig: Record<string, ActivityChecklist> = {
  '徒步': {
    items: [
      createItem('运动鞋/登山鞋', '👟', '穿着'),
      createItem('运动服装', '👕', '穿着'),
      createItem('遮阳帽/太阳镜', '🕶️', '穿着'),
      createItem('防晒霜', '🧴', '防护'),
      createItem('驱蚊液', '🦟', '防护'),
      createItem('充电宝', '🔋', '电子'),
      createItem('手机', '📱', '电子'),
      createItem('饮用水（至少500ml）', '💧', '补给'),
      createItem('能量棒/小零食', '🍫', '补给'),
      createItem('纸巾/湿巾', '🧻', '其他'),
      createItem('垃圾袋', '🗑️', '其他'),
      createItem('急救包（创可贴等）', '🩹', '其他'),
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约45分钟', risk: 'low', description: '准时可靠，推荐' },
      { type: '公交', icon: '🚌', duration: '约60分钟', risk: 'medium', description: '可能堵车，预留时间' },
      { type: '打车', icon: '🚕', duration: '约30分钟', risk: 'low', description: '费用较高，可拼车' },
      { type: '自驾', icon: '🚗', duration: '约25分钟', risk: 'medium', description: '停车可能紧张' },
    ],
    tips: [
      '提前15分钟到达集合点，热身准备',
      '沿途注意补水，少量多次',
      '跟随队伍，不要擅自离队',
      '下山时注意膝盖保护',
    ],
  },
  '打球': {
    items: [
      createItem('运动服', '👕', '穿着'),
      createItem('运动鞋', '👟', '穿着'),
      createItem('运动手环/护具', '⌚', '装备'),
      createItem('球拍（如有）', '🏸', '装备'),
      createItem('运动毛巾', '🧣', '装备'),
      createItem('换洗衣物', '👔', '其他'),
      createItem('饮用水/运动饮料', '💧', '补给'),
      createItem('香蕉/能量棒', '🍌', '补给'),
      createItem('手机', '📱', '电子'),
      createItem('充电宝', '🔋', '电子'),
      createItem('纸巾/湿巾', '🧻', '其他'),
      createItem('洗发水/沐浴露', '🧴', '其他'),
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约40分钟', risk: 'low', description: '准时可靠' },
      { type: '公交', icon: '🚌', duration: '约55分钟', risk: 'medium', description: '可能堵车' },
      { type: '打车', icon: '🚕', duration: '约25分钟', risk: 'low', description: '携带装备方便' },
      { type: '自驾', icon: '🚗', duration: '约20分钟', risk: 'low', description: '有停车场，方便' },
    ],
    tips: [
      '运动前做好热身，避免拉伤',
      '运动中注意补充水分',
      '运动后做好拉伸放松',
      '记得带换洗衣物，运动后可洗澡',
    ],
  },
  '桌游': {
    items: [
      createItem('手机+充电宝', '📱', '电子'),
      createItem('身份证', '🪪', '证件'),
      createItem('口罩（可选）', '😷', '防护'),
      createItem('纸巾', '🧻', '其他'),
      createItem('口香糖/薄荷糖', '🍬', '其他'),
      createItem('雨伞（看天气）', '☂️', '其他'),
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约35分钟', risk: 'low', description: '推荐，不堵车' },
      { type: '公交', icon: '🚌', duration: '约50分钟', risk: 'medium', description: '晚高峰可能堵车' },
      { type: '打车', icon: '🚕', duration: '约20分钟', risk: 'low', description: '方便快捷' },
      { type: '骑行', icon: '🚲', duration: '约25分钟', risk: 'medium', description: '锻炼身体，注意安全' },
    ],
    tips: [
      '提前了解桌游规则，快速上手',
      '桌游吧一般有饮料，可自带水杯',
      '玩到深夜注意安全，结伴回家',
      '保持手机电量充足',
    ],
  },
  '聚餐': {
    items: [
      createItem('手机+充电宝', '📱', '电子'),
      createItem('身份证', '🪪', '证件'),
      createItem('口罩（可选）', '😷', '防护'),
      createItem('纸巾/湿巾', '🧻', '其他'),
      createItem('口香糖/薄荷糖', '🍬', '其他'),
      createItem('雨伞（看天气）', '☂️', '其他'),
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约40分钟', risk: 'low', description: '不堵车，推荐' },
      { type: '公交', icon: '🚌', duration: '约55分钟', risk: 'high', description: '晚高峰大概率堵车' },
      { type: '打车', icon: '🚕', duration: '约25分钟', risk: 'medium', description: '晚高峰难打车' },
      { type: '骑行', icon: '🚲', duration: '约30分钟', risk: 'medium', description: '注意交通安全' },
    ],
    tips: [
      '聚餐一般AA制，带好手机支付',
      '如有忌口提前告知组织者',
      '适量饮酒，切勿贪杯',
      '饭后注意安全回家',
    ],
  },
  '探店': {
    items: [
      createItem('手机+充电宝', '📱', '电子'),
      createItem('相机（可选）', '📷', '电子'),
      createItem('口罩（可选）', '😷', '防护'),
      createItem('纸巾/湿巾', '🧻', '其他'),
      createItem('口香糖/薄荷糖', '🍬', '其他'),
      createItem('雨伞（看天气）', '☂️', '其他'),
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约35分钟', risk: 'low', description: '推荐，商圈附近都有地铁' },
      { type: '公交', icon: '🚌', duration: '约50分钟', risk: 'medium', description: '可能堵车' },
      { type: '打车', icon: '🚕', duration: '约20分钟', risk: 'medium', description: '商圈附近打车方便' },
      { type: '步行', icon: '🚶', duration: '约15分钟', risk: 'low', description: '住得近可步行' },
    ],
    tips: [
      '提前了解店铺营业时间',
      '网红店可能需要排队，早点到',
      '拍照注意礼貌，不要影响其他顾客',
      '探店后可以写评价分享',
    ],
  },
  '其他': {
    items: [
      createItem('手机+充电宝', '📱', '电子'),
      createItem('身份证', '🪪', '证件'),
      createItem('口罩（可选）', '😷', '防护'),
      createItem('纸巾', '🧻', '其他'),
      createItem('雨伞（看天气）', '☂️', '其他'),
    ],
    transportOptions: [
      { type: '地铁', icon: '🚇', duration: '约40分钟', risk: 'low', description: '准时可靠' },
      { type: '公交', icon: '🚌', duration: '约55分钟', risk: 'medium', description: '可能堵车' },
      { type: '打车', icon: '🚕', duration: '约25分钟', risk: 'low', description: '方便快捷' },
    ],
    tips: [
      '提前了解活动详情',
      '准时到达集合地点',
      '保持手机畅通',
      '有问题及时联系组织者',
    ],
  },
}

export function getChecklistByType(type: string): ActivityChecklist {
  return typeChecklistConfig[type] || typeChecklistConfig['其他']
}

export function cloneChecklistItems(items: ChecklistItem[]): ChecklistItem[] {
  return items.map(item => ({ ...item, checked: false }))
}
