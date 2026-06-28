import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'
import { useAuth } from '../auth/AuthContext'

vi.mock('../auth/AuthContext', () => ({ useAuth: vi.fn() }))
const mockedUseAuth = vi.mocked(useAuth)

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route
          path="/secret"
          element={
            <ProtectedRoute>
              <div>секрет</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div>страница входа</div>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('ProtectedRoute', () => {
  it('renders children for an authenticated user', () => {
    mockedUseAuth.mockReturnValue({ user: { expired: false } as never, loading: false, login: vi.fn(), logout: vi.fn() })
    renderAt('/secret')
    expect(screen.getByText('секрет')).toBeInTheDocument()
  })

  it('redirects to /login when not authenticated', () => {
    mockedUseAuth.mockReturnValue({ user: null, loading: false, login: vi.fn(), logout: vi.fn() })
    renderAt('/secret')
    expect(screen.getByText('страница входа')).toBeInTheDocument()
  })
})
