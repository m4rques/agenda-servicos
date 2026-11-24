# Estudo e preparação para a apresentação

Este guia acompanha o código em `src/agenda`. Leia as classes e execute os cenários antes de apresentar. O PDF exige apresentação e explicação individual de todos os integrantes; a ordem é sorteada no dia.

## O que o sistema faz

O sistema organiza uma barbearia com um profissional. Primeiro, cadastra os clientes em uma árvore binária de busca. Ao marcar um serviço, localiza o cliente nessa árvore e coloca o agendamento em uma fila ordenada pelo horário. O atendimento retira o primeiro da fila. O cancelamento retira um agendamento da fila e guarda seus dados, o motivo e o instante em uma pilha.

São horários de 30 minutos, de 08:00 até 17:30, terminando às 18:00. Há uma data selecionada por vez. Só é possível trocar a data quando não existem agendamentos pendentes. Todos os dados ficam em memória durante a execução.

## Como a ABB organiza os clientes

Cada nó contém um cliente e duas referências: filho esquerdo e filho direito. O ID é a chave. Valores menores que o ID do nó ficam à esquerda; maiores ficam à direita. IDs iguais são recusados.

Para buscar o ID 40 em uma árvore cuja raiz é 50, o programa segue à esquerda, porque 40 é menor que 50. Se encontrar 30, segue à direita, pois 40 é maior que 30. Essa comparação continua até achar a chave ou uma referência vazia. O telefone é guardado no cliente, mas não é usado para ordenar esta ABB.

Para observar os percursos, cadastre nesta ordem: **50, 30, 70, 20, 40, 60, 80, 65**. A forma resultante é:

```text
         50
       /    \
     30      70
    /  \    /  \
   20  40  60  80
             \
              65
```

| Percurso | Regra | IDs exibidos |
|---|---|---|
| In-ordem | Esquerda, nó, direita | 20, 30, 40, 50, 60, 65, 70, 80 |
| Pré-ordem | Nó, esquerda, direita | 50, 30, 20, 40, 70, 60, 65, 80 |
| Pós-ordem | Esquerda, direita, nó | 20, 40, 30, 65, 60, 80, 70, 50 |

In-ordem produz IDs crescentes pela propriedade da ABB. Pré-ordem permite observar a raiz antes de seus descendentes. Pós-ordem visita os filhos antes do pai. Nenhum percurso altera a árvore.

### Remoção nos três casos

1. **Folha:** sem filhos, basta desligar o nó de seu pai. No exemplo, remova 20.
2. **Um filho:** o pai passa a apontar para o único filho do nó removido. No exemplo, remova 60; seu filho 65 ocupa a ligação que antes chegava a 60.
3. **Dois filhos:** escolha o sucessor, que é o menor ID da subárvore direita. Substitua os dados do nó por esse cliente e remova o nó de origem do sucessor. Como o sucessor é o mais à esquerda naquela subárvore, não pode ter filho esquerdo; sua remoção cai em um dos dois casos simples.

A mesma regra vale para a raiz. O resultado da remoção deve atualizar a referência `raiz`. Se a raiz for o único elemento, a árvore ficará vazia. No exemplo, após remover 20 e 60, remova 50: o sucessor passa a ser 65. A nova raiz armazena o cliente 65 e sua antiga ocorrência é retirada da subárvore direita.

A ABB foi escolhida porque o tema exige que ela seja o cadastro central e porque permite localizar um cliente comparando sua chave. Ela não é balanceada: inserir IDs em ordem crescente pode formar uma sequência de nós com desempenho semelhante a uma lista.

## Como a fila mantém a ordem cronológica

Cada nó da fila contém um agendamento e a referência para o próximo nó. `inicio` identifica quem será atendido; `fim` identifica o último nó. Uma fila vazia tem as duas referências vazias.

O cadastro pode chegar fora de ordem: primeiro 10:00, depois 09:00, depois 09:30. A inserção percorre a fila e liga cada nó no lugar correto. O resultado é **09:00 → 09:30 → 10:00**.

Essa é uma adaptação da inserção de uma fila. A saída normal de atendimento continua sendo pelo início, mas a chegada não é sempre no fim. Portanto, a ordem de atendimento segue o horário marcado, não a ordem em que o operador cadastrou os registros.

Ao inserir antes do primeiro, `inicio` muda. Ao inserir depois do último, `fim` muda. Ao atender ou cancelar o único elemento, os dois passam a ficar vazios. Ao cancelar o último de vários elementos, o nó anterior passa a ser o novo `fim`.

Cancelar por ID exige encontrar o nó e, quando existir, seu anterior. O anterior passa a apontar para o próximo do nó removido. Cancelar é uma operação administrativa adicional; o atendimento continua retirando somente o primeiro.

