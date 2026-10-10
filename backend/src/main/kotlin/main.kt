package com.lucasx7.linksaver

import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureDatabases() // <-- Conecta ao banco e roda as migrações ao subir
    configureSerialization()
    configureRouting()
}