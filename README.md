# LinkSaver 📌

Repositório unificado monorepo para as disciplinas de Dispositivos Móveis e Web II (UFRN - DIMAp).

Autor: Lucas Oliveira  
Entrega: Sprint 1 (16/10/2026)  

## 📱 1. Aplicativo Móvel (Compose Multiplatform)
- Telas do MVP: Lista em LazyColumn, formulário e tela de detalhes.
- Validação: Bloqueio de URLs sem http/https e URLs duplicadas.
- Tags: Adição dinâmica em chips.
- Navegação: Rotas tipadas com @Serializable e Deep Link linksaver://link.
- Responsividade: Layout adaptativo (BoxWithConstraints) em duas larguras com estado preservado na rotação via rememberSaveable.
- Acessibilidade: Suporte a TalkBack, headings e alvos de toque >= 48dp.

## 🖥️ 2. Servidor Backend (Kotlin + Ktor + Exposed)
- Camadas: Domínio limpo isolado, repositório Exposed e rotas HTTP.
- Migrações: Versionamento do PostgreSQL 17 com Flyway (V1 e V2).
- Segurança: Senha do banco lida obrigatoriamente de DB_PASSWORD.
- Endpoints: POST /links (201 Created + Location), GET /links (paginado) e GET /links/{id}.
- Banco de Dados: Subir com docker compose up -d.

## 🎬 3. Demonstração em Vídeo e Documentação
- Vídeo e Minutagem detalhada: docs/demonstracao-video.md (2 min 42 s).
- Relatório de Uso de IA: docs/uso-de-ia.md.
- Relatório Técnico Completo: docs/relatorio-tecnico.html.

## ⚙️ 4. Integração Contínua (CI/CD)
- GitHub Actions (.github/workflows/ci.yml) compilando Android e Backend com selo verde.
