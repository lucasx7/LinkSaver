package com.lucasx7.linksaver.presentation.detalhe

import app.cash.turbine.test
import com.lucasx7.linksaver.FakeLinkRepository
import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class DetalheLinkViewModelTest : KoinTest {
    private val linkExemplo = Link(1, "https://kotlinlang.org", "Kotlin", listOf("Dev"), 1_760_000_000_000L, null)

    private fun executarComKoin(
        fakeRepo: FakeLinkRepository,
        bloco: suspend TestScope.(DetalheLinkViewModel) -> Unit,
    ) = runTest {
        stopKoin()
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        startKoin {
            modules(
                module {
                    single<LinkRepository> { fakeRepo }
                    viewModel { DetalheLinkViewModel(get()) }
                },
            )
        }
        try {
            bloco(get())
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun carregarIdExistenteEmiteCarregandoEDepoisSucesso() =
        executarComKoin(FakeLinkRepository(listOf(linkExemplo))) { vm ->
            vm.estado.test {
                assertIs<DetalheLinkUiState.Carregando>(awaitItem())
                vm.carregar(1)
                assertEquals("https://kotlinlang.org", assertIs<DetalheLinkUiState.Sucesso>(awaitItem()).link.url)
            }
        }

    @Test
    fun carregarIdInexistenteEmiteNaoEncontrado() =
        executarComKoin(FakeLinkRepository(listOf(linkExemplo))) { vm ->
            vm.estado.test {
                assertIs<DetalheLinkUiState.Carregando>(awaitItem())
                vm.carregar(999)
                assertIs<DetalheLinkUiState.NaoEncontrado>(awaitItem())
            }
        }
}
