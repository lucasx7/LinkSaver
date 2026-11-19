package com.lucasx7.linksaver.dominio

import java.net.URI

class UrlInvalidaException(mensagem: String) : IllegalArgumentException(mensagem)

object RegrasLink {
    fun validarUrl(url: String): String {
        val limpa = url.trim()
        val uri = runCatching { URI(limpa) }.getOrNull()
            ?: throw UrlInvalidaException("URL malformada")
        val esquema = uri.scheme?.lowercase()
        if (esquema != "http" && esquema != "https") {
            throw UrlInvalidaException("URL deve usar http ou https")
        }
        if (uri.host.isNullOrBlank()) throw UrlInvalidaException("URL sem host valido")
        return limpa
    }
}
