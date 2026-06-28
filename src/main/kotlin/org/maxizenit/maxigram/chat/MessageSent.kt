package org.maxizenit.maxigram.chat

/** Published when a message is persisted. Drives real-time delivery (and notifications, Etap 5). */
data class MessageSent(val chatId: Long, val message: Message)
