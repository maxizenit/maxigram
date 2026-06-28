import { useEffect, useState, type FormEvent } from 'react'
import { ApiError } from '../api/client'
import { profileApi } from '../api/profile'
import type { Interest, Profile } from '../api/types'

export function ProfilePage() {
  const [allInterests, setAllInterests] = useState<Interest[]>([])
  const [firstName, setFirstName] = useState('')
  const [lastName, setLastName] = useState('')
  const [birthdate, setBirthdate] = useState('')
  const [timezone, setTimezone] = useState('UTC')
  const [interestIds, setInterestIds] = useState<number[]>([])
  const [saved, setSaved] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    profileApi.interests().then(setAllInterests).catch(() => undefined)
    profileApi
      .me()
      .then(fill)
      .catch((e: unknown) => {
        if (!(e instanceof ApiError && e.status === 404)) setError('Не удалось загрузить профиль')
      })
  }, [])

  function fill(profile: Profile) {
    setFirstName(profile.firstName)
    setLastName(profile.lastName)
    setBirthdate(profile.birthdate)
    setTimezone(profile.timezone)
    setInterestIds(profile.interests.map((interest) => interest.id))
  }

  function toggleInterest(id: number) {
    setInterestIds((ids) => (ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]))
  }

  async function save(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSaved(false)
    try {
      fill(await profileApi.save({ firstName, lastName, birthdate, timezone, interestIds }))
      setSaved(true)
    } catch {
      setError('Не удалось сохранить профиль')
    }
  }

  return (
    <section>
      <h1>Профиль</h1>
      <form onSubmit={save}>
        <label>
          Имя
          <input value={firstName} onChange={(e) => setFirstName(e.target.value)} aria-label="Имя" />
        </label>
        <label>
          Фамилия
          <input value={lastName} onChange={(e) => setLastName(e.target.value)} aria-label="Фамилия" />
        </label>
        <label>
          Дата рождения
          <input type="date" value={birthdate} onChange={(e) => setBirthdate(e.target.value)} aria-label="Дата рождения" />
        </label>
        <label>
          Таймзона
          <input value={timezone} onChange={(e) => setTimezone(e.target.value)} aria-label="Таймзона" />
        </label>
        <fieldset>
          <legend>Интересы</legend>
          {allInterests.map((interest) => (
            <label key={interest.id}>
              <input
                type="checkbox"
                checked={interestIds.includes(interest.id)}
                onChange={() => toggleInterest(interest.id)}
              />
              {interest.name}
            </label>
          ))}
        </fieldset>
        <button type="submit">Сохранить</button>
      </form>
      {saved && <p role="status">Профиль сохранён</p>}
      {error && <p role="alert">{error}</p>}
    </section>
  )
}
