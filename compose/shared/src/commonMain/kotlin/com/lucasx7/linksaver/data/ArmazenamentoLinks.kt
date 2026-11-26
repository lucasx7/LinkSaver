package com.lucasx7.linksaver.data

internal interface ArmazenamentoLinks {
    fun ler(): String?

    fun salvar(conteudo: String)
}

internal class ArmazenamentoEmMemoria : ArmazenamentoLinks {
    private var dados: String? = null

    override fun ler(): String? = dados

    override fun salvar(conteudo: String) {
        dados = conteudo
    }
}

internal expect fun criarArmazenamentoLinks(): ArmazenamentoLinks
