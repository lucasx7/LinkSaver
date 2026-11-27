package com.lucasx7.linksaver.data

import com.lucasx7.linksaver.domain.RespostaInvalidaException
import com.lucasx7.linksaver.domain.SemConexaoException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LinkRepositoryHibridoTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun comBackendOnlineBuscaECacheiaNoArquivo() =
        runTest {
            val body =
                """[{"id":1,"url":"https://ufrn.br","descricao":"UFRN",""" +
                    """"dataCriacao":"2026-11-27","idUsuario":1}]"""
            val arm = ArmazenamentoEmMemoria()
            val repo =
                LinkRepositoryHibrido(
                    LinkApiClient(criarHttpClient(MockEngine { respond(body, HttpStatusCode.OK, jsonHeaders) })),
                    LinkRepositoryArquivo(arm, 0L, emptyList()),
                )
            assertEquals(1, repo.listar().size)
            assertNull(repo.avisoOffline)
            assertEquals(1, LinkRepositoryArquivo(arm, 0L, emptyList()).listar().size)
        }

    @Test
    fun semRedeComCacheDevolveCacheEAvisaOffline() =
        runTest {
            val arm = ArmazenamentoEmMemoria()
            LinkRepositoryArquivo(arm, 0L, emptyList()).adicionar("https://ufrn.br", "UFRN", emptyList())
            val repo =
                LinkRepositoryHibrido(
                    LinkApiClient(criarHttpClient(MockEngine { error("offline") })),
                    LinkRepositoryArquivo(arm, 0L, emptyList()),
                )
            assertEquals(1, repo.listar().size)
            assertNotNull(repo.avisoOffline)
        }

    @Test
    fun semRedeSemCacheLancaSemConexaoE500LancaRespostaInvalida() =
        runTest {
            val rSemRede =
                LinkRepositoryHibrido(
                    LinkApiClient(criarHttpClient(MockEngine { error("offline") })),
                    LinkRepositoryArquivo(ArmazenamentoEmMemoria(), 0L, emptyList()),
                )
            assertFailsWith<SemConexaoException> { rSemRede.listar() }
            val r500 =
                LinkRepositoryHibrido(
                    LinkApiClient(criarHttpClient(MockEngine { respond("erro", HttpStatusCode.InternalServerError) })),
                    LinkRepositoryArquivo(ArmazenamentoEmMemoria(), 0L, emptyList()),
                )
            assertFailsWith<RespostaInvalidaException> { r500.listar() }
        }

    @Test
    fun adicionarSemRedeSalvaLocalmenteComoPendente() =
        runTest {
            val repo =
                LinkRepositoryHibrido(
                    LinkApiClient(criarHttpClient(MockEngine { error("offline") })),
                    LinkRepositoryArquivo(ArmazenamentoEmMemoria(), 0L, emptyList()),
                )
            val salvo = repo.adicionar("https://kotlinlang.org", "Docs", listOf("dev"))
            assertNull(salvo.sincronizadoEm)
            assertNotNull(repo.avisoOffline)
        }

    @Test
    fun comPendenciaLocalSobeParaBackendAoSincronizar() =
        runTest {
            val cache = LinkRepositoryArquivo(ArmazenamentoEmMemoria(), 0L, emptyList())
            cache.adicionar("https://kotlinlang.org", "Docs", listOf("dev"))
            var enviouPost = false
            val item =
                """{"id":9,"url":"https://kotlinlang.org","descricao":"Docs",""" +
                    """"dataCriacao":"2026-11-27","idUsuario":1}"""
            val eng =
                MockEngine { req ->
                    if (req.method.value == "POST") {
                        enviouPost = true
                        respond(item, HttpStatusCode.Created, jsonHeaders)
                    } else {
                        respond("[$item]", HttpStatusCode.OK, jsonHeaders)
                    }
                }
            val api = LinkApiClient(criarHttpClient(eng))
            val lista = LinkRepositoryHibrido(api, cache).listar()
            assertEquals(true, enviouPost)
            assertNotNull(lista.first().sincronizadoEm)
            assertEquals(listOf("dev"), lista.first().tags)
        }
}
