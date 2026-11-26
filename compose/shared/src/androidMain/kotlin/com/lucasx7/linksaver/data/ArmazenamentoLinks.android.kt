package com.lucasx7.linksaver.data

import android.content.Context
import java.io.File

private var diretorioAndroid: File? = null

fun configurarArmazenamentoAndroid(context: Context) {
    diretorioAndroid = context.applicationContext.filesDir
}

private class ArmazenamentoArquivo(
    private val arquivo: File,
) : ArmazenamentoLinks {
    override fun ler(): String? = if (arquivo.exists()) arquivo.readText() else null

    override fun salvar(conteudo: String) {
        arquivo.parentFile?.mkdirs()
        arquivo.writeText(conteudo)
    }
}

internal actual fun criarArmazenamentoLinks(): ArmazenamentoLinks =
    diretorioAndroid?.let { ArmazenamentoArquivo(File(it, "links.json")) }
        ?: ArmazenamentoEmMemoria()
