package com.lucasx7.linksaver.presentation.detalhe

import com.lucasx7.linksaver.domain.Link

sealed interface DetalheLinkUiState {
    data object Carregando : DetalheLinkUiState
    data object NaoEncontrado : DetalheLinkUiState
    data class Sucesso(val link: Link) : DetalheLinkUiState
}
