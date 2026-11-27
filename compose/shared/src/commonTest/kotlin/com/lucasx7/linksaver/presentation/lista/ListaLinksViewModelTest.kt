package com.lucasx7.linksaver.presentation.lista

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
class ListaLinksViewModelTest : KoinTest {
    private val linkExemplo = Link(1, "https://kotlinlang.org", "Kotlin", listOf("Dev"), 1_760_000_000_000L, null)

    private fun executarComKoin(
        fakeRepo: FakeLinkRepository,
        bloco: suspend TestScope.(ListaLinksViewModel) -> Unit,
    ) = runTest {
        stopKoin()
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        startKoin {
            modules(
                module {
                    single<LinkRepository> { fakeRepo }
                    viewModel { ListaLinksViewModel(get()) }
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
    fun carregarComLinksEmiteCarregandoEDepoisSucesso() =
        executarComKoin(FakeLinkRepository(listOf(linkExemplo))) { vm ->
            vm.estado.test {
                assertIs<ListaLinksUiState.Carregando>(awaitItem())
                assertEquals(1, assertIs<ListaLinksUiState.Sucesso>(awaitItem()).links.size)
            }
        }

    @Test
    fun carregarSemLinksEmiteCarregandoEDepoisVazio() =
        executarComKoin(FakeLinkRepository(emptyList())) { vm ->
            vm.estado.test {
                assertIs<ListaLinksUiState.Carregando>(awaitItem())
                assertIs<ListaLinksUiState.Vazio>(awaitItem())
            }
        }

    @Test
    fun carregarComErroEmiteCarregandoEDepoisErro() =
        executarComKoin(FakeLinkRepository(deveFalhar = true)) { vm ->
            vm.estado.test {
                assertIs<ListaLinksUiState.Carregando>(awaitItem())
                assertEquals("Falha simulada", assertIs<ListaLinksUiState.Erro>(awaitItem()).mensagem)
            }
        }

    @Test
    fun tentarDeNovoAposErroRecuperaParaSucesso() {
        val repo = FakeLinkRepository(listOf(linkExemplo), deveFalhar = true)
        executarComKoin(repo) { vm ->
            vm.estado.test {
                assertIs<ListaLinksUiState.Carregando>(awaitItem())
                assertIs<ListaLinksUiState.Erro>(awaitItem())
                repo.deveFalhar = false
                vm.carregar()
                assertIs<ListaLinksUiState.Carregando>(awaitItem())
                assertIs<ListaLinksUiState.Sucesso>(awaitItem())
            }
        }
    }

    @Test
    fun removerUltimoLinkEmiteVazio() =
        executarComKoin(FakeLinkRepository(listOf(linkExemplo))) { vm ->
            vm.estado.test {
                assertIs<ListaLinksUiState.Carregando>(awaitItem())
                assertIs<ListaLinksUiState.Sucesso>(awaitItem())
                vm.remover(1)
                assertIs<ListaLinksUiState.Vazio>(awaitItem())
            }
        }

    @Test
    fun buscarFiltraPorTextoEMantemSucessoComTotalSemFiltro() {
        val outro = Link(2, "https://youtu.be/abc", "Video", listOf("youtube"), 2L, null)
        executarComKoin(FakeLinkRepository(listOf(linkExemplo, outro))) { vm ->
            vm.estado.test {
                assertIs<ListaLinksUiState.Carregando>(awaitItem())
                assertEquals(2, assertIs<ListaLinksUiState.Sucesso>(awaitItem()).links.size)
                vm.aoMudarBusca("youtube")
                val filtrado = assertIs<ListaLinksUiState.Sucesso>(awaitItem())
                assertEquals(listOf(2), filtrado.links.map { it.id })
                assertEquals(2, filtrado.totalSemFiltro)
                vm.aoMudarBusca("inexistente")
                val vazio = assertIs<ListaLinksUiState.Sucesso>(awaitItem())
                assertEquals(0, vazio.links.size)
                assertEquals(2, vazio.totalSemFiltro)
            }
        }
    }
}
