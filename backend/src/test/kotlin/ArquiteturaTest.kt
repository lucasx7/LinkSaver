package com.lucasx7.linksaver

import com.lemonappdev.konsist.api.Konsist
import kotlin.test.Test
import kotlin.test.assertTrue

class ArquiteturaTest {
    private val proibidosNoDominio = listOf(
        "io.grpc",
        "linksaver.metadados",
        "io.ktor",
        "org.jetbrains.exposed",
        "org.flywaydb",
        "com.lucasx7.linksaver.adaptadores",
    )
    @Test fun `dominio nao importa gRPC, stubs gerados, frameworks ou adaptadores`() {
        val dominio = Konsist.scopeFromDirectory("src/main/kotlin").files
            .filter { it.packagee?.name?.startsWith("com.lucasx7.linksaver.dominio") == true }
        assertTrue(dominio.isNotEmpty(), "Pacote dominio vazio")
        val erros = dominio.flatMap { a ->
            a.imports.filter { i -> proibidosNoDominio.any { i.name.startsWith(it) } }.map { "${a.name}: ${it.name}" }
        }
        assertTrue(erros.isEmpty(), "Dependencias proibidas no dominio: $erros")
    }

    @Test fun `adaptador gRPC nao depende de banco nem HTTP`() {
        val grpc = Konsist.scopeFromDirectory("src/main/kotlin").files
            .filter { it.packagee?.name?.startsWith("com.lucasx7.linksaver.adaptadores.grpc") == true }
        assertTrue(grpc.isNotEmpty(), "Adaptador gRPC vazio")
        val erros = grpc.flatMap { a ->
            a.imports.filter { it.name.contains(".adaptadores.banco") || it.name.contains(".adaptadores.http") }
                .map { "${a.name}: ${it.name}" }
        }
        assertTrue(erros.isEmpty(), "Acoplamento indevido no adaptador gRPC: $erros")
    }
}
