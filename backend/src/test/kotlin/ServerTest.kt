package com.lucasx7.linksaver
import com.lucasx7.linksaver.dominio.*
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.*
class ServerTest {
    @Test fun `test root endpoint`() = testApplication {
        environment { config = MapApplicationConfig() }
        val r = object : LinkRepository { override fun listar(limite: Int, deslocamento: Long) = emptyList<Link>(); override fun buscarPorId(id: Int): Link? = null; override fun criar(novo: NovoLink) = error("x") }
        val e = object : ExtratorDeMetadados { override fun extrair(url: String) = MetadadosExtraidos() }
        application { configureSerialization(); configureRouting(r, e) }
        assertEquals(HttpStatusCode.OK, client.get("/").status)
    }
}
