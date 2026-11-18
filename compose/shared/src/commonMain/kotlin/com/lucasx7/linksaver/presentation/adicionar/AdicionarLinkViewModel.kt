package com.lucasx7.linksaver.presentation.adicionar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private var urlsExistentes: List<String> = emptyList()

    init {
        atualizarUrlsExistentes()
    }

    fun atualizarUrlsExistentes() {
        viewModelScope.launch {
            try {
                urlsExistentes = repository.listar().map { it.url }
                if (_estado.value.url.isNotEmpty()) aoMudarUrl(_estado.value.url)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
            }
        }
    }

    fun aoMudarUrl(novaUrl: String) {
        val limpa = novaUrl.trim()
        val erro = validarUrl(limpa, urlsExistentes)
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

    fun consumirSalvo() {
        _estado.update { it.copy(salvo = false) }
    }

    fun limpar() {
        _estado.value = AdicionarLinkUiState()
    }

    fun salvar() {
        val atual = _estado.value
        val urlLimpa = atual.url.trim()
        val erro = validarUrl(urlLimpa, urlsExistentes)
        if (erro != null) {
            _estado.update { it.copy(erroUrl = erro, podeSalvar = false) }
            return
        }
        viewModelScope.launch {
            _estado.update { it.copy(salvando = true, podeSalvar = false) }
            try {
                repository.adicionar(urlLimpa, atual.descricao, atual.tags)
                urlsExistentes = repository.listar().map { it.url }
                _estado.value = AdicionarLinkUiState(salvo = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(salvando = false, erroUrl = e.message ?: "Erro ao salvar.", podeSalvar = false) }
            }
        }
    }
}
