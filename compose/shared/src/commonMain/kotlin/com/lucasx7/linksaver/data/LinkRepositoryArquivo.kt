package com.lucasx7.linksaver.data

import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
import com.lucasx7.linksaver.domain.validarUrl
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.time.Clock

@Serializable
private data class LinkDto(
    val id: Int,
    val url: String,
    val descricao: String,
    val tags: List<String>,
    val salvoEm: Long,
    val sincronizadoEm: Long? = null,
) {
    fun paraDominio(): Link = Link(id, url, descricao, tags, salvoEm, sincronizadoEm)
}

private fun Link.paraDto(): LinkDto = LinkDto(id, url, descricao, tags, salvoEm, sincronizadoEm)

internal class LinkRepositoryArquivo(
    private val armazenamento: ArmazenamentoLinks,
    private val atrasoMs: Long = 250L,
    linksPadrao: List<Link> = linksIniciaisPadrao(),
) : LinkRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val links = carregarIniciais(linksPadrao).toMutableList()

    override suspend fun listar(): List<Link> {
        if (atrasoMs > 0) delay(atrasoMs)
        return links.toList()
    }

    override suspend fun buscarPorId(id: Int): Link? = links.find { it.id == id }

    override suspend fun remover(id: Int) {
        links.removeAll { it.id == id }
        persistir()
    }

    override suspend fun adicionar(
        url: String,
        descricao: String,
        tags: List<String>,
    ): Link {
        val urlLimpa = url.trim()
        val erro = validarUrl(urlLimpa, links.map { it.url })
        require(erro == null) { erro ?: "URL invalida" }
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
        persistir()
        return novo
    }

    private fun carregarIniciais(padrao: List<Link>): List<Link> {
        val texto = armazenamento.ler() ?: return padrao
        return runCatching {
            json
                .decodeFromString(ListSerializer(LinkDto.serializer()), texto)
                .map { it.paraDominio() }
        }.getOrElse { padrao }
    }

    private fun persistir() {
        val dtos = links.map { it.paraDto() }
        armazenamento.salvar(json.encodeToString(ListSerializer(LinkDto.serializer()), dtos))
    }
}

private fun linksIniciaisPadrao(): List<Link> =
    listOf(
        Link(1, "https://kotlinlang.org/docs", "Documentacao Kotlin", listOf("Kotlin"), 1_760_000_000_000L, null),
        Link(
            2,
            "https://developer.android.com/jetpack/compose",
            "Documentacao Compose",
            listOf("Android", "Compose"),
            1_760_000_000_000L,
            1_760_000_001_000L,
        ),
    )
