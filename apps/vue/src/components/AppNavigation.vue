<template>
  <nav class="navbar navbar-light">
    <div class="container">
      <AppLink
        class="navbar-brand"
        name="global-feed"
      >
        conduit
      </AppLink>

      <ul class="nav navbar-nav pull-xs-right">
        <li
          v-if="isAuthorized"
          class="nav-item"
          style="display: flex; align-items: center;"
        >
          <form
            @submit.prevent="onSearch"
            style="display: flex; flex-wrap: nowrap; align-items: center; gap: 4px;"
          >
            <input
              v-model="searchInput"
              type="text"
              class="form-control form-control-sm"
              placeholder="Search articles..."
              aria-label="Search articles"
              style="width: 160px;"
            >
            <button
              type="submit"
              class="btn btn-sm btn-outline-secondary"
            >
              <i class="ion-search" />
            </button>
          </form>
        </li>
        <li
          v-for="link in navLinks"
          :key="link.name"
          class="nav-item"
        >
          <AppLink
            class="nav-link"
            :aria-label="link.title"
            :name="link.name"
            active-class="active"
            :params="link.params"
          >
            <i
              v-if="link.icon"
              :class="link.icon"
            />
            {{ link.title }}
          </AppLink>
        </li>
      </ul>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { RouteParams } from 'vue-router'
import { storeToRefs } from 'pinia'
import type { AppRouteNames } from 'src/router'
import { router } from 'src/router'
import { useUserStore } from 'src/store/user'

interface NavLink {
  name: AppRouteNames
  params?: Partial<RouteParams>
  title: string
  icon?: string
  display: 'all' | 'anonym' | 'authorized'
}

const { user, isAuthorized } = storeToRefs(useUserStore())

const username = computed(() => user.value?.username)
const displayStatus = computed(() => username.value ? 'authorized' : 'anonym')

const searchInput = ref('')

function onSearch() {
  const input = searchInput.value.trim()
  if (!input) return
  const q = input.replace(/\s+/g, '+')
  router.push(`/search?q=${q}`)
  searchInput.value = ''
}

const allNavLinks = computed<NavLink[]>(() => [
  {
    name: 'global-feed',
    title: 'Home',
    display: 'all',
  },
  {
    name: 'login',
    title: 'Sign in',
    display: 'anonym',
  },
  {
    name: 'register',
    title: 'Sign up',
    display: 'anonym',
  },
  {
    name: 'create-article',
    title: 'New Post',
    display: 'authorized',
    icon: 'ion-compose',
  },
  {
    name: 'settings',
    title: 'Settings',
    display: 'authorized',
    icon: 'ion-gear-a',
  },
  {
    name: 'profile',
    params: { username: username.value },
    title: username.value || '',
    display: 'authorized',
  },
])

const navLinks = computed(() => allNavLinks.value.filter(
  l => l.display === displayStatus.value || l.display === 'all',
))
</script>
