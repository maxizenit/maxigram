import { expect, request, type Page } from '@playwright/test'

export const API = 'http://localhost:8080'
export const PASS = 'Password123!'

export async function registerUser(email: string) {
  const ctx = await request.newContext()
  await ctx.post(`${API}/api/identity/registrations`, {
    data: { email, password: PASS },
    failOnStatusCode: false,
  })
  await ctx.dispose()
}

/** Logs in through the real OIDC Authorization Code + PKCE flow and waits for the feed. */
export async function login(page: Page, email: string) {
  await page.goto('/')
  await page.getByRole('button', { name: 'Войти' }).click()
  await page.locator('#username').fill(email)
  await page.locator('#password').fill(PASS)
  await page.locator('button[type="submit"]').click()
  await expect(page.getByRole('heading', { name: 'Лента' })).toBeVisible({ timeout: 15_000 })
}

/** Fills the current user's profile. Identical attributes across users -> high matching similarity. */
export async function ensureProfile(page: Page, firstName: string) {
  await page.goto('/profile')
  await expect(page.getByRole('heading', { name: 'Профиль' })).toBeVisible()
  await page.getByLabel('Имя').fill(firstName)
  await page.getByLabel('Фамилия').fill('Тестов')
  await page.getByLabel('Дата рождения').fill('2000-01-01')
  await page.getByLabel('Таймзона').fill('UTC')
  const interests = page.locator('fieldset input[type="checkbox"]')
  await interests.nth(0).check()
  await interests.nth(1).check()
  await page.getByRole('button', { name: 'Сохранить' }).click()
  await expect(page.getByRole('status')).toHaveText('Профиль сохранён')
}
