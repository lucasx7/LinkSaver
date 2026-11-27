package com.lucasx7.linksaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegrasTest {
    @Test
    fun extrairUrlExtraiLinkLimpoOuRetornaNull() {
        assertEquals("https://youtu.be/abc", extrairUrl("https://youtu.be/abc"))
        assertEquals(
            "https://youtu.be/abc?t=10",
            extrairUrl("Veja isso: https://youtu.be/abc?t=10."),
        )
        assertNull(extrairUrl("Texto sem nenhum link"))
    }

    @Test
    fun normalizarUrlAdicionaHttpsQuandoDominioSemProtocolo() {
        assertEquals("https://youtube.com/watch?v=1", normalizarUrl("youtube.com/watch?v=1"))
        assertEquals("https://ufrn.br", normalizarUrl("  ufrn.br  "))
        assertEquals("http://localhost:8080", normalizarUrl("http://localhost:8080"))
        assertEquals("ftp://x.com", normalizarUrl("ftp://x.com"))
        assertEquals("texto invalido", normalizarUrl("texto invalido"))
    }

    @Test
    fun tagDoSiteIdentificaDominioOuRetornaNull() {
        assertEquals("youtube", tagDoSite("youtu.be/abc"))
        assertEquals("youtube", tagDoSite("https://m.youtube.com/watch?v=1"))
        assertEquals("github", tagDoSite("https://github.com/lucasx7/LinkSaver"))
        assertNull(tagDoSite("ftp://x.com"))
    }

    @Test
    fun filtrarLinksBuscaPorUrlDescricaoOuTag() {
        val links =
            listOf(
                Link(1, "https://kotlinlang.org", "Docs oficiais", listOf("dev"), 1L, null),
                Link(2, "https://youtu.be/abc", "Aula de Compose", listOf("youtube"), 2L, null),
            )
        assertEquals(listOf(1), filtrarLinks(links, "KOTLIN").map { it.id })
        assertEquals(listOf(2), filtrarLinks(links, "compose").map { it.id })
        assertEquals(listOf(2), filtrarLinks(links, "you").map { it.id })
        assertEquals(2, filtrarLinks(links, "  ").size)
    }
}
