package org.maxizenit.maxigram.common

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Default email transport: logs the message instead of sending it. A real SMTP-backed
 * implementation can replace this later without touching callers.
 */
@Component
class LoggingEmailSender : EmailSender {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(to: String, subject: String, body: String) {
        log.info("[email] to={} subject=\"{}\"\n{}", to, subject, body)
    }
}
