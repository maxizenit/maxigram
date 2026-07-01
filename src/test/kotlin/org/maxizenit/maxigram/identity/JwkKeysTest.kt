package org.maxizenit.maxigram.identity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class JwkKeysTest {

    @Test
    fun `persists the generated key and reloads the same one`(@TempDir dir: Path) {
        val path = dir.resolve("keys/jwk.json")

        val first = loadOrCreateRsaKey(path)
        val second = loadOrCreateRsaKey(path)

        assertThat(Files.exists(path)).isTrue()
        assertThat(second.keyID).isEqualTo(first.keyID)
        assertThat(second.toRSAPrivateKey().encoded).isEqualTo(first.toRSAPrivateKey().encoded)
    }

    @Test
    fun `generates an ephemeral key when no path is configured`() {
        val first = loadOrCreateRsaKey(null)
        val second = loadOrCreateRsaKey(null)

        assertThat(first.keyID).isNotEqualTo(second.keyID)
        assertThat(first.isPrivate).isTrue()
    }
}
