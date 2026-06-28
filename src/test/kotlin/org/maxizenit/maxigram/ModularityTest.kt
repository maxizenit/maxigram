package org.maxizenit.maxigram

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModularityTest {

    private val modules =
        ApplicationModules.of(
            MaxigramApplication::class.java,
            // jOOQ-generated code lives under the base package but is infrastructure,
            // not a domain module — exclude it from the module model.
            DescribedPredicate.describe<JavaClass>("jOOQ generated code") {
                it.packageName.startsWith("org.maxizenit.maxigram.jooq")
            },
        )

    @Test
    fun `module boundaries are respected`() {
        modules.verify()
    }
}
