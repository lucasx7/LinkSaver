package com.lucasx7.linksaver

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}
