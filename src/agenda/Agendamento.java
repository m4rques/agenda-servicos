package agenda;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Agendamento {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private final int id;
    private final Cliente cliente;
    private final String servico;
    private final LocalDate data;
    private final LocalTime horario;
    private final BigDecimal valor;

    public Agendamento(int id, Cliente cliente, String servico, LocalDate data,
                       LocalTime horario, BigDecimal valor) {
        Validacao.id(id);
        if (cliente == null || data == null || horario == null) {
            throw new IllegalArgumentException("Cliente, data e horario devem ser informados.");
        }
        this.servico = Validacao.texto(servico, "Servico");
        if (valor == null || valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Valor deve ser positivo, com no maximo duas casas decimais.");
        }
        this.id = id;
        this.cliente = cliente;
        this.data = data;
        this.horario = horario;
        this.valor = valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getServico() {
        return servico;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public BigDecimal getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return "Agendamento #" + id + " | " + DATA.format(data) + " " + HORA.format(horario)
                + " | " + cliente + " | " + servico + " | R$ "
                + valor.toPlainString().replace('.', ',');
    }
}
