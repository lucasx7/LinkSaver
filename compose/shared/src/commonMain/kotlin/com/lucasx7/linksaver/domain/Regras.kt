package com.lucasx7.linksaver.domain

private val regexUrl = Regex("""https?://\S+""")

fun normalizarUrl(texto: String): String {
    val limpa = texto.trim()
    val semEspaco = limpa.none { it.isWhitespace() }
    val dominio = '.' in limpa && !limpa.startsWith('.') && !limpa.endsWith('.')
    val semProto = limpa.isNotEmpty() && "://" !in limpa
    return if (semProto && semEspaco && dominio) {
        "https://$limpa"
    } else {
        limpa
    }
}

fun validarUrl(
    texto: String,
    jaExistentes: List<String>,
): String? =
    when {
        texto.isBlank() -> "Informe uma URL"
        !(texto.startsWith("http://") || texto.startsWith("https://")) ->
            "Comece com http:// ou https://"
        texto in jaExistentes -> "Este link já foi salvo"
        else -> null
    }

fun dominioDe(url: String): String = url.substringAfter("://").substringBefore("/").removePrefix("www.")

fun extrairUrl(texto: String): String? = regexUrl.find(texto)?.value?.trimEnd('.', ',', ';', ')', ']', '"', '\'')

fun tagDoSite(url: String): String? {
    val norm = normalizarUrl(url)
    if (validarUrl(norm, emptyList()) != null) return null
    val host = dominioDe(norm).substringBefore(":").lowercase()
    return when {
        host == "youtu.be" ||
            host == "youtube.com" ||
            host.endsWith(".youtube.com") -> "youtube"
        else -> host.substringBefore(".").takeIf { it.length >= 2 }
    }
}

fun filtrarLinks(
    links: List<Link>,
    busca: String,
): List<Link> {
    val termo = busca.trim()
    if (termo.isEmpty()) return links
    return links.filter { l ->
        l.url.contains(termo, ignoreCase = true) ||
            l.descricao.contains(termo, ignoreCase = true) ||
            l.tags.any { it.contains(termo, ignoreCase = true) }
    }
}