O bloqueio de horários repetidos impede dois serviços pendentes no mesmo intervalo. Os horários ficam alinhados em blocos de 30 minutos, então não há um serviço válido começando às 09:15 e atravessando o intervalo de 09:00.

## Como a pilha guarda os cancelamentos

A pilha tem uma referência `topo`. Cada nó guarda um registro de cancelamento e a referência para o nó que estava abaixo dele.

Ao empilhar, o novo nó aponta para o topo antigo e passa a ser o novo topo. Ao desempilhar, o topo passa a apontar para o nó de baixo. A regra é LIFO: o último que entrou é o primeiro que sai.

Se forem cancelados os agendamentos 1, 2 e 3 nessa ordem, a listagem será **3 → 2 → 1**. Uma busca percorre os nós usando uma referência auxiliar; não muda o topo. A listagem faz o mesmo. Buscar ou listar duas vezes deve produzir os mesmos registros na mesma ordem.

A retirada do topo apaga o registro mais recente do histórico. Ela não desfaz o cancelamento nem recoloca o serviço na agenda. O histórico contém dados preservados do agendamento e de seu cliente; excluir o cliente da ABB depois não apaga esses dados.

## Como as estruturas trabalham juntas

| Operação | Caminho dos dados |
|---|---|
| Cadastrar cliente | Validar → verificar ID → inserir na ABB. |
| Criar agendamento | Validar dados e data → buscar cliente na ABB → verificar horário → inserir na fila ordenada. |
| Atender | Remover o agendamento do início da fila. |
| Cancelar | Localizar na fila → validar motivo → preparar registro → retirar da fila → empilhar no histórico. |
| Excluir cliente | Verificar se há agendamento pendente na fila → se não houver, remover da ABB. |
| Trocar data | Verificar se a fila está vazia → selecionar outra data. |

O `AgendaService` coordena essas regras. As classes de estrutura cuidam das ligações entre os nós. `Main` e `EntradaConsole` cuidam da conversa com o operador, enquanto as classes de dados e `Validacao` impedem informações inválidas.

Validar antes da alteração evita perder um agendamento se, por exemplo, o motivo do cancelamento estiver vazio. Os testes devem verificar tanto a mensagem de erro quanto a permanência dos registros após uma tentativa inválida.

## Complexidades de tempo e memória

Considere `n` clientes, `h` como a altura da ABB, `m` agendamentos pendentes e `k` cancelamentos. As estimativas abaixo tratam comparação de IDs e horários e acesso a campos como operações constantes.

| Operação | Tempo | Motivo |
|---|---|---|
| ABB: inserir, buscar e remover | O(h), pior caso O(n) | Segue um caminho da raiz até o local buscado; na remoção também pode procurar o sucessor. |
| ABB: cada percurso | O(n) | Visita todos os nós uma vez. |
| Fila: inserir em ordem | O(m) | Pode precisar percorrer os nós até a posição final. |
| Fila: buscar por ID, remover por ID, verificar conflito ou cliente | O(m) | Pode precisar verificar todos os nós. |
| Fila: atender o próximo | O(1) | Atualiza o início e, se necessário, o fim. |
| Fila: listar | O(m), além do texto produzido | Visita cada nó uma vez. |
| Pilha: inserir e remover o topo | O(1) | Atualiza apenas as referências do topo. |
| Pilha: buscar e listar | O(k), além do texto produzido | Pode percorrer todos os cancelamentos. |
| Serviço: criar agendamento | O(h + m) | Busca o cliente e percorre a fila para validação/inserção. |
| Serviço: excluir cliente | O(m + h) | Verifica pendências e remove da ABB. |
| Serviço: cancelar agendamento | O(m) | Localiza/remove da fila e empilha em O(1). |

Percorrer a fila mais de uma vez na mesma operação ainda é O(m), pois uma quantidade fixa de passagens resulta em um múltiplo constante de `m`. Uma ABB com boa distribuição costuma ter altura próxima de log₂(n), mas esta implementação não oferece garantia de O(log n): uma árvore degenerada pode ter altura proporcional a `n`.

Os nós ocupam O(n + m + k) de memória. Os percursos recursivos da ABB usam até O(h) de memória auxiliar na pilha de chamadas; um percurso em árvore degenerada também pode chegar a O(n) de profundidade. Inserção, busca e remoção da ABB são iterativas e usam O(1) de memória auxiliar. As estruturas não usam a classe pronta `Stack`: a pilha de chamadas dos percursos faz parte da execução normal dos métodos da linguagem.

As listagens retornam texto. Considerando `L` caracteres produzidos, montar o resultado exige O(L) de memória e tempo para essa saída, além de visitar os nós. Não se deve chamar uma listagem completa de O(1) por retornar apenas uma referência a `String`.

