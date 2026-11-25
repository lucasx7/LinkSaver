# ADR 0002 - Implantacao na nuvem, migrations e servico Go

## Contexto
Para publicar o LinkSaver (T10), o banco PostgreSQL precisa ficar em nuvem com TLS, as migrations Flyway nao podem depender de senhas no repositorio e a rede do campus bloqueia conexoes diretas na porta 5432. Alem disso, o plano gratuito de PaaS dificulta manter dois servicos internos via gRPC no ar juntos.

## Decisao
1. **Banco gerenciado (Neon):** PostgreSQL em nuvem com `sslmode=require` e credenciais guardadas apenas em **GitHub Secrets** (`NEON_JDBC_URL`, `NEON_USER`, `NEON_PASSWORD`) e variaveis de ambiente do host.
2. **Migrations via CI (`migrate.yml` + Gradle Flyway):** `./gradlew flywayMigrate flywayInfo` roda sob demanda no GitHub Actions (fora do firewall local) e registra log auditavel de execucao.
3. **API Ktor portavel e servico Go:** `application.yaml` aceita `$PORT:8080` e `backend/Dockerfile` sobe a API de forma independente (degradacao graciosa sem o extrator), mantendo o stack completo (Postgres + Go + Ktor) reprodutivel via `docker compose up`.

## Alternativas descartadas
- Versionar URL/senha do banco em arquivo de configuracao (risco de vazamento).
- Aplicar SQL manualmente pelo cliente local (bloqueado pelo firewall e sem rastreabilidade).

## Consequencias
- Deploy seguro sem segredos no Git, historico de migrations verificado no Actions e demonstracao completa do gRPC mantida no `docker-compose.yml`.
