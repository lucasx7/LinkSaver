package com.lucasx7.linksaver.data

import java.io.File

private class ArmazenamentoDesktop(
    private val arquivo: File,
) : ArmazenamentoLinks {
    override fun ler(): String? = if (arquivo.exists()) arquivo.readText() else null

    override fun salvar(conteudo: String) {
        arquivo.parentFile?.mkdirs()
        arquivo.writeText(conteudo)
    }
}

internal actual fun criarArmazenamentoLinks(): ArmazenamentoLinks {
    val pasta = File(System.getProperty("user.home"), ".linksaver")
    return ArmazenamentoDesktop(File(pasta, "links.json"))
}
