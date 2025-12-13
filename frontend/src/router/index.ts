import {createRouter, createWebHistory} from 'vue-router'
import HomeView from '../views/HomeView.vue'
import DashboardView from '@/views/DashboardView.vue'
import ForumView from '@/views/ForumView.vue'
import PartyView from "@/views/PartyView.vue";
import PartyDetailView from "@/views/PartyDetailView.vue";
import LoginView from '@/views/LoginView.vue'
import RegisterView from '@/views/RegisterView.vue'
import PageNotFound from '@/views/PageNotFound.vue'
import {useAuthStore} from '@/stores/auth'
import CompareView from '@/views/CompareView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    //political about page
    {
      path: '/political-about',
      name: 'political-about',
      component: () => import('../views/PoliticalAboutView.vue'),
    },
    {
      path: '/CompareView',
      name: 'compare-view',
      component: () => import('../views/CompareView.vue'),
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
    },
    {
      path: '/forum',
      name: 'forum',
      component: ForumView,
      meta: {requiresAuth: true},
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/forum/:id',
      name: 'forum-post',
      component: () => import('../views/ForumPostView.vue'),
      props: true,
    },
    {
      path: '/vergelijken',
      name: 'vergelijken',
      component: () => import('@/views/CompareView.vue'), // ✅ lazy-loaded
    },
    {
      path: '/vergelijken',
      name: 'vergelijken',
      component: CompareView,
    },
    {
      path: '/parties',
      name: 'parties',
      component: PartyView,
    },
    {
      path: '/parties/:id',
      name: 'party-detail',
      component: PartyDetailView,
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: PageNotFound,
    },
  ],
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({
      name: 'login',
      query: {redirect: to.fullPath},
    })
  } else if (to.meta.requiresAdmin && !authStore.isAdmin) {
    // Redirect non-admin users trying to access admin pages
    next({ name: 'home' })
  } else {
    next()
  }
})

export default router
