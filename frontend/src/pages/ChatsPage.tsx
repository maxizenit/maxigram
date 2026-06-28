import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { chatsApi } from '../api/chats'
import { matchingApi } from '../api/matching'
import type { ChatSummary } from '../api/types'

export function ChatsPage() {
  const navigate = useNavigate()
  const [chats, setChats] = useState<ChatSummary[]>([])
  const [queued, setQueued] = useState(false)

  useEffect(() => {
    chatsApi.list().then(setChats).catch(() => undefined)
  }, [])

  async function findCompanion() {
    const result = await matchingApi.request()
    if (result.matched && result.chatId != null) {
      navigate(`/chats/${result.chatId}`)
    } else {
      setQueued(true)
    }
  }

  return (
    <section>
      <h1>Чаты</h1>
      <button onClick={findCompanion}>Найти анонимного собеседника</button>
      {queued && <p role="status">Вы в очереди — подберём собеседника.</p>}
      {chats.length === 0 ? (
        <p>Чатов пока нет.</p>
      ) : (
        <ul>
          {chats.map((chat) => (
            <li key={chat.id}>
              <Link to={`/chats/${chat.id}`}>
                {chat.anonymous ? 'Аноним' : chat.partnerId} — {chat.lastMessage ?? 'нет сообщений'}
              </Link>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
