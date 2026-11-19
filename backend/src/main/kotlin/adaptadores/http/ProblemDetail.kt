package com.lucasx7.linksaver.adaptadores.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

data class ProblemDetail(
    val type: String, val title: String, val status: Int,
    val detail: String, val instance: String,
)

private val problemMapper = jacksonObjectMapper()

suspend fun ApplicationCall.responderProblema(
    status: HttpStatusCode, type: String, title: String, detail: String,
) {
    val json = problemMapper.writeValueAsString(ProblemDetail(type, title, status.value, detail, request.uri))
    respondText(json, ContentType.Application.ProblemJson, status)
}
