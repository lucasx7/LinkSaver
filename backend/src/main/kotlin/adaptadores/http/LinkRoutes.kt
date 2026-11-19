package com.lucasx7.linksaver.adaptadores.http

import com.lucasx7.linksaver.dominio.ExtratorDeMetadados
import com.lucasx7.linksaver.dominio.Link
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.lucasx7.linksaver.dominio.LinkRepository
import com.lucasx7.linksaver.dominio.NovoLink
import com.lucasx7.linksaver.dominio.RegrasLink
import com.lucasx7.linksaver.dominio.UrlInvalidaException
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
    val titulo: String? = null,
    val faviconUrl: String? = null,
)

data class CriarLinkRequisicao(
    val url: String,
    val descricao: String? = null,
    val idUsuario: Int,
)

private fun Link.paraResposta() = LinkResposta(id, url, descricao, dataCriacao.toString(), idUsuario, titulo, faviconUrl)

fun Route.linkRoutes(repositorio: LinkRepository, extrator: ExtratorDeMetadados) {
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
                call.responderProblema(HttpStatusCode.NotFound, "https://linksaver.dev/erros/nao-encontrado", "Link nao encontrado", "Nenhum link com este id")
            } else {
                call.respond(link.paraResposta())
            }
        }

        post {
            val corpo = call.receive<CriarLinkRequisicao>()
            val urlLimpa = try { RegrasLink.validarUrl(corpo.url) } catch (e: UrlInvalidaException) {
                return@post call.responderProblema(HttpStatusCode.BadRequest, "https://linksaver.dev/erros/url-invalida", "URL invalida", e.message ?: "URL invalida")
            }
            val meta = withContext(Dispatchers.IO) { extrator.extrair(urlLimpa) }
            val criado = repositorio.criar(NovoLink(urlLimpa, corpo.descricao, corpo.idUsuario, meta.titulo, meta.faviconUrl))
            call.response.header(HttpHeaders.Location, "/links/${criado.id}")
            call.respond(HttpStatusCode.Created, criado.paraResposta())
        }
    }
}
