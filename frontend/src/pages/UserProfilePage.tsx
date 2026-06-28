import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { profileApi } from '../api/profile'
import type { Profile } from '../api/types'

export function UserProfilePage() {
  const { id } = useParams()
  const [profile, setProfile] = useState<Profile | null>(null)
  const [subscribed, setSubscribed] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    profileApi
      .byId(id)
      .then(setProfile)
      .catch(() => setError('Профиль не найден'))
    profileApi
      .subscriptions()
      .then((ids) => setSubscribed(ids.includes(id)))
      .catch(() => undefined)
  }, [id])

  async function toggleSubscription() {
    if (!id) return
    if (subscribed) {
      await profileApi.unsubscribe(id)
      setSubscribed(false)
    } else {
      await profileApi.subscribe(id)
      setSubscribed(true)
    }
  }

  if (error) return <p role="alert">{error}</p>
  if (!profile) return <p>Загрузка…</p>

  return (
    <section>
      <h1>
        {profile.firstName} {profile.lastName}
      </h1>
      <p>Дата рождения: {profile.birthdate}</p>
      <p>Таймзона: {profile.timezone}</p>
      <p>Интересы: {profile.interests.map((interest) => interest.name).join(', ') || '—'}</p>
      <button onClick={toggleSubscription}>{subscribed ? 'Отписаться' : 'Подписаться'}</button>
    </section>
  )
}
