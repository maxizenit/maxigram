import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiFetch, ApiError, setTokenProvider } from './client'

describe('apiFetch', () => {
  afterEach(() => vi.restoreAllMocks())

  it('attaches the bearer token when present', async () => {
    setTokenProvider(() => 'test-token')
    const fetchMock = vi
      .spyOn(globalThis, 'fetch')
      .mockResolvedValue(new Response(JSON.stringify({ ok: true }), { status: 200 }))

    await apiFetch('/api/test')

    const headers = (fetchMock.mock.calls[0]![1] as RequestInit).headers as Headers
    expect(headers.get('Authorization')).toBe('Bearer test-token')
  })

  it('omits the header when there is no token', async () => {
    setTokenProvider(() => null)
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('[]', { status: 200 }))

    await apiFetch('/api/test')

    const headers = (fetchMock.mock.calls[0]![1] as RequestInit).headers as Headers
    expect(headers.has('Authorization')).toBe(false)
  })

  it('throws ApiError on a non-2xx response', async () => {
    setTokenProvider(() => 'test-token')
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('forbidden', { status: 403 }))

    await expect(apiFetch('/api/test')).rejects.toBeInstanceOf(ApiError)
  })

  it('returns undefined for 204', async () => {
    setTokenProvider(() => 'test-token')
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response(null, { status: 204 }))

    await expect(apiFetch('/api/test')).resolves.toBeUndefined()
  })
})
