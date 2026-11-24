# Aplicativo de Agenda de Serviços

Trabalho final de Estrutura de Dados 2026.2, tema 10. Aplicação Java de console para uma barbearia, com cadastro de clientes em árvore binária de busca, agendamentos em fila cronológica e histórico de cancelamentos em pilha. As três estruturas usam nós e referências implementados no projeto.

Integrantes:

- Nome: Kauan Henrique Capinam Marques | GitHub: https://github.com/m4rques
- Nome: Guilherme de Carvalho Pimentel Ferreira | GitHub: https://github.com/ferreiragp
- Nome: Kelvin Cerqueira de Sousa | GitHub: https://github.com/kelvincerqueirasouza 

O PDF exige que o sistema esteja no GitHub de **todos os integrantes**. Todos devem apresentar, com ordem individual sorteada no dia. Entrega indicada no enunciado: **17/11**; apresentação: **17 ou 24/11**, pela ordem do tema. O contexto do documento é o semestre 2026.2. A publicação e a identificação da equipe devem ser feitas pelos integrantes.

## Executar no Windows

Requer um **JDK 8 ou superior**, que inclui `javac`. O código usa recursos e APIs do Java 8. Não há dependências externas, Maven, Gradle ou banco de dados.

Para usar sem digitar comandos, abra a pasta do projeto no Explorador de Arquivos e dê dois cliques em:

- **`INICIAR_AGENDA.bat`**: compila e abre o menu da agenda. Informe a data e use as opções normalmente.
- **`TESTAR_AGENDA.bat`**: compila e executa todas as verificações automáticas, exibindo o resultado.

A janela permanece aberta ao terminar, para você ler as mensagens. Pressione uma tecla para fechá-la. O programa continua sendo uma aplicação de console, como proposto para o trabalho; os atalhos dispensam digitar os comandos de compilação e execução.

Se estiver no VS Code, clique com o botão direito no arquivo e escolha **Revelar no Explorador de Arquivos**; dê dois cliques nele na janela que abrir.

Também é possível executar no PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\agenda.ps1
```

O script localiza o JDK no PATH, em JAVA_HOME ou nas pastas usuais de instalação, compila em UTF-8 para `out` e abre o menu. O parâmetro `ExecutionPolicy` vale apenas para esse processo. Para compilar sem abrir o programa ou para executar os testes:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\agenda.ps1 -SomenteCompilar
powershell -NoProfile -ExecutionPolicy Bypass -File .\agenda.ps1 -Testar
```

Alternativa com `javac` e `java` disponíveis no PATH, no PowerShell:

```powershell
New-Item -ItemType Directory -Path out -Force | Out-Null
javac -encoding UTF-8 -d out src/agenda/*.java
java "-Dfile.encoding=UTF-8" -cp out agenda.Main
```

Com JDK 9 ou superior, acrescente `--release 8` após `javac` para gerar classes compatíveis com Java 8. Para compilar e executar os testes manualmente:

```text
javac -encoding UTF-8 -d out src/agenda/*.java testes/agenda/*.java
java "-Dfile.encoding=UTF-8" -cp out agenda.TestesAgenda
java "-Dfile.encoding=UTF-8" -cp out agenda.TestesConsole
```

As entradas do terminal devem usar UTF-8. O script configura essa codificação; na execução manual no Windows, use `chcp 65001` antes de executar. Os testes são programas Java comuns e lançam `AssertionError` se houver falha; não dependem da opção `-ea`.

## Funcionamento e decisões

Ao abrir o programa, informe o dia no formato `dd/MM/aaaa`. Cadastre clientes antes de agendar. Os IDs de clientes são inteiros positivos escolhidos no cadastro; IDs duplicados são recusados. O telefone deve conter DDD e 10 ou 11 dígitos; espaços, parênteses e hífen são aceitos e removidos. A busca central utiliza o ID.

A fila recebe agendamentos na **posição cronológica**, mesmo que sejam digitados fora de ordem. Essa escolha adapta a inserção de uma fila FIFO convencional: a ordem de chegada dos cadastros pode diferir da ordem de atendimento. O atendimento sempre remove o início. A exclusão por ID é usada para cancelar um item em qualquer posição.

Premissas adotadas para tornar a agenda precisa:

