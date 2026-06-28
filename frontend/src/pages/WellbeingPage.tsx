import { useEffect, useState, type FormEvent } from 'react'
import { wellbeingApi, type Restraint } from '../api/wellbeing'

export function WellbeingPage() {
  const [restraint, setRestraint] = useState<Restraint | null>(null)
  const [start, setStart] = useState('')
  const [end, setEnd] = useState('')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    wellbeingApi
      .get()
      .then((current) => setRestraint(current ?? null))
      .catch(() => undefined)
  }, [])

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    try {
      setRestraint(await wellbeingApi.set(new Date(start).toISOString(), new Date(end).toISOString()))
    } catch {
      setError('Не удалось установить ограничение')
    }
  }

  async function remove() {
    setError(null)
    try {
      await wellbeingApi.remove()
      setRestraint(null)
    } catch {
      setError('Активное ограничение нельзя снять до конца окна')
    }
  }

  return (
    <section>
      <h1>Самоограничение</h1>
      {restraint ? (
        <div>
          <p>
            Доступ ограничен с {new Date(restraint.startTime).toLocaleString()} по{' '}
            {new Date(restraint.endTime).toLocaleString()}.
          </p>
          <button onClick={remove}>Снять</button>
        </div>
      ) : (
        <form onSubmit={submit}>
          <label>
            Начало
            <input type="datetime-local" value={start} onChange={(e) => setStart(e.target.value)} aria-label="Начало" />
          </label>
          <label>
            Конец
            <input type="datetime-local" value={end} onChange={(e) => setEnd(e.target.value)} aria-label="Конец" />
          </label>
          <button type="submit">Установить</button>
        </form>
      )}
      {error && <p role="alert">{error}</p>}
    </section>
  )
}
