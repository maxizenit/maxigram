import { StrictMode } from 'react'
import { describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { CallbackPage } from './CallbackPage'
import { userManager } from '../auth/oidc'

vi.mock('../auth/oidc', () => ({
  userManager: { signinRedirectCallback: vi.fn().mockResolvedValue({}) },
}))

describe('CallbackPage', () => {
  it('completes the redirect callback exactly once under StrictMode', async () => {
    render(
      <StrictMode>
        <MemoryRouter initialEntries={['/callback']}>
          <Routes>
            <Route path="/callback" element={<CallbackPage />} />
            <Route path="/" element={<p>дом</p>} />
          </Routes>
        </MemoryRouter>
      </StrictMode>,
    )

    await waitFor(() => expect(screen.getByText('дом')).toBeInTheDocument())
    expect(userManager.signinRedirectCallback).toHaveBeenCalledTimes(1)
  })
})
