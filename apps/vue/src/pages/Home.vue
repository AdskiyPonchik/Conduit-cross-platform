<template>
  <div class="home-page">
    <div class="banner">
      <div class="container">
        <h1 class="logo-font">conduit</h1>
        <p>A place to share your knowledge.</p>
      </div>
    </div>

    <div class="container page">
      <div class="row">
        <div class="col-md-9">
          <div v-if="isAuthorized" class="search-container" style="margin-bottom: 1rem;">
            <form @submit.prevent="executeSearch" style="display: flex; gap: 8px;">
              <input
                v-model="searchInput"
                type="text"
                class="form-control"
                placeholder="Artikel durchsuchen (Begriffe mit Leerzeichen trennen)..."
              />
              <button type="submit" class="btn btn-primary btn-sm">Suchen</button>
            </form>
          </div>

          <Suspense>
            <ArticlesList
              use-global-feed
              use-my-feed
              use-tag-feed
              :use-search-feed="true"
            />
            <template #fallback>
              Articles are downloading...
            </template>
          </Suspense>
        </div>

        <div class="col-md-3">
          <div class="sidebar">
            <Suspense>
              <PopularTags />
              <template #fallback>
                Popular tags are downloading...
              </template>
            </Suspense>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useUserStore } from 'src/store/user'
import ArticlesList from 'src/components/ArticlesList.vue'
import PopularTags from 'src/components/PopularTags.vue'

const router = useRouter()
const route = useRoute()
const { isAuthorized } = storeToRefs(useUserStore())

// Lokaler State für das Eingabefeld, synchronisiert mit URL
const searchInput = ref((route.query.search as string) || '')

watch(() => route.query.search, (newSearch) => {
    searchInput.value = (newSearch as string) || ''
})

function executeSearch() {
  if (searchInput.value.trim()) {
    router.push({ query: { ...route.query, search: searchInput.value.trim() } })
  } else {
    const query = { ...route.query }
    delete query.search
    router.push({ query })
  }
}
</script>
