<template>
  <div class="settings-page">
    <div class="container page">
      <div class="row">
        <div class="col-md-6 offset-md-3 col-xs-12">
          <h1 class="text-xs-center">
            Your Settings
          </h1>

          <ul class="error-messages">
            <li v-for="(error, field) in errors" :key="field">
              {{ field }} {{ error ? error[0] : '' }}
            </li>
          </ul>

          <form @submit.prevent="onSubmit">
            <fieldset>
              <fieldset class="form-group">
                <label class="btn btn-sm btn-outline-secondary" style="display: block; text-align: center; cursor: pointer; margin-bottom: 10px;">
                  Select Local Profile Picture...
                  <input
                    type="file"
                    accept=".jpg,.jpeg,.png"
                    style="display: none;"
                    @change="onFileChange"
                  >
                </label>
                
                <div v-if="localFilePath" class="text-xs-center text-muted" style="margin-bottom: 10px; font-size: 0.85rem;">
                  Selected file: <code>{{ localFilePath }}</code>
                </div>

                <input
                  type="text"
                  class="form-control"
                  aria-label="Avatar picture url"
                  v-model="form.image"
                  placeholder="URL of profile picture"
                >
              </fieldset>
              <fieldset class="form-group">
                <input
                  type="text"
                  class="form-control form-control-lg"
                  aria-label="Username"
                  v-model="form.username"
                  placeholder="Your name"
                >
              </fieldset>
              <fieldset class="form-group">
                <textarea
                  class="form-control form-control-lg"
                  aria-label="Bio"
                  v-model="form.bio"
                  placeholder="Short bio about you"
                  :rows="8"
                />
              </fieldset>
              <fieldset class="form-group">
                <input
                  type="email"
                  class="form-control form-control-lg"
                  aria-label="Email"
                  v-model="form.email"
                  placeholder="Email"
                >
              </fieldset>
              <fieldset class="form-group">
                <input
                  type="password"
                  class="form-control form-control-lg"
                  aria-label="New password"
                  v-model="form.password"
                  placeholder="New password"
                >
              </fieldset>
              <button
                type="submit"
                class="btn btn-lg btn-primary pull-xs-right"
                :disabled="isButtonDisabled"
              >
                Update Settings
              </button>
            </fieldset>
          </form>

          <hr>

          <button
            class="btn btn-outline-danger"
            aria-label="Logout"
            @click="onLogout"
          >
            Or click here to logout.
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { CONFIG } from 'src/config' // Importiert die Konfiguration für den korrekten API-Host
import { routerPush } from 'src/router'
import { api, isFetchError } from 'src/services'
import type { UpdateUser } from 'src/services/api'
import { useUserStore } from 'src/store/user'

const form: UpdateUser = reactive({})

const userStore = useUserStore()
const errors = ref()

// REAKTIVITÄTSVARIABLE: Speichert das rohe, binäre Datei-Objekt (File), wenn ein Nutzer ein Bild wählt.
// Wird im Code verwendet, um die Daten per Multipart-Formular an das Backend zu senden.
const selectedFile = ref<File | null>(null)
// REAKTIVITÄTSVARIABLE: Speichert rein den Dateinamen (z. B. 'avatar.png') als String.
// Dient ausschließlich dazu, dem Benutzer im HTML-Template visuelles Feedback zu geben.  
const localFilePath = ref<string>('')

function onFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  // Prüfen, ob überhaupt eine Datei ausgewählt wurde
  if (target.files && target.files.length > 0) {
    const file = target.files[0]
    // Zuweisung an die reaktiven Variablen. 
    // .value ist zwingend erforderlich, da es sich um 'ref'-Variablen handelt.
    selectedFile.value = file
    localFilePath.value = file.name
  }
}
async function onSubmit() {
  errors.value = {}

  try {

    // Aufgabe 4: Bild-Upload an die bestehende Backend-Schnittstelle
    if (selectedFile.value) {
      const formData = new FormData()
      // Der Parameter-Name MUSS exakt 'file' heißen, passend zu IFormFile file im C#-Backend
      formData.append('file', selectedFile.value)

      // Nutzt CONFIG.API_HOST, um das richtige Backend-Ziel anzusprechen
      const uploadResponse = await fetch(`${CONFIG.API_HOST}/api/images`, {
        method: 'POST',
        headers: {
          // Nutzt das Token-Schema der Anwendung zur Authentifizierung
          Authorization: `Token ${userStore.user?.token || ''}`,
        },
        body: formData,
      }).then(res => {
        if (!res.ok) throw new Error('Profile picture upload failed.')
        return res.json()
      })

      // Weist der Formular-URL den vom Backend generierten Bildpfad (data.image) zu
      if (uploadResponse && uploadResponse.image) {
        form.image = uploadResponse.image
      }
    }
    // Fehlerbehebung Passwort-Bug (Ausschluss von leeren Strings bei 'password')
    const filteredForm = Object.entries(form).reduce((acc, [k, v]) => {
      if (v === null || (k === 'password' && v === '')) {
        return acc
      }
      return Object.assign(acc, { [k]: v })
    }, {})
    const userData = await api.user.updateCurrentUser({ user: filteredForm }).then(res => res.data.user)
    userStore.updateUser(userData)
    await routerPush('profile', { username: userData.username })
  }
  catch (error) {
    if (isFetchError(error))
      errors.value = error.error?.errors
  }
}

async function onLogout() {
  userStore.updateUser(null)
  await routerPush('global-feed')
}

onMounted(async () => {
  if (!userStore.isAuthorized)
    return await routerPush('login')

  form.image = userStore.user?.image
  form.username = userStore.user?.username
  form.bio = userStore.user?.bio
  form.email = userStore.user?.email
  form.password = ''
})

const isButtonDisabled = computed(() =>
  form.image === userStore.user?.image
  && form.username === userStore.user?.username
  && form.bio === userStore.user?.bio
  && form.email === userStore.user?.email
  && !form.password
  && !selectedFile.value, // Aktiviert den Button, sobald eine lokale Datei gewählt wurde
)
</script>
