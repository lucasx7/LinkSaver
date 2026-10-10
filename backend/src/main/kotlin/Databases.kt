package com.lucasx7.linksaver

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val driver = environment.config.property("db.driver").getString()
    val jdbcUrl = environment.config.property("db.jdbcUrl").getString()
    val user = environment.config.property("db.user").getString()
    val password = environment.config.property("db.password").getString()

    // 1. Executa as migrações do Flyway no PostgreSQL da VM
    val flyway = Flyway.configure()
        .dataSource(jdbcUrl, user, password)
        .load()
    flyway.migrate()

    // 2. Conecta o Exposed ao banco de dados
    Database.connect(
        url = jdbcUrl,
        driver = driver,
        user = user,
        password = password
    )
}