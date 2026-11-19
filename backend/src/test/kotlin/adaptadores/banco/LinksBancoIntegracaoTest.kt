package com.lucasx7.linksaver.adaptadores.banco

import com.lucasx7.linksaver.dominio.NovoLink
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.testcontainers.postgresql.PostgreSQLContainer
import kotlin.test.*

class LinksBancoIntegracaoTest {
    @Test fun `roda migracoes Flyway e persiste link no PostgreSQL real`() {
        PostgreSQLContainer("postgres:17-alpine").use { pg ->
            pg.start()
            Flyway.configure().dataSource(pg.jdbcUrl, pg.username, pg.password).load().migrate()
            Database.connect(pg.jdbcUrl, "org.postgresql.Driver", pg.username, pg.password)
            transaction { exec("INSERT INTO usuario (login, email, data_criacao) VALUES ('lucas', 'l@test.com', CURRENT_DATE)") }
            val repo = LinkRepositoryExposed()
            val criado = repo.criar(NovoLink("https://example.com", "Doc", 1, "Example", "https://example.com/favicon.ico"))
            assertEquals("Example", repo.buscarPorId(criado.id)?.titulo)
            assertEquals(1, repo.listar(10, 0).size)
        }
    }
}
