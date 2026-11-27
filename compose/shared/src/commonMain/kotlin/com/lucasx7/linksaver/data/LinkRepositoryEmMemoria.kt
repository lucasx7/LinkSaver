package com.lucasx7.linksaver.data

import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
import com.lucasx7.linksaver.domain.validarUrl
import kotlinx.coroutines.delay
import kotlin.time.Clock

class LinkRepositoryEmMemoria(
    linksIniciais: List<Link> = linksDeExemplo(),
    private val atrasoMs: Long = 400L,
    private val simularErro: Boolean = false,
) : LinkRepository {
    private val links = linksIniciais.toMutableList()

    override suspend fun listar(): List<Link> {
        if (atrasoMs > 0) delay(atrasoMs)
        if (simularErro) error("Não foi possível carregar os links.")
        return links.toList()
    }

    override suspend fun buscarPorId(id: Int): Link? = links.find { it.id == id }

    override suspend fun remover(id: Int) {
        links.removeAll { it.id == id }
    }

    override suspend fun adicionar(
        url: String,
        descricao: String,
        tags: List<String>,
    ): Link {
        val urlLimpa = url.trim()
        val erro = validarUrl(urlLimpa, links.map { it.url })
        require(erro == null) { erro ?: "URL inválida" }
        val proximoId = (links.maxOfOrNull { it.id } ?: 0) + 1
        val novo =
            Link(
                id = proximoId,
                url = urlLimpa,
                descricao = descricao.trim(),
                tags = tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
                salvoEm = Clock.System.now().toEpochMilliseconds(),
                sincronizadoEm = null,
            )
        links.add(novo)
        return novo
    }

    override suspend fun atualizar(
        id: Int,
        url: String,
        descricao: String,
        tags: List<String>,
    ): Link {
        val i = links.indexOfFirst { it.id == id }
        require(i >= 0) { "Link não encontrado" }
        val limpa = url.trim()
        val erro = validarUrl(limpa, links.filter { it.id != id }.map { it.url })
        require(erro == null) { erro ?: "URL inválida" }
        links[i] =
            links[i].copy(
                url = limpa,
                descricao = descricao.trim(),
                sincronizadoEm = null,
                tags = tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
            )
        return links[i]
    }
}

private fun linksDeExemplo(): List<Link> =
    listOf(
        Link(1, "https://kotlinlang.org/docs", "Documentação do Kotlin", listOf("Kotlin"), 1_760_000_000_000L, null),
        Link(
            2,
            "https://developer.android.com/jetpack/compose",
            "Documentação do Jetpack Compose",
            listOf("Android", "Compose"),
            1_760_000_000_000L,
            1_760_000_001_000L,
        ),
    )
