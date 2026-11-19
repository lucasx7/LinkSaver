package com.lucasx7.linksaver

import com.lucasx7.linksaver.adaptadores.banco.LinkRepositoryExposed
import com.lucasx7.linksaver.adaptadores.grpc.ExtratorDeMetadadosGrpc
import com.lucasx7.linksaver.adaptadores.http.linkRoutes
import com.lucasx7.linksaver.dominio.ExtratorDeMetadados
import com.lucasx7.linksaver.dominio.LinkRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    linkRepositorio: LinkRepository = LinkRepositoryExposed(),
    extrator: ExtratorDeMetadados = ExtratorDeMetadadosGrpc.conectar(
        System.getenv("EXTRATOR_GRPC_HOST") ?: "localhost",
        System.getenv("EXTRATOR_GRPC_PORT")?.toIntOrNull() ?: 50051,
    ),
) {
    routing {
        get("/") { call.respondText("Hello, World!") }
        get("/health") { call.respond(mapOf("status" to "ok")) }
        linkRoutes(linkRepositorio, extrator)
    }
}
