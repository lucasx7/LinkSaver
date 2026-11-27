package com.lucasx7.linksaver

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.lucasx7.linksaver.di.appModule
import com.lucasx7.linksaver.presentation.adicionar.AdicionarLinkViewModel
import com.lucasx7.linksaver.presentation.detalhe.DetalheLinkViewModel
import com.lucasx7.linksaver.presentation.lista.ListaLinksUiState
import com.lucasx7.linksaver.presentation.lista.ListaLinksViewModel
import com.lucasx7.linksaver.presentation.navegacao.Adicionar
import com.lucasx7.linksaver.presentation.navegacao.Detalhe
import com.lucasx7.linksaver.presentation.navegacao.Editar
import com.lucasx7.linksaver.presentation.navegacao.Lista
import com.lucasx7.linksaver.presentation.telas.TelaAdicionarLink
import com.lucasx7.linksaver.presentation.telas.TelaDetalheLink
import com.lucasx7.linksaver.presentation.telas.TelaListaLinks
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

@Composable
fun App(linkCompartilhado: LinkCompartilhado? = null) {
    KoinApplication(koinConfiguration { modules(appModule) }) {
        LinkSaverConteudo(linkCompartilhado = linkCompartilhado)
    }
}

@Composable
private fun LinkSaverConteudo(
    linkCompartilhado: LinkCompartilhado? = null,
    listaViewModel: ListaLinksViewModel = koinViewModel(),
    adicionarViewModel: AdicionarLinkViewModel = koinViewModel(),
    detalheViewModel: DetalheLinkViewModel = koinViewModel(),
) {
    val estadoLista by listaViewModel.estado.collectAsStateWithLifecycle()
    val estadoAdicionar by adicionarViewModel.estado.collectAsStateWithLifecycle()
    val estadoDetalhe by detalheViewModel.estado.collectAsStateWithLifecycle()
    var rotaAtual by rememberSaveable { mutableStateOf("lista") }
    var idSelecionado by rememberSaveable { mutableStateOf(1) }
    val navController = rememberNavController()

    MaterialTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val ehTelaLarga = maxWidth >= 600.dp
            var ultimoEvento by rememberSaveable { mutableStateOf(0L) }
            LaunchedEffect(linkCompartilhado?.idEvento) {
                val ev = linkCompartilhado ?: return@LaunchedEffect
                if (ev.idEvento == ultimoEvento) return@LaunchedEffect
                ultimoEvento = ev.idEvento
                adicionarViewModel.preencherCompartilhado(ev.url, ev.descricao)
                rotaAtual = "adicionar"
                if (!ehTelaLarga) {
                    navController.navigate(Adicionar) {
                        popUpTo(Lista)
                        launchSingleTop = true
                    }
                }
            }
            LaunchedEffect(estadoAdicionar.salvo) {
                if (estadoAdicionar.salvo) {
                    val idEdit = estadoAdicionar.idEdicao
                    listaViewModel.carregar()
                    if (idEdit != null) {
                        idSelecionado = idEdit
                        detalheViewModel.carregar(idEdit)
                    }
                    adicionarViewModel.consumirSalvo()
                    if (!ehTelaLarga && navController.currentBackStackEntry != null) {
                        rotaAtual = if (idEdit != null) "detalhe" else "lista"
                        navController.popBackStack()
                    } else {
                        rotaAtual = "detalhe"
                    }
                }
            }
            if (!ehTelaLarga) {
                NavHost(navController = navController, startDestination = Lista, modifier = Modifier.fillMaxSize()) {
                    composable<Lista> {
                        rotaAtual = "lista"
                        TelaListaLinks(
                            estado = estadoLista,
                            onAbrir = { id ->
                                idSelecionado = id
                                rotaAtual = "detalhe"
                                navController.navigate(Detalhe(id))
                            },
                            onAdicionar = {
                                adicionarViewModel.iniciarNovo()
                                rotaAtual = "adicionar"
                                navController.navigate(Adicionar)
                            },
                            onTentarDeNovo = { listaViewModel.carregar() },
                            onRemover = { listaViewModel.remover(it) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    composable<Adicionar> {
                        rotaAtual = "adicionar"
                        TelaAdicionarLink(
                            estado = estadoAdicionar,
                            onUrlChange = adicionarViewModel::aoMudarUrl,
                            onDescricaoChange = adicionarViewModel::aoMudarDescricao,
                            onTextoTagChange = adicionarViewModel::aoMudarTextoTag,
                            onAdicionarTag = adicionarViewModel::adicionarTag,
                            onVoltar = {
                                rotaAtual = "lista"
                                navController.popBackStack()
                            },
                            onSalvar = adicionarViewModel::salvar,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    composable<Detalhe>(deepLinks = listOf(navDeepLink<Detalhe>(basePath = "linksaver://link"))) { backStackEntry ->
                        val rota = backStackEntry.toRoute<Detalhe>()
                        idSelecionado = rota.id
                        rotaAtual = "detalhe"
                        LaunchedEffect(rota.id) { detalheViewModel.carregar(rota.id) }
                        TelaDetalheLink(estadoDetalhe, {
                            rotaAtual = "lista"
                            navController.popBackStack()
                        }, Modifier.fillMaxSize()) { id ->
                            adicionarViewModel.carregarParaEdicao(id)
                            rotaAtual = "editar"
                            navController.navigate(Editar(id))
                        }
                    }
                    composable<Editar> {
                        rotaAtual = "editar"
                        TelaAdicionarLink(
                            estadoAdicionar,
                            adicionarViewModel::aoMudarUrl,
                            adicionarViewModel::aoMudarDescricao,
                            adicionarViewModel::aoMudarTextoTag,
                            adicionarViewModel::adicionarTag,
                            {
                                rotaAtual = "detalhe"
                                navController.popBackStack()
                            },
                            adicionarViewModel::salvar,
                            Modifier.fillMaxSize(),
                        )
                    }
                }
            } else {
                PainelLargo(
                    rotaAtual,
                    idSelecionado,
                    listaViewModel,
                    adicionarViewModel,
                    detalheViewModel,
                ) { novaRota, novoId ->
                    rotaAtual = novaRota
                    idSelecionado = novoId
                }
            }
        }
    }
}

@Composable
private fun PainelLargo(
    rotaAtual: String,
    idSelecionado: Int,
    listaViewModel: ListaLinksViewModel,
    adicionarViewModel: AdicionarLinkViewModel,
    detalheViewModel: DetalheLinkViewModel,
    onNavegar: (String, Int) -> Unit,
) {
    val estadoLista by listaViewModel.estado.collectAsStateWithLifecycle()
    val estadoAdicionar by adicionarViewModel.estado.collectAsStateWithLifecycle()
    val estadoDetalhe by detalheViewModel.estado.collectAsStateWithLifecycle()
    val links = (estadoLista as? ListaLinksUiState.Sucesso)?.links.orEmpty()
    Row(modifier = Modifier.fillMaxSize()) {
        TelaListaLinks(
            estado = estadoLista,
            onAbrir = { id -> onNavegar("detalhe", id) },
            onAdicionar = {
                adicionarViewModel.iniciarNovo()
                onNavegar("adicionar", idSelecionado)
            },
            onTentarDeNovo = { listaViewModel.carregar() },
            onRemover = { listaViewModel.remover(it) },
            modifier = Modifier.weight(0.4f).fillMaxHeight(),
        )
        Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
            if (rotaAtual == "adicionar" || rotaAtual == "editar") {
                val volta = if (rotaAtual == "editar") "detalhe" else "lista"
                TelaAdicionarLink(
                    estadoAdicionar,
                    adicionarViewModel::aoMudarUrl,
                    adicionarViewModel::aoMudarDescricao,
                    adicionarViewModel::aoMudarTextoTag,
                    adicionarViewModel::adicionarTag,
                    { onNavegar(volta, idSelecionado) },
                    adicionarViewModel::salvar,
                    Modifier.fillMaxSize(),
                )
            } else {
                val idEf =
                    links.find { it.id == idSelecionado }?.id
                        ?: links.firstOrNull()?.id ?: idSelecionado
                LaunchedEffect(idEf) { detalheViewModel.carregar(idEf) }
                TelaDetalheLink(
                    estadoDetalhe,
                    { onNavegar("lista", idSelecionado) },
                    Modifier.fillMaxSize(),
                ) { id ->
                    adicionarViewModel.carregarParaEdicao(id)
                    onNavegar("editar", id)
                }
            }
        }
    }
}
