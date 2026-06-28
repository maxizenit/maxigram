package org.maxizenit.maxigram.profile

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class SubscriptionService(
    private val subscriptions: SubscriptionRepository,
    private val clock: Clock = Clock.systemUTC(),
) {

    /** Idempotent: subscribing twice is a no-op. (v1 lost subscriptions entirely.) */
    fun subscribe(subscriberId: UUID, authorId: UUID) {
        if (subscriberId == authorId) {
            throw InvalidSubscriptionException("Cannot subscribe to yourself")
        }
        // TODO(Etap 5): publish a "new subscriber" domain event for the notification module.
        subscriptions.insertIfAbsent(subscriberId, authorId, Instant.now(clock))
    }

    fun unsubscribe(subscriberId: UUID, authorId: UUID) {
        subscriptions.delete(subscriberId, authorId)
    }

    fun authorsFollowedBy(subscriberId: UUID): List<UUID> =
        subscriptions.findAuthorIds(subscriberId)
}
