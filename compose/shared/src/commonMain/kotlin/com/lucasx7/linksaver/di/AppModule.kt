package com.lucasx7.linksaver.di

import com.lucasx7.linksaver.data.LinkRepositoryEmMemoria
import com.lucasx7.linksaver.domain.LinkRepository
import com.lucasx7.linksaver.presentation.adicionar.AdicionarLinkViewModel
import com.lucasx7.linksaver.presentation.detalhe.DetalheLinkViewModel
import com.lucasx7.linksaver.presentation.lista.ListaLinksViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        single<LinkRepository> { LinkRepositoryEmMemoria() }
        viewModel { ListaLinksViewModel(get()) }
        viewModel { AdicionarLinkViewModel(get()) }
        viewModel { DetalheLinkViewModel(get()) }
    }
