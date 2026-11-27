# LinkSaver

Monorepo das disciplinas de **Dispositivos Moveis** e **Web II** (UFRN - DIMAp).
**Autor:** Lucas Oliveira | **Licenca:** [MIT](LICENSE) | **Sprint 2 (30/11/2026)**

## 1. Aplicativo Movel (Compose Multiplatform)

- **Camadas (`compose/shared/src/commonMain`):** `domain/` puro, `data/` (`LinkApiClient` Ktor + cache JSON em `LinkRepositoryHibrido`),
  `presentation/` (`ViewModel` + `StateFlow` + `UiState` selado) e `di/` (Koin).
- **Recursos:** normalizacao automatica de URL (`https://`), tag automatica por site,
  chips removiveis e tags existentes, busca, abrir/copiar/editar detalhes,
  modo online/offline com sincronizacao de pendencias, erros de rede distintos,
  layout adaptativo (`BoxWithConstraints`), TalkBack, Deep Link e `ACTION_SEND`.
- **Backend no App:** aponta para `http://10.0.2.2:8080` (emulador Android) e
  `http://localhost:8080` (Desktop), com fallback para cache offline.
- **Qualidade e APK:** 45 testes (`desktopTest` + `testAndroidHostTest`),
  `ktlintCheck`, `detekt` e APK na aba **Releases**.

~~~bash
cd compose
./gradlew :shared:desktopTest :shared:testAndroidHostTest
./gradlew :shared:ktlintCheck :shared:detektCommonMainSourceSet \
  :shared:detektCommonTestSourceSet :shared:detektDesktopTestSourceSet
./gradlew :androidApp:assembleDebug
adb shell am start -a android.intent.action.VIEW -d "linksaver://link/1"
~~~

## 2. Backend (Ktor) + Microsservico Go (gRPC)

- **Arquitetura:** dominio puro Kotlin verificado por **Konsist**, dominio puro
  Go (`services/extrator-go`) e contrato Protobuf/gRPC (`buf lint`).
- **Resiliencia e API:** prazo de 2s com **degradacao graciosa** (sem Go,
  `POST /links` salva com metadados `null`), erros RFC 9457, `/health`,
  OpenAPI (`/openapi.yaml` e `/openapi`) e Flyway (`V1`–`V4`).

~~~bash
docker compose up -d --build
curl -s http://localhost:8080/health
curl -X POST http://localhost:8080/links -H "Content-Type: application/json" \
  -d '{"url":"https://example.com","descricao":"Teste","idUsuario":1}'
curl -s http://localhost:8080/links
~~~

### Testes e Banco em Nuvem (Neon + Actions)
- Backend + Testcontainers + Konsist: `cd backend && ./gradlew test`
- Servico Go: `cd services/extrator-go && go test -v ./...`
- Neon (`sslmode=require`) + GitHub Secrets; `.github/workflows/migrate.yml`
  executa `./gradlew migrarBanco` sob demanda aplicando `V1`–`V4`.

## 3. Documentacao, ADRs e Videos (5 min)
- [ADR 0001 (Go/gRPC)](docs/decisoes/0001-servico-go-extrator.md) |
  [ADR 0002 (Neon/Secrets)](docs/decisoes/0002-implantacao-e-servico-go.md)
- [Arquitetura Mobile](docs/arquitetura.md) | [Uso de IA](docs/uso-de-ia.md)
- **Video Web II (Sprint 2):** *(link sera adicionado apos a gravacao)*
- **Video Mobile (Sprint 2):** *(link sera adicionado apos a gravacao)*
- [Video Sprint 1](docs/demonstracao-video.md) | `docs/relatorio-tecnico.html`
- **CI:** `.github/workflows/ci.yml` (Android, Backend, Proto/Go e Docker).
