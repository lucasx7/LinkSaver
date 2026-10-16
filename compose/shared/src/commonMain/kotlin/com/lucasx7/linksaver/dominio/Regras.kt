package com.lucasx7.linksaver.dominio

fun validarUrl(texto: String, jaExistentes: List<String>): String? = when {
    texto.isBlank() -> "Informe uma URL"
    !(texto.startsWith("http://") || texto.startsWith("https://")) -> "Comece com http:// ou https://"
    texto in jaExistentes -> "Este link já foi salvo"
    else -> null
}

fun dominioDe(url: String): String =
    url.substringAfter("://").substringBefore("/").removePrefix("www.")
