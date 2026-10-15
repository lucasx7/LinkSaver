package com.lucasx7.linksaver.adaptadores.banco

import com.lucasx7.linksaver.dominio.Link
import com.lucasx7.linksaver.dominio.LinkRepository
import com.lucasx7.linksaver.dominio.NovoLink
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class LinkRepositoryExposed : LinkRepository {

    private fun ResultRow.paraLink() = Link(
        id = this[LinksTable.id],
        url = this[LinksTable.url],
        descricao = this[LinksTable.descricao],
        dataCriacao = this[LinksTable.dataCriacao],
        idUsuario = this[LinksTable.idUsuario],
    )

    override fun listar(limite: Int, deslocamento: Long): List<Link> = transaction {
        LinksTable.selectAll()
            .orderBy(LinksTable.id)
            .limit(limite, deslocamento)
            .map { it.paraLink() }
    }

    override fun buscarPorId(id: Int): Link? = transaction {
        LinksTable.selectAll()
            .where { LinksTable.id eq id }
            .singleOrNull()
            ?.paraLink()
    }

    override fun criar(novo: NovoLink): Link = transaction {
        val hoje = LocalDate.now()
        val resultado = LinksTable.insert {
            it[LinksTable.url] = novo.url
            it[LinksTable.descricao] = novo.descricao
            it[LinksTable.dataCriacao] = hoje
            it[LinksTable.idUsuario] = novo.idUsuario
        }
        Link(
            id = resultado[LinksTable.id],
            url = novo.url,
            descricao = novo.descricao,
            dataCriacao = hoje,
            idUsuario = novo.idUsuario,
        )
    }
}
