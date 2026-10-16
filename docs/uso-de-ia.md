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
