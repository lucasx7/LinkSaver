package com.lucasx7.linksaver.presentation.lista

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
import com.lucasx7.linksaver.domain.filtrarLinks
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
    private var todosLinks: List<Link> = emptyList()
    private var buscaAtual: String = ""

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
                val msg = e.message ?: "Não foi possível carregar os links."
                _estado.value = ListaLinksUiState.Erro(msg)
            }
        }
    }

    private suspend fun buscarEAtualizar() {
        todosLinks = repository.listar()
        publicarLista()
    }

    private fun publicarLista() {
        _estado.value =
            if (todosLinks.isEmpty()) {
                ListaLinksUiState.Vazio
            } else {
                ListaLinksUiState.Sucesso(
                    links = filtrarLinks(todosLinks, buscaAtual),
                    busca = buscaAtual,
                    totalSemFiltro = todosLinks.size,
                )
            }
    }

    fun aoMudarBusca(novaBusca: String) {
        buscaAtual = novaBusca
        if (todosLinks.isNotEmpty() || _estado.value is ListaLinksUiState.Sucesso) {
            publicarLista()
        }
    }

    fun remover(id: Int) {
        viewModelScope.launch {
            try {
                repository.remover(id)
                buscarEAtualizar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.message ?: "Não foi possível remover o link."
                _estado.value = ListaLinksUiState.Erro(msg)
            }
        }
    }

    fun adicionar(
        url: String,
        descricao: String,
        tags: List<String>,
    ) {
        viewModelScope.launch {
            try {
                repository.adicionar(url, descricao, tags)
                buscarEAtualizar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val msg = e.message ?: "Não foi possível salvar o link."
                _estado.value = ListaLinksUiState.Erro(msg)
            }
        }
    }
}
