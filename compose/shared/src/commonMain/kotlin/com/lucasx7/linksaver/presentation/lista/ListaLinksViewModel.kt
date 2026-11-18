package com.lucasx7.linksaver.presentation.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucasx7.linksaver.domain.LinkRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListaLinksViewModel(
    private val repository: LinkRepository,
) : ViewModel() {

    private val _estado = MutableStateFlow<ListaLinksUiState>(ListaLinksUiState.Carregando)
    val estado: StateFlow<ListaLinksUiState> = _estado.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _estado.value = ListaLinksUiState.Carregando
            try {
                buscarEAtualizar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = ListaLinksUiState.Erro(
                    e.message ?: "Não foi possível carregar os links."
                )
            }
        }
    }

    private suspend fun buscarEAtualizar() {
        val links = repository.listar()
        _estado.value = if (links.isEmpty()) ListaLinksUiState.Vazio else ListaLinksUiState.Sucesso(links)
    }

    fun remover(id: Int) {
        viewModelScope.launch {
            try {
                repository.remover(id)
                buscarEAtualizar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = ListaLinksUiState.Erro(e.message ?: "Não foi possível remover o link.")
            }
        }
    }

    fun adicionar(url: String, descricao: String, tags: List<String>) {
        viewModelScope.launch {
            try {
                repository.adicionar(url, descricao, tags)
                buscarEAtualizar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = ListaLinksUiState.Erro(e.message ?: "Não foi possível salvar o link.")
            }
        }
    }
}
