package com.lucasx7.linksaver

import com.lucasx7.linksaver.adaptadores.banco.LinkRepositoryExposed
import com.lucasx7.linksaver.adaptadores.http.linkRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val linkRepositorio = LinkRepositoryExposed()

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }
        linkRoutes(linkRepositorio)
    }
}