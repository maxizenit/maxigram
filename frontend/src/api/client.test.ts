import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiFetch, ApiError, setLockedHandler, setTokenProvider, setUnauthorizedHandler } from './client'

describe('apiFetch', () => {
  afterEach(() => {
    vi.restoreAllMocks()
    setUnauthorizedHandler(null)
    setLockedHandler(null)
  })

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

  it('notifies the unauthorized handler on 401 (and still throws)', async () => {
    setTokenProvider(() => 'stale-token')
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('unauthorized', { status: 401 }))
    const onUnauthorized = vi.fn()
    setUnauthorizedHandler(onUnauthorized)

    await expect(apiFetch('/api/test')).rejects.toBeInstanceOf(ApiError)
    expect(onUnauthorized).toHaveBeenCalledTimes(1)
  })

  it('does not call the unauthorized handler on 401 when no token was attached', async () => {
    setTokenProvider(() => null)
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('unauthorized', { status: 401 }))
    const onUnauthorized = vi.fn()
    setUnauthorizedHandler(onUnauthorized)

    await expect(apiFetch('/api/test')).rejects.toBeInstanceOf(ApiError)
    expect(onUnauthorized).not.toHaveBeenCalled()
  })

  it('does not call the unauthorized handler for other errors', async () => {
    setTokenProvider(() => 'test-token')
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('boom', { status: 500 }))
    const onUnauthorized = vi.fn()
    setUnauthorizedHandler(onUnauthorized)

    await expect(apiFetch('/api/test')).rejects.toBeInstanceOf(ApiError)
    expect(onUnauthorized).not.toHaveBeenCalled()
  })

  it('notifies the locked handler on 423 (active self-restraint)', async () => {
    setTokenProvider(() => 'test-token')
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(new Response('{"error":"locked"}', { status: 423 }))
    const onLocked = vi.fn()
    setLockedHandler(onLocked)

    await expect(apiFetch('/api/test')).rejects.toBeInstanceOf(ApiError)
    expect(onLocked).toHaveBeenCalledTimes(1)
  })
})
