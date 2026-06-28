package org.maxizenit.maxigram.common

/** Sends transactional emails. Shared kernel API used by identity and notification. */
interface EmailSender {
    fun send(to: String, subject: String, body: String)
}
