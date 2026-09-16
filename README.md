# LinkSaver
Esse é um projeto para as disciplinas Desenvolvimento de Sistemas Web II e Desenvolvimento de Sistemas Para Dispositivos Móveis, ambas do Departamento de Informática e Matemática Aplicada da UFRN.

## Visão do Produto

O produto é para quem tem dificuldades de organizar links salvos e mantê-los sincronizados em vários dispositivos, para isso o produto vai existir como uma plataforma centralizada que salva e gerência links por meio de tags atribuídas pelo usuário, também permitindo que o usuário crie regras para tags automáticas nos links salvos. O produto deve funcionar offline, salvando links no armazenamento do dispositivo, e quando disponível sincronizando com o servidor, permitindo que o usuário possa acessar links de vários dispositivos diferentes a qualquer momento.


### Definição de MVP

| Essencial |Opcional |
| - | - |
| CRUD para salvar links. | Snapshot do link salvo para visualização caso o site esteja fora do ar. |
| CRUD para criar tags para os links salvos. | Microsserviço para gerar tags automaticamente para os links salvos. |
| Pesquisa de links por URL ou tags | |
| Salvar uma cópia da lista de links, assim são acessíveis mesmo sem que o servidor esteja disponível. | |
| Microsserviço para extrair dados dos links salvos, como favicon e título. | |
| Criação e alteração de links em modo offline, com sincronização com o servidor quando se conectar novamente. | |

### Escolha das tecnologias
Decidi usar o Kotlin e Compose multiplatform para fazer o frontend e mobile, pois me permite utilizar o mesmo projeto para todas as plataformas, e também decidi usar o Kotlin com Ktor no backend para manter a mesma linguagem entre todos os projetos, reduzindo o esforço cognitivo por não precisar trocar de linguagem, e também por ser uma linguagem mais moderna e com interoperabilidade com Java.




