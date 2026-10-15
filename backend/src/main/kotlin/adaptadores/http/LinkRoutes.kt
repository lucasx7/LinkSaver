package com.lucasx7.linksaver.adaptadores.http

import com.lucasx7.linksaver.dominio.Link
import com.lucasx7.linksaver.dominio.LinkRepository
import com.lucasx7.linksaver.dominio.NovoLink
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

data class LinkResposta(
    val id: Int,
    val url: String,
    val descricao: String?,
    val dataCriacao: String,
    val idUsuario: Int,
)

data class CriarLinkRequisicao(
    val url: String,
    val descricao: String? = null,
    val idUsuario: Int,
)

private fun Link.paraResposta() = LinkResposta(id, url, descricao, dataCriacao.toString(), idUsuario)

fun Route.linkRoutes(repositorio: LinkRepository) {
    route("/links") {
        get {
            val pagina = (call.request.queryParameters["pagina"]?.toIntOrNull() ?: 0).coerceAtLeast(0)
            val tamanho = (call.request.queryParameters["tamanho"]?.toIntOrNull() ?: 20).coerceIn(1, 100)
            val links = repositorio.listar(tamanho, pagina.toLong() * tamanho)
            call.respond(links.map { it.paraResposta() })
        }

        get("/{id}") {
            val link = call.parameters["id"]?.toIntOrNull()?.let { repositorio.buscarPorId(it) }
            if (link == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(link.paraResposta())
            }
        }

        post {
            val corpo = call.receive<CriarLinkRequisicao>()
            val criado = repositorio.criar(NovoLink(corpo.url, corpo.descricao, corpo.idUsuario))
            call.response.header(HttpHeaders.Location, "/links/${criado.id}")
            call.respond(HttpStatusCode.Created, criado.paraResposta())
        }
    }
}
