package com.lucasx7.linksaver.presentation.telas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
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
    onRemoverTag: (String) -> Unit = {},
    onSelecionarTagExistente: (String) -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .safeContentPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onVoltar) { Text("← Voltar") }
        Text(
            text = if (estado.idEdicao != null) "Editar link" else "Adicionar link",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        OutlinedTextField(
            value = estado.url,
            onValueChange = onUrlChange,
            label = { Text("URL") },
            placeholder = { Text("exemplo.com ou https://exemplo.com") },
            singleLine = true,
            isError = estado.erroUrl != null,
            supportingText = {
                Text(estado.erroUrl ?: "Pode digitar só o domínio (ex.: youtube.com)")
            },
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdicionarTag() }),
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onAdicionarTag) { Text("Adicionar") }
        }
        if (estado.tags.isNotEmpty()) ChipsTagsSelecionadas(estado.tags, onRemoverTag)
        val sugestoes =
            estado.tagsExistentes.filter { t ->
                estado.tags.none { it.equals(t, ignoreCase = true) }
            }
        if (sugestoes.isNotEmpty()) ChipsTagsSugeridas(sugestoes, onSelecionarTagExistente)
        Button(
            onClick = onSalvar,
            enabled = estado.podeSalvar,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (estado.idEdicao != null) "Salvar alterações" else "Salvar link")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsTagsSelecionadas(
    tags: List<String>,
    onRemoverTag: (String) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            InputChip(
                selected = true,
                onClick = { onRemoverTag(tag) },
                label = { Text(tag) },
                shape = RoundedCornerShape(50),
                colors =
                    InputChipDefaults.inputChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                trailingIcon = { Text("✕") },
                modifier = Modifier.semantics { contentDescription = "Remover tag $tag" },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipsTagsSugeridas(
    sugestoes: List<String>,
    onSelecionar: (String) -> Unit,
) {
    Text("Tags existentes:", style = MaterialTheme.typography.bodySmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        sugestoes.forEach { tag ->
            SuggestionChip(
                onClick = { onSelecionar(tag) },
                label = { Text("+ $tag") },
                shape = RoundedCornerShape(50),
                modifier = Modifier.semantics { contentDescription = "Adicionar tag $tag" },
            )
        }
    }
}
