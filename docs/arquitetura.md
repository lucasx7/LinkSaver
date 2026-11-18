# Arquitetura do LinkSaver (Mobile — Sprint 2)

O módulo compartilhado (`compose/shared/src/commonMain/kotlin/com/lucasx7/linksaver/`) segue uma arquitetura em camadas com **injeção de dependências via Koin** e **gerenciamento de estado unidirecional (UDF)** com `ViewModel` + `StateFlow`.

## Diagrama de camadas

```text
+---------------------------------------------------+
|   presentation/                                   |
|   Telas @Composable + ViewModels + UiState selado |
+-------------------------+-------------------------+
                          | depende de
                          v
+---------------------------------------------------+
|   domain/   (Kotlin puro, zero Compose/Koin/rede) |
|   Link, Regras (validarUrl, dominioDe),           |
|   DataUtil e interface LinkRepository             |
+-------------------------^-------------------------+
                          | implementa
+-------------------------+-------------------------+
|   data/                                           |
|   LinkRepositoryEmMemoria (futura API/banco)      |
+---------------------------------------------------+
```

*O pacote `di/` (`AppModule.kt`) conecta `data` e `presentation` usando o Koin (`single<LinkRepository>` e `viewModel { ... }`).*

## Papel de cada camada

- **`domain/`**: entidades (`Link`), regras de negócio puras (`validarUrl`, `dominioDe`, `formatarData`) e o contrato `LinkRepository`. Não importa Compose, Koin, Ktor nem DataStore, garantindo independência de framework.
- **`data/`**: implementação concreta `LinkRepositoryEmMemoria` (com atraso configurável para evidenciar o estado `Carregando`). Na Sprint 3 poderá ser trocada por repositório remoto/local sem alterar `ViewModel` nem telas.
- **`presentation/`**:
  - `ListaLinksViewModel` e `DetalheLinkViewModel` expõem `StateFlow` público a partir de `MutableStateFlow` privado, usando `sealed interface` (`Carregando`, `Vazio`/`NaoEncontrado`, `Sucesso`, `Erro`). As telas usam `when` sem `else` e `collectAsStateWithLifecycle()`.
  - `AdicionarLinkViewModel` centraliza o estado e a validação do formulário fora do `@Composable`.
- **`di/`**: registra o grafo de dependências no `commonMain` (`KoinApplication` + `koinViewModel()`), permitindo substituir o repositório por `FakeLinkRepository` nos testes.

## Escolha de gerenciamento de estado e alternativa descartada

**Escolha adotada (`ViewModel` multiplataforma + `StateFlow` + `UiState` selado + Koin):**
Mantém regras de validação e carregamento fora das funções `@Composable`, preserva o estado da tela em mudanças de layout (estreito/largo na rotação) e obriga a interface a tratar todos os estados possíveis (`when` exaustivo). Além disso, possibilita testar toda a lógica em milissegundos na JVM com `StandardTestDispatcher`, `Turbine` e `FakeLinkRepository`.

**Alternativa descartada (estado local com `remember`/`rememberSaveable` direto no `App.kt` ou `object` global):**
Na Sprint 1 o estado ficava centralizado em `App.kt` com `remember`. Essa abordagem foi descartada para a Sprint 2 porque mistura regras de negócio (validação de URL, geração de ID, timestamp) dentro de `@Composable`, dificulta modelar estados assíncronos (`Carregando`/`Erro`) de forma segura e impede testar a lógica isoladamente sem renderizar a interface gráfica.
