package com.lucasx7.linksaver.domain

interface LinkRepository {
    val avisoOffline: String?
        get() = null

    suspend fun listar(): List<Link>

    suspend fun buscarPorId(id: Int): Link?

    suspend fun adicionar(
        url: String,
        descricao: String,
        tags: List<String>,
    ): Link

    suspend fun atualizar(
        id: Int,
        url: String,
        descricao: String,
        tags: List<String>,
    ): Link

    suspend fun remover(id: Int)
}
