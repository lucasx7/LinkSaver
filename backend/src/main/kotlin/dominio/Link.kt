package com.lucasx7.linksaver.dominio

import java.time.LocalDate

data class Link(
    val id: Int, val url: String, val descricao: String?,
    val dataCriacao: LocalDate, val idUsuario: Int,
    val titulo: String? = null, val faviconUrl: String? = null,
)

data class NovoLink(
    val url: String, val descricao: String?, val idUsuario: Int,
    val titulo: String? = null, val faviconUrl: String? = null,
)
