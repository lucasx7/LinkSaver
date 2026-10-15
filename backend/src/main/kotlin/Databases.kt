package com.lucasx7.linksaver

import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
    val driver = environment.config.property("db.driver").getString()
    val host = environment.config.property("db.host").getString()
    val port = environment.config.property("db.port").getString()
    val name = environment.config.property("db.name").getString()
    val user = environment.config.property("db.user").getString()
    val password = environment.config.property("db.password").getString()

    // Monta a URL a partir dos pedaços, para cada um poder vir de uma variável de ambiente
    val jdbcUrl = "jdbc:postgresql://$host:$port/$name"

    // 1. Executa as migrações do Flyway no PostgreSQL
    Flyway.configure()
        .dataSource(jdbcUrl, user, password)
        .load()
        .migrate()

    // 2. Conecta o Exposed ao banco de dados
    Database.connect(
        url = jdbcUrl,
        driver = driver,
        user = user,
        password = password
    )
}