import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { chatsApi } from '../api/chats'
import type { ChatSummary } from '../api/types'

export function ChatsPage() {
  const [chats, setChats] = useState<ChatSummary[]>([])

  useEffect(() => {
    chatsApi.list().then(setChats).catch(() => undefined)
  }, [])

  return (
    <section>
      <h1>Чаты</h1>
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
