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
}
