package org.maxizenit.maxigram.matching.activity

import org.springframework.messaging.simp.user.SimpUserRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

/** Records online activity (WebSocket source): periodically marks currently-connected users active. */
@Component
class ActivityPresenceSampler(
    private val userRegistry: SimpUserRegistry,
    private val recorder: ActivityRecorder,
) {

    @Scheduled(fixedRateString = "\${maxigram.matching.presence-sample-ms:900000}")
    fun sampleOnlineUsers() {
        userRegistry.users.forEach { user ->
            runCatching { recorder.record(UUID.fromString(user.name)) }
        }
    }
}
