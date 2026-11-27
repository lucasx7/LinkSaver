package com.lucasx7.linksaver.presentation.adicionar

data class AdicionarLinkUiState(
    val idEdicao: Int? = null,
    val url: String = "",
    val descricao: String = "",
    val textoTag: String = "",
    val tags: List<String> = emptyList(),
    val erroUrl: String? = null,
    val podeSalvar: Boolean = false,
    val salvando: Boolean = false,
    val salvo: Boolean = false,
)
