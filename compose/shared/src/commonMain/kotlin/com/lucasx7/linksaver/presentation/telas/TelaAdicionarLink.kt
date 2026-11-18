package com.lucasx7.linksaver.presentation.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lucasx7.linksaver.presentation.adicionar.AdicionarLinkUiState

@Composable
fun TelaAdicionarLink(
    estado: AdicionarLinkUiState,
    onUrlChange: (String) -> Unit,
    onDescricaoChange: (String) -> Unit,
    onTextoTagChange: (String) -> Unit,
    onAdicionarTag: () -> Unit,
    onVoltar: () -> Unit,
    onSalvar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.safeContentPadding().verticalScroll(rememberScrollState()).padding(16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onVoltar) { Text("← Voltar") }
        Text(text = "Adicionar link", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        OutlinedTextField(
            value = estado.url,
            onValueChange = onUrlChange,
            label = { Text("URL") },
            placeholder = { Text("https://exemplo.com") },
            singleLine = true,
            isError = estado.erroUrl != null,
            supportingText = { Text(estado.erroUrl ?: "Informe uma URL iniciada por http:// ou https://") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = estado.descricao,
            onValueChange = onDescricaoChange,
            label = { Text("Descrição (opcional)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(text = "Tags", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = estado.textoTag,
                onValueChange = onTextoTagChange,
                label = { Text("Nova tag") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onAdicionarTag) { Text("Adicionar") }
        }
        if (estado.tags.isNotEmpty()) Text("Tags adicionadas: ${estado.tags.joinToString(", ")}")
        Button(onClick = onSalvar, enabled = estado.podeSalvar, modifier = Modifier.fillMaxWidth()) { Text("Salvar link") }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
