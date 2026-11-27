package com.lucasx7.linksaver.domain

sealed class ErroRedeException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class SemConexaoException(
    cause: Throwable? = null,
) : ErroRedeException("Sem conexão com o servidor.", cause)

class RespostaInvalidaException(
    message: String = "Resposta inválida do servidor.",
    cause: Throwable? = null,
) : ErroRedeException(message, cause)
