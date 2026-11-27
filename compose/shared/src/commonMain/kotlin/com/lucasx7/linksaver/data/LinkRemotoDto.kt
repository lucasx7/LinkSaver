package com.lucasx7.linksaver.data

import com.lucasx7.linksaver.domain.Link
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
internal data class LinkRemotoDto(
    val id: Int,
    val url: String,
    val descricao: String? = null,
    val dataCriacao: String = "",
    val idUsuario: Int = 1,
    val titulo: String? = null,
    val faviconUrl: String? = null,
) {
    fun paraDominio(
        tagsLocais: List<String> = emptyList(),
        sincronizadoEm: Long? = null,
    ): Link {
        val descFinal = descricao?.takeIf { it.isNotBlank() } ?: titulo.orEmpty()
        return Link(id, url, descFinal, tagsLocais, dataIsoParaMillis(dataCriacao), sincronizadoEm)
    }
}

@Serializable
internal data class CriarLinkRemotoRequest(
    val url: String,
    val descricao: String? = null,
    val idUsuario: Int = 1,
)

private fun dataIsoParaMillis(iso: String): Long {
    val nums = iso.trim().split("-").mapNotNull { it.toIntOrNull() }
    if (nums.size != 3) return Clock.System.now().toEpochMilliseconds()
    val (ano, mes, dia) = nums
    val y = if (mes <= 2) ano - 1 else ano
    val era = y / 400
    val yoe = y - era * 400
    val doy = (153 * (mes + if (mes > 2) -3 else 9) + 2) / 5 + dia - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return ((era * 146_097L + doe - 719_468L) * 86_400L + 43_200L) * 1000L
}
