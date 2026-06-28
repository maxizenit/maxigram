package org.maxizenit.maxigram.support

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

/**
 * Base class for integration tests. Uses a single PostgreSQL container shared across all
 * test classes (singleton pattern): it is started once and kept running for the whole JVM,
 * so the cached Spring context's datasource never points at a stopped container. Testcontainers
 * Ryuk reaps it on JVM exit.
 */
@SpringBootTest
abstract class AbstractIntegrationTest {

    companion object {
        @JvmStatic
        @ServiceConnection
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine")).also { it.start() }
    }
}
