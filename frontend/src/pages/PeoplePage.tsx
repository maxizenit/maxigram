import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { profileApi } from '../api/profile'
import type { Profile } from '../api/types'

export function PeoplePage() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Profile[] | null>(null)
  const [following, setFollowing] = useState<Profile[]>([])

  useEffect(() => {
    profileApi
      .subscriptions()
      .then((ids) => profileApi.byIds(ids))
      .then(setFollowing)
      .catch(() => undefined)
  }, [])

  async function search(event: FormEvent) {
    event.preventDefault()
    if (!query.trim()) return
    setResults(await profileApi.search(query))
  }

  async function unfollow(id: string) {
    await profileApi.unsubscribe(id)
    setFollowing((prev) => prev.filter((profile) => profile.id !== id))
  }

  return (
    <section>
      <h1>Люди</h1>

      <form className="chat-send" onSubmit={search}>
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Имя или фамилия"
          aria-label="Поиск людей"
        />
        <button type="submit">Найти</button>
      </form>

      {results !== null &&
        (results.length === 0 ? (
          <p>Никого не нашли.</p>
        ) : (
          <ul className="item-list">
            {results.map((profile) => (
              <li key={profile.id}>
                <Link to={`/profiles/${profile.id}`}>
                  {profile.firstName} {profile.lastName}
                </Link>
              </li>
            ))}
          </ul>
        ))}

      <h2>Мои подписки</h2>
      {following.length === 0 ? (
        <p>Вы пока ни на кого не подписаны.</p>
      ) : (
        <ul className="item-list">
          {following.map((profile) => (
            <li key={profile.id}>
              <Link to={`/profiles/${profile.id}`}>
                {profile.firstName} {profile.lastName}
              </Link>
              <button onClick={() => unfollow(profile.id)}>Отписаться</button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
