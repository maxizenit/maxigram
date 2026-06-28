package org.maxizenit.maxigram.chat.ws

import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.maxizenit.maxigram.chat.ChatRepository
import org.maxizenit.maxigram.chat.MessageService
import org.maxizenit.maxigram.jooq.Tables.APP_USER
import org.maxizenit.maxigram.support.AbstractIntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.lang.reflect.Type
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketIT : AbstractIntegrationTest() {

    @Value("\${local.server.port}")
    private var port: Int = 0

    @Autowired
    private lateinit var dsl: DSLContext

    @Autowired
    private lateinit var jwkSource: JWKSource<SecurityContext>

    @Autowired
    private lateinit var chatRepository: ChatRepository

    @Autowired
    private lateinit var messageService: MessageService

    private fun createUser(): UUID {
        val id = UUID.randomUUID()
        dsl.insertInto(APP_USER)
            .set(APP_USER.ID, id)
            .set(APP_USER.EMAIL, "$id@example.com")
            .set(APP_USER.PASSWORD_HASH, "x")
            .set(APP_USER.EMAIL_VERIFIED, true)
            .set(APP_USER.CREATED_AT, OffsetDateTime.now())
            .execute()
        return id
    }

    private fun tokenFor(userId: UUID): String {
        val now = Instant.now()
        val claims =
            JwtClaimsSet.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build()
        return NimbusJwtEncoder(jwkSource).encode(JwtEncoderParameters.from(claims)).tokenValue
    }

    @Test
    fun `a participant receives a message over the websocket in real time`() {
        val alice = createUser()
        val bob = createUser()
        val chat = chatRepository.insert(alice, bob, Instant.now())

        val stompClient = WebSocketStompClient(StandardWebSocketClient())
        stompClient.messageConverter = MappingJackson2MessageConverter()

        val connectHeaders = StompHeaders()
        connectHeaders.add("Authorization", "Bearer ${tokenFor(bob)}")
        val session =
            stompClient
                .connectAsync(
                    "ws://localhost:$port/ws",
                    WebSocketHttpHeaders(),
                    connectHeaders,
                    object : StompSessionHandlerAdapter() {},
                )
                .get(5, TimeUnit.SECONDS)

        val received = CompletableFuture<Map<*, *>>()
        session.subscribe(
            "/topic/chats/${chat.id}",
            object : StompFrameHandler {
                override fun getPayloadType(headers: StompHeaders): Type = Map::class.java
                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    received.complete(payload as Map<*, *>)
                }
            },
        )
        Thread.sleep(300)

        messageService.send(chat.id, alice, "hello over ws")

        val message = received.get(5, TimeUnit.SECONDS)
        assertThat(message["text"]).isEqualTo("hello over ws")
    }
}
