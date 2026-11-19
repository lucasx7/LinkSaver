package com.lucasx7.linksaver.adaptadores.http

import com.lucasx7.linksaver.adaptadores.grpc.ExtratorDeMetadadosGrpc
import com.lucasx7.linksaver.configureRouting
import com.lucasx7.linksaver.dominio.*
import io.grpc.inprocess.*
import io.grpc.stub.StreamObserver
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import linksaver.metadados.v1.Metadados.*
import linksaver.metadados.v1.MetadadosServiceGrpc
import java.time.LocalDate
import kotlin.test.*
class LinkRoutesTest {
    private class RepoEmMemoria : LinkRepository {
        val itens = mutableListOf<Link>()
        override fun listar(limite: Int, deslocamento: Long) = itens.drop(deslocamento.toInt()).take(limite)
        override fun buscarPorId(id: Int) = itens.find { it.id == id }
        override fun criar(novo: NovoLink) = Link(itens.size + 1, novo.url, novo.descricao, LocalDate.now(), novo.idUsuario, novo.titulo, novo.faviconUrl).also { itens += it }
    }
    @Test fun `POST links preenche titulo quando gRPC responde`() = testApplication {
        val nome = InProcessServerBuilder.generateName()
        val impl = object : MetadadosServiceGrpc.MetadadosServiceImplBase() {
            override fun extrairMetadados(r: ExtrairMetadadosRequest, o: StreamObserver<ExtrairMetadadosResponse>) {
                o.onNext(ExtrairMetadadosResponse.newBuilder().setTitulo("Example Domain").setFaviconUrl("https://example.com/favicon.ico").build())
                o.onCompleted()
            }
        }
        val srv = InProcessServerBuilder.forName(nome).addService(impl).build().start()
        val canal = InProcessChannelBuilder.forName(nome).build()
        environment { config = MapApplicationConfig() }
        application { this@application.install(ContentNegotiation) { jackson() }; routing { linkRoutes(RepoEmMemoria(), ExtratorDeMetadadosGrpc(canal)) } }
        try {
            val resp = client.post("/links") {
                contentType(ContentType.Application.Json)
                setBody("""{"url":"https://example.com","idUsuario":1}""")
            }
            assertEquals(HttpStatusCode.Created, resp.status)
            assertTrue(resp.bodyAsText().contains("Example Domain"))
        } finally { canal.shutdownNow(); srv.shutdownNow() }
    }

    @Test fun `POST links responde 201 mesmo com gRPC fora do ar`() = testApplication {
        val canal = InProcessChannelBuilder.forName("inexistente").build()
        environment { config = MapApplicationConfig() }
        application { this@application.install(ContentNegotiation) { jackson() }; routing { linkRoutes(RepoEmMemoria(), ExtratorDeMetadadosGrpc(canal)) } }
        try {
            val resp = client.post("/links") { contentType(ContentType.Application.Json); setBody("""{"url":"https://example.com","idUsuario":1}""") }
            assertEquals(HttpStatusCode.Created, resp.status)
            assertTrue(resp.bodyAsText().contains(""""titulo":null"""))
        } finally { canal.shutdownNow() }
    }

    @Test fun `POST links rejeita URL invalida com 400 e GET health responde 200`() = testApplication {
        environment { config = MapApplicationConfig() }
        val ext = object : ExtratorDeMetadados { override fun extrair(url: String) = fail("nao devia chamar") }
        application { this@application.install(ContentNegotiation) { jackson() }; configureRouting(RepoEmMemoria(), ext) }
        val bad = client.post("/links") { contentType(ContentType.Application.Json); setBody("""{"url":"ftp://x.com","idUsuario":1}""") }
        assertEquals(HttpStatusCode.BadRequest, bad.status)
        assertTrue(bad.bodyAsText().contains("url-invalida"))
        assertEquals(HttpStatusCode.NotFound, client.get("/links/999").status)
        val ok = client.get("/health")
        assertEquals(HttpStatusCode.OK, ok.status)
        assertTrue(ok.bodyAsText().contains("ok"))
    }
}
