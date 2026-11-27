package com.lucasx7.linksaver.presentation.telas

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.lucasx7.linksaver.presentation.adicionar.AdicionarLinkUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TelaAdicionarLinkTest {
    @Test
    fun urlInvalida_exibeErroEDesabilitaSalvar() =
        runComposeUiTest {
            val estado =
                AdicionarLinkUiState(
                    url = "ftp://x",
                    erroUrl = "URL inválida",
                    podeSalvar = false,
                )
            setContent {
                MaterialTheme {
                    TelaAdicionarLink(estado, {}, {}, {}, {}, {}, {})
                }
            }
            onNodeWithText("URL inválida").assertIsDisplayed()
            onNodeWithText("Salvar link").assertIsNotEnabled()
        }

    @Test
    fun estadoValido_habilitaEAcionaSalvar() =
        runComposeUiTest {
            var salvou = false
            val estado = AdicionarLinkUiState(url = "https://ufrn.br", podeSalvar = true)
            setContent {
                MaterialTheme {
                    TelaAdicionarLink(estado, {}, {}, {}, {}, {}, { salvou = true })
                }
            }
            onNodeWithText("Salvar link").assertIsEnabled()
            onNodeWithText("Salvar link").performClick()
            assertTrue(salvou)
        }

    @Test
    fun exibeTagsComRemoverETagsExistentes() =
        runComposeUiTest {
            var rem = ""
            var sel = ""
            val est =
                AdicionarLinkUiState(
                    url = "https://youtu.be/abc",
                    tags = listOf("youtube"),
                    tagsExistentes = listOf("youtube", "Dev"),
                )
            setContent {
                TelaAdicionarLink(
                    est,
                    {},
                    {},
                    {},
                    {},
                    {},
                    {},
                    onRemoverTag = { rem = it },
                    onSelecionarTagExistente = { sel = it },
                )
            }
            onNodeWithContentDescription("Remover tag youtube").performClick()
            assertEquals("youtube", rem)
            onNodeWithContentDescription("Adicionar tag Dev").performClick()
            assertEquals("Dev", sel)
        }
}
