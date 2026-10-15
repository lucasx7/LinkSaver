package com.lucasx7.linksaver.dominio

interface LinkRepository {
    fun listar(limite: Int, deslocamento: Long): List<Link>
    fun buscarPorId(id: Int): Link?
    fun criar(novo: NovoLink): Link
}
