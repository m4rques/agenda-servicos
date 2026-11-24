# Requisitos e decisões do projeto

Este documento separa três fontes: as exigências do professor no PDF, os pedidos adicionais do texto colado e as decisões adotadas para a implementação. O texto extraído está em [referencia/enunciado_extraido.txt](../referencia/enunciado_extraido.txt). A referência original é o PDF `Trabalho Final 2026.2_Estrutura_Dados.pdf`, fornecido pelo aluno.

## Exigências do professor

### Regras gerais — página 1

- Desenvolver uma aplicação real em Java, na qual estruturas de dados interajam para resolver um problema de negócio.
- Formar equipe com 2 a 4 integrantes.
- Disponibilizar o sistema no GitHub de todos os integrantes.
- Todos os integrantes devem apresentar; a ordem individual será sorteada no dia.
- Implementar manualmente as estruturas. O PDF proíbe estruturas prontas do Java, citando Collections como `ArrayList`, `LinkedList`, `Stack` e `Queue`.
- Entregar código que compile. O PDF também rejeita código com comentários, copiado, trabalhos iguais e trabalhos sem apresentação.
- Usar a ABB como estrutura principal do cadastro.

O cabeçalho informa entrega em **17/11** e apresentação em **17 ou 24/11**, conforme a ordem dos temas. O ano de **2026** decorre da identificação da disciplina como **2026.2**. O documento não fixa, individualmente, qual das duas datas corresponde ao tema 10. O valor total é de 8,00 pontos.

### Estruturas e avaliação — página 2

- ABB: inserir respeitando menor à esquerda e maior à direita; buscar pela chave; remover folha, nó com um filho e nó com dois filhos; exibir in-ordem, pré-ordem e pós-ordem.
- Implementar e usar pelo menos duas estruturas auxiliares no fluxo do negócio.
- Cada auxiliar precisa de inserir/adicionar, buscar/localizar, excluir/remover e listar.
- Implementar a interação completa entre a ABB e as auxiliares do tema.
- A avaliação considera correção das estruturas, funcionamento da aplicação, organização com orientação a objetos e limpeza, apresentação, explicação e avaliação individuais.

### Tema 10 — página 3

| Estrutura | Papel exigido |
|---|---|
| ABB | Cadastro de clientes fiéis, com busca por telefone/ID. |
| Fila | Agendamentos do dia em ordem cronológica de atendimento. |
| Pilha | Histórico de serviços cancelados para análise. |

O PDF não exige interface gráfica, banco de dados, persistência, autenticação ou bibliotecas externas. A escolha de um console simples permite concentrar o trabalho nas estruturas e em sua integração.

## Pedidos adicionais do texto colado

O pedido do aluno detalha o tema como agenda de uma barbearia e acrescenta:

- Cliente com ID inteiro único, nome e telefone; busca da ABB pelo ID; validação e rejeição de duplicados.
- Agendamento com ID único, cliente cadastrado, serviço, data, horário e valor.
- Agenda cronológica mesmo com cadastros fora de ordem; impedir conflitos para um profissional com duração fixa; selecionar o dia sem misturar datas.
- Atendimento sempre pelo início da fila. Cancelamento em qualquer posição da fila, com atualização do início e do fim.
- Cancelamento com dados do agendamento, motivo e instante do cancelamento; histórico em ordem LIFO; busca e listagem sem alterações.
- Impedir excluir cliente com agendamento pendente; preservar o histórico após exclusão posterior do cliente; validar antes de modificar estruturas.
- Menu para as operações de clientes, percursos, agendamentos, atendimentos, cancelamentos e saída, com tratamento de entradas inválidas e estruturas vazias.
- Código completo em arquivos separados, sem comentários nem Javadoc e sem Collections ou atalhos equivalentes.
- README, instruções exatas de compilação e execução, roteiro de testes, rastreabilidade, explicações das estruturas, complexidades, roteiro de apresentação, perguntas e exercícios.
- Compilar e testar quando possível; informar somente verificações realmente executadas.
- Não inventar integrantes nem prometer garantia de originalidade.

Esses detalhes não devem ser apresentados como citações do professor quando não constam do PDF.

## Decisões adotadas

### Fila cronológica

A fila encadeada mantém referências para `inicio` e `fim`. Uma fila FIFO comum coloca cada novo registro no fim. Aqui, a **inserção é adaptada para percorrer os nós e inserir pela ordem do horário**. Essa escolha atende ao requisito de agenda cronológica mesmo que 10:00 seja cadastrado antes de 09:00.

