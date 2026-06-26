<template>
  <div class="editor-page">
    <div class="container page">
      <div class="row">
        <div class="col-md-10 offset-md-1 col-xs-12">
          <form @submit.prevent="onSubmit">
            <fieldset class="form-group">
              <input
                type="text"
                class="form-control form-control-lg"
                aria-label="Title"
                v-model="form.title"
                placeholder="Article Title"
              >
            </fieldset>
            <fieldset class="form-group">
              <input
                type="text"
                class="form-control form-control-lg"
                aria-label="Description"
                v-model="form.description"
                placeholder="What's this article about?"
              >
            </fieldset>

            <div class="row">
              <div class="col-md-6">
                <fieldset class="form-group">
                  <label><strong>Artikel-Inhalt (Markdown)</strong></label>
                  <textarea
                    class="form-control"
                    aria-label="Body"
                    v-model="form.body"
                    placeholder="Write your article (in markdown)"
                    :rows="12"
                  />
                </fieldset>
              </div>
              <div class="col-md-6">
                <fieldset class="form-group">
                  <label><strong>Live-Vorschau (WYSIWYG)</strong></label>
                  <div
                    id="article-content"
                    class="form-control article-content"
                    v-html="renderedBody"
                    style="min-height: 290px; height: auto; overflow-y: auto; background-color: #fafafa; border: 1px solid #ccc; padding: 10px; border-radius: 4px;"
                  />
                </fieldset>
              </div>
            </div>

            <fieldset class="form-group" v-if="slug">
              <label><strong>Artikelbild hochladen</strong></label>
              <input
                type="file"
                class="form-control"
                accept="image/*"
                @change="onImageUpload"
                :disabled="isUploading"
              />
              <div v-if="isUploading" class="text-muted mt-1">Bild wird hochgeladen...</div>
              <div v-if="uploadError" class="text-danger mt-1">{{ uploadError }}</div>

              <div v-if="uploadedImages.length > 0" class="mt-3">
                <label><strong>Verfügbare Bild-Links für diesen Artikel:</strong></label>
                <div class="list-group">
                  <div 
                    v-for="imgUrl in uploadedImages" 
                    :key="imgUrl" 
                    class="list-group-item"
                    style="padding: 10px; background: #fff; border: 1px solid #ddd; margin-bottom: 5px; border-radius: 4px; display: flex; justify-content: space-between; align-items: center;"
                  >
                    <span style="font-family: monospace; font-size: 0.9em; word-break: break-all;">
                      ![alt text]({{ imgUrl }} "Title")
                    </span>
                    <div>
                      <button 
                        type="button" 
                        class="btn btn-sm btn-outline-primary" 
                        @click="copyToClipboard(imgUrl)"
                      >
                        Kopieren
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </fieldset>
            
            <fieldset class="form-group" v-else>
              <div class="alert alert-info" style="padding: 10px; background-color: #e8f4fd; color: #2196f3; border-radius: 4px;">
                Hinweis: Bilder können hochgeladen und verwaltet werden, sobald der Artikel zum ersten Mal veröffentlicht wurde (Slug vorhanden ist).
              </div>
            </fieldset>

            <fieldset class="form-group">
              <input
                type="text"
                class="form-control"
                aria-label="Tags"
                v-model="newTag"
                placeholder="Enter tags"
                @change="addTag"
                @keypress.enter.prevent="addTag"
              >
              <div class="tag-list">
                <span
                  v-for="tag in form.tagList"
                  :key="tag"
                  class="tag-default tag-pill"
                >
                  <i
                    class="ion-close-round"
                    role="button"
                    :aria-label="`Delete tag: ${tag}`"
                    tabindex="0"
                    @click="removeTag(tag)"
                    @keypress.enter="removeTag(tag)"
                  />
                  {{ tag }}
                </span>
              </div>
            </fieldset>
            <button
              type="submit"
              class="btn btn-lg pull-xs-right btn-primary"
              :disabled="!(form.title && form.description && form.body)"
            >
              Publish Article
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from 'src/services'
import type { Article } from 'src/services/api'
import renderMarkdown from 'src/plugins/marked'
import { CONFIG } from 'src/config'
import { useUserStore } from 'src/store/user'

interface FormState {
  title: string
  description: string
  body: string
  tagList: string[]
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const slug = computed<string>(() => route.params.slug as string)

const form: FormState = reactive({
  title: '',
  description: '',
  body: '',
  tagList: [],
})

const newTag = ref<string>('')
function addTag() {
  form.tagList.push(newTag.value.trim())
  newTag.value = ''
}
function removeTag(tag: string) {
  form.tagList = form.tagList.filter(t => t !== tag)
}

const uploadedImages = ref<string[]>([])
const isUploading = ref<boolean>(false)
const uploadError = ref<string>('')

const renderedBody = computed(() => renderMarkdown(form.body))

async function fetchArticle(slug: string) {
  const article = await api.articles.getArticle(slug).then(res => res.data.article)

  form.title = article.title
  form.description = article.description
  form.body = article.body
  form.tagList = article.tagList

  // Lädt die Bilder-Liste direkt über die API-Antwort, die im Backend über den ArticlesMapper befüllt wird
  const backendImages = (article as any).images || (article as any).Images
  if (backendImages && Array.isArray(backendImages)) {
    uploadedImages.value = [...backendImages]
  }
}

onMounted(async () => {
  if (slug.value)
    await fetchArticle(slug.value)
})

async function onImageUpload(event: Event) {
  const target = event.target as HTMLInputElement
  if (!target.files || target.files.length === 0) return

  const file = target.files[0]
  const formData = new FormData()
  formData.append('file', file)

  try {
    uploadError.value = ''
    isUploading.value = true

    const url = `${CONFIG.API_HOST}/api/images/articles/${slug.value}`

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': `Token ${userStore.user?.token || ''}`
      },
      body: formData
    })

    const result = await response.json()

    if (!response.ok) {
      throw new Error(result?.message || `Upload fehlgeschlagen (Status ${response.status})`)
    }

    if (result && result.image) {
      uploadedImages.value.push(result.image)
    }
  } catch (err: any) {
    uploadError.value = err.message || 'Fehler beim Hochladen des Bildes.'
  } finally {
    isUploading.value = false
    target.value = ''
  }
}

function copyToClipboard(url: string) {
  navigator.clipboard.writeText(`![alt text](${url} "Title")`)
}

async function onSubmit() {
  let article: Article
  if (slug.value)
    article = await api.articles.updateArticle(slug.value, { article: form }).then(res => res.data.article)
  else
    article = await api.articles.createArticle({ article: form }).then(res => res.data.article)

  return router.push({ name: 'article', params: { slug: article.slug } })
}
</script>