package com.lucasx7.linksaver.adaptadores.grpc

import com.lucasx7.linksaver.dominio.ExtratorDeMetadados
import com.lucasx7.linksaver.dominio.MetadadosExtraidos
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import linksaver.metadados.v1.Metadados.ExtrairMetadadosRequest
import linksaver.metadados.v1.MetadadosServiceGrpc
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class ExtratorDeMetadadosGrpc(
    private val canal: ManagedChannel,
    private val prazoMs: Long = 2000L,
) : ExtratorDeMetadados {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun extrair(url: String): MetadadosExtraidos = try {
        val req = ExtrairMetadadosRequest.newBuilder().setUrl(url).build()
        val resp = MetadadosServiceGrpc.newBlockingStub(canal)
            .withDeadlineAfter(prazoMs, TimeUnit.MILLISECONDS)
            .extrairMetadados(req)
        MetadadosExtraidos(resp.titulo.ifBlank { null }, resp.faviconUrl.ifBlank { null })
    } catch (e: StatusRuntimeException) {
        log.warn("Falha gRPC ao extrair metadados ({}): {}", e.status.code, e.message)
        MetadadosExtraidos()
    }

    companion object {
        fun conectar(host: String, porta: Int, prazoMs: Long = 2000L) =
            ExtratorDeMetadadosGrpc(ManagedChannelBuilder.forAddress(host, porta).usePlaintext().build(), prazoMs)
    }
}