- Um profissional, com serviços de duração fixa de 30 minutos. Inícios válidos: de 08:00 a 17:30, sempre em minutos `00` ou `30`; o último termina às 18:00. O nome do serviço não muda sua duração.
- Apenas um dia ativo. Só é possível trocar a data quando não há pendências; a troca mantém clientes e cancelamentos. Um agendamento deve usar exatamente a data selecionada.
- Um horário só pode ter um agendamento pendente. Um horário liberado por atendimento ou cancelamento pode ser reservado novamente. O programa é didático e permite datas e horários passados, sem comparar o agendamento com o relógio atual.
- IDs de agendamentos são automáticos e crescentes durante a sessão. Operações recusadas não consomem IDs. Atender, cancelar, desempilhar e mudar o dia não reiniciam o contador.
- Valores são positivos, com até duas casas decimais, representados por `BigDecimal`. No menu, use `35,50` ou `35.50`, sem separador de milhar.
- Cliente com pendências não pode ser excluído. Os modelos são imutáveis: o histórico preserva nome, telefone e demais dados originais, mesmo depois da exclusão e de um novo cadastro com o mesmo ID.
- Cancelar transfere o registro da fila para a pilha com motivo e momento do cancelamento no fuso `America/Bahia`. O topo contém o cancelamento mais recente. Desempilhar remove apenas esse registro do histórico; não restaura a reserva.
- Os dados ficam somente em memória e são perdidos ao encerrar. Não há persistência nem histórico de atendimentos concluídos. Não são usadas Collections ou streams para armazenar os registros; `StringBuilder` apenas monta o texto exibido.

Essas premissas detalham o pedido do texto colado. O PDF não fixa duração, expediente ou política de troca de dia. A separação completa e o checklist estão em [docs/requisitos.md](docs/requisitos.md).

## Menu

| Opção | Ação |
| --- | --- |
| 1 / 2 / 3 / 4 | Cadastrar, buscar, listar em in-ordem e excluir clientes |
| 5 / 6 | Percursos pré-ordem e pós-ordem |
| 7 | Selecionar o dia da agenda |
| 8 / 9 / 10 | Criar, buscar e listar agendamentos |
| 11 | Atender o próximo horário |
| 12 | Cancelar com motivo |
| 13 / 14 / 15 | Listar, buscar e desempilhar cancelamentos |
| 0 | Sair |

Entradas de formato inválido são solicitadas novamente. Recusas de negócio, como ID duplicado ou horário ocupado, devolvem o controle ao menu sem alterar os registros. O fim da entrada também encerra o programa normalmente.

## Arquivos e responsabilidades

```text
INICIAR_AGENDA.bat
TESTAR_AGENDA.bat
agenda.ps1
README.md
src/agenda/
    Main.java
    EntradaConsole.java
    Validacao.java
    Cliente.java
    Agendamento.java
    Cancelamento.java
    ArvoreClientes.java
    FilaAgendamentos.java
    PilhaCancelamentos.java
    AgendaService.java
testes/agenda/
    TestesAgenda.java
    TestesConsole.java
docs/
    requisitos.md
    testes.md
    apresentacao.md
referencia/
    enunciado_extraido.txt
```

| Classe | Responsabilidade |
| --- | --- |
| `Main` | Menu e apresentação dos resultados |
| `EntradaConsole` | Leitura de linhas, números, datas, horários e valores |
| `Validacao` | Validações comuns de ID e texto |
| `Cliente`, `Agendamento`, `Cancelamento` | Dados imutáveis e suas validações básicas |
| `ArvoreClientes` | Nós com filhos esquerdo/direito; cadastro por ID e percursos |
| `FilaAgendamentos` | Nós encadeados, referências de início/fim e inserção ordenada |
| `PilhaCancelamentos` | Nós encadeados e referência para o topo |
| `AgendaService` | Regras de negócio e integração das três estruturas |

Os arquivos Java contêm o código completo, sem comentários ou Javadoc. As explicações estão nos documentos Markdown.

## Verificação e preparação

- [Requisitos, origem de cada regra e checklist](docs/requisitos.md)
- [Roteiro de testes, entradas e resultados verificados](docs/testes.md)
- [Explicações, complexidades, demonstração de 5 a 8 minutos, perguntas e exercícios](docs/apresentacao.md)

Para a entrega, preencha os integrantes, execute os testes na máquina da apresentação, publique no GitHub de cada participante e pratique as alterações propostas. O enunciado proíbe código copiado e trabalhos iguais; este material não oferece garantia de originalidade e deve ser compreendido e revisado pela equipe antes da entrega.
