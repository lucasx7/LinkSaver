package com.lucasx7.linksaver.presentation.lista

import com.lucasx7.linksaver.domain.Link

sealed interface ListaLinksUiState {
    data object Carregando : ListaLinksUiState

    data object Vazio : ListaLinksUiState

    data class Sucesso(
        val links: List<Link>,
        val busca: String = "",
        val totalSemFiltro: Int = links.size,
        val avisoOffline: String? = null,
    ) : ListaLinksUiState

    data class Erro(
        val mensagem: String,
    ) : ListaLinksUiState
}
