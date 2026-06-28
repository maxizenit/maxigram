package org.maxizenit.maxigram.chat

import java.util.UUID

/** Published when a message is persisted. Drives real-time delivery and notifications. */
data class MessageSent(val chatId: Long, val recipientId: UUID, val anonymous: Boolean, val message: Message)
