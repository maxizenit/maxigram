import { test, expect } from '@playwright/test'
import { login, registerUser } from './helpers'

const EMAIL = 'e2e-feed@maxigram.dev'

test.beforeAll(async () => {
  await registerUser(EMAIL)
})

test('logs in via OIDC and publishes a post', async ({ page }) => {
  await login(page, EMAIL)

  const text = `e2e пост ${Date.now()}`
  await page.getByLabel('Новый пост').fill(text)
  await page.getByRole('button', { name: 'Опубликовать' }).click()

  await expect(page.getByText(text)).toBeVisible()
})
