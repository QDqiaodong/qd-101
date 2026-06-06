import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Home from '@/pages/Home.vue'
import Publish from '@/pages/Publish.vue'
import ActivityDetail from '@/pages/ActivityDetail.vue'
import Profile from '@/pages/Profile.vue'
import Match from '@/pages/Match.vue'
import NightLife from '@/pages/NightLife.vue'
import CreatorHall from '@/pages/CreatorHall.vue'
import CreatorDetail from '@/pages/CreatorDetail.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: Home,
  },
  {
    path: '/nightlife',
    name: 'nightlife',
    component: NightLife,
  },
  {
    path: '/match',
    name: 'match',
    component: Match,
  },
  {
    path: '/creators',
    name: 'creators',
    component: CreatorHall,
  },
  {
    path: '/creator/:id',
    name: 'creator',
    component: CreatorDetail,
  },
  {
    path: '/publish',
    name: 'publish',
    component: Publish,
  },
  {
    path: '/activity/:id',
    name: 'activity',
    component: ActivityDetail,
  },
  {
    path: '/profile',
    name: 'profile',
    component: Profile,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
