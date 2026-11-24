# Roteiro e resultados de testes

## Execução verificada

Em **16/11/2026**, os fontes de produção e de testes foram compilados com **JDK 25.0.2**, usando `--release 8 -encoding UTF-8 -Xlint:all,-options -Werror`. A opção `-options` desativa somente os avisos do compilador moderno sobre a idade do alvo Java 8.

Resultado das duas classes de testes:

```text
Todos os testes passaram: 160 verificacoes.
Todos os testes de console passaram: 100 verificacoes.
```

São **260 verificações**. As mesmas classes compiladas foram executadas com sucesso no **Java 8, versão 1.8.0_481**, além do Java 25 utilizado pelo script. Não foi utilizado um compilador JDK 8; a compatibilidade de APIs e bytecode foi obtida com `--release 8` e conferida executando no Java 8.

O script `agenda.ps1` foi executado nas modalidades `-SomenteCompilar` e `-Testar`. A descoberta do JDK fora do PATH e a compilação de todos os fontes também foram verificadas. A regravação dos `.class` precisou ocorrer fora da sandbox da ferramenta por uma restrição de permissões do ambiente; isso não exigiu alterações no código Java.

Os arquivos `INICIAR_AGENDA.bat` e `TESTAR_AGENDA.bat` também foram executados a partir de outra pasta. O primeiro abriu o programa, recebeu a data e encerrou pela opção `0`; o segundo concluiu as 260 verificações. Ambos retornaram código de saída zero e exibiram a pausa final.

Para repetir na pasta do projeto:

Dê dois cliques em **`TESTAR_AGENDA.bat`** no Explorador de Arquivos. O resultado fica visível até você pressionar uma tecla. Para experimentar o menu manualmente, abra **`INICIAR_AGENDA.bat`**.

