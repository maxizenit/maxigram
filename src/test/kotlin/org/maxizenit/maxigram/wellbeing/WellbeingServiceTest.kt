package org.maxizenit.maxigram.wellbeing

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class WellbeingServiceTest {

    private val now = Instant.parse("2026-06-28T12:00:00Z")
    private val repository = FakeSelfRestraintRepository()
    private val service = WellbeingService(repository, Clock.fixed(now, ZoneOffset.UTC))

    private val user = UUID.randomUUID()
    private val hour = Duration.ofHours(1)

    @Test
    fun `sets a future restraint`() {
        val restraint = service.setRestraint(user, now.plus(hour), now.plus(hour).plus(hour))

        assertThat(restraint.startTime).isEqualTo(now.plus(hour))
        assertThat(service.isRestrained(user)).isFalse()
    }

    @Test
    fun `rejects a window whose start is not before its end`() {
        assertThatThrownBy { service.setRestraint(user, now.plus(hour), now) }
            .isInstanceOf(InvalidRestraintException::class.java)
    }

    @Test
    fun `rejects a window ending in the past`() {
        assertThatThrownBy { service.setRestraint(user, now.minus(hour).minus(hour), now.minus(hour)) }
            .isInstanceOf(InvalidRestraintException::class.java)
    }

    @Test
    fun `an active restraint reports as restrained`() {
        service.setRestraint(user, now.minus(hour), now.plus(hour))

        assertThat(service.isRestrained(user)).isTrue()
    }

    @Test
    fun `cannot change an active restraint`() {
        service.setRestraint(user, now.minus(hour), now.plus(hour))

        assertThatThrownBy { service.setRestraint(user, now.plus(hour), now.plus(hour).plus(hour)) }
            .isInstanceOf(RestraintActiveException::class.java)
    }

    @Test
    fun `cannot remove an active restraint`() {
        service.setRestraint(user, now.minus(hour), now.plus(hour))

        assertThatThrownBy { service.removeRestraint(user) }.isInstanceOf(RestraintActiveException::class.java)
    }

    @Test
    fun `can remove a restraint that is not active`() {
        service.setRestraint(user, now.plus(hour), now.plus(hour).plus(hour))

        service.removeRestraint(user)

        assertThat(service.getRestraint(user)).isNull()
    }

    private class FakeSelfRestraintRepository : SelfRestraintRepository {
        private val store = mutableMapOf<UUID, SelfRestraint>()

        override fun upsert(userId: UUID, startTime: Instant, endTime: Instant) {
            store[userId] = SelfRestraint(userId, startTime, endTime)
        }

        override fun findByUser(userId: UUID): SelfRestraint? = store[userId]

        override fun delete(userId: UUID) {
            store.remove(userId)
        }
    }
}
