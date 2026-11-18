package com.lucasx7.linksaver.presentation.adicionar

import app.cash.turbine.test
import com.lucasx7.linksaver.FakeLinkRepository
import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.LinkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AdicionarLinkViewModelTest : KoinTest {
    private val linkExemplo = Link(1, "https://kotlinlang.org", "Kotlin", listOf("Dev"), 1_760_000_000_000L, null)

    private fun executarComKoin(
        fakeRepo: FakeLinkRepository,
        bloco: suspend TestScope.(AdicionarLinkViewModel) -> Unit,
    ) = runTest {
        stopKoin()
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        startKoin {
            modules(module {
                single<LinkRepository> { fakeRepo }
                viewModel { AdicionarLinkViewModel(get()) }
            })
        }
        try {
            bloco(get())
        } finally {
            stopKoin()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun adicionarLinkValidoSalvaNoRepositorio() {
        val repo = FakeLinkRepository(listOf(linkExemplo))
        executarComKoin(repo) { vm ->
            advanceUntilIdle()
            vm.estado.test {
                awaitItem()
                vm.aoMudarUrl("https://compose.dev")
                assertTrue(awaitItem().podeSalvar)
                vm.aoMudarTextoTag("  UI  ")
                awaitItem()
                vm.adicionarTag()
                assertEquals(listOf("UI"), awaitItem().tags)
                vm.salvar()
                assertTrue(awaitItem().salvando)
                assertTrue(awaitItem().salvo)
            }
            assertNotNull(repo.buscarPorId(2))
        }
    }

    @Test
    fun rejeitarUrlInvalidaMantemBotaoDesabilitado() {
        val repo = FakeLinkRepository(listOf(linkExemplo))
        executarComKoin(repo) { vm ->
            advanceUntilIdle()
            vm.estado.test {
                awaitItem()
                vm.aoMudarUrl("exemplo.com")
                val invalido = awaitItem()
                assertNotNull(invalido.erroUrl)
                assertFalse(invalido.podeSalvar)
                vm.salvar()
                expectNoEvents()
            }
            assertNull(repo.buscarPorId(2))
        }
    }

    @Test
    fun rejeitarUrlDuplicadaExibeErroEMantemBotaoDesabilitado() = executarComKoin(FakeLinkRepository(listOf(linkExemplo))) { vm ->
        advanceUntilIdle()
        vm.estado.test {
            awaitItem()
            vm.aoMudarUrl("https://kotlinlang.org")
            val duplicado = awaitItem()
            assertEquals("Este link já foi salvo", duplicado.erroUrl)
            assertFalse(duplicado.podeSalvar)
        }
    }
}
