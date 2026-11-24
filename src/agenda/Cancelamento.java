package agenda;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Cancelamento {
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm:ss");
    private final Agendamento agendamento;
    private final String motivo;
    private final LocalDateTime dataHora;

    public Cancelamento(Agendamento agendamento, String motivo, LocalDateTime dataHora) {
        if (agendamento == null || dataHora == null) {
            throw new IllegalArgumentException("Agendamento e momento do cancelamento devem ser informados.");
        }
        this.motivo = Validacao.texto(motivo, "Motivo");
        this.agendamento = agendamento;
        this.dataHora = dataHora;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    @Override
    public String toString() {
        return agendamento + " | Motivo: " + motivo + " | Cancelado em: " + DATA_HORA.format(dataHora);
    }
}
