<template>
  <div class="auth-page">
    <div class="container page">
      <div class="row">
        <div class="col-md-6 offset-md-3 col-xs-12">
          <h1 class="text-xs-center">
            Sign up
          </h1>
          <p class="text-xs-center">
            <AppLink name="login">
              Have an account?
            </AppLink>
          </p>
          <ul class="error-messages">
            <li
              v-for="(error, field) in errors"
              :key="field"
            >
              {{ field }} {{ error ? error[0] : '' }}
            </li>
          </ul>
          <form
            ref="formRef"
            aria-label="Registration form"
            @submit.prevent="register"
          >
            <fieldset class="form-group">
              <input
                type="text"
                class="form-control form-control-lg"
                aria-label="Username"
                v-model="form.username"
                pattern="^[a-zA-Z0-9]+$"
                required
                placeholder="Your Name"
                @invalid="onInvalidUsername"
                @input="onInputUsername"
              >
            </fieldset>
            <fieldset class="form-group">
              <input
                type="email"
                class="form-control form-control-lg"
                aria-label="Email"
                v-model="form.email"
                required
                placeholder="Email"
              >
            </fieldset>
            <fieldset class="form-group">
              <input
                type="password"
                class="form-control form-control-lg"
                aria-label="Password"
                v-model="form.password"
                required
                :minLength="8"
                placeholder="Password"
              >
            </fieldset>
            <button
              type="submit"
              class="btn btn-lg btn-primary pull-xs-right"
              :disabled="!(form.email && form.username && form.password)"
            >
              Sign up
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { routerPush } from 'src/router'
import { api, isFetchError } from 'src/services'
import type { NewUser } from 'src/services/api'
import { useUserStore } from 'src/store/user'

const formRef = ref<HTMLFormElement | null>(null)
const form: NewUser = reactive({
  username: '',
  email: '',
  password: '',
})

const { updateUser } = useUserStore()

const errors = ref()

function onInvalidUsername(event: Event) {
  const target = event.target as HTMLInputElement
  // Überschreiben des Defaulttextes
  target.setCustomValidity('Der Benutzername darf nur aus Buchstaben und Zahlen bestehen. Keine Sonderzeichen erlaubt!')
}

function onInputUsername(event: Event) {
  const target = event.target as HTMLInputElement
  // Wenn der Nutzertippt, wird die Fehlermeldung wieder verworfen
  // Nur so weiß der Browser, dass er beim nächsten Klick den Text neu validieren darf!
  target.setCustomValidity('')
}

async function register() {
  errors.value = {}

  if (!formRef.value?.checkValidity())
    return

  try {
    const result = await api.users.createUser({ user: form })
    updateUser(result.data.user)
    await routerPush('global-feed')
  }
  catch (error) {
    if (isFetchError(error))
      errors.value = error.error?.errors
  }
}
</script>
