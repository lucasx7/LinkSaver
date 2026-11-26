# Registro Crítico de Uso de Inteligência Artificial — LinkSaver (Sprint 1)

**Autor:** Lucas Oliveira  
**Disciplinas:** Desenvolvimento de Sistemas para Dispositivos Móveis / Desenvolvimento de Sistemas Web II (DIMAp - UFRN)  
**Data:** 16 de outubro de 2026  

---

## 1. Ferramentas e Modelos Utilizados
Durante o desenvolvimento do LinkSaver, foram utilizados múltiplos assistentes de IA para contornar limitações de cotas e avaliar a qualidade das soluções geradas:
1. **Claude (Anthropic):** Foi o modelo com o melhor desempenho geral no projeto. Ajudou significativamente a estruturar a ordem de prioridades (o que atacar primeiro com base nas rubricas da disciplina) e demonstrou a maior assertividade na geração de código limpo, com a menor taxa de erros ou problemas inesperados.
2. **ChatGPT (OpenAI):** Utilizado em etapas de planejamento e na proposta inicial das rotas tipadas `@Serializable` e no intent-filter do manifesto.
3. **Gemini 3.8 Flash e Gemini 3 (Google):** Utilizados em partes do backend e na implementação da navegação, responsividade e refinamentos no Android. Foram os modelos que mais frequentemente sugeriram códigos com problemas (como imports inexistentes ou soluções complexas/quebradas, a exemplo do divisor visual que causou tela branca na horizontal), o que motivou a experimentação e alternância entre as diferentes ferramentas.

---

## 2. Aplicação por Camada do Projeto

### Backend (Web II — Ktor e Exposed)
- Desenvolvido em boa parte com o auxílio do **Claude** e do **Gemini**, estruturando o domínio em camadas independentes, repositório com Exposed e rotas HTTP com paginação e respostas em formato padrão.

### Frontend Android (Sistemas Móveis — Compose Multiplatform)
- **Navegação e Deep Link:** Início com Claude e ChatGPT (definição dos destinos `@Serializable` e configuração do `intent-filter` no `AndroidManifest.xml`); integração do `NavHost` e `toRoute` com Gemini.
- **Validação e Telas:** Regras puras no domínio (`validarUrl`), impedindo URLs sem `http/https` e URLs duplicadas em memória.
- **Responsividade e Rotação:** Uso do `BoxWithConstraints` para dois painéis (40% lista e 60% detalhes/formulário).
- **Resolução de Problemas Críticos:** Foi necessário intervir e corrigir ideias complicadas sugeridas pelo Gemini (como o uso indevido de `HorizontalDivider` dentro de um `Row`, que quebrou a renderização em paisagem, e a perda de dados de formulário na rotação, resolvida com a elevação de estado com `rememberSaveable`).

---

## 3. Avaliação Comparativa e Aprendizado do Aluno
A ordem de eficácia das IAs neste projeto foi claramente: **Claude > ChatGPT > Gemini**.

O processo evidenciou que assistentes de IA não devem ser seguidos cegamente: modelos podem sugerir bibliotecas não instaladas ou estruturas que quebram o layout em tempo de execução. O papel do desenvolvedor na validação rigorosa de cada linha de código, testes manuais no emulador Android e verificação contínua no terminal foi o fator decisivo para o funcionamento integral do app.

---

## 4. Registro da Sprint 2 — Arquitetura, Estado, Testes e Qualidade (18/11/2026)

- **O que foi pedido:** refatoração em camadas (`domain`, `data`, `presentation`, `di`) em `commonMain`, configuração do Koin, criação dos 3 ViewModels (`Lista`, `Adicionar`, `Detalhe`) com `StateFlow` e `UiState` selado, testes com Turbine/Koin, testes de interface (`runComposeUiTest`), `ktlint`/`detekt` e `docs/arquitetura.md`.
- **O que foi aproveitado:** isolamento completo do domínio (conferido via `grep`), `LinkRepository` como interface com implementação em memória e dublê de teste, exaustividade de `when` na UI, 10 testes de ViewModel, 8 testes de interface em `desktopTest` e automação no CI.
- **O que foi corrigido pelo desenvolvedor:** atualização de APIs depreciadas sugeridas inicialmente (`KoinApplication(application = ...)` para `koinConfiguration` e `runComposeUiTest` para `v2`), correção de edição indevida em `libs.versions.toml`, exclusão de arquivos gerados em `build/generated/` no `ktlint`/`detekt`, ajuste de regras para `@Composable`/`@Test` no `detekt.yml` e validação contínua com `./gradlew` após cada mudança.

## 5. Registro da Sprint 2 Web II — Servico Go, Protobuf e gRPC (19/11/2026)

- **O que foi pedido:** ADR 0001, dominio Go puro (`internal/extrator`), contrato `.proto` com `buf`, servidor gRPC em Go (`bufconn` + `grpcurl`), porta/adaptador gRPC no Ktor com prazo e degradacao, e testes com `InProcessServer`.
- **O que foi aproveitado:** isolamento de dominio em Go (`DOMINIO_GO_LIMPO`) e Kotlin (`DOMINIO_KT_LIMPO`), 4 testes unitarios em Go, teste `bufconn`, 3 testes `InProcessServer` no adaptador e 2 testes de rota `POST /links` no Ktor.
- **O que foi corrigido pelo desenvolvedor:** ajuste da raiz do `buf lint`/`buf generate` dentro de `protos/`, decisao de nao versionar `services/extrator-go/gen/`, correcao do receptor `this@application.install` no Ktor 3 `testApplication` e validacao manual com `grpcurl`.

## 6. Registro da Sprint 2 Web II e Entrega Final — Qualidade, Docker, Nuvem e Unificacao (25/11/2026)

- **O que foi pedido:** regras de dominio (`RegrasLink`), testes de arquitetura com Konsist, integracao real com PostgreSQL via Testcontainers, `ProblemDetail` (RFC 9457), OpenAPI (`/openapi.yaml` e Swagger UI), `Dockerfile` multi-stage (Go e Ktor), `docker-compose.yml` com healthchecks, deploy/migrations no Neon (T10), ADR 0002 e unificacao do `README.md`.
- **O que foi aproveitado:** validacao real na VM Debian com os 3 containers saudaveis (`postgres`, `extrator`, `backend`), prova ao vivo da degradacao graciosa (`201 Created` com metadados `null` quando o Go para), `V4__usuario_padrao.sql` e `migrarBanco` no GitHub Actions aplicando `V1`..`V4` no Neon.
- **O que foi corrigido pelo desenvolvedor:** adicao de `start_period: 30s` no Postgres do Compose apos falso `unhealthy` na primeira inicializacao; substituicao do plugin `org.flywaydb.flyway` `10.11.0` (incompativel com Gradle 9.5.1 por `JavaPluginConvention`) pela task `migrarBanco` (`MigrarBanco.kt`); e unificacao dos READMEs de Mobile e Web II sem sobrescrever entregas anteriores.
- **Persistencia local extra (26/11):** adicionado `LinkRepositoryArquivo` (`expect`/`actual` para `filesDir` no Android e arquivo local no Desktop) com `LinkDto` serializado em JSON fora do `domain/`, 3 testes unitarios novos e validacao real no emulador com `force-stop`.
- **Compartilhamento Android extra (26/11):** suporte a `ACTION_SEND` (`text/plain`)
  no manifesto e `MainActivity` (`singleTop` + `onNewIntent`), funcao pura `extrairUrl`
  no dominio, `LinkCompartilhado` em arquivo proprio (`detekt`), 2 testes novos
  (24 no total) e validacao no emulador.
