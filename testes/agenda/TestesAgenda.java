package agenda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class TestesAgenda {
    private static int verificacoes;
    private static final LocalDate DIA = LocalDate.of(2026, 11, 16);

    public static void main(String[] args) {
        testarClientesEValidacoes();
        testarArvoreVaziaELeituras();
        testarRemocoesDaArvore();
        testarFilaOrdenadaEValidacoes();
        testarRemocoesDaFila();
        testarPilha();
        testarIntegracaoECancelamentos();
        testarDiaEHorarios();
        System.out.println("Todos os testes passaram: " + verificacoes + " verificacoes.");
    }

    private static void testarClientesEValidacoes() {
        Cliente cliente = new Cliente(1, "  Ana Silva  ", "(71) 99999-1234");
        igual(1, cliente.getId(), "ID do cliente");
        igual("Ana Silva", cliente.getNome(), "Nome sem espacos externos");
        igual("71999991234", cliente.getTelefone(), "Telefone normalizado");
        falha(IllegalArgumentException.class, () -> new Cliente(0, "Ana", "71999991234"));
        falha(IllegalArgumentException.class, () -> new Cliente(-1, "Ana", "71999991234"));
        falha(IllegalArgumentException.class, () -> new Cliente(1, null, "71999991234"));
        falha(IllegalArgumentException.class, () -> new Cliente(1, "   ", "71999991234"));
        falha(IllegalArgumentException.class, () -> new Cliente(1, "Ana", null));
        falha(IllegalArgumentException.class, () -> new Cliente(1, "Ana", "123"));
        falha(IllegalArgumentException.class, () -> new Cliente(1, "Ana", "7199999abcd"));
        falha(IllegalArgumentException.class, () -> new Cliente(1, "Ana", "123456789012"));
        igual("7133331234", new Cliente(2, "Bia", "71 3333-1234").getTelefone(), "Telefone fixo");
    }

    private static void testarArvoreVaziaELeituras() {
        ArvoreClientes arvore = new ArvoreClientes();
        igual(0, arvore.quantidade(), "ABB inicialmente vazia");
        igual(null, arvore.buscar(1), "Busca em ABB vazia");
        igual(null, arvore.remover(1), "Remocao em ABB vazia");
        igual("", arvore.listarEmOrdem(), "In-ordem vazia");
        igual("", arvore.listarPreOrdem(), "Pre-ordem vazia");
        igual("", arvore.listarPosOrdem(), "Pos-ordem vazia");
        falha(IllegalArgumentException.class, () -> arvore.inserir(null));
        Cliente centro = cliente(20);
        Cliente esquerda = cliente(10);
        Cliente direita = cliente(30);
        arvore.inserir(centro);
        arvore.inserir(esquerda);
        arvore.inserir(direita);
        igual(linha(esquerda) + linha(centro) + linha(direita), arvore.listarEmOrdem(), "Percurso in-ordem");
        igual(linha(centro) + linha(esquerda) + linha(direita), arvore.listarPreOrdem(), "Percurso pre-ordem");
        igual(linha(esquerda) + linha(direita) + linha(centro), arvore.listarPosOrdem(), "Percurso pos-ordem");
        mesmo(centro, arvore.buscar(20), "Busca do centro");
        igual(null, arvore.buscar(99), "Cliente inexistente");
        String antes = arvore.listarPreOrdem();
        falha(IllegalArgumentException.class, () -> arvore.inserir(cliente(20)));
        igual(antes, arvore.listarPreOrdem(), "Duplicidade nao altera ABB");
        igual(3, arvore.quantidade(), "Leituras nao alteram ABB");
    }

    private static void testarRemocoesDaArvore() {
        ArvoreClientes arvore = new ArvoreClientes();
        Cliente c50 = cliente(50);
        Cliente c30 = cliente(30);
        Cliente c70 = cliente(70);
        Cliente c20 = cliente(20);
        Cliente c40 = cliente(40);
        Cliente c60 = cliente(60);
        Cliente c80 = cliente(80);
        Cliente c65 = cliente(65);
        arvore.inserir(c50);
        arvore.inserir(c30);
        arvore.inserir(c70);
        arvore.inserir(c20);
        arvore.inserir(c40);
        arvore.inserir(c60);
        arvore.inserir(c80);
        arvore.inserir(c65);
        mesmo(c20, arvore.remover(20), "Remocao de folha");
        mesmo(c30, arvore.remover(30), "Remocao de no com um filho");
        mesmo(c40, arvore.buscar(40), "Filho preservado apos remocao");
        mesmo(c50, arvore.remover(50), "Remocao da raiz com dois filhos");
        igual(null, arvore.buscar(50), "Raiz removida deixa de existir");
        mesmo(c60, arvore.buscar(60), "Sucessor profundo preservado");
        mesmo(c65, arvore.buscar(65), "Filho direito do sucessor preservado");
        igual(linha(c40) + linha(c60) + linha(c65) + linha(c70) + linha(c80), arvore.listarEmOrdem(), "Ordem apos sucessor profundo");
        igual(5, arvore.quantidade(), "Quantidade apos remocoes");
        mesmo(c70, arvore.remover(70), "Remocao de no interno com dois filhos");
        mesmo(c65, arvore.buscar(65), "Subarvore esquerda preservada");
        mesmo(c80, arvore.buscar(80), "Sucessor imediato preservado");
        igual(null, arvore.remover(999), "Remocao inexistente");
        igual(4, arvore.quantidade(), "Remocao inexistente nao altera quantidade");
        ArvoreClientes simples = new ArvoreClientes();
        Cliente raiz = cliente(1);
        Cliente filho = cliente(2);
        simples.inserir(raiz);
        simples.inserir(filho);
        mesmo(raiz, simples.remover(1), "Raiz com um filho");
        mesmo(filho, simples.buscar(2), "Filho assume raiz");
        mesmo(filho, simples.remover(2), "Raiz folha");
        igual(0, simples.quantidade(), "ABB esvaziada");
        igual("", simples.listarPreOrdem(), "Raiz nula apos ultima remocao");
        simples.inserir(cliente(3));
        igual(3, simples.buscar(3).getId(), "Reinsercao apos esvaziar ABB");
        ArvoreClientes esquerda = new ArvoreClientes();
        esquerda.inserir(cliente(2));
        esquerda.inserir(cliente(1));
        esquerda.remover(2);
        igual(1, esquerda.buscar(1).getId(), "Raiz com filho esquerdo");
    }

    private static void testarFilaOrdenadaEValidacoes() {
        FilaAgendamentos fila = new FilaAgendamentos();
        igual(null, fila.buscar(1), "Busca em fila vazia");
        igual(null, fila.remover(1), "Remocao em fila vazia");
        igual(null, fila.atenderProximo(), "Atendimento em fila vazia");
        igual("", fila.listar(), "Listagem de fila vazia");
        igual(false, fila.possuiCliente(1), "Cliente ausente da fila vazia");
        falha(IllegalArgumentException.class, () -> fila.inserir(null));
        Agendamento terceiro = agendamento(3, 10, 0);
        Agendamento primeiro = agendamento(1, 8, 0);
        Agendamento segundo = agendamento(2, 9, 0);
        fila.inserir(terceiro);
        fila.inserir(primeiro);
        fila.inserir(segundo);
        String antes = linha(primeiro) + linha(segundo) + linha(terceiro);
        igual(antes, fila.listar(), "Insercao fora de ordem cronologica");
        mesmo(segundo, fila.buscar(2), "Busca no meio da fila");
        igual(null, fila.buscar(999), "Busca de agendamento inexistente");
        igual(true, fila.possuiCliente(1), "Cliente com agendamento");
        igual(false, fila.possuiCliente(99), "Outro cliente nao possui agendamento");
        falha(IllegalArgumentException.class, () -> fila.inserir(agendamento(1, 11, 0)));
        falha(IllegalArgumentException.class, () -> fila.inserir(agendamento(4, 9, 0)));
        Agendamento outroDia = new Agendamento(5, cliente(1), "Corte", DIA.plusDays(1), LocalTime.of(11, 0), new BigDecimal("30.00"));
        falha(IllegalArgumentException.class, () -> fila.inserir(outroDia));
        igual(antes, fila.listar(), "Validacoes e leituras nao alteram fila");
        igual(3, fila.quantidade(), "Quantidade preservada apos falhas");
        mesmo(primeiro, fila.atenderProximo(), "Primeiro horario atendido primeiro");
        mesmo(segundo, fila.atenderProximo(), "Segundo horario atendido depois");
        mesmo(terceiro, fila.atenderProximo(), "Ultimo horario atendido por ultimo");
        igual(0, fila.quantidade(), "Fila esvaziada por atendimento");
        igual(null, fila.atenderProximo(), "Atendimento apos esvaziar");
        fila.inserir(outroDia);
        mesmo(outroDia, fila.atenderProximo(), "Fila vazia aceita outro dia");
    }

    private static void testarRemocoesDaFila() {
        FilaAgendamentos fila = new FilaAgendamentos();
        Agendamento primeiro = agendamento(1, 8, 0);
        Agendamento segundo = agendamento(2, 9, 0);
        Agendamento terceiro = agendamento(3, 10, 0);
        Agendamento quarto = agendamento(4, 11, 0);
        fila.inserir(primeiro);
        fila.inserir(segundo);
        fila.inserir(terceiro);
        fila.inserir(quarto);
        mesmo(primeiro, fila.remover(1), "Cancelamento no inicio da fila");
        mesmo(terceiro, fila.remover(3), "Cancelamento no meio da fila");
        mesmo(quarto, fila.remover(4), "Cancelamento no fim da fila");
        igual(linha(segundo), fila.listar(), "Fila encadeada apos remocoes");
        Agendamento novoFim = agendamento(5, 12, 0);
        fila.inserir(novoFim);
        igual(linha(segundo) + linha(novoFim), fila.listar(), "Fim atualizado apos cancelamento");
        mesmo(novoFim, fila.remover(5), "Remocao do novo fim");
        mesmo(segundo, fila.remover(2), "Remocao do unico elemento");
        igual(0, fila.quantidade(), "Fila vazia apos ultimo cancelamento");
        igual("", fila.listar(), "Inicio nulo apos ultimo cancelamento");
        igual(null, fila.remover(2), "Remocao repetida");
        Agendamento reinserido = agendamento(6, 8, 30);
        fila.inserir(reinserido);
        mesmo(reinserido, fila.atenderProximo(), "Reinsercao depois do ultimo cancelamento");
    }

    private static void testarPilha() {
        PilhaCancelamentos pilha = new PilhaCancelamentos();
        igual(0, pilha.quantidade(), "Pilha inicialmente vazia");
        igual(null, pilha.buscar(1), "Busca na pilha vazia");
        igual(null, pilha.remover(), "Remocao na pilha vazia");
        igual("", pilha.listar(), "Listagem da pilha vazia");
        falha(IllegalArgumentException.class, () -> pilha.inserir(null));
        Cancelamento primeiro = new Cancelamento(agendamento(1, 8, 0), "Viagem", LocalDateTime.of(DIA, LocalTime.of(7, 0)));
        Cancelamento segundo = new Cancelamento(agendamento(2, 9, 0), "Imprevisto", LocalDateTime.of(DIA, LocalTime.of(7, 15)));
        pilha.inserir(primeiro);
        pilha.inserir(segundo);
        String antes = linha(segundo) + linha(primeiro);
        igual(antes, pilha.listar(), "Listagem do topo para base");
        mesmo(primeiro, pilha.buscar(1), "Busca de registro na base");
        mesmo(segundo, pilha.buscar(2), "Busca de registro no topo");
        igual(null, pilha.buscar(99), "Busca inexistente na pilha");
        igual(antes, pilha.listar(), "Busca nao modifica pilha");
        igual(2, pilha.quantidade(), "Leituras nao modificam quantidade");
        mesmo(segundo, pilha.remover(), "LIFO: ultimo cancelamento sai primeiro");
        mesmo(primeiro, pilha.remover(), "LIFO: primeiro cancelamento sai depois");
        igual(0, pilha.quantidade(), "Pilha esvaziada");
        pilha.inserir(segundo);
        mesmo(segundo, pilha.remover(), "Reinsercao apos esvaziar pilha");
    }

    private static void testarIntegracaoECancelamentos() {
        AgendaService agenda = new AgendaService(DIA);
        igual(null, agenda.atenderProximo(), "Atendimento integrado sem agenda");
        igual(null, agenda.cancelarAgendamento(999, "Ausencia"), "Cancelamento inexistente");
        igual(null, agenda.removerCancelamentoRecente(), "Historico inicialmente vazio");
        Cliente ana = agenda.cadastrarCliente(1, "Ana", "71999991234");
        agenda.cadastrarCliente(2, "Bia", "71988881234");
        falha(IllegalArgumentException.class, () -> agenda.cadastrarCliente(1, "Outra Ana", "71977771234"));
        mesmo(ana, agenda.buscarCliente(1), "Duplicidade preserva cliente original");
        igual(null, agenda.buscarCliente(999), "Busca integrada inexistente");
        igual(null, agenda.excluirCliente(999), "Exclusao integrada inexistente");
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(999, "Corte", DIA, LocalTime.of(8, 0), new BigDecimal("30.00")));
        Agendamento a = agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(10, 0), new BigDecimal("30.00"));
        Agendamento b = agenda.criarAgendamento(2, "Barba", DIA, LocalTime.of(8, 0), new BigDecimal("20.00"));
        Agendamento c = agenda.criarAgendamento(1, "Corte e barba", DIA, LocalTime.of(9, 0), new BigDecimal("45.00"));
        igual(a.getId() + 1, b.getId(), "IDs consecutivos de agendamentos");
        igual(b.getId() + 1, c.getId(), "IDs crescentes de agendamentos");
        igual(linha(b) + linha(c) + linha(a), agenda.listarAgenda(), "Agenda integrada cronologica");
        String clientesAntes = agenda.listarClientesEmOrdem();
        String agendaAntes = agenda.listarAgenda();
        falha(IllegalStateException.class, () -> agenda.excluirCliente(1));
        igual(clientesAntes, agenda.listarClientesEmOrdem(), "Exclusao bloqueada preserva ABB");
        igual(agendaAntes, agenda.listarAgenda(), "Exclusao bloqueada preserva fila");
        falha(IllegalArgumentException.class, () -> agenda.cancelarAgendamento(c.getId(), "   "));
        falha(IllegalArgumentException.class, () -> agenda.cancelarAgendamento(c.getId(), null));
        mesmo(c, agenda.buscarAgendamento(c.getId()), "Motivo invalido preserva agendamento");
        igual("", agenda.listarCancelamentos(), "Motivo invalido nao cria historico");
        Cancelamento cancelamentoMeio = agenda.cancelarAgendamento(c.getId(), "  Viagem  ");
        mesmo(c, cancelamentoMeio.getAgendamento(), "Cancelamento contem dados originais");
        igual("Viagem", cancelamentoMeio.getMotivo(), "Motivo normalizado");
        verifica(cancelamentoMeio.getDataHora() != null, "Cancelamento registra data e horario");
        igual(null, agenda.buscarAgendamento(c.getId()), "Cancelado removido da fila");
        mesmo(cancelamentoMeio, agenda.buscarCancelamento(c.getId()), "Cancelado localizado no historico");
        Cancelamento cancelamentoInicio = agenda.cancelarAgendamento(b.getId(), "Imprevisto");
        Cancelamento cancelamentoFim = agenda.cancelarAgendamento(a.getId(), "Ausencia");
        igual("", agenda.listarAgenda(), "Cancelamentos esvaziam fila");
        igual(linha(cancelamentoFim) + linha(cancelamentoInicio) + linha(cancelamentoMeio), agenda.listarCancelamentos(), "Historico integrado em LIFO");
        mesmo(ana, agenda.excluirCliente(1), "Cliente pode ser excluido apos cancelamento");
        agenda.cadastrarCliente(1, "Ana Nova", "71977771234");
        igual("Ana", agenda.buscarCancelamento(c.getId()).getAgendamento().getCliente().getNome(), "Historico preserva nome anterior");
        igual("71999991234", agenda.buscarCancelamento(c.getId()).getAgendamento().getCliente().getTelefone(), "Historico preserva telefone anterior");
        mesmo(cancelamentoFim, agenda.removerCancelamentoRecente(), "Desempilha cancelamento mais recente");
        mesmo(cancelamentoInicio, agenda.removerCancelamentoRecente(), "Desempilha segundo cancelamento");
        mesmo(cancelamentoMeio, agenda.removerCancelamentoRecente(), "Desempilha cancelamento mais antigo");
        igual(null, agenda.removerCancelamentoRecente(), "Desempilha historico esgotado");
        Agendamento novo = agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 0), new BigDecimal("30.00"));
        verifica(novo.getId() > c.getId(), "ID nao reutilizado apos limpar historico");
        mesmo(novo, agenda.atenderProximo(), "Atendimento remove inicio da fila integrada");
        igual(null, agenda.buscarAgendamento(novo.getId()), "Atendido nao fica pendente");
        igual(null, agenda.buscarCancelamento(novo.getId()), "Atendimento nao cria cancelamento");
    }

    private static void testarDiaEHorarios() {
        falha(IllegalArgumentException.class, () -> new AgendaService(null));
        AgendaService agenda = new AgendaService(DIA);
        agenda.cadastrarCliente(1, "Ana", "71999991234");
        igual(DIA, agenda.getDiaSelecionado(), "Dia inicial selecionado");
        falha(IllegalArgumentException.class, () -> agenda.selecionarDia(null));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", null, LocalTime.of(8, 0), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA.plusDays(1), LocalTime.of(8, 0), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, null, DIA, LocalTime.of(8, 0), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "  ", DIA, LocalTime.of(8, 0), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, null, new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(7, 30), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(18, 0), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 15), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 0, 1), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 0, 0, 1), new BigDecimal("30.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 0), null));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 0), BigDecimal.ZERO));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 0), new BigDecimal("-1.00")));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 0), new BigDecimal("30.001")));
        igual("", agenda.listarAgenda(), "Entradas invalidas nao alteram agenda");
        Agendamento primeiro = agenda.criarAgendamento(1, "Corte", DIA, LocalTime.of(8, 0), new BigDecimal("30.00"));
        falha(IllegalArgumentException.class, () -> agenda.criarAgendamento(1, "Barba", DIA, LocalTime.of(8, 0), new BigDecimal("20.00")));
        mesmo(primeiro, agenda.buscarAgendamento(primeiro.getId()), "Conflito preserva agendamento anterior");
        falha(IllegalStateException.class, () -> agenda.selecionarDia(DIA.plusDays(1)));
        igual(DIA, agenda.getDiaSelecionado(), "Troca bloqueada preserva dia");
        agenda.selecionarDia(DIA);
        mesmo(primeiro, agenda.buscarAgendamento(primeiro.getId()), "Selecionar o mesmo dia preserva fila");
        Agendamento ultimo = agenda.criarAgendamento(1, "Barba", DIA, LocalTime.of(17, 30), new BigDecimal("20.00"));
        igual(primeiro.getId() + 1, ultimo.getId(), "Falhas nao consomem IDs");
        mesmo(primeiro, agenda.atenderProximo(), "Limite inicial valido");
        mesmo(ultimo, agenda.atenderProximo(), "Limite final valido");
        agenda.selecionarDia(DIA.plusDays(1));
        igual(DIA.plusDays(1), agenda.getDiaSelecionado(), "Troca do dia com fila vazia");
        Agendamento outroDia = agenda.criarAgendamento(1, "Corte", DIA.plusDays(1), LocalTime.of(8, 0), new BigDecimal("30.00"));
        igual(DIA.plusDays(1), outroDia.getData(), "Novo agendamento usa novo dia");
    }

    private static Cliente cliente(int id) {
        return new Cliente(id, "Cliente " + id, "71999991234");
    }

    private static Agendamento agendamento(int id, int hora, int minuto) {
        return new Agendamento(id, cliente(1), "Corte", DIA, LocalTime.of(hora, minuto), new BigDecimal("30.00"));
    }

    private static String linha(Object objeto) {
        return objeto.toString() + System.lineSeparator();
    }

    private static void verifica(boolean condicao, String descricao) {
        verificacoes++;
        if (!condicao) {
            throw new AssertionError(descricao);
        }
    }

    private static void igual(Object esperado, Object atual, String descricao) {
        verifica(esperado == null ? atual == null : esperado.equals(atual), descricao + ": esperado=" + esperado + ", atual=" + atual);
    }

    private static void mesmo(Object esperado, Object atual, String descricao) {
        verifica(esperado == atual, descricao);
    }

    private static void falha(Class<? extends Throwable> tipo, Runnable acao) {
        Throwable obtida = null;
        try {
            acao.run();
        } catch (Throwable erro) {
            obtida = erro;
        }
        verifica(obtida != null && tipo.isInstance(obtida), "Esperada excecao " + tipo.getSimpleName() + "; recebida " + obtida);
    }
}
