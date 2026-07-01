export interface Interest {
  id: number
  name: string
}

export interface Profile {
  id: string
  firstName: string
  lastName: string
  birthdate: string
  timezone: string
  interests: Interest[]
}

export interface ProfileInput {
  firstName: string
  lastName: string
  birthdate: string
  timezone: string
  interestIds: number[]
}

export interface Post {
  id: number
  authorId: string
  text: string
  createdAt: string
  likesCount: number
  commentsCount: number
  likedByMe: boolean
}

export interface Comment {
  id: number
  postId: number
  authorId: string
  text: string
  createdAt: string
  likesCount: number
  likedByMe: boolean
}

export interface ChatSummary {
  id: number
  partnerId: string | null
  anonymous: boolean
  lastMessage: string | null
  createdAt: string
}

export interface Chat {
  id: number
  partnerId: string | null
  anonymous: boolean
  iAgreed: boolean
  partnerAgreed: boolean
  closed: boolean
  createdAt: string
}

export interface Message {
  id: number
  chatId: number
  senderId: string | null
  text: string
  createdAt: string
  read: boolean
}

export interface Notification {
  id: number
  type: string
  actorId: string | null
  text: string
  read: boolean
  createdAt: string
}
