package com.lucasx7.linksaver

import com.lucasx7.linksaver.adaptadores.banco.LinkRepositoryExposed
import com.lucasx7.linksaver.adaptadores.grpc.ExtratorDeMetadadosGrpc
import com.lucasx7.linksaver.adaptadores.http.linkRoutes
import com.lucasx7.linksaver.dominio.ExtratorDeMetadados
import com.lucasx7.linksaver.dominio.LinkRepository
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.ContentType
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
        get("/openapi.yaml") { val y = Application::class.java.getResource("/openapi/documentation.yaml")?.readText() ?: ""; call.respondText(y, ContentType.Text.Plain) }
        get("/openapi") { call.respondText("<!doctype html><html><head><link rel=\"stylesheet\" href=\"https://unpkg.com/swagger-ui-dist@5/swagger-ui.css\"></head><body><div id=\"ui\"></div><script src=\"https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js\"></script><script>SwaggerUIBundle({url:\"/openapi.yaml\",dom_id:\"#ui\"})</script></body></html>", ContentType.Text.Html) }
        linkRoutes(linkRepositorio, extrator)
    }
}