## Roteiro de demonstração de aproximadamente 7 minutos

Use uma execução nova, com as estruturas vazias, para que os IDs automáticos de agendamento comecem em 1. Os nomes abaixo são dados fictícios de demonstração, não nomes de integrantes. Use telefones de exemplo válidos no formato aceito pelo programa, como `71900000050`, e altere os dois últimos dígitos conforme o ID do cliente.

| Tempo | Ação | O que explicar ou observar |
|---|---|---|
| 0:00–0:40 | Informar a data inicial `17/11/2026`; apresentar o problema e a escolha das três estruturas. | ABB cadastra; fila organiza atendimento; pilha registra cancelamentos. Dizer que a inserção da fila é ordenada. |
| 0:40–2:00 | Cadastrar IDs 50, 30, 70, 20, 40, 60, 80 e 65, com nomes `Cliente 50`, etc.; mostrar os três percursos e buscar 65. | Desenhar rapidamente a árvore acima. In-ordem ordena os IDs; as outras visitas mudam a sequência. Tentar cadastrar 50 novamente e mostrar a rejeição. |
| 2:00–3:20 | Criar agendamento para cliente 50 às 10:00, cliente 30 às 09:00 e cliente 70 às 09:30, todos em 17/11/2026; serviço `Corte`, valor `35,00`. Listar a fila. | IDs esperados 1, 2 e 3; ordem da fila 2, 3, 1. Tentar outro cadastro às 09:00: deve falhar sem alterar a fila. Tentar excluir cliente 30: deve falhar porque há pendência. |
| 3:20–4:00 | Atender o próximo; listar novamente. | Deve sair o agendamento 2, de 09:00. A fila passa a ser 3, 1. A saída é sempre pelo início. |
| 4:00–5:00 | Cancelar ID 1 com motivo `Cliente indisponível`; depois cancelar ID 3 com motivo `Solicitação do cliente`. Listar, buscar ID 1 e listar de novo; retirar o cancelamento mais recente. | O primeiro cancelamento remove o fim da fila; o segundo remove o único nó. A pilha aparece 3, 1. Busca/listagem não alteram a pilha. Retirar o topo remove 3 e deixa 1. |
| 5:00–5:50 | Excluir cliente 50 e listar o histórico. Selecionar outra data agora que a fila está vazia. | Os dados do cancelamento 1 continuam disponíveis após excluir o cliente. A troca de dia só é aceita sem pendências. |
| 5:50–6:40 | Remover cliente 20, depois 30 e depois 60. Mostrar in-ordem. | Após a exclusão anterior de 50, a raiz é 60. Agora 20 é folha; 30 tem apenas o filho 40; 60 é raiz com dois filhos e é substituído pelo sucessor 65. |
| 6:40–7:10 | Mostrar as classes de nós e resumir as complexidades. | ABB O(h), pior O(n); inserção/busca da fila O(m); atendimento e topo da pilha O(1). |

Se preferir demonstrar a ABB em uma execução separada, cadastre os oito IDs novamente, remova 20 (folha), remova 60 (um filho) e remova 50 (raiz com dois filhos, sucessor 65). Essa alternativa usa a árvore original desenhada acima e pode levar a apresentação para aproximadamente 8 minutos.

As opções do menu são: 1 cadastrar cliente; 2 buscar cliente; 3 listar in-ordem; 4 excluir cliente; 5 pré-ordem; 6 pós-ordem; 7 selecionar dia; 8 criar agendamento; 9 buscar agendamento; 10 listar agenda; 11 atender; 12 cancelar; 13 listar cancelamentos; 14 buscar cancelamento; 15 retirar o topo; 0 sair.

O roteiro principal não cobre sozinho todo o roteiro de testes. Cancelamento de meio da fila, entradas inválidas e remoções em estruturas vazias devem ser verificados antes e podem ser mostrados se a professora solicitar.

## Perguntas que a professora pode fazer

**Por que a ABB usa o ID, se o tema menciona telefone?**

O PDF permite telefone/ID e o pedido escolheu um ID inteiro único como chave. O telefone permanece como atributo obrigatório. Uma busca pelo telefone exigiria percorrer a árvore inteira ou criar outra estratégia de indexação, pois a ordenação atual é por ID.

**O que acontece quando tento inserir um cliente de ID repetido?**

A inserção é recusada e o cadastro original permanece. Permitir duas chaves iguais tornaria ambígua a busca por ID e quebraria a regra definida de menor à esquerda e maior à direita.

**Por que usar o menor da direita na remoção com dois filhos?**

Ele é maior que todos os valores da subárvore esquerda e é o menor disponível da direita. Por isso pode substituir a chave removida sem quebrar a ordenação. Depois é preciso retirar sua ocorrência antiga para não duplicar o cliente.

