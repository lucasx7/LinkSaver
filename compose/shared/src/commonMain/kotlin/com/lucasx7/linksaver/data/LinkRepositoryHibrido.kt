package com.lucasx7.linksaver.data

import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
import com.lucasx7.linksaver.domain.RespostaInvalidaException
import com.lucasx7.linksaver.domain.SemConexaoException

internal class LinkRepositoryHibrido(
    private val api: LinkApiClient,
    private val cache: LinkRepositoryArquivo,
) : LinkRepository {
    override var avisoOffline: String? = null
        private set

    override suspend fun listar(): List<Link> =
        try {
            sincronizarPendentes()
            val remotos = api.listar()
            avisoOffline = null
            val mesclados = mesclar(remotos, cache.todosSemAtraso())
            cache.substituirTodos(mesclados)
            mesclados
        } catch (e: SemConexaoException) {
            val locais = cache.todosSemAtraso()
            if (locais.isEmpty()) {
                avisoOffline = null
                throw e
            }
            avisoOffline = "Sem conexão com o servidor — exibindo links salvos no aparelho."
            locais
        } catch (e: RespostaInvalidaException) {
            avisoOffline = null
            throw e
        }

    override suspend fun buscarPorId(id: Int): Link? = cache.buscarPorId(id)

    override suspend fun remover(id: Int) = cache.remover(id)

    override suspend fun adicionar(
        url: String,
        descricao: String,
        tags: List<String>,
    ): Link {
        val local = cache.adicionar(url, descricao, tags)
        return try {
            val remoto = api.enviar(local.url, local.descricao)
            avisoOffline = null
            val descFinal = local.descricao.ifBlank { remoto.descricao }
            cache.marcarSincronizado(local.id, remoto.sincronizadoEm ?: local.salvoEm, descFinal)
        } catch (_: SemConexaoException) {
            avisoOffline = "Sem conexão — link salvo localmente (aguardando)."
            local
        } catch (e: RespostaInvalidaException) {
            cache.remover(local.id)
            throw e
        }
    }

    override suspend fun atualizar(
        id: Int,
        url: String,
        descricao: String,
        tags: List<String>,
    ): Link = cache.atualizar(id, url, descricao, tags)

    private fun mesclar(
        remotos: List<Link>,
        locais: List<Link>,
    ): List<Link> {
        val porUrl = locais.associateBy { it.url }
        val comTags =
            remotos.map { rem ->
                val local = porUrl[rem.url]
                if (local == null) {
                    rem
                } else {
                    rem.copy(tags = local.tags, descricao = rem.descricao.ifBlank { local.descricao })
                }
            }
        val urls = comTags.map { it.url }.toSet()
        var proxId = (comTags.maxOfOrNull { it.id } ?: 0) + 1
        val pendentes = locais.filter { it.pendente && it.url !in urls }.map { it.copy(id = proxId++) }
        return comTags + pendentes
    }

    private suspend fun sincronizarPendentes() {
        val pendentes = cache.todosSemAtraso().filter { it.pendente }
        for (item in pendentes) {
            try {
                val enviado = api.enviar(item.url, item.descricao)
                val descFinal = item.descricao.ifBlank { enviado.descricao }
                cache.marcarSincronizado(
                    item.id,
                    enviado.sincronizadoEm ?: item.salvoEm,
                    descFinal,
                )
            } catch (_: RespostaInvalidaException) {
                // mantem pendente localmente se o servidor rejeitar este item
            }
        }
    }
}
