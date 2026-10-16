package com.lucasx7.linksaver.ui.navegacao

import kotlinx.serialization.Serializable

@Serializable
data object Lista

@Serializable
data object Adicionar

@Serializable
data class Detalhe(val id: Int)

@Serializable
data class Editar(val id: Int)
