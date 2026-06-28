package org.maxizenit.maxigram.wellbeing

import java.time.Instant
import java.util.UUID

data class SelfRestraint(val userId: UUID, val startTime: Instant, val endTime: Instant) {
    fun isActiveAt(now: Instant): Boolean = !now.isBefore(startTime) && now.isBefore(endTime)
}

class InvalidRestraintException(message: String) : RuntimeException(message)

/** A self-restraint cannot be changed or removed while it is active (the commitment). */
class RestraintActiveException : RuntimeException("Cannot change an active self-restraint")