O atendimento continua removendo exclusivamente o nó do início. A estrutura funciona como uma fila de atendimento ordenada por horário; não representa FIFO pela ordem do cadastro. A remoção por ID é uma operação adicional para cancelamentos e não é usada para escolher quem será atendido.

### Expediente, datas e conflitos

- Há um profissional.
- Cada serviço ocupa um intervalo fixo de 30 minutos.
- Os horários válidos começam às 08:00 e avançam em blocos de 30 minutos, até 17:30. O último serviço termina às 18:00.
- O usuário seleciona uma data para a agenda. Todos os agendamentos pendentes pertencem a essa data.
- A data só pode ser trocada quando a fila estiver vazia. Isso evita abandonar ou misturar pendências de dias diferentes.
- Um horário não pode ter dois agendamentos pendentes. Como os intervalos são fixos e alinhados, igualdade do início representa conflito.
- Datas passadas são permitidas para exercícios e demonstrações. A aplicação não compara o horário agendado com o relógio para impedir cadastro ou atendimento.
- Após atender ou cancelar, o horário deixa de estar ocupado na fila. A aplicação não mantém histórico de atendimentos nem controla a ocupação passada do profissional.
- Data e horário são tratados com `java.time`; o console usa `dd/MM/aaaa` e `HH:mm`, com validação estrita.

### Identificação e dados

- O ID do cliente é informado pelo usuário e é a chave da ABB. O telefone é um atributo obrigatório. A opção do PDF “telefone/ID” é concretizada pelo ID, como solicitado no texto colado.
- Os IDs dos agendamentos são automáticos e crescentes durante uma sessão. Um número usado não é reutilizado após atendimento, cancelamento ou retirada do histórico.
- Os valores são tratados com `BigDecimal`, evitando erros típicos de ponto flutuante em dinheiro.
- Os objetos de cliente, agendamento e cancelamento preservam seus dados. O histórico retém os dados do cliente mesmo depois de o nó correspondente sair da ABB.
- A remoção do cancelamento mais recente descarta esse registro do histórico; não restaura o agendamento na fila.
- Toda informação fica em memória. Ao sair, o cadastro, a agenda, o histórico e a sequência dos agendamentos são reiniciados.
- O código usa recursos compatíveis com Java 8 ou superior, sem frameworks ou dependências externas.

### Validação e integração

`AgendaService` coordena as regras. Para agendar, verifica os dados, localiza o cliente e verifica a agenda antes da inserção. Para cancelar, verifica a existência e o motivo antes de transferir os dados da fila para a pilha. Para excluir cliente, consulta primeiro se ele possui pendências.

Erros de validação devem deixar o estado anterior preservado. Isso cobre entradas e regras de negócio; a aplicação didática não implementa recuperação de falhas da máquina ou falta de memória.

## Responsabilidades e rastreabilidade

Os caminhos abaixo são relativos à raiz do projeto. As verificações executadas e o roteiro de teste ficam documentados separadamente; a tabela identifica onde cada requisito pode ser inspecionado.

