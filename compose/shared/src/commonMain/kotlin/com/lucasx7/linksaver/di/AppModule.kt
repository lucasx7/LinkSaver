package com.lucasx7.linksaver.di

import com.lucasx7.linksaver.data.LinkRepositoryEmMemoria
import com.lucasx7.linksaver.domain.LinkRepository
import org.koin.dsl.module

val appModule = module {
    single<LinkRepository> { LinkRepositoryEmMemoria() }
}
