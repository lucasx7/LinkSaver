# Demonstração em Vídeo — LinkSaver (Sprint 1)

**Autor:** Lucas Oliveira  
**Vídeo:** [https://drive.google.com/file/d/1ziwW_CBgjgGxFVXqUZOSMuAz7iTX0r5D/view?usp=sharing]  
**Duração:** 2 min 42 s  

Este documento apresenta a minutagem detalhada da demonstração em vídeo do aplicativo LinkSaver (Android com Jetpack Compose Multiplatform), comprovando o atendimento integral a todos os requisitos e rubricas da Sprint 1 da disciplina de Desenvolvimento para Dispositivos Móveis (UFRN).

---

## Tabela de Minutagem e Requisitos

| Tempo | Ação no Vídeo | Requisito / Rubrica Comprovada |
|---|---|---|
| **0:00 – 0:08** | Exibição da tela inicial de listagem com cartões e links iniciais | **Telas do MVP**: listagem com Material 3 e estado elevado |
| **0:09 – 0:28** | Abertura do formulário e validação de URL inválida sem protocolo | **Validação de Formulário**: mensagem de erro e bloqueio de submissão |
| **0:29 – 0:47** | Preenchimento de URL válida, descrição e adição dinâmica de tags em chips | **Componentes do MVP**: tags dinâmicas e formulário reativo |
| **0:48 – 0:52** | Salvamento do novo link e retorno imediato à lista atualizada | **Navegação e Estado**: fluxo de telas com estado compartilhado |
| **0:53 – 1:04** | Abertura de detalhes com status de sincronização (ícone + texto) | **Acessibilidade e Detalhes**: semântica acessível e diferenciação de estado |
| **1:07 – 1:33** | Validação de URL repetida com mensagem "Este link já foi salvo" | **Validação Avançada**: prevenção de duplicatas no cliente |
| **1:35 – 1:48** | Início do preenchimento de nova URL | **Formulário Reativo** |
| **1:49 – 1:59** | Rotação para horizontal: ativação de dois painéis lado a lado (40% lista e 60% formulário) | **Responsividade**: layout adaptativo decidido por largura (`BoxWithConstraints`) |
| **2:00 – 2:14** | Rotação de volta para vertical com dados preservados | **Arquitetura**: preservação de estado com `rememberSaveable` na rotação |
| **2:15 – 2:25** | Lista com múltiplos links adicionados | **MVP Funcional** |
| **2:26 – 2:35** | Disparo de Deep Link externo (`linksaver://link/1`) abrindo a tela de detalhes | **Navegação Tipada e Deep Link**: `intent-filter` no manifesto |
| **2:36 – 2:42** | Disparo de Deep Link com ID inexistente (`999`) exibindo "Link não encontrado" | **Resiliência e Tratamento de Erros**: navegação sem travamentos |
