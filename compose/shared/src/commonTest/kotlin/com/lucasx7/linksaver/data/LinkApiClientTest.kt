package com.lucasx7.linksaver.data

import com.lucasx7.linksaver.domain.RespostaInvalidaException
import com.lucasx7.linksaver.domain.SemConexaoException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LinkApiClientTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun listarConverteJsonParaDominio() =
        runTest {
            val body =
                """[{"id":1,"url":"https://ufrn.br","descricao":"",""" +
                    """"dataCriacao":"2026-11-27","idUsuario":1,"titulo":"UFRN"}]"""
            val api =
                LinkApiClient(
                    criarHttpClient(
                        MockEngine {
                            respond(body, HttpStatusCode.OK, jsonHeaders)
                        },
                    ),
                    "http://localhost:8080",
                )
            val links = api.listar()
            assertEquals(1, links.size)
            assertEquals("UFRN", links[0].descricao)
            assertNotNull(links[0].sincronizadoEm)
        }

    @Test
    fun listarSemRedeLancaSemConexao() =
        runTest {
            val eng = MockEngine { error("sem rede") }
            val api = LinkApiClient(criarHttpClient(eng), "http://x")
            assertFailsWith<SemConexaoException> { api.listar() }
        }

    @Test
    fun listarHttp500LancaRespostaInvalida() =
        runTest {
            val eng = MockEngine { respond("erro", HttpStatusCode.InternalServerError) }
            val api = LinkApiClient(criarHttpClient(eng), "http://x")
            assertFailsWith<RespostaInvalidaException> { api.listar() }
        }

    @Test
    fun listarJsonQuebradoLancaRespostaInvalida() =
        runTest {
            val eng = MockEngine { respond("{ruim", HttpStatusCode.OK, jsonHeaders) }
            val api = LinkApiClient(criarHttpClient(eng), "http://x")
            assertFailsWith<RespostaInvalidaException> { api.listar() }
        }

    @Test
    fun enviarEnviaPostERetornaLink() =
        runTest {
            val body =
                """{"id":5,"url":"https://ufrn.br","descricao":"Portal",""" +
                    """"dataCriacao":"2026-11-27","idUsuario":1}"""
            var json = ""
            val eng =
                MockEngine { r ->
                    json = (r.body as TextContent).text
                    respond(body, HttpStatusCode.Created, jsonHeaders)
                }
            val api = LinkApiClient(criarHttpClient(eng), "http://x")
            val link = api.enviar("https://ufrn.br", "Portal")
            assertEquals(5, link.id)
            assertNotNull(link.sincronizadoEm)
            assertTrue("\"idUsuario\":1" in json)
        }
}
