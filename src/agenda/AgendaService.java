package agenda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public final class AgendaService {
    private final ArvoreClientes clientes = new ArvoreClientes();
    private final FilaAgendamentos agenda = new FilaAgendamentos();
    private final PilhaCancelamentos cancelamentos = new PilhaCancelamentos();
    private LocalDate diaSelecionado;
    private long proximoId = 1;

    public AgendaService(LocalDate dia) {
        selecionarDia(dia);
    }

    public LocalDate getDiaSelecionado() {
        return diaSelecionado;
    }

    public void selecionarDia(LocalDate dia) {
        if (dia == null) {
            throw new IllegalArgumentException("Informe o dia da agenda.");
        }
        if (!dia.equals(diaSelecionado) && agenda.quantidade() > 0) {
            throw new IllegalStateException("Atenda ou cancele os pendentes antes de trocar o dia.");
        }
        diaSelecionado = dia;
    }

    public Cliente cadastrarCliente(int id, String nome, String telefone) {
        Cliente cliente = new Cliente(id, nome, telefone);
        clientes.inserir(cliente);
        return cliente;
    }

    public Cliente buscarCliente(int id) {
        return clientes.buscar(id);
    }

    public Cliente excluirCliente(int id) {
        if (agenda.possuiCliente(id)) {
            throw new IllegalStateException("Cliente possui agendamento pendente e nao pode ser excluido.");
        }
        return clientes.remover(id);
    }

    public String listarClientesEmOrdem() {
        return clientes.listarEmOrdem();
    }

    public String listarClientesPreOrdem() {
        return clientes.listarPreOrdem();
    }

    public String listarClientesPosOrdem() {
        return clientes.listarPosOrdem();
    }

    public Agendamento criarAgendamento(int idCliente, String servico, LocalDate data,
                                       LocalTime horario, BigDecimal valor) {
        Cliente cliente = clientes.buscar(idCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente nao cadastrado.");
        }
        if (data == null || !data.equals(diaSelecionado)) {
            throw new IllegalArgumentException("A data deve ser igual ao dia selecionado na agenda.");
        }
        if (horario == null || horario.getSecond() != 0 || horario.getNano() != 0
                || (horario.getMinute() != 0 && horario.getMinute() != 30)
                || horario.isBefore(LocalTime.of(8, 0)) || horario.isAfter(LocalTime.of(17, 30))) {
            throw new IllegalArgumentException("Use horarios de 30 minutos, de 08:00 a 17:30.");
        }
        if (proximoId > Integer.MAX_VALUE) {
            throw new IllegalStateException("Limite de IDs de agendamento atingido nesta sessao.");
        }
        Agendamento agendamento = new Agendamento((int) proximoId, cliente, servico, data, horario, valor);
        agenda.inserir(agendamento);
        proximoId++;
        return agendamento;
    }

    public Agendamento buscarAgendamento(int id) {
        return agenda.buscar(id);
    }

    public String listarAgenda() {
        return agenda.listar();
    }

    public Agendamento atenderProximo() {
        return agenda.atenderProximo();
    }

    public Cancelamento cancelarAgendamento(int id, String motivo) {
        String motivoValidado = Validacao.texto(motivo, "Motivo");
        Agendamento agendamento = agenda.buscar(id);
        if (agendamento == null) {
            return null;
        }
        Cancelamento cancelamento = new Cancelamento(agendamento, motivoValidado,
                LocalDateTime.now(ZoneId.of("America/Bahia")));
        agenda.remover(id);
        cancelamentos.inserir(cancelamento);
        return cancelamento;
    }

    public Cancelamento buscarCancelamento(int idAgendamento) {
        return cancelamentos.buscar(idAgendamento);
    }

    public String listarCancelamentos() {
        return cancelamentos.listar();
    }

    public Cancelamento removerCancelamentoRecente() {
        return cancelamentos.remover();
    }
}
