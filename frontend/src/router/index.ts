import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/students',
      name: 'students',
      component: () => import('../views/StudentsView.vue'),
    },
    {
      path: '/tests',
      name: 'tests',
      component: () => import('../views/TestsView.vue'),
    },
    {
      path: '/tests/:id',
      name: 'test-detail',
      component: () => import('../views/TestDetailView.vue'),
    },
    {
      path: '/tests/:id/answers',
      name: 'test-answers',
      component: () => import('../views/TestAnswersView.vue'),
    },
    {
      path: '/tests/:id/feedback',
      name: 'test-feedback',
      component: () => import('../views/TestFeedbackView.vue'),
    },
  ],
})

export default router
