package com.lucasx7.linksaver.adaptadores.grpc

import com.lucasx7.linksaver.dominio.MetadadosExtraidos
import io.grpc.Status
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.StreamObserver
import linksaver.metadados.v1.Metadados.ExtrairMetadadosRequest
import linksaver.metadados.v1.Metadados.ExtrairMetadadosResponse
import linksaver.metadados.v1.MetadadosServiceGrpc
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtratorDeMetadadosGrpcTest {
    private fun comServidor(
        impl: MetadadosServiceGrpc.MetadadosServiceImplBase,
        prazoMs: Long = 500L,
        bloco: (ExtratorDeMetadadosGrpc) -> Unit,
    ) {
        val nome = InProcessServerBuilder.generateName()
        val srv = InProcessServerBuilder.forName(nome).addService(impl).build().start()
        val canal = InProcessChannelBuilder.forName(nome).build()
        try { bloco(ExtratorDeMetadadosGrpc(canal, prazoMs)) }
        finally { canal.shutdownNow(); srv.shutdownNow() }
    }

    @Test
    fun `devolve titulo e favicon quando servidor responde`() {
        val impl = object : MetadadosServiceGrpc.MetadadosServiceImplBase() {
            override fun extrairMetadados(req: ExtrairMetadadosRequest, obs: StreamObserver<ExtrairMetadadosResponse>) {
                obs.onNext(ExtrairMetadadosResponse.newBuilder()
                    .setTitulo("Example").setFaviconUrl("https://example.com/favicon.ico").build())
                obs.onCompleted()
            }
        }
        comServidor(impl) {
            assertEquals(MetadadosExtraidos("Example", "https://example.com/favicon.ico"), it.extrair("https://example.com"))
        }
    }
    @Test fun `degrada sem excecao quando servidor estoura o prazo`() {
        val impl = object : MetadadosServiceGrpc.MetadadosServiceImplBase() {
            override fun extrairMetadados(req: ExtrairMetadadosRequest, obs: StreamObserver<ExtrairMetadadosResponse>) {
                try { Thread.sleep(300) } catch (_: InterruptedException) {}
                obs.onNext(ExtrairMetadadosResponse.newBuilder().setTitulo("Tarde").build()); obs.onCompleted()
            }
        }
        comServidor(impl, prazoMs = 50L) { assertEquals(MetadadosExtraidos(), it.extrair("https://example.com")) }
    }

    @Test fun `degrada sem excecao quando servidor esta indisponivel`() {
        val impl = object : MetadadosServiceGrpc.MetadadosServiceImplBase() {
            override fun extrairMetadados(req: ExtrairMetadadosRequest, obs: StreamObserver<ExtrairMetadadosResponse>) {
                obs.onError(Status.UNAVAILABLE.asRuntimeException())
            }
        }
        comServidor(impl) { assertEquals(MetadadosExtraidos(), it.extrair("https://example.com")) }
    }
}
