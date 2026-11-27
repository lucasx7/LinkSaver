package com.lucasx7.linksaver.data

import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.RespostaInvalidaException
import com.lucasx7.linksaver.domain.SemConexaoException
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.ContentConvertException
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class LinkApiClient(
    private val client: HttpClient = criarHttpClient(),
    private val baseUrl: String = urlBackendBase(),
) {
    suspend fun listar(): List<Link> =
        executar {
            val dtos: List<LinkRemotoDto> =
                client.get("$baseUrl/links?pagina=0&tamanho=100").body()
            val agora = Clock.System.now().toEpochMilliseconds()
            dtos.map { it.paraDominio(sincronizadoEm = agora) }
        }

    suspend fun enviar(
        url: String,
        descricao: String,
    ): Link =
        executar {
            val req = CriarLinkRemotoRequest(url = url, descricao = descricao.ifBlank { null })
            val dto: LinkRemotoDto =
                client
                    .post("$baseUrl/links") {
                        contentType(ContentType.Application.Json)
                        setBody(req)
                    }.body()
            dto.paraDominio(sincronizadoEm = Clock.System.now().toEpochMilliseconds())
        }

    private suspend fun <T> executar(bloco: suspend () -> T): T =
        try {
            bloco()
        } catch (e: CancellationException) {
            throw e
        } catch (e: ResponseException) {
            val msg = "Servidor respondeu HTTP ${e.response.status.value}."
            throw RespostaInvalidaException(msg, e)
        } catch (e: SerializationException) {
            throw RespostaInvalidaException("Resposta JSON inválida do servidor.", e)
        } catch (e: ContentConvertException) {
            throw RespostaInvalidaException("Formato de resposta inválido do servidor.", e)
        } catch (e: NoTransformationFoundException) {
            throw RespostaInvalidaException("Resposta inesperada do servidor.", e)
        } catch (e: Exception) {
            throw SemConexaoException(e)
        }
}

internal fun criarHttpClient(engine: HttpClientEngine? = null): HttpClient {
    val jsonCfg =
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    return if (engine != null) {
        HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) { json(jsonCfg) }
        }
    } else {
        HttpClient {
            expectSuccess = true
            install(ContentNegotiation) { json(jsonCfg) }
            install(HttpTimeout) {
                connectTimeoutMillis = 1500L
                requestTimeoutMillis = 5000L
                socketTimeoutMillis = 5000L
            }
        }
    }
}