| Exigência do professor | Fonte | Arquivo e responsabilidade | Situação de entrega |
|---|---|---|---|
| Aplicação em Java que compile | p. 1 | `src/agenda/Main.java`; todos os arquivos de `src/agenda`; comandos no `README.md` | Validar compilando com JDK, conforme instruções. |
| Contexto real com integração | pp. 1–2 | `AgendaService.criarAgendamento`, `atenderProximo`, `cancelarAgendamento` e `excluirCliente` | Fluxo da barbearia integra as três estruturas. |
| Implementação manual das estruturas | pp. 1–2 | `ArvoreClientes.java`, `FilaAgendamentos.java`, `PilhaCancelamentos.java`; nós e referências próprios | Inspecionar essas classes. |
| Não usar estruturas prontas | p. 1 | Todos os arquivos Java | Inspecionar imports e implementação; não usar Collections ou streams para guardar os registros. |
| Código sem comentários | p. 1 | Todos os arquivos Java | Explicações ficam em Markdown, fora do código. |
| Não copiar código nem entregar trabalhos iguais | p. 1 | Responsabilidade da equipe sobre autoria, estudo e entrega | Este projeto não garante originalidade; a equipe precisa revisar e compreender o material antes de entregar. |
| ABB como cadastro central | pp. 1 e 3 | `src/agenda/ArvoreClientes.java`; `AgendaService` consulta a árvore para agendar | Clientes identificados pelo ID. |
| Inserir com menor à esquerda e maior à direita | p. 2 | `ArvoreClientes.inserir` | Rejeita chave duplicada. |
| Buscar pela chave | p. 2 | `ArvoreClientes.buscar` | A chave escolhida é o ID. |
| Remover folha, nó com um filho e dois filhos | p. 2 | `ArvoreClientes.remover` | Com dois filhos, usa o sucessor da subárvore direita; atualizar a raiz também faz parte da remoção. |
| Exibir os três percursos | p. 2 | `ArvoreClientes.listarEmOrdem`, `listarPreOrdem` e `listarPosOrdem`; opções em `Main` | Exibe os clientes sem alterar a árvore. |
| Pelo menos duas auxiliares no negócio | p. 2 | `FilaAgendamentos.java`, `PilhaCancelamentos.java` e `AgendaService.java` | Fila para pendências; pilha para cancelamentos. |
| Auxiliar com inserir/adicionar | p. 2 | `FilaAgendamentos.inserir`; `PilhaCancelamentos.inserir` | A fila insere por horário; a pilha insere no topo. |
| Auxiliar com buscar/localizar | p. 2 | `FilaAgendamentos.buscar`; `PilhaCancelamentos.buscar` | Busca por ID do agendamento, sem remover. |
| Auxiliar com excluir/remover | p. 2 | `FilaAgendamentos.remover`, `atenderProximo`; `PilhaCancelamentos.remover` | Cancelamento remove da fila por ID; atendimento remove do início; pilha remove do topo. |
| Auxiliar com listar | p. 2 | `FilaAgendamentos.listar`; `PilhaCancelamentos.listar` | Fila em ordem de atendimento; pilha do topo à base. |
| Fila do dia em ordem cronológica | p. 3 | `FilaAgendamentos.inserir`; `AgendaService.selecionarDia` e `criarAgendamento` | Uma data ativa e horários ordenados. |
| Pilha do histórico de serviços cancelados | p. 3 | `Cancelamento.java`; `PilhaCancelamentos.java`; `AgendaService.cancelarAgendamento` | Mais recente no topo, com dados e motivo. |
| Orientação a objetos e limpeza | p. 2 | `Cliente.java`, `Agendamento.java`, `Cancelamento.java`, estruturas, `AgendaService.java`, `Validacao.java`, `EntradaConsole.java`, `Main.java` | Modelos, estruturas, regras e console têm responsabilidades separadas. |
| Equipe de 2 a 4 integrantes | p. 1 | Campos de integrantes no `README.md` | Preenchimento e formação da equipe são tarefas humanas. |
| GitHub de todos os integrantes | p. 1 | Orientação e campos no `README.md` | Cada integrante deve disponibilizar o sistema em seu GitHub antes da entrega. |
| Todos apresentam e explicam individualmente | pp. 1–2 | `docs/apresentacao.md` | Cada integrante deve estudar e ensaiar; documentação não substitui a apresentação. |
| Respeitar prazo e ordem de apresentação | p. 1 | Datas registradas neste documento | Entrega em 17/11/2026; apresentação em 17 ou 24/11/2026, conforme definição da professora. |

## Conferência dos detalhes pedidos pelo aluno

| Pedido | Local principal |
|---|---|
| ID, nome, telefone e validação de cliente | `Cliente`, `Validacao`, `ArvoreClientes.inserir` |
| Agendamento com cliente, serviço, data, horário e valor | `Agendamento`, `AgendaService.criarAgendamento` |
| Horários fora de ordem e conflitos | `FilaAgendamentos.inserir`, `AgendaService.criarAgendamento` |
| Agenda de uma única data | `AgendaService.selecionarDia` |
| Cancelar início, meio e fim | `FilaAgendamentos.remover`, `AgendaService.cancelarAgendamento` |
| Atender o primeiro | `FilaAgendamentos.atenderProximo`, `AgendaService.atenderProximo` |
| LIFO e consultas sem modificar a pilha | `PilhaCancelamentos` |
| Histórico preservado após excluir cliente | Dados preservados em `Cliente`, `Agendamento` e `Cancelamento` |
| Bloquear exclusão com pendência | `AgendaService.excluirCliente`, `FilaAgendamentos.possuiCliente` |
| Erros sem perda de registros | Validação nos modelos, estruturas, serviço e console antes das mutações |
| Menu e tratamento de entradas | `Main`, `EntradaConsole` |
| Execução e organização | `README.md` |
| Testes e resultados reais | Roteiro de testes do projeto e registro de execução, quando realizados |
| Explicações, demonstração e exercícios | `docs/apresentacao.md` |
