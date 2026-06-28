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