**O sucessor pode ter dois filhos?**

Não. Ao chegar ao nó mais à esquerda da subárvore direita, não existe filho esquerdo. Ele pode ser folha ou ter apenas filho direito. Esse filho, quando existe, deve ser religado ao pai do sucessor.

**A ABB sempre busca em O(log n)?**

Não. O custo depende da altura. Sem balanceamento, IDs inseridos em ordem crescente podem formar uma cadeia. Nesse caso, buscar a última chave custa O(n).

**Essa fila é FIFO?**

A retirada de atendimento ocorre pelo início. A inserção foi adaptada para ordenar os horários, então não preserva FIFO pela ordem de cadastro. Isso é necessário porque o requisito prioriza a ordem cronológica dos agendamentos. A decisão está declarada na documentação.

**Para que manter a referência para o fim?**

Ela identifica o último nó e precisa acompanhar inserções e remoções. A presença de `fim` não torna toda inserção O(1), pois a posição correta pode estar no meio da fila. Ao esvaziar a fila, `inicio` e `fim` precisam ficar vazios.

**Por que a busca da pilha não usa desempilhar?**

Porque uma consulta não deve alterar o histórico. Uma referência auxiliar percorre os nós, enquanto o topo permanece igual. Desempilhar removeria registros e exigiria restaurar a estrutura, criando trabalho e risco desnecessários.

**O que impede dois clientes no mesmo horário?**

Antes de inserir, a aplicação verifica se o horário já está ocupado na fila. Como todos os serviços duram 30 minutos e só começam em horários alinhados, dois horários de início diferentes e válidos não se sobrepõem.

**Posso trocar a data com pessoas ainda na fila?**

Não. A troca é recusada enquanto existirem pendências. A implementação mantém apenas uma agenda diária ativa; atender ou cancelar as pendências esvazia a fila e permite selecionar outra data.

**Excluir um cliente apaga o histórico dele?**

Não. A exclusão retira o nó da ABB. Os registros de cancelamento continuam com os dados preservados do agendamento e do cliente. A exclusão é bloqueada somente enquanto o cliente tiver agendamentos pendentes.

**Retirar um cancelamento da pilha desfaz o cancelamento?**

Não. Essa opção remove apenas o registro mais recente do histórico. Reagendar exigiria uma operação explícita, com nova validação da data, cliente e disponibilidade.

**Por que não usar ArrayList, Stack ou TreeMap?**

O objetivo da disciplina é implementar as ligações, buscas e remoções manualmente. Além disso, o PDF proíbe estruturas prontas. Usá-las esconderia justamente a parte que está sendo avaliada.

**Por que BigDecimal e java.time são permitidos?**

Essas classes representam valores monetários, datas e horários. Elas não armazenam os clientes, a fila ou a pilha no lugar das estruturas manuais. As estruturas de negócio continuam implementadas com nós e referências.

**O que acontece ao fechar o programa?**

Os dados são perdidos, pois a proposta usa memória durante uma sessão. Persistência não foi incluída; essa limitação está documentada.

## Exercícios para verificar a compreensão

Faça as alterações em uma cópia ou em uma branch e execute novamente os cenários afetados. Mantenha as explicações fora dos arquivos Java.

1. **Contar clientes por percurso:** implemente um método recursivo na ABB que retorne a quantidade de clientes sem usar o campo `quantidade` nem Collections. Compare o resultado com o método `quantidade()` existente antes e depois de remover uma folha e a raiz. Explique a diferença de custo entre os dois métodos.
2. **Buscar por telefone:** adicione uma busca que percorra a ABB por telefone sem mudar sua chave. Explique por que ela pode precisar visitar todos os clientes, mesmo quando a busca por ID é curta.
3. **Mudar a duração:** adapte os intervalos de 30 para 60 minutos, preservando o fim às 18:00. Liste os horários válidos e teste um horário intermediário que deve ser recusado. Atualize também os prompts e a documentação.
4. **Resumir os cancelamentos:** calcule a quantidade e a soma dos valores da pilha sem remover registros. Liste antes e depois para demonstrar que o topo e a ordem continuam iguais.
5. **Trocar o substituto da remoção:** use o maior da subárvore esquerda como antecessor em vez do menor da direita. Desenhe e teste a remoção de um nó com dois filhos, incluindo a raiz.
6. **Investigar a altura:** calcule a altura da ABB e compare inserir 1, 2, 3, 4, 5, 6, 7 com inserir 4, 2, 6, 1, 3, 5, 7. Relacione a forma da árvore ao custo das buscas.

Para cada exercício, explique primeiro quais referências ou regras mudarão e só depois altere o código. Conseguir prever o resultado e justificar as ligações é mais útil para a apresentação do que memorizar linhas.
