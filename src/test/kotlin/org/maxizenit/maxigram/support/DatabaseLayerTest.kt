package org.maxizenit.maxigram.support

import org.assertj.core.api.Assertions.assertThat
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DatabaseLayerTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var dsl: DSLContext

    @Test
    fun `jooq dsl context executes a query`() {
        val one = dsl.selectOne().fetchOne()!!.value1()
        assertThat(one).isEqualTo(1)
    }

    @Test
    fun `flyway baseline migration was applied`() {
        val applied = dsl.fetchCount(DSL.table("flyway_schema_history"))
        assertThat(applied).isGreaterThanOrEqualTo(1)
    }
}
