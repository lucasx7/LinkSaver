# ADR 0001 — Microsserviço Go para extração de metadados de links

## Contexto
Ao salvar um link na API Ktor, o LinkSaver precisa extrair título e URL do favicon da página informada. Essa tarefa envolve chamadas HTTP para sites externos que podem ser lentos, retornar HTML inválido ou estar fora do ar.

## Decisão
Isolar a extração de metadados em um microsserviço Go (`services/extrator-go`), comunicado via gRPC com contrato Protobuf (`v1`) e prazo rígido (`context.Context` / deadline). O serviço Go não acessa o banco PostgreSQL nem divide tabelas com a API. Se o serviço falhar ou estourar o prazo, a API degrada de forma controlada e salva o link mesmo assim. O extrator também limita a leitura do corpo HTML (1 MiB) e valida esquema `http`/`https`.

## Alternativas consideradas e descartadas
1. **Extrair metadados dentro da própria API Ktor:** páginas lentas prenderiam coroutines/threads da API principal, falhas externas contaminariam o fluxo de salvamento e o parsing HTML ficaria misturado ao domínio de links.
2. **Comunicação REST/JSON entre API e serviço Go:** exigiria manter clientes e DTOs manualmente nos dois lados, sem geração automática de código nem verificação estrita de contrato (`buf lint`).

## Consequências
- Exige manter o contrato `.proto` versionado e gerar stubs nas duas pontas durante o build.
- Isola falhas externas, aproveita nativamente `context` e concorrência do Go e protege a API contra lentidão de terceiros.
