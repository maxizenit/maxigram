package org.maxizenit.maxigram.chat

/** Published when a chat's state changes (consent, in-place conversion, close). Drives live UI updates. */
data class ChatStateChanged(val chatId: Long)
