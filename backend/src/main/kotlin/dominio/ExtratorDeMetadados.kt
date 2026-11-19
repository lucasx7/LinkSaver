package com.lucasx7.linksaver.dominio

data class MetadadosExtraidos(
    val titulo: String? = null,
    val faviconUrl: String? = null,
)

interface ExtratorDeMetadados {
    fun extrair(url: String): MetadadosExtraidos
}
