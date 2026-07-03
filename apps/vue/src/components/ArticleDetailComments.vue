<template>
  <div class="card">
    <div class="card-block">
      <p class="card-text">
        {{ comment.body }}
      </p>
    </div>
    <div class="card-footer">
      <AppLink
        class="comment-author"
        name="profile"
        :params="{ username: comment.author.username }"
      >
        <img
          class="comment-author-img"
          :alt="comment.author.username"
          :src="comment.author.image"
        >
      </AppLink>
      &nbsp;
      <AppLink
        class="comment-author"
        name="profile"
        :params="{ username: comment.author.username }"
      >
        {{ comment.author.username }}
      </AppLink>
      <span class="date-posted">{{ (new Date(comment.createdAt)).toLocaleDateString('en-US', { month: 'long', day: 'numeric' }) }}</span>
      <span class="mod-options">
        <i v-if="showRemove" class="ion-trash-a" @click="$emit('remove')" />
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Comment } from 'src/services/api'
import { useUserStore } from 'src/store/user'

interface Props {
  comment: Comment
}

defineEmits(['remove'])
const props = defineProps<Props>()
const userStore = useUserStore()

const showRemove = computed(() => {
  if (!userStore.isAuthorized) return false
  const currentUsername = userStore.user?.username
  const currentUserRole = userStore.user?.role
  return currentUsername === props.comment.author.username ||
         currentUserRole === 'Moderator' ||
         currentUserRole === 'Admin'
})
</script>