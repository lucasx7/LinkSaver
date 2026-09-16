package com.lucasx7.linksaver

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform