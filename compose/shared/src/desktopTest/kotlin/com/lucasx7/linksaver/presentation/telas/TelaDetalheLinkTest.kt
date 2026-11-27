package com.lucasx7.linksaver.presentation.telas

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.presentation.detalhe.DetalheLinkUiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class TelaDetalheLinkTest {
    @Test
    fun naoEncontrado_exibeMensagemEAcionaVoltar() =
        runComposeUiTest {
            var voltou = false
            setContent {
                MaterialTheme {
                    TelaDetalheLink(DetalheLinkUiState.NaoEncontrado, { voltou = true })
                }
            }
            onNodeWithText("Link não encontrado").assertIsDisplayed()
            onNodeWithText("Voltar para a lista").performClick()
            assertTrue(voltou)
        }

    @Test
    fun sucesso_exibeUrlDescricaoETags() =
        runComposeUiTest {
            val link =
                Link(
                    id = 1,
                    url = "https://ufrn.br/sigaa",
                    descricao = "Portal acadêmico",
                    tags = listOf("ufrn", "estudos"),
                    salvoEm = 1_700_000_000_000L,
                    sincronizadoEm = null,
                )
            setContent {
                MaterialTheme {
                    TelaDetalheLink(DetalheLinkUiState.Sucesso(link), {})
                }
            }
            onNodeWithText("https://ufrn.br/sigaa").assertIsDisplayed()
            onNodeWithText("Portal acadêmico").assertIsDisplayed()
            onNodeWithText("ufrn").assertIsDisplayed()
            onNodeWithText("estudos").assertIsDisplayed()
        }

    @Test
    fun sucesso_exibeAcoesCopiarEAcionaEditar() =
        runComposeUiTest {
            var editId = -1
            val link = Link(1, "https://ufrn.br", "", emptyList(), 1_700_000_000_000L, null)
            setContent {
                MaterialTheme {
                    TelaDetalheLink(DetalheLinkUiState.Sucesso(link), {}) { editId = it }
                }
            }
            onNodeWithText("Abrir link").assertIsDisplayed()
            onNodeWithText("Copiar URL").performClick()
            onNodeWithText("✓ Copiado!").assertIsDisplayed()
            onNodeWithText("Editar").performClick()
            assertEquals(1, editId)
        }
}
