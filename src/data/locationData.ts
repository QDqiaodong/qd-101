export interface BusinessDistrict {
  id: string
  name: string
  city: string
  type: '商圈' | '片区'
  convenienceScore: number
  landmarks: string[]
}

export interface ConvenienceOption {
  value: number
  label: string
  description: string
}

export const businessDistricts: BusinessDistrict[] = [
  { id: 'cbd-bj', name: 'CBD商圈', city: '北京', type: '商圈', convenienceScore: 95, landmarks: ['国贸', 'SKP', '世贸天阶'] },
  { id: 'wangjing-bj', name: '望京', city: '北京', type: '商圈', convenienceScore: 88, landmarks: ['望京SOHO', '合生麒麟社', '望京西园'] },
  { id: 'sanlitun-bj', name: '三里屯', city: '北京', type: '商圈', convenienceScore: 92, landmarks: ['太古里', '酒吧街', '工体'] },
  { id: 'chaoyangpark-bj', name: '朝阳公园', city: '北京', type: '片区', convenienceScore: 85, landmarks: ['朝阳公园', '蓝色港湾', '枣营'] },
  { id: 'wudaokou-bj', name: '五道口', city: '北京', type: '商圈', convenienceScore: 82, landmarks: ['清华', '北大', '中关村'] },
  { id: 'guijie-bj', name: '簋街', city: '北京', type: '商圈', convenienceScore: 90, landmarks: ['东直门', '北新桥', '雍和宫'] },
  { id: 'aosen-bj', name: '奥森', city: '北京', type: '片区', convenienceScore: 78, landmarks: ['奥林匹克公园', '鸟巢', '水立方'] },
  { id: 'xiangshan-bj', name: '香山', city: '北京', type: '片区', convenienceScore: 65, landmarks: ['香山公园', '植物园', '卧佛寺'] },
  
  { id: 'lujiazui-sh', name: '陆家嘴', city: '上海', type: '商圈', convenienceScore: 96, landmarks: ['东方明珠', '环球金融中心', '正大广场'] },
  { id: 'xintiandi-sh', name: '新天地', city: '上海', type: '商圈', convenienceScore: 93, landmarks: ['石库门', '淮海路', '人民广场'] },
  { id: 'jingan-sh', name: '静安寺', city: '上海', type: '商圈', convenienceScore: 94, landmarks: ['久光', '芮欧', '静安嘉里'] },
  { id: 'xujiahui-sh', name: '徐家汇', city: '上海', type: '商圈', convenienceScore: 90, landmarks: ['港汇', '美罗城', '徐家汇公园'] },
  { id: 'gubei-sh', name: '古北', city: '上海', type: '片区', convenienceScore: 85, landmarks: ['黄金城道', '古北家乐福', '耀中'] },
  
  { id: 'tianhe-gz', name: '天河', city: '广州', type: '商圈', convenienceScore: 95, landmarks: ['天河城', '正佳', '太古汇'] },
  { id: 'yuexiu-gz', name: '越秀', city: '广州', type: '商圈', convenienceScore: 88, landmarks: ['北京路', '中华广场', '东山口'] },
  { id: 'liwan-gz', name: '荔湾', city: '广州', type: '商圈', convenienceScore: 86, landmarks: ['上下九', '沙面', '陈家祠'] },
  { id: 'pazhou-gz', name: '琶洲', city: '广州', type: '片区', convenienceScore: 80, landmarks: ['会展中心', '万胜围', '黄埔村'] },
  
  { id: 'nanshan-sz', name: '科技园', city: '深圳', type: '商圈', convenienceScore: 92, landmarks: ['腾讯大厦', '万象天地', '深圳湾'] },
  { id: 'futian-sz', name: '福田CBD', city: '深圳', type: '商圈', convenienceScore: 94, landmarks: ['会展中心', '购物公园', '连城天地'] },
  { id: 'luohu-sz', name: '罗湖', city: '深圳', type: '商圈', convenienceScore: 88, landmarks: ['东门', '国贸', '万象城'] },
  { id: 'shekou-sz', name: '蛇口', city: '深圳', type: '片区', convenienceScore: 85, landmarks: ['海上世界', '蛇口港', '南海意库'] },
  
  { id: 'westlake-hz', name: '西湖', city: '杭州', type: '商圈', convenienceScore: 93, landmarks: ['湖滨银泰', '龙翔桥', '西湖景区'] },
  { id: 'binjiang-hz', name: '滨江', city: '杭州', type: '片区', convenienceScore: 86, landmarks: ['阿里园区', '钱塘江', '星光大道'] },
  { id: 'xixi-hz', name: '西溪', city: '杭州', type: '片区', convenienceScore: 80, landmarks: ['西溪湿地', '淘宝城', '未来科技城'] },
  
  { id: 'jinji-suz', name: '金鸡湖', city: '苏州', type: '商圈', convenienceScore: 90, landmarks: ['东方之门', '久光', '诚品书店'] },
  { id: 'shilu-suz', name: '石路', city: '苏州', type: '商圈', convenienceScore: 85, landmarks: ['山塘街', '石路步行街', '留园'] },
  { id: 'suzhouyuan-suz', name: '苏州园区', city: '苏州', type: '片区', convenienceScore: 88, landmarks: ['独墅湖', '月亮湾', '白鹭园'] },
  
  { id: 'chunxi-cd', name: '春熙路', city: '成都', type: '商圈', convenienceScore: 95, landmarks: ['IFS', '太古里', '总府路'] },
  { id: 'jinli-cd', name: '锦里', city: '成都', type: '商圈', convenienceScore: 88, landmarks: ['武侯祠', '锦里古街', '洗面桥'] },
  { id: 'jiuyanqiao-cd', name: '九眼桥', city: '成都', type: '商圈', convenienceScore: 90, landmarks: ['兰桂坊', '九眼桥酒吧街', '合江亭'] },
]

export const convenienceOptions: ConvenienceOption[] = [
  { value: 90, label: '极便利', description: '地铁直达+商圈核心' },
  { value: 80, label: '很便利', description: '步行10分钟到地铁' },
  { value: 70, label: '较便利', description: '有公交直达' },
  { value: 0, label: '不限', description: '不限制便利程度' },
]

export function getDistrictsByCity(city: string): BusinessDistrict[] {
  return businessDistricts.filter(d => d.city === city)
}

export function getDistrictByName(name: string, city: string): BusinessDistrict | undefined {
  return businessDistricts.find(d => d.name === name && d.city === city)
}

export function matchDistrictByLocation(location: string, city: string): BusinessDistrict | null {
  const districts = getDistrictsByCity(city)
  for (const district of districts) {
    if (location.includes(district.name) || 
        district.landmarks.some(l => location.includes(l.split('')[0]))) {
      return district
    }
  }
  return null
}
