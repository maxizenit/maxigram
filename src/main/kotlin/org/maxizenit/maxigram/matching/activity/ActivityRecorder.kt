package org.maxizenit.maxigram.matching.activity

import org.maxizenit.maxigram.profile.ProfileService
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Records "user is active now" into the local-hour activity histogram. The local hour is computed
 * with the user's current timezone, so the histogram stays correct across timezone changes.
 * Per-user debounced and best-effort (never fails the caller).
 */
@Component
class ActivityRecorder(
    private val activity: ActivityRepository,
    private val profileService: ProfileService,
    private val clock: Clock = Clock.systemUTC(),
) {

    private val lastRecorded = ConcurrentHashMap<UUID, Instant>()

    fun record(userId: UUID) {
        try {
            val now = Instant.now(clock)
            val previous = lastRecorded[userId]
            if (previous != null && Duration.between(previous, now) < DEBOUNCE) return
            lastRecorded[userId] = now

            val zone = profileService.timezoneOf(userId) ?: ZoneOffset.UTC
            activity.incrementHour(userId, now.atZone(zone).hour)
        } catch (e: Exception) {
            // Best-effort analytics: never break the request that triggered recording.
        }
    }

    private companion object {
        val DEBOUNCE: Duration = Duration.ofMinutes(15)
    }
}
