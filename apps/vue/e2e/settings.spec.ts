import { test, expect } from '@playwright/test'

test.describe('Settings E2E Tests - Password Bug & Workflow Verification', () => {
  test.beforeEach(async ({ page }) => {
    // 1. Init-Skript registrieren: Wird ausgeführt, BEVOR die Vue-App startet!
    await page.addInitScript(() => {
      window.localStorage.setItem('user', JSON.stringify({
        username: 'FDMP',
        email: 'fdmp@example.com',
        token: 'mocked-jwt-token',
        bio: 'Developer',
        image: ''
      }))
    })

    // 2. Direkt die Einstellungs-Seite ansteuern
    await page.goto('http://localhost:4173/#/settings')
  })

  test('should update user profile successfully without sending an empty password', async ({ page }) => {
    let submittedPayload: any = null

    await page.route('**/api/user', async (route) => {
      if (route.request().method() === 'PUT') {
        submittedPayload = route.request().postDataJSON()
        
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            user: {
              username: 'FDMP Updated',
              email: 'fdmp@example.com',
              bio: 'Developer',
              image: '',
              token: 'mocked-jwt-token'
            }
          })
        })
      } else {
        await route.continue()
      }
    })

    // Eingabefeld für den Namen modifizieren
    const nameInput = page.locator('input[placeholder="Your name"]')
    await nameInput.fill('FDMP Updated')

    // Verifizieren, dass das Passwortfeld standardmäßig leer ist
    const passwordInput = page.locator('input[placeholder="New password"]')
    await expect(passwordInput).toHaveValue('')

    // Formular absenden
    await page.click('button:has-text("Update Settings")')

    // Überprüfen, ob die Weiterleitung angetriggert wurde
    await expect(page).toHaveURL(/\/profile\/FDMP/)

    // Kritische Assertions: Payload validieren
    expect(submittedPayload).not.toBeNull()
    expect(submittedPayload.user.username).toBe('FDMP Updated')
    
    // Alte Logik password= "" -> Test schlägt hier fehl!
    // Neue Logik password = undefined -> Test besteht!
    expect(submittedPayload.user.password).toBeUndefined()
  })
})