package org.maxizenit.maxigram.profile

import java.util.UUID

class InvalidSubscriptionException(message: String) : RuntimeException(message)

/** Published when a user newly subscribes to an author. */
data class SubscriptionCreated(val subscriberId: UUID, val authorId: UUID)
