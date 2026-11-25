package com.lucasx7.linksaver

import org.flywaydb.core.Flyway

fun main() {
    val url = System.getenv("FLYWAY_URL").orEmpty()
    val user = System.getenv("FLYWAY_USER").orEmpty()
    val password = System.getenv("FLYWAY_PASSWORD").orEmpty()
    require(url.isNotBlank() && user.isNotBlank() && password.isNotBlank()) {
        "Defina FLYWAY_URL, FLYWAY_USER e FLYWAY_PASSWORD."
    }
    val flyway = Flyway.configure().dataSource(url, user, password).load()
    val resultado = flyway.migrate()
    println("Migrations aplicadas agora: ${resultado.migrationsExecuted}")
    flyway.info().all().forEach {
        println(" - ${it.version} | ${it.description} | ${it.state}")
    }
}
