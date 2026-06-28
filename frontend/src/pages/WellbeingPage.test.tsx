import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest'
import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { WellbeingPage } from './WellbeingPage'
import { setTokenProvider } from '../api/client'

const server = setupServer()

beforeAll(() => {
  server.listen()
  setTokenProvider(() => 'test-token')
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

describe('WellbeingPage', () => {
  it('shows the form when there is no restraint and sets one', async () => {
    server.use(
      http.get('http://localhost:8080/api/wellbeing/restraint', () => new HttpResponse(null, { status: 200 })),
      http.put('http://localhost:8080/api/wellbeing/restraint', () =>
        HttpResponse.json({ startTime: '2026-07-01T10:00:00Z', endTime: '2026-07-01T12:00:00Z' }),
      ),
    )
    render(<WellbeingPage />)

    fireEvent.change(await screen.findByLabelText('Начало'), { target: { value: '2026-07-01T10:00' } })
    fireEvent.change(screen.getByLabelText('Конец'), { target: { value: '2026-07-01T12:00' } })
    fireEvent.click(screen.getByText('Установить'))

    expect(await screen.findByText('Снять')).toBeInTheDocument()
  })

  it('removes an existing restraint and shows the form again', async () => {
    server.use(
      http.get('http://localhost:8080/api/wellbeing/restraint', () =>
        HttpResponse.json({ startTime: '2026-07-01T10:00:00Z', endTime: '2026-07-01T12:00:00Z' }),
      ),
      http.delete('http://localhost:8080/api/wellbeing/restraint', () => new HttpResponse(null, { status: 204 })),
    )
    const user = userEvent.setup()
    render(<WellbeingPage />)

    await user.click(await screen.findByText('Снять'))

    expect(await screen.findByLabelText('Начало')).toBeInTheDocument()
  })
})
