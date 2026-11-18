# LinkSaver 📌

Repositório unificado (monorepo) para as disciplinas de **Desenvolvimento de Sistemas para Dispositivos Móveis** e **Desenvolvimento de Sistemas Web II** (UFRN - DIMAp).

**Autor:** Lucas Oliveira  
**Entrega atual:** Sprint 2 — Mobile (30/11/2026)

---

## 📱 1. Aplicativo Móvel (Compose Multiplatform — Sprint 2)

- **Arquitetura em camadas (`compose/shared/src/commonMain`):** `domain/` (entidades, regras e interface `LinkRepository` sem dependências de UI/frameworks), `data/` (`LinkRepositoryEmMemoria`), `presentation/` (`ViewModel` + `StateFlow` + `UiState` selado + telas stateless) e `di/` (Koin `AppModule`).
- **Estados explícitos de UI:** `Carregando`, `Vazio`, `Sucesso` e `Erro` (com botão *"Tentar de novo"*) tratados exaustivamente via `when` sem `else`.
- **Funcionalidades do app:** lista de links, validação de URL (`http`/`https` e duplicatas), tags, tela de detalhes, layout responsivo em duas larguras (`BoxWithConstraints`), acessibilidade (TalkBack, `heading()`, alvos `>= 48dp`) e Deep Link (`linksaver://link/{id}`).
- **Qualidade e Testes:** 10 testes de ViewModel (Turbine + dublê via Koin), 8 testes de interface (`runComposeUiTest` em `desktopTest`), `ktlintCheck` e `detekt` 2.0.

### Pré-requisitos (rodar em menos de 15 min)
- **JDK 21** e **Git**
- *(Opcional, apenas para rodar no emulador/aparelho)* Android SDK / `adb` (ou baixar o APK pronto na aba **Releases** do repositório)
- *(Linux headless para testes de UI desktop)* `libgl1`, `libegl1`, `libfontconfig1`

### Comandos para testar, validar e compilar
```bash
git clone https://github.com/lucasx7/LinkSaver.git
cd LinkSaver/compose

# 1. Rodar todos os testes (10 de ViewModel + 8 de interface + 1 comum)
./gradlew :shared:desktopTest :shared:testAndroidHostTest

# 2. Rodar análise de estilo e qualidade (ktlint e detekt)
./gradlew :shared:ktlintCheck :shared:detektCommonMainSourceSet :shared:detektCommonTestSourceSet :shared:detektDesktopTestSourceSet

# 3. Gerar o APK Debug Android (compose/androidApp/build/outputs/apk/debug/androidApp-debug.apk)
./gradlew :androidApp:assembleDebug
```

Testando o Deep Link com um emulador ou aparelho conectado via `adb`:
```bash
adb shell am start -a android.intent.action.VIEW -d "linksaver://link/1"
```

---

## 🎬 2. Vídeos e Documentação

- **Vídeo da Sprint 2 (5 min):** *(link será adicionado após a gravação)*
- **Arquitetura e Decisões de Estado:** [docs/arquitetura.md](docs/arquitetura.md)
- **Registro de Uso de IA (Sprints 1 e 2):** [docs/uso-de-ia.md](docs/uso-de-ia.md)
- **Vídeo e Minutagem da Sprint 1:** [docs/demonstracao-video.md](docs/demonstracao-video.md) (2 min 42 s)
- **Relatório Técnico da Sprint 1:** `docs/relatorio-tecnico.html`

---

## 🖥️ 3. Servidor Backend (Kotlin + Ktor + Exposed)

- **Camadas:** Domínio limpo isolado, repositório Exposed e rotas HTTP.
- **Migrações:** Versionamento do PostgreSQL 17 com Flyway (`V1` e `V2`).
- **Segurança:** Senha do banco lida obrigatoriamente de `DB_PASSWORD`.
- **Endpoints:** `POST /links` (`201 Created` + `Location`), `GET /links` (paginado) e `GET /links/{id}`.
- **Banco de Dados:** Subir com `docker compose up -d`.

---

## ⚙️ 4. Integração Contínua (CI/CD)

- GitHub Actions (`.github/workflows/ci.yml`) executando `ktlintCheck`, `detekt`, testes (`desktopTest` + `testAndroidHostTest`), compilação do APK Debug Android e compilação do Backend Ktor.
