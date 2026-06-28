package org.maxizenit.maxigram

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModularityTest {

    private val modules = ApplicationModules.of(MaxigramApplication::class.java)

    @Test
    fun `module boundaries are respected`() {
        modules.verify()
    }
}
