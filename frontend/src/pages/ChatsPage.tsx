import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { chatsApi } from '../api/chats'
import { matchingApi } from '../api/matching'
import { profileApi } from '../api/profile'
import type { ChatSummary } from '../api/types'

export function ChatsPage() {
  const navigate = useNavigate()
  const [chats, setChats] = useState<ChatSummary[]>([])
  const [queued, setQueued] = useState(false)
  const [names, setNames] = useState<Record<string, string>>({})

  useEffect(() => {
    chatsApi
      .list()
      .then((list) => {
        setChats(list)
        // Resolve partner names for regular chats (anonymous ones stay masked).
        const ids = [...new Set(list.map((chat) => chat.partnerId).filter((id): id is string => id !== null))]
        ids.forEach((id) =>
          profileApi
            .byId(id)
            .then((profile) => setNames((prev) => ({ ...prev, [id]: `${profile.firstName} ${profile.lastName}` })))
            .catch(() => undefined),
        )
      })
      .catch(() => undefined)
  }, [])

  async function findCompanion() {
    const result = await matchingApi.request()
    if (result.matched && result.chatId != null) {
      navigate(`/chats/${result.chatId}`)
    } else {
      setQueued(true)
    }
  }

  function title(chat: ChatSummary): string {
    if (chat.anonymous) return 'Аноним'
    return (chat.partnerId && names[chat.partnerId]) ?? 'Собеседник'
  }

  return (
    <section>
      <h1>Чаты</h1>
      <button onClick={findCompanion}>Найти анонимного собеседника</button>
      {queued && <p role="status">Вы в очереди — подберём собеседника.</p>}
      {chats.length === 0 ? (
        <p>Чатов пока нет.</p>
      ) : (
        <ul className="item-list">
          {chats.map((chat) => (
            <li key={chat.id}>
              <Link to={`/chats/${chat.id}`}>
                {title(chat)} — {chat.lastMessage ?? 'нет сообщений'}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
