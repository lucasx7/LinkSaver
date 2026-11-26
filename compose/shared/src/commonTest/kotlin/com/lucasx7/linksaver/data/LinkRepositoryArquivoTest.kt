package com.lucasx7.linksaver.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LinkRepositoryArquivoTest {
    @Test
    fun persisteEntreInstancias() =
        runTest {
            val arm = ArmazenamentoEmMemoria()
            LinkRepositoryArquivo(arm, 0L, emptyList()).adicionar("https://example.com", "Ex", listOf("Web"))
            val salvo = LinkRepositoryArquivo(arm, 0L, emptyList()).listar()
            assertEquals(listOf("https://example.com"), salvo.map { it.url })
        }

    @Test
    fun removerTodosPersisteListaVazia() =
        runTest {
            val arm = ArmazenamentoEmMemoria()
            val repo = LinkRepositoryArquivo(arm, 0L)
            repo.remover(1)
            repo.remover(2)
            assertEquals(emptyList(), LinkRepositoryArquivo(arm, 0L).listar())
        }

    @Test
    fun jsonCorrompidoEUrlInvalida() =
        runTest {
            val repo = LinkRepositoryArquivo(ArmazenamentoEmMemoria().apply { salvar("{ruim") }, 0L)
            assertEquals(2, repo.listar().size)
            assertFailsWith<IllegalArgumentException> { repo.adicionar("ftp://x.com", "", emptyList()) }
        }
}
