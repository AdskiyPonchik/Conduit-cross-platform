import type { RouteParams, RouteRecordRaw } from 'vue-router'
import { createRouter, createWebHashHistory } from 'vue-router'
import { isAuthorized, userStorage } from './store/user'

export type AppRouteNames =
  | 'global-feed'
  | 'my-feed'
  | 'tag'
  | 'search'
  | 'article'
  | 'create-article'
  | 'edit-article'
  | 'login'
  | 'register'
  | 'profile'
  | 'profile-favorites'
  | 'settings'
  | 'admin'

export const routes: RouteRecordRaw[] = [
  {
    name: 'global-feed',
    path: '/',
    component: () => import('./pages/Home.vue'),
  },
  {
    name: 'my-feed',
    path: '/my-feeds',
    component: () => import('./pages/Home.vue'),
  },
  {
    name: 'tag',
    path: '/tag/:tag',
    component: () => import('./pages/Home.vue'),
  },
  {
    name: 'search',
    path: '/search',
    component: () => import('./pages/Home.vue'),
    beforeEnter: () => isAuthorized() || { name: 'login' },
  },
  {
    name: 'article',
    path: '/article/:slug',
    component: () => import('./pages/Article.vue'),
  },
  {
    name: 'edit-article',
    path: '/article/:slug/edit',
    component: () => import('./pages/EditArticle.vue'),
  },
  {
    name: 'create-article',
    path: '/article/create',
    component: () => import('./pages/EditArticle.vue'),
  },
  {
    name: 'login',
    path: '/login',
    component: () => import('./pages/Login.vue'),
    beforeEnter: () => !isAuthorized(),
  },
  {
    name: 'register',
    path: '/register',
    component: () => import('./pages/Register.vue'),
    beforeEnter: () => !isAuthorized(),
  },
  {
    name: 'profile',
    path: '/profile/:username',
    component: () => import('./pages/Profile.vue'),
  },
  {
    name: 'profile-favorites',
    path: '/profile/:username/favorites',
    component: () => import('./pages/Profile.vue'),
  },
  {
    name: 'settings',
    path: '/settings',
    component: () => import('./pages/Settings.vue'),
  },
  {
    name: 'admin',
    path: '/admin',
    component: () => import('./pages/Admin.vue'),
    beforeEnter: () => (isAuthorized() && (userStorage.get() as any)?.role === 'Admin') || { name: 'global-feed' },
  },
]
export const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

export async function routerPush(name: AppRouteNames, params?: RouteParams): ReturnType<typeof router.push> {
  return params === undefined
    ? await router.push({ name })
    : await router.push({ name, params })
}