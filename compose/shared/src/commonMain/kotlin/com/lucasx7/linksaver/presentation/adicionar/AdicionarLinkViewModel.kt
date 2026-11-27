package com.lucasx7.linksaver.presentation.adicionar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
import com.lucasx7.linksaver.domain.normalizarUrl
import com.lucasx7.linksaver.domain.tagDoSite
import com.lucasx7.linksaver.domain.validarUrl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdicionarLinkViewModel(
    private val repository: LinkRepository,
) : ViewModel() {
    private val _estado = MutableStateFlow(AdicionarLinkUiState())
    val estado: StateFlow<AdicionarLinkUiState> = _estado.asStateFlow()
    private var linksExistentes: List<Link> = emptyList()
    private val tagsIgnoradas = mutableSetOf<String>()
    private var ultimaTagAuto: String? = null

    init {
        atualizarUrlsExistentes()
    }

    fun atualizarUrlsExistentes() {
        viewModelScope.launch {
            try {
                linksExistentes = repository.listar()
                val existentes = tagsExistentesDe(linksExistentes)
                _estado.update { it.copy(tagsExistentes = existentes) }
                if (_estado.value.url.isNotEmpty()) aoMudarUrl(_estado.value.url)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    fun aoMudarUrl(novaUrl: String) {
        val norm = normalizarUrl(novaUrl)
        val erro = validarUrl(norm, urlsParaValidar(linksExistentes, _estado.value.idEdicao))
        val novaTag = tagDoSite(norm)
        _estado.update { atual ->
            val tags = aplicarTagAuto(atual.tags, ultimaTagAuto, novaTag, tagsIgnoradas)
            ultimaTagAuto = if (novaTag != null && novaTag !in tagsIgnoradas) novaTag else null
            val ok = erro == null && norm.isNotEmpty() && !atual.salvando
            atual.copy(url = novaUrl, tags = tags, erroUrl = erro, podeSalvar = ok)
        }
    }

    fun aoMudarDescricao(novaDescricao: String) {
        _estado.update { it.copy(descricao = novaDescricao) }
    }

    fun aoMudarTextoTag(novoTexto: String) {
        _estado.update { it.copy(textoTag = novoTexto) }
    }

    fun adicionarTag(tagInformada: String? = null) {
        _estado.update { atual ->
            val limpa = (tagInformada ?: atual.textoTag).trim()
            val repetida = atual.tags.any { it.equals(limpa, ignoreCase = true) }
            val novas = if (limpa.isNotEmpty() && !repetida) atual.tags + limpa else atual.tags
            val proxTexto = if (tagInformada == null) "" else atual.textoTag
            atual.copy(tags = novas, textoTag = proxTexto)
        }
    }

    fun removerTag(tag: String) {
        tagsIgnoradas.add(tag)
        if (ultimaTagAuto == tag) ultimaTagAuto = null
        _estado.update { atual -> atual.copy(tags = atual.tags.filterNot { it == tag }) }
    }

    fun preencherCompartilhado(
        url: String,
        descricao: String = "",
    ) {
        tagsIgnoradas.clear()
        ultimaTagAuto = null
        val existentes = tagsExistentesDe(linksExistentes)
        _estado.value =
            AdicionarLinkUiState(descricao = descricao.trim(), tagsExistentes = existentes)
        aoMudarUrl(url)
        atualizarUrlsExistentes()
    }

    fun iniciarNovo() {
        tagsIgnoradas.clear()
        ultimaTagAuto = null
        val existentes = tagsExistentesDe(linksExistentes)
        _estado.value = AdicionarLinkUiState(tagsExistentes = existentes)
        atualizarUrlsExistentes()
    }

    fun consumirSalvo() {
        _estado.update { it.copy(salvo = false, idEdicao = null) }
    }

    fun carregarParaEdicao(id: Int) {
        viewModelScope.launch {
            linksExistentes = runCatching { repository.listar() }.getOrDefault(emptyList())
            val l =
                linksExistentes.find { it.id == id } ?: repository.buscarPorId(id)
                    ?: return@launch
            tagsIgnoradas.clear()
            ultimaTagAuto = null
            tagDoSite(l.url)?.let { t ->
                if (l.tags.none { it.equals(t, ignoreCase = true) }) tagsIgnoradas.add(t)
            }
            _estado.value =
                AdicionarLinkUiState(
                    idEdicao = l.id,
                    url = l.url,
                    descricao = l.descricao,
                    tags = l.tags,
                    tagsExistentes = tagsExistentesDe(linksExistentes),
                    podeSalvar = true,
                )
        }
    }

    fun salvar() {
        val atual = _estado.value
        val urlLimpa = normalizarUrl(atual.url)
        val erro = validarUrl(urlLimpa, urlsParaValidar(linksExistentes, atual.idEdicao))
        if (erro != null) {
            _estado.update { it.copy(erroUrl = erro, podeSalvar = false) }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(salvando = true, podeSalvar = false) }
            try {
                if (atual.idEdicao != null) {
                    repository.atualizar(atual.idEdicao, urlLimpa, atual.descricao, atual.tags)
                } else {
                    repository.adicionar(urlLimpa, atual.descricao, atual.tags)
                }
                linksExistentes = repository.listar()
                tagsIgnoradas.clear()
                ultimaTagAuto = null
                _estado.value =
                    AdicionarLinkUiState(
                        idEdicao = atual.idEdicao,
                        salvo = true,
                        tagsExistentes = tagsExistentesDe(linksExistentes),
                    )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.message ?: "Erro ao salvar."
                _estado.update { it.copy(salvando = false, erroUrl = msg, podeSalvar = false) }
            }
        }
    }
}

private fun urlsParaValidar(
    links: List<Link>,
    idEdicao: Int?,
): List<String> = links.filter { it.id != idEdicao }.map { it.url }

private fun tagsExistentesDe(links: List<Link>): List<String> = links.flatMap { it.tags }.distinct().sortedBy { it.lowercase() }

private fun aplicarTagAuto(
    tags: List<String>,
    ant: String?,
    nova: String?,
    ignoradas: Set<String>,
): List<String> {
    val base = if (ant != null && ant != nova) tags.filterNot { it == ant } else tags
    val existe = nova != null && base.any { it.equals(nova, ignoreCase = true) }
    return if (nova != null && nova !in ignoradas && !existe) base + nova else base
}
