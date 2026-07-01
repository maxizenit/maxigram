package org.maxizenit.maxigram.identity

import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.UUID

/**
 * Loads the token-signing RSA key from [path], generating a new one when absent. With a path the
 * generated key is persisted, so application restarts keep previously issued tokens valid; with
 * no path (tests, ephemeral runs) the key lives only in memory.
 */
fun loadOrCreateRsaKey(path: Path?): RSAKey {
    if (path != null && Files.exists(path)) {
        return JWKSet.parse(Files.readString(path)).keys.first() as RSAKey
    }
    val keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair()
    val rsaKey =
        RSAKey.Builder(keyPair.public as RSAPublicKey)
            .privateKey(keyPair.private as RSAPrivateKey)
            .keyID(UUID.randomUUID().toString())
            .build()
    if (path != null) {
        path.parent?.let { Files.createDirectories(it) }
        // toString(false) = include private params (the flag means "public params only").
        Files.writeString(path, JWKSet(rsaKey).toString(false))
    }
    return rsaKey
}
