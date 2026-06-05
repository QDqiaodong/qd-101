import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import Home from '@/pages/Home.vue'
import Publish from '@/pages/Publish.vue'
import ActivityDetail from '@/pages/ActivityDetail.vue'
import Profile from '@/pages/Profile.vue'
import Match from '@/pages/Match.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'home',
    component: Home,
  },
  {
    path: '/match',
    name: 'match',
    component: Match,
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
