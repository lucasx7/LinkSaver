package com.lucasx7.linksaver

import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
import com.lucasx7.linksaver.domain.validarUrl
import kotlinx.coroutines.delay

class FakeLinkRepository(
    linksIniciais: List<Link> = emptyList(),
    var atrasoMs: Long = 100L,
    var deveFalhar: Boolean = false,
) : LinkRepository {
    private val links = linksIniciais.toMutableList()

    override suspend fun listar(): List<Link> {
        if (atrasoMs > 0) delay(atrasoMs)
        if (deveFalhar) error("Falha simulada")
        return links.toList()
    }

    override suspend fun buscarPorId(id: Int): Link? = links.find { it.id == id }

    override suspend fun adicionar(url: String, descricao: String, tags: List<String>): Link {
        val urlLimpa = url.trim()
        val erro = validarUrl(urlLimpa, links.map { it.url })
        require(erro == null) { erro ?: "URL inválida" }
        val novo = Link((links.maxOfOrNull { it.id } ?: 0) + 1, urlLimpa, descricao.trim(), tags, 1_760_000_000_000L, null)
        links.add(novo)
        return novo
    }

    override suspend fun remover(id: Int) {
        links.removeAll { it.id == id }
    }
}
