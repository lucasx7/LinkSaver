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
            modules(
                module {
                    single<LinkRepository> { fakeRepo }
                    viewModel { AdicionarLinkViewModel(get()) }
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
                assertEquals(listOf("compose", "UI"), awaitItem().tags)
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
                vm.aoMudarUrl("ftp://x")
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
    fun rejeitarUrlDuplicadaExibeErroEMantemBotaoDesabilitado() =
        executarComKoin(FakeLinkRepository(listOf(linkExemplo))) { vm ->
            advanceUntilIdle()
            vm.estado.test {
                awaitItem()
                vm.aoMudarUrl("https://kotlinlang.org")
                val duplicado = awaitItem()
                assertEquals("Este link já foi salvo", duplicado.erroUrl)
                assertFalse(duplicado.podeSalvar)
            }
        }

    @Test
    fun preencherCompartilhadoPreencheCamposEValidaDuplicata() =
        executarComKoin(FakeLinkRepository(listOf(linkExemplo))) { vm ->
            advanceUntilIdle()
            vm.preencherCompartilhado("https://youtu.be/abc", "  Video teste  ")
            advanceUntilIdle()
            assertEquals("https://youtu.be/abc", vm.estado.value.url)
            assertEquals("Video teste", vm.estado.value.descricao)
            assertTrue(vm.estado.value.podeSalvar)
            vm.preencherCompartilhado("https://kotlinlang.org")
            advanceUntilIdle()
            assertEquals("Este link já foi salvo", vm.estado.value.erroUrl)
            assertFalse(vm.estado.value.podeSalvar)
        }

    @Test
    fun editarLinkMantemUrlSemAcusarDuplicataESalvaAlteracoes() {
        val repo = FakeLinkRepository(listOf(linkExemplo))
        executarComKoin(repo) { vm ->
            advanceUntilIdle()
            vm.carregarParaEdicao(1)
            advanceUntilIdle()
            vm.aoMudarUrl("https://kotlinlang.org")
            assertNull(vm.estado.value.erroUrl)
            vm.aoMudarDescricao("Kotlin atualizado")
            vm.salvar()
            advanceUntilIdle()
            assertTrue(vm.estado.value.salvo)
            assertEquals("Kotlin atualizado", repo.buscarPorId(1)?.descricao)
        }
    }

    @Test
    fun normalizaUrlSemProtocoloEAdicionaERemoveTag() {
        val repo = FakeLinkRepository(listOf(linkExemplo))
        executarComKoin(repo) { vm ->
            advanceUntilIdle()
            assertEquals(listOf("Dev"), vm.estado.value.tagsExistentes)
            vm.aoMudarUrl("youtube.com/watch?v=1")
            assertTrue(vm.estado.value.podeSalvar)
            assertEquals(listOf("youtube"), vm.estado.value.tags)
            vm.removerTag("youtube")
            vm.aoMudarUrl("youtube.com/watch?v=2")
            assertEquals(emptyList(), vm.estado.value.tags)
            vm.adicionarTag("Dev")
            vm.salvar()
            advanceUntilIdle()
            assertEquals("https://youtube.com/watch?v=2", repo.buscarPorId(2)?.url)
            assertEquals(listOf("Dev"), repo.buscarPorId(2)?.tags)
        }
    }
}
