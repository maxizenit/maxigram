import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { chatsApi } from '../api/chats'
import type { Chat, Message } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { useStompSubscription } from '../realtime/RealtimeContext'

export function ChatConversation() {
  const { id } = useParams()
  const chatId = Number(id)
  const { user } = useAuth()
  const myId = user?.profile.sub

  const [chat, setChat] = useState<Chat | null>(null)
  const [messages, setMessages] = useState<Message[]>([])
  const [text, setText] = useState('')

  useEffect(() => {
    chatsApi.get(chatId).then(setChat).catch(() => undefined)
    chatsApi.messages(chatId).then(setMessages).catch(() => undefined)
  }, [chatId])

  // Live messages arrive on the chat topic (the sender's own echo is de-duplicated by id).
  useStompSubscription<Message>(`/topic/chats/${chatId}`, (incoming) => {
    setMessages((prev) => (prev.some((m) => m.id === incoming.id) ? prev : [...prev, incoming]))
  })

  async function send(event: FormEvent) {
    event.preventDefault()
    if (!text.trim()) return
    const sent = await chatsApi.send(chatId, text)
    setMessages((prev) => (prev.some((m) => m.id === sent.id) ? prev : [...prev, sent]))
    setText('')
  }

  if (!chat) return <p>Загрузка…</p>

  return (
    <section>
      <h1>{chat.anonymous ? 'Анонимный чат' : `Чат с ${chat.partnerId}`}</h1>

      {chat.anonymous && (
        <div>
          <button onClick={() => chatsApi.agree(chatId).then(setChat)} disabled={chat.iAgreed || chat.closed}>
            {chat.iAgreed ? 'Согласие подано' : 'Согласиться на деанонимизацию'}
          </button>
          <button onClick={() => chatsApi.close(chatId).then(setChat)} disabled={chat.closed}>
            {chat.closed ? 'Чат закрыт' : 'Закрыть чат'}
          </button>
          {chat.partnerAgreed && !chat.iAgreed && <span> Собеседник готов раскрыться</span>}
          {chat.newChatId != null && <Link to={`/chats/${chat.newChatId}`}>Перейти в обычный чат</Link>}
        </div>
      )}

      <ul>
        {messages.map((message) => (
          <li key={message.id}>
            <strong>{message.senderId === myId ? 'Вы' : 'Собеседник'}:</strong> {message.text}
          </li>
        ))}
      </ul>

      <form onSubmit={send}>
        <input
          value={text}
          onChange={(event) => setText(event.target.value)}
          aria-label="Сообщение"
          disabled={chat.closed}
        />
        <button type="submit" disabled={chat.closed}>
          Отправить
        </button>
      </form>
    </section>
  )
}
