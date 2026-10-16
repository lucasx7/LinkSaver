package com.lucasx7.linksaver.dominio

data class Link(
    val id: Int,
    val url: String,
    val descricao: String,
    val tags: List<String>,
    val salvoEm: Long,
    val sincronizadoEm: Long?,
) {
    val pendente: Boolean
        get() = sincronizadoEm == null
}
