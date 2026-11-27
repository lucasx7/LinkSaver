package com.lucasx7.linksaver.presentation.telas

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.presentation.lista.ListaLinksUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TelaListaLinksTest {
    @Test
    fun carregando_exibeIndicadorETexto() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    TelaListaLinks(ListaLinksUiState.Carregando, {}, {}, {}, {})
                }
            }
            onNodeWithContentDescription("Carregando links").assertIsDisplayed()
            onNodeWithText("Carregando links...").assertIsDisplayed()
        }

    @Test
    fun vazio_exibeMensagemInicial() =
        runComposeUiTest {
            setContent {
                MaterialTheme {
                    TelaListaLinks(ListaLinksUiState.Vazio, {}, {}, {}, {})
                }
            }
            onNodeWithText("Nenhum link salvo ainda. Toque em + para adicionar o primeiro.").assertIsDisplayed()
        }

    @Test
    fun erro_exibeMensagemEAcionaTentarDeNovo() =
        runComposeUiTest {
            var tentouDeNovo = false
            setContent {
                MaterialTheme {
                    TelaListaLinks(ListaLinksUiState.Erro("Falha de rede"), {}, {}, { tentouDeNovo = true }, {})
                }
            }
            onNodeWithText("Falha de rede").assertIsDisplayed()
            onNodeWithText("Tentar de novo").performClick()
            assertTrue(tentouDeNovo)
        }

    @Test
    fun sucesso_exibeLinkEAcionaRemover() =
        runComposeUiTest {
            var idRemovido = -1
            val links =
                listOf(
                    Link(
                        id = 7,
                        url = "https://exemplo.com/a",
                        descricao = "",
                        tags = emptyList(),
                        salvoEm = 1_700_000_000_000L,
                        sincronizadoEm = null,
                    ),
                )
            setContent {
                MaterialTheme {
                    TelaListaLinks(ListaLinksUiState.Sucesso(links), {}, {}, {}, { idRemovido = it })
                }
            }
            onNodeWithText("exemplo.com").assertIsDisplayed()
            onNodeWithContentDescription("Remover exemplo.com").performClick()
            assertEquals(7, idRemovido)
        }

    @Test
    fun sucesso_exibeTagsECampoDeBusca() =
        runComposeUiTest {
            var busca = ""
            val link = Link(1, "https://youtu.be/abc", "", listOf("youtube"), 1L, null)
            setContent {
                TelaListaLinks(
                    ListaLinksUiState.Sucesso(listOf(link)),
                    {},
                    {},
                    {},
                    {},
                    onBuscaChange = { busca = it },
                )
            }
            onNodeWithText("youtube").assertIsDisplayed()
            onNodeWithText("Buscar por URL, descrição ou tag").performTextInput("dev")
            assertEquals("dev", busca)
        }
}
