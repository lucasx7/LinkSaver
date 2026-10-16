package com.lucasx7.linksaver.dominio

/**
 * Converte timestamp em milissegundos (Long) para formato legível dd/MM/yyyy.
 * Algoritmo puramente matemático baseado no calendário gregoriano, sem bibliotecas externas.
 */
fun formatarData(millis: Long): String {
    val dias = millis / 86_400_000L
    val z = dias + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1024 + doe / 1461 - doe / 36524) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val m = (mp + (if (mp < 10) 3 else -9)).toInt()
    val ano = (y + (if (m <= 2) 1 else 0)).toInt()

    val diaStr = if (d < 10) "0$d" else "$d"
    val mesStr = if (m < 10) "0$m" else "$m"
    return "$diaStr/$mesStr/$ano"
}
