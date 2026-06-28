import { test, expect } from '@playwright/test'
import { ensureProfile, login, registerUser } from './helpers'

// Fresh users per run so neither has leftover chats / queue entries.
const stamp = Date.now()
const ALICE = `e2e-alice-${stamp}@maxigram.dev`
const BOB = `e2e-bob-${stamp}@maxigram.dev`

test.beforeAll(async () => {
  await registerUser(ALICE)
  await registerUser(BOB)
})

test('two users match anonymously and exchange a message in real time', async ({ browser }) => {
  const ctxA = await browser.newContext()
  const ctxB = await browser.newContext()
  const pageA = await ctxA.newPage()
  const pageB = await ctxB.newPage()

  await login(pageA, ALICE)
  await ensureProfile(pageA, 'Алиса')
  await login(pageB, BOB)
  await ensureProfile(pageB, 'Боб')

  // Bob enqueues first; Alice then matches with him.
  await pageB.goto('/chats')
  await pageB.getByRole('button', { name: 'Найти анонимного собеседника' }).click()
  await expect(pageB.getByText(/в очереди/)).toBeVisible()

  await pageA.goto('/chats')
  await pageA.getByRole('button', { name: 'Найти анонимного собеседника' }).click()
  await expect(pageA.getByRole('heading', { name: 'Анонимный чат' })).toBeVisible({ timeout: 15_000 })

  // Bob opens the now-existing anonymous chat and stays on it (STOMP subscription active).
  await pageB.goto('/chats')
  await pageB.getByRole('link', { name: /Аноним/ }).first().click()
  await expect(pageB.getByRole('heading', { name: 'Анонимный чат' })).toBeVisible()
  await pageB.waitForTimeout(1500)

  // Alice sends; Bob must receive it live, without reloading.
  const message = `привет ${Date.now()}`
  await pageA.getByLabel('Сообщение').fill(message)
  await pageA.getByRole('button', { name: 'Отправить' }).click()
  await expect(pageA.getByText(message)).toBeVisible()

  await expect(pageB.getByText(message)).toBeVisible({ timeout: 15_000 })

  await ctxA.close()
  await ctxB.close()
})