Se preferir usar um comando:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\agenda.ps1 -Testar
```

Cada teste verifica resultados e lança `AssertionError` em caso de falha. Os testes não dependem de frameworks ou de `-ea`. O programa e os testes não acessam a rede nem gravam os dados da barbearia em arquivos.

## Cobertura automatizada

| Área | Verificações efetivamente executadas |
| --- | --- |
| Clientes | ID zero/negativo, nome nulo/vazio, telefone nulo/curto/longo/com letras, telefone fixo/celular e normalização |
| ABB | Vazia, busca inexistente, inserção nula/duplicada, três percursos e leituras que preservam a quantidade |
| Remoção ABB | Folha, um filho, nó interno com dois filhos, raiz folha, raiz com filho esquerdo/direito e raiz com dois filhos |
| Sucessor ABB | Sucessor profundo com filho direito, sucessor imediato, ordenação e quantidade após remover, reinserção após esvaziar |
| Fila | Vazia, IDs duplicados, conflito de horário, rejeição de outro dia e inserção fora de ordem |
| Referências da fila | Remoção no início, meio, fim e único elemento; inserção após remover o fim; esvaziamento por atendimento e por cancelamento |
| Pilha | Vazia, inserção nula, busca no topo/base/inexistente, listagem topo→base, LIFO e reinserção após esvaziar |
| Integração | Cliente obrigatório, bloqueio de exclusão pendente, cancelar retira da fila e empilha, atender não cria cancelamento |
| Histórico | Motivo inválido preserva a reserva; motivo normalizado; momento registrado; dados preservados após excluir e recadastrar cliente com mesmo ID |
| IDs automáticos | Crescem, não são reutilizados após desempilhar e não são consumidos por operações recusadas |
| Datas e horários | Dia nulo/diferente, troca bloqueada com pendências, troca aceita com fila vazia, seleção do mesmo dia, horários extremos válidos e horários fora da grade |
| Dinheiro | Valor nulo, zero, negativo e mais de duas casas decimais |
| Entrada de console | Texto vazio, UTF-8, inteiro inválido/overflow, datas inexistentes, anos bissextos, formatos inválidos, horas inválidas e valores com ponto/vírgula |
| Menu | Recuperação após entradas e operações recusadas; cadastro→agendamento→conflito→cancelamento→busca do histórico→desempilhamento→saída |
| Fim da entrada | EOF na abertura, no menu, durante cadastro e durante agendamento; encerramento sem confirmação parcial nem stacktrace |

`testes/agenda/TestesAgenda.java` cobre os modelos, estruturas e integração. `testes/agenda/TestesConsole.java` testa as leituras e chama o menu com entrada simulada, capturando a saída e restaurando as entradas/saídas padrão depois de cada cenário.

As verificações automáticas do console não substituem um ensaio no terminal da apresentação. Não foram avaliados bancos de dados, persistência, concorrência, desempenho com milhões de registros ou uma ABB com profundidade extrema. Os percursos recursivos estão sujeitos ao limite da pilha de chamadas em uma árvore muito degenerada.

## Roteiro manual com entradas e resultados esperados

Inicie uma execução nova. Informe `17/11/2026` como dia. Use nomes como `Cliente 50` e telefone `71999991234`; os nomes são dados fictícios, sem relação com os integrantes.

| Passo | Entradas/ação | Resultado esperado |
| --- | --- | --- |
| 1 | Menu `2`, ID `99`; depois `3`, `5` e `6` | Cliente não encontrado; percursos vazios, sem encerrar |
| 2 | Menu `11`; `12`, ID `99`, motivo `Teste`; `15` | Fila e pilha vazias tratadas; cancelamento inexistente não cria registro |
| 3 | Menu `1`, ID `50`, nome `Cliente 50`, telefone `71999991234` | Cliente cadastrado |
| 4 | Repita cadastro do ID `50` | Recusa duplicidade; dados originais permanecem |
| 5 | Cadastre IDs `30`, `70`, `20`, `40`, `60`, `80`, `65`, nessa ordem | Oito clientes contando a raiz 50 |
| 6 | Menu `3` | IDs `20, 30, 40, 50, 60, 65, 70, 80` |
| 7 | Menu `5` | IDs `50, 30, 20, 40, 70, 60, 65, 80` |
| 8 | Menu `6` | IDs `20, 40, 30, 65, 60, 80, 70, 50` |
| 9 | Menu `8`: cliente `50`, `Corte`, `17/11/2026`, `10:00`, `35,00` | Agendamento ID 1 |
| 10 | Menu `8`: cliente `30`, `Barba`, mesma data, `09:00`, `25,00` | Agendamento ID 2 |
| 11 | Menu `8`: cliente `70`, `Corte`, mesma data, `09:30`, `35,00` | Agendamento ID 3 |
| 12 | Menu `10` | Ordem de agendamentos `2, 3, 1`, horários `09:00, 09:30, 10:00` |
| 13 | Tente outro agendamento às `09:00` | Horário ocupado; fila permanece `2, 3, 1` |
| 14 | Menu `4`, ID `50`; menu `7`, data `18/11/2026` | Exclusão e troca de data recusadas; cliente, fila e data preservados |
| 15 | Menu `12`, ID `3`, motivo `Viagem` | Remove o meio; fila `2, 1`; histórico `3` |
| 16 | Menu `12`, ID `2`, motivo `Imprevisto` | Remove o início; fila `1`; histórico `2, 3` |
| 17 | Crie reserva para cliente `30` às `11:00` | ID 4; fila `1, 4` |
| 18 | Menu `12`, ID `4`, motivo `Indisponibilidade` | Remove o fim; fila `1`; histórico `4, 2, 3` |
| 19 | Menu `11` e depois `10` | Atende ID 1 e esvazia; cancelamentos continuam `4, 2, 3` |
| 20 | Menu `14`, ID `3`; menu `13` duas vezes | Busca encontra; ambas as listagens mantêm `4, 2, 3` |
| 21 | Menu `15`, depois `13` | Retira ID 4; histórico `2, 3` |
| 22 | Menu `4`, ID `30`; menu `14`, ID `2` | Cliente agora pode sair da ABB; histórico preserva dados do cliente 30 |
| 23 | Menu `7`, data `18/11/2026` | Troca aceita porque a fila está vazia; histórico permanece |

Para testar a remoção do único agendamento por cancelamento, crie uma reserva na nova data e cancele seu ID. Depois crie outra e atenda: a fila deve continuar funcionando após ambos os esvaziamentos.

## Remoções da ABB em uma execução separada

Cadastre apenas `50, 30, 70, 20, 40, 60, 80, 65`, sem agendamentos pendentes. Mostre in-ordem após cada operação:

| Exclusão | Caso | In-ordem esperado |
| --- | --- | --- |
| `20` | Folha | `30, 40, 50, 60, 65, 70, 80` |
| `30` | Um filho: 40 | `40, 50, 60, 65, 70, 80` |
| `50` | Raiz com dois filhos; sucessor 60 tem filho direito 65 | `40, 60, 65, 70, 80` |
| `70` | Nó interno com dois filhos; sucessor imediato 80 | `40, 60, 65, 80` |

Busque `50`: não deve existir. Busque `65`: deve continuar cadastrado. Em uma nova execução, cadastre somente `2, 1`; exclua `2` e depois `1` para demonstrar raiz com um filho e raiz folha. Tente excluir novamente: deve informar ausência.

## Entradas inválidas para experimentar no menu

| Campo | Entradas inválidas | Entrada válida para continuar | Resultado esperado |
| --- | --- | --- | --- |
| Data inicial | `31/02/2026`, `29/02/2025`, `1/11/2026` | `17/11/2026` | Pede data novamente |
| Menu | `abc`, `2147483648`, `-1`, `99` | `3` | Explica erro ou opção inexistente e continua |
| ID | `abc`, `0`, `-5` | `10` | Pede inteiro positivo novamente |
| Nome ou motivo | Linha vazia ou espaços | Texto preenchido | Pede o campo novamente |
| Telefone | `123`, `telefone` | Faça novo cadastro com `71999991234` | Operação recusada, volta ao menu sem inserir |
| Hora | `24:00`, `08:60`, `8:00` | `08:30` | Pede formato válido novamente |
| Hora de negócio | `07:30`, `08:15`, `18:00` | Faça nova reserva com `08:30` | Recusa a operação antes da inserção |
| Valor | `-1`, `0`, `30.001`, `NaN`, `1.000,00` | `35,50` | Pede valor positivo no formato aceito |
| Data da reserva | Data diferente do dia selecionado | Faça nova reserva no dia selecionado | Recusa sem criar agendamento |

Ao fechar a entrada durante um formulário, o programa deve encerrar com `Entrada encerrada. Programa finalizado.`. Os testes simulam esse caso diretamente, sem depender de uma combinação específica de teclas do terminal.
