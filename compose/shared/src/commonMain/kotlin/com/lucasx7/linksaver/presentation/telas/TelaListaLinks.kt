package com.lucasx7.linksaver.presentation.telas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lucasx7.linksaver.domain.Link
import com.lucasx7.linksaver.domain.dominioDe
import com.lucasx7.linksaver.presentation.lista.ListaLinksUiState

@Composable
fun TelaListaLinks(
    estado: ListaLinksUiState,
    onAbrir: (Int) -> Unit,
    onAdicionar: () -> Unit,
    onTentarDeNovo: () -> Unit,
    onRemover: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdicionar,
                modifier = Modifier.semantics { contentDescription = "Adicionar link" },
            ) { Text("+") }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .safeContentPadding()
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "LinkSaver",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 16.dp).semantics { heading() },
            )
            Text(
                text = "Seus links salvos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when (estado) {
                ListaLinksUiState.Carregando ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = "Carregando links" })
                            Text("Carregando links...")
                        }
                    }
                ListaLinksUiState.Vazio ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Nenhum link salvo ainda. Toque em + para adicionar o primeiro.")
                    }
                is ListaLinksUiState.Erro ->
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(text = estado.mensagem, color = MaterialTheme.colorScheme.error)
                            Button(onClick = onTentarDeNovo) { Text("Tentar de novo") }
                        }
                    }
                is ListaLinksUiState.Sucesso ->
                    ListaLinksSucesso(
                        links = estado.links,
                        onAbrir = onAbrir,
                        onRemover = onRemover,
                        modifier = Modifier.weight(1f),
                    )
            }
        }
    }
}

@Composable
private fun ListaLinksSucesso(
    links: List<Link>,
    onAbrir: (Int) -> Unit,
    onRemover: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        items(items = links, key = { it.id }) { link ->
            val dominio = dominioDe(link.url)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onAbrir(link.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = dominio, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = link.url,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(
                        onClick = { onRemover(link.id) },
                        modifier = Modifier.semantics { contentDescription = "Remover $dominio" },
                    ) { Text("Remover") }
                }
            }
        }
    }
}
