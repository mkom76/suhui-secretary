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
      path: '/students/:id',
      name: 'student-detail',
      component: () => import('../views/StudentDetailView.vue'),
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
    {
      path: '/homeworks',
      name: 'homeworks',
      component: () => import('../views/HomeworksView.vue'),
    },
    {
      path: '/academies',
      name: 'academies',
      component: () => import('../views/AcademiesView.vue'),
    },
    {
      path: '/academy-classes',
      name: 'academy-classes',
      component: () => import('../views/AcademyClassesView.vue'),
    },
  ],
})

export default router
