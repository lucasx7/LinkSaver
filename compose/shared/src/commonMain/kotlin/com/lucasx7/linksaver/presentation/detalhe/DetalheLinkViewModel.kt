package com.lucasx7.linksaver.presentation.detalhe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lucasx7.linksaver.domain.LinkRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetalheLinkViewModel(
    private val repository: LinkRepository,
) : ViewModel() {
    private val _estado = MutableStateFlow<DetalheLinkUiState>(DetalheLinkUiState.Carregando)
    val estado: StateFlow<DetalheLinkUiState> = _estado.asStateFlow()

    fun carregar(id: Int) {
        viewModelScope.launch {
            _estado.value = DetalheLinkUiState.Carregando
            try {
                val link = repository.buscarPorId(id)
                _estado.value = if (link == null) DetalheLinkUiState.NaoEncontrado else DetalheLinkUiState.Sucesso(link)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _estado.value = DetalheLinkUiState.NaoEncontrado
            }
        }
    }
}
