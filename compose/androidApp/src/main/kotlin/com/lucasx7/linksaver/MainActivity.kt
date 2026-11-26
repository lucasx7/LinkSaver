package com.lucasx7.linksaver

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.lucasx7.linksaver.data.configurarArmazenamentoAndroid
import com.lucasx7.linksaver.domain.extrairUrl

class MainActivity : ComponentActivity() {
    private var linkCompartilhado by mutableStateOf<LinkCompartilhado?>(null)
    private var proximoIdEvento = 1L

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        configurarArmazenamentoAndroid(applicationContext)
        if (savedInstanceState == null) tratarCompartilhamento(intent)
        setContent { App(linkCompartilhado = linkCompartilhado) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        tratarCompartilhamento(intent)
    }

    private fun tratarCompartilhamento(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        if (intent.type?.startsWith("text/") != true) return
        val texto = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val assunto = intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()
        val url = extrairUrl(texto)
        if (url == null && texto.isBlank() && assunto.isBlank()) return
        val desc = assunto.ifBlank { if (url == null) texto.trim() else "" }
        linkCompartilhado = LinkCompartilhado(proximoIdEvento++, url.orEmpty(), desc)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
