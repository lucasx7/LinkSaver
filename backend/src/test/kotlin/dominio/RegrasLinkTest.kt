package com.lucasx7.linksaver.dominio

import kotlin.test.*

class RegrasLinkTest {
    @Test fun `aceita http e https removendo espacos`() {
        assertEquals("https://example.com", RegrasLink.validarUrl("  https://example.com  "))
        assertEquals("http://localhost:8080/a", RegrasLink.validarUrl("http://localhost:8080/a"))
    }

    @Test fun `rejeita esquema invalido ou sem host`() {
        assertFailsWith<UrlInvalidaException> { RegrasLink.validarUrl("ftp://example.com") }
        assertFailsWith<UrlInvalidaException> { RegrasLink.validarUrl("https://") }
        assertFailsWith<UrlInvalidaException> { RegrasLink.validarUrl("texto solto") }
    }
}
