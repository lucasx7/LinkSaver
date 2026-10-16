package com.lucasx7.linksaver

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.lucasx7.linksaver.dominio.Link
import com.lucasx7.linksaver.dominio.dominioDe
import com.lucasx7.linksaver.dominio.validarUrl
import com.lucasx7.linksaver.ui.navegacao.Adicionar
import com.lucasx7.linksaver.ui.navegacao.Detalhe
import com.lucasx7.linksaver.ui.navegacao.Lista
import com.lucasx7.linksaver.ui.telas.TelaDetalheLink
import kotlin.time.Clock

@Composable
fun App() {
    var links by remember { mutableStateOf(linksDeExemplo()) }
    
    // Estado unificado da navegação (sobrevive a qualquer rotação)
    var rotaAtual by rememberSaveable { mutableStateOf("lista") } // "lista", "adicionar", "detalhe"
    var idSelecionado by rememberSaveable { mutableStateOf(1) }

    // Estado do formulário no nível raiz (sobrevive a qualquer rotação)
    var formUrl by rememberSaveable { mutableStateOf("") }
    var formDescricao by rememberSaveable { mutableStateOf("") }
    var formTagsCsv by rememberSaveable { mutableStateOf("") }

    val navController = rememberNavController()

    MaterialTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val ehTelaLarga = maxWidth >= 600.dp

            if (!ehTelaLarga) {
                // Modo Estreito: Celular em pé
                NavHost(
                    navController = navController,
                    startDestination = Lista,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable<Lista> {
                        rotaAtual = "lista"
                        TelaListaLinks(
                            links = links,
                            onAbrir = { id ->
                                idSelecionado = id
                                rotaAtual = "detalhe"
                                navController.navigate(Detalhe(id))
                            },
                            onAdicionar = {
                                rotaAtual = "adicionar"
                                navController.navigate(Adicionar)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    composable<Adicionar> {
                        rotaAtual = "adicionar"
                        TelaAdicionarLink(
                            url = formUrl,
                            onUrlChange = { formUrl = it },
                            descricao = formDescricao,
                            onDescricaoChange = { formDescricao = it },
                            tagsCsv = formTagsCsv,
                            onTagsCsvChange = { formTagsCsv = it },
                            jaExistentes = links.map { it.url },
                            onVoltar = {
                                rotaAtual = "lista"
                                navController.popBackStack()
                            },
                            onSalvar = { url, descricao, tags ->
                                val agora = Clock.System.now().toEpochMilliseconds()
                                val proximoId = (links.maxOfOrNull { it.id } ?: 0) + 1

                                val novoLink = Link(
                                    id = proximoId,
                                    url = url.trim(),
                                    descricao = descricao.trim(),
                                    tags = tags,
                                    salvoEm = agora,
                                    sincronizadoEm = null
                                )

                                links = links + novoLink
                                idSelecionado = proximoId
                                formUrl = ""
                                formDescricao = ""
                                formTagsCsv = ""
                                rotaAtual = "lista"
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    composable<Detalhe>(
                        deepLinks = listOf(
                            navDeepLink<Detalhe>(basePath = "linksaver://link")
                        )
                    ) { backStackEntry ->
                        val rota = backStackEntry.toRoute<Detalhe>()
                        idSelecionado = rota.id
                        rotaAtual = "detalhe"
                        val linkEncontrado = links.find { it.id == rota.id }

                        TelaDetalheLink(
                            link = linkEncontrado,
                            onVoltar = {
                                rotaAtual = "lista"
                                navController.popBackStack()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                // Modo Largo: Celular deitado / Tablet (Dois Painéis)
                Row(modifier = Modifier.fillMaxSize()) {
                    // Painel Esquerdo: Lista de links (40%)
                    TelaListaLinks(
                        links = links,
                        onAbrir = { id ->
                            idSelecionado = id
                            rotaAtual = "detalhe"
                        },
                        onAdicionar = {
                            rotaAtual = "adicionar"
                        },
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                    )

                    // Painel Direito: Detalhes ou Formulário (60%)
                    Box(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                    ) {
                        if (rotaAtual == "adicionar") {
                            // Se estava adicionando, continua adicionando com o texto intacto!
                            TelaAdicionarLink(
                                url = formUrl,
                                onUrlChange = { formUrl = it },
                                descricao = formDescricao,
                                onDescricaoChange = { formDescricao = it },
                                tagsCsv = formTagsCsv,
                                onTagsCsvChange = { formTagsCsv = it },
                                jaExistentes = links.map { it.url },
                                onVoltar = { rotaAtual = "lista" },
                                onSalvar = { url, descricao, tags ->
                                    val agora = Clock.System.now().toEpochMilliseconds()
                                    val proximoId = (links.maxOfOrNull { it.id } ?: 0) + 1

                                    val novoLink = Link(
                                        id = proximoId,
                                        url = url.trim(),
                                        descricao = descricao.trim(),
                                        tags = tags,
                                        salvoEm = agora,
                                        sincronizadoEm = null
                                    )

                                    links = links + novoLink
                                    idSelecionado = proximoId
                                    formUrl = ""
                                    formDescricao = ""
                                    formTagsCsv = ""
                                    rotaAtual = "detalhe"
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Exibe os detalhes do link selecionado
                            val linkParaExibir = links.find { it.id == idSelecionado }
                                ?: links.firstOrNull()

                            TelaDetalheLink(
                                link = linkParaExibir,
                                onVoltar = { rotaAtual = "lista" },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelaListaLinks(
    links: List<Link>,
    onAbrir: (Int) -> Unit,
    onAdicionar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdicionar,
                modifier = Modifier.semantics {
                    contentDescription = "Adicionar link"
                }
            ) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .safeContentPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "LinkSaver",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .semantics { heading() }
            )

            Text(
                text = "Seus links salvos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ListaDeLinks(
                links = links,
                onAbrir = onAbrir,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ListaDeLinks(
    links: List<Link>,
    onAbrir: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (links.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Nenhum link salvo ainda. Toque em + para adicionar o primeiro.")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            items(
                items = links,
                key = { link -> link.id }
            ) { link ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAbrir(link.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = dominioDe(link.url),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = link.url,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

private fun linksDeExemplo(): List<Link> = listOf(
    Link(
        id = 1,
        url = "https://kotlinlang.org/docs",
        descricao = "Documentação do Kotlin",
        tags = listOf("Kotlin"),
        salvoEm = 1_760_000_000_000L,
        sincronizadoEm = null
    ),
    Link(
        id = 2,
        url = "https://developer.android.com/jetpack/compose",
        descricao = "Documentação do Jetpack Compose",
        tags = listOf("Android", "Compose"),
        salvoEm = 1_760_000_000_000L,
        sincronizadoEm = 1_760_000_001_000L
    )
)

@Preview
@Composable
fun AppPreview() {
    MaterialTheme {
        TelaListaLinks(
            links = linksDeExemplo(),
            onAbrir = {},
            onAdicionar = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
fun ListaVaziaPreview() {
    MaterialTheme {
        TelaListaLinks(
            links = emptyList(),
            onAbrir = {},
            onAdicionar = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
