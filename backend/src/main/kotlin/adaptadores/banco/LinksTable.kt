package com.lucasx7.linksaver.adaptadores.banco

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object LinksTable : Table("links_list") {
    val id = integer("id_link").autoIncrement()
    val url = text("url")
    val descricao = text("descricao").nullable()
    val dataCriacao = date("data_criacao")
    val idUsuario = integer("id_user")
    val titulo = text("titulo").nullable()
    val faviconUrl = text("favicon_url").nullable()

    override val primaryKey = PrimaryKey(id)
}
