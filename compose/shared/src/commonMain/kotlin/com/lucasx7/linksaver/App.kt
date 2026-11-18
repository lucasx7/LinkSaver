package com.lucasx7.linksaver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.lucasx7.linksaver.di.appModule
import com.lucasx7.linksaver.domain.validarUrl
import com.lucasx7.linksaver.presentation.lista.ListaLinksUiState
import com.lucasx7.linksaver.presentation.lista.ListaLinksViewModel
import com.lucasx7.linksaver.presentation.navegacao.Adicionar
import com.lucasx7.linksaver.presentation.navegacao.Detalhe
import com.lucasx7.linksaver.presentation.navegacao.Lista
import com.lucasx7.linksaver.presentation.telas.TelaDetalheLink
import com.lucasx7.linksaver.presentation.telas.TelaListaLinks
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinApplication(koinConfiguration { modules(appModule) }) {
        LinkSaverConteudo()
    }
}

@Composable
private fun LinkSaverConteudo(
    listaViewModel: ListaLinksViewModel = koinViewModel(),
) {
    val estadoLista by listaViewModel.estado.collectAsStateWithLifecycle()
    val links = (estadoLista as? ListaLinksUiState.Sucesso)?.links.orEmpty()
    var rotaAtual by rememberSaveable { mutableStateOf("lista") }
    var idSelecionado by rememberSaveable { mutableStateOf(1) }
    var formUrl by rememberSaveable { mutableStateOf("") }
    var formDescricao by rememberSaveable { mutableStateOf("") }
    var formTagsCsv by rememberSaveable { mutableStateOf("") }
    val navController = rememberNavController()

    MaterialTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val ehTelaLarga = maxWidth >= 600.dp
            if (!ehTelaLarga) {
                NavHost(navController = navController, startDestination = Lista, modifier = Modifier.fillMaxSize()) {
                    composable<Lista> {
                        rotaAtual = "lista"
                        TelaListaLinks(
                            estado = estadoLista,
                            onAbrir = { id -> idSelecionado = id; rotaAtual = "detalhe"; navController.navigate(Detalhe(id)) },
                            onAdicionar = { rotaAtual = "adicionar"; navController.navigate(Adicionar) },
                            onTentarDeNovo = { listaViewModel.carregar() },
                            onRemover = { listaViewModel.remover(it) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    composable<Adicionar> {
                        rotaAtual = "adicionar"
                        TelaAdicionarLink(
                            url = formUrl, onUrlChange = { formUrl = it },
                            descricao = formDescricao, onDescricaoChange = { formDescricao = it },
                            tagsCsv = formTagsCsv, onTagsCsvChange = { formTagsCsv = it },
                            jaExistentes = links.map { it.url },
                            onVoltar = { rotaAtual = "lista"; navController.popBackStack() },
                            onSalvar = { url, desc, tags ->
                                listaViewModel.adicionar(url, desc, tags)
                                formUrl = ""; formDescricao = ""; formTagsCsv = ""
                                rotaAtual = "lista"; navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    composable<Detalhe>(deepLinks = listOf(navDeepLink<Detalhe>(basePath = "linksaver://link"))) { backStackEntry ->
                        val rota = backStackEntry.toRoute<Detalhe>()
                        idSelecionado = rota.id; rotaAtual = "detalhe"
                        if (estadoLista is ListaLinksUiState.Carregando) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.semantics { contentDescription = "Carregando link" })
                            }
                        } else {
                            TelaDetalheLink(links.find { it.id == rota.id }, { rotaAtual = "lista"; navController.popBackStack() }, Modifier.fillMaxSize())
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    TelaListaLinks(
                        estado = estadoLista,
                        onAbrir = { id -> idSelecionado = id; rotaAtual = "detalhe" },
                        onAdicionar = { rotaAtual = "adicionar" },
                        onTentarDeNovo = { listaViewModel.carregar() },
                        onRemover = { listaViewModel.remover(it) },
                        modifier = Modifier.weight(0.4f).fillMaxHeight(),
                    )
                    Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                        if (rotaAtual == "adicionar") {
                            TelaAdicionarLink(
                                url = formUrl, onUrlChange = { formUrl = it },
                                descricao = formDescricao, onDescricaoChange = { formDescricao = it },
                                tagsCsv = formTagsCsv, onTagsCsvChange = { formTagsCsv = it },
                                jaExistentes = links.map { it.url },
                                onVoltar = { rotaAtual = "lista" },
                                onSalvar = { url, desc, tags ->
                                    listaViewModel.adicionar(url, desc, tags)
                                    formUrl = ""; formDescricao = ""; formTagsCsv = ""; rotaAtual = "detalhe"
                                },
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else if (estadoLista is ListaLinksUiState.Carregando) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.semantics { contentDescription = "Carregando link" })
                            }
                        } else {
                            TelaDetalheLink(links.find { it.id == idSelecionado } ?: links.firstOrNull(), { rotaAtual = "lista" }, Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelaAdicionarLink(
    url: String,
    onUrlChange: (String) -> Unit,
    descricao: String,
    onDescricaoChange: (String) -> Unit,
    tagsCsv: String,
    onTagsCsvChange: (String) -> Unit,
    jaExistentes: List<String>,
    onVoltar: () -> Unit,
    onSalvar: (String, String, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var textoTagInput by rememberSaveable { mutableStateOf("") }
    val tagsList = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(",")

    val urlLimpa = url.trim()
    val erroUrl = validarUrl(urlLimpa, jaExistentes)

    Column(
        modifier = modifier
            .safeContentPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onVoltar) {
            Text("← Voltar")
        }

        Text(
            text = "Adicionar link",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() }
        )

        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("URL") },
            placeholder = { Text("https://exemplo.com") },
            singleLine = true,
            isError = erroUrl != null,
            supportingText = {
                if (erroUrl != null) {
                    Text(erroUrl)
                } else {
                    Text("Informe uma URL iniciada por http:// ou https://")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = descricao,
            onValueChange = onDescricaoChange,
            label = { Text("Descrição (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textoTagInput,
                onValueChange = { textoTagInput = it },
                label = { Text("Nova tag") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            TextButton(
                onClick = {
                    val tagLimpa = textoTagInput.trim()
                    if (tagLimpa.isNotEmpty() && tagLimpa !in tagsList) {
                        val novaLista = tagsList + tagLimpa
                        onTagsCsvChange(novaLista.joinToString(","))
                    }
                    textoTagInput = ""
                }
            ) {
                Text("Adicionar")
            }
        }

        if (tagsList.isNotEmpty()) {
            Text("Tags adicionadas: ${tagsList.joinToString(", ")}")
        }

        Button(
            onClick = {
                onSalvar(urlLimpa, descricao, tagsList)
            },
            enabled = erroUrl == null && urlLimpa.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar link")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
