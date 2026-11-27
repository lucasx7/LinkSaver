# Arquitetura do LinkSaver (Mobile — Sprint 2)

O modulo compartilhado (`compose/shared/src/commonMain/kotlin/com/lucasx7/linksaver/`) usa arquitetura em camadas com **Koin**, **Ktor Client** e **UDF** (`ViewModel` + `StateFlow`).

## Diagrama de camadas

~~~text
presentation/ (Telas @Composable + ViewModels + UiState selado)
      |
      v
domain/       (Kotlin puro: Link, Regras, ErrosRede, LinkRepository)
      ^
      |
data/         LinkRepositoryHibrido
               ├── LinkApiClient (Ktor + LinkRemotoDto) -> Backend
               └── LinkRepositoryArquivo (JSON local / cache)
~~~

*`di/AppModule.kt` conecta `data/` e `presentation/` via Koin.*

## Caminho de um dado do backend ate a tela

1. `TelaListaLinks` chama `ListaLinksViewModel.carregar()`.
2. `LinkRepositoryHibrido.listar()` sobe pendentes locais (`POST /links`) e busca
   `GET /links` via `LinkApiClient` (`10.0.2.2:8080` no Android, `localhost:8080`
   no Desktop).
3. `LinkRemotoDto.paraDominio()` converte o JSON (`ignoreUnknownKeys = true`) em
   `Link` puro; falhas viram `SemConexaoException` ou `RespostaInvalidaException`.
4. O resultado atualiza o cache JSON (`LinkRepositoryArquivo`). Sem rede e com
   cache, retorna os dados locais com banner offline.
5. O `ViewModel` emite `ListaLinksUiState` no `StateFlow` para a tela coletar.

## Estado e persistencia hibrida

- **Estado (`ViewModel` + `StateFlow`):** substituiu o `remember` da Sprint 1,
  isolando regras e viabilizando testes rapidos na JVM (`Turbine` + `MockEngine`).
- **Persistencia (`LinkRepositoryHibrido` + `LinkRepositoryArquivo`):** evita
  depender apenas da rede ou apenas de arquivo local; mantem uso offline e
  sincroniza pendencias automaticamente quando o servidor volta.
