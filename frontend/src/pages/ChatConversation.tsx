import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { chatsApi } from '../api/chats'
import { profileApi } from '../api/profile'
import type { Chat, Message } from '../api/types'
import { useAuth } from '../auth/AuthContext'
import { useStompSubscription } from '../realtime/RealtimeContext'

function sortedById(messages: Message[]): Message[] {
  return [...messages].sort((a, b) => a.id - b.id)
}

export function ChatConversation() {
  const { id } = useParams()
  const chatId = Number(id)
  const { user } = useAuth()
  const myId = user?.profile.sub

  const [chat, setChat] = useState<Chat | null>(null)
  const [partnerName, setPartnerName] = useState<string | null>(null)
  const [messages, setMessages] = useState<Message[]>([])
  const [text, setText] = useState('')

  useEffect(() => {
    chatsApi.get(chatId).then(setChat).catch(() => undefined)
    chatsApi.messages(chatId).then(setMessages).catch(() => undefined)
  }, [chatId])

  // In a regular chat the identity is known: resolve the partner's name for the header/messages.
  // Re-runs when a mutual agreement converts the anonymous chat in place.
  useEffect(() => {
    if (!chat || chat.anonymous || !chat.partnerId) return
    profileApi
      .byId(chat.partnerId)
      .then((p) => setPartnerName(`${p.firstName} ${p.lastName}`))
      .catch(() => undefined)
  }, [chat?.anonymous, chat?.partnerId])

  // The realtime broadcast masks the sender in anonymous chats (senderId=null for everyone).
  // Only add a frame we don't already have — the POST response below is authoritative for our
  // own messages and must win the race, so we never overwrite an existing message here.
  useStompSubscription<Message>(`/topic/chats/${chatId}`, (incoming) => {
    setMessages((prev) => (prev.some((m) => m.id === incoming.id) ? prev : sortedById([...prev, incoming])))
  })

  // State frames are empty triggers: refetch to get our own requester-relative view, so the
  // partner's consent, the in-place conversion and closing are visible without a reload.
  useStompSubscription<unknown>(`/topic/chats/${chatId}/state`, () => {
    chatsApi.get(chatId).then(setChat).catch(() => undefined)
    chatsApi.messages(chatId).then(setMessages).catch(() => undefined)
  })

  async function send(event: FormEvent) {
    event.preventDefault()
    if (!text.trim()) return
    const sent = await chatsApi.send(chatId, text)
    // Replace any masked echo of this same message with the self-attributed POST response.
    setMessages((prev) => sortedById([...prev.filter((m) => m.id !== sent.id), sent]))
    setText('')
  }

  async function agree() {
    const updated = await chatsApi.agree(chatId)
    setChat(updated)
    // Mutual consent converts the chat in place: reload history with real sender ids.
    if (!updated.anonymous) chatsApi.messages(chatId).then(setMessages).catch(() => undefined)
  }

  if (!chat) return <p>Загрузка…</p>

  const partnerLabel = chat.anonymous ? 'Собеседник' : partnerName ?? 'Собеседник'

  return (
    <section>
      <h1>
        {chat.anonymous ? (
          'Анонимный чат'
        ) : (
          <>
            Чат с <Link to={`/profiles/${chat.partnerId}`}>{partnerName ?? chat.partnerId}</Link>
          </>
        )}
      </h1>

      {chat.anonymous && (
        <div className="chat-controls">
          <button onClick={agree} disabled={chat.iAgreed || chat.closed}>
            {chat.iAgreed ? 'Согласие подано' : 'Согласиться на деанонимизацию'}
          </button>
          <button onClick={() => chatsApi.close(chatId).then(setChat)} disabled={chat.closed}>
            {chat.closed ? 'Чат закрыт' : 'Закрыть чат'}
          </button>
          {chat.partnerAgreed && !chat.iAgreed && <span> Собеседник готов раскрыться</span>}
        </div>
      )}

      <ul className="messages">
        {messages.map((message) => (
          <li key={message.id} className={message.senderId === myId ? 'msg mine' : 'msg'}>
            <strong>{message.senderId === myId ? 'Вы' : partnerLabel}:</strong> {message.text}
          </li>
        ))}
      </ul>

      <form className="chat-send" onSubmit={send}>
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
