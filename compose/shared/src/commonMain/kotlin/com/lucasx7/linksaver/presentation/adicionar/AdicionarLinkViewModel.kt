package com.lucasx7.linksaver.presentation.adicionar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
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

    init {
        atualizarUrlsExistentes()
    }

    fun atualizarUrlsExistentes() {
        viewModelScope.launch {
            try {
                linksExistentes = repository.listar()
                if (_estado.value.url.isNotEmpty()) aoMudarUrl(_estado.value.url)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    fun aoMudarUrl(novaUrl: String) {
        val limpa = novaUrl.trim()
        val erro = validarUrl(limpa, urlsParaValidar())
        _estado.update {
            it.copy(url = novaUrl, erroUrl = erro, podeSalvar = erro == null && limpa.isNotEmpty() && !it.salvando)
        }
    }

    fun aoMudarDescricao(novaDescricao: String) {
        _estado.update { it.copy(descricao = novaDescricao) }
    }

    fun aoMudarTextoTag(novoTexto: String) {
        _estado.update { it.copy(textoTag = novoTexto) }
    }

    fun adicionarTag() {
        _estado.update { atual ->
            val tagLimpa = atual.textoTag.trim()
            val novasTags = if (tagLimpa.isNotEmpty() && tagLimpa !in atual.tags) atual.tags + tagLimpa else atual.tags
            atual.copy(tags = novasTags, textoTag = "")
        }
    }

    fun preencherCompartilhado(
        url: String,
        descricao: String = "",
    ) {
        _estado.value = AdicionarLinkUiState(descricao = descricao.trim())
        aoMudarUrl(url)
        atualizarUrlsExistentes()
    }

    private fun urlsParaValidar(): List<String> = linksExistentes.filter { it.id != _estado.value.idEdicao }.map { it.url }

    fun iniciarNovo() {
        _estado.value = AdicionarLinkUiState()
        atualizarUrlsExistentes()
    }

    fun carregarParaEdicao(id: Int) {
        viewModelScope.launch {
            linksExistentes = runCatching { repository.listar() }.getOrDefault(emptyList())
            val l =
                linksExistentes.find { it.id == id }
                    ?: repository.buscarPorId(id) ?: return@launch
            _estado.value =
                AdicionarLinkUiState(
                    l.id,
                    l.url,
                    l.descricao,
                    "",
                    l.tags,
                    podeSalvar = true,
                )
        }
    }

    fun consumirSalvo() {
        _estado.update { it.copy(salvo = false, idEdicao = null) }
    }

    fun salvar() {
        val atual = _estado.value
        val urlLimpa = atual.url.trim()
        val erro = validarUrl(urlLimpa, urlsParaValidar())
        if (erro != null) {
            _estado.update { it.copy(erroUrl = erro, podeSalvar = false) }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(salvando = true, podeSalvar = false) }
            try {
                if (atual.idEdicao != null) {
                    repository.atualizar(
                        atual.idEdicao,
                        urlLimpa,
                        atual.descricao,
                        atual.tags,
                    )
                } else {
                    repository.adicionar(urlLimpa, atual.descricao, atual.tags)
                }
                linksExistentes = repository.listar()
                _estado.value = AdicionarLinkUiState(idEdicao = atual.idEdicao, salvo = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(salvando = false, erroUrl = e.message ?: "Erro ao salvar.", podeSalvar = false) }
            }
        }
    }
}
