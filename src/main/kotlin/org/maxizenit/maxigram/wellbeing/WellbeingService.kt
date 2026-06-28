package org.maxizenit.maxigram.wellbeing

import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Instant
import java.util.UUID

@Service
class WellbeingService(
    private val restraints: SelfRestraintRepository,
    private val clock: Clock = Clock.systemUTC(),
) {

    fun setRestraint(userId: UUID, startTime: Instant, endTime: Instant): SelfRestraint {
        val now = Instant.now(clock)
        if (!startTime.isBefore(endTime)) throw InvalidRestraintException("Start must be before end")
        if (!endTime.isAfter(now)) throw InvalidRestraintException("End must be in the future")
        requireNotCurrentlyActive(userId, now)

        restraints.upsert(userId, startTime, endTime)
        return SelfRestraint(userId, startTime, endTime)
    }

    fun getRestraint(userId: UUID): SelfRestraint? = restraints.findByUser(userId)

    fun removeRestraint(userId: UUID) {
        requireNotCurrentlyActive(userId, Instant.now(clock))
        restraints.delete(userId)
    }

    fun isRestrained(userId: UUID): Boolean =
        restraints.findByUser(userId)?.isActiveAt(Instant.now(clock)) ?: false

    private fun requireNotCurrentlyActive(userId: UUID, now: Instant) {
        if (restraints.findByUser(userId)?.isActiveAt(now) == true) throw RestraintActiveException()
    }
}
