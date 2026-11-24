package agenda;

public final class FilaAgendamentos {
    private static final class No {
        private final Agendamento agendamento;
        private No proximo;

        private No(Agendamento agendamento) {
            this.agendamento = agendamento;
        }
    }

    private No inicio;
    private No fim;
    private int quantidade;

    public void inserir(Agendamento agendamento) {
        if (agendamento == null) {
            throw new IllegalArgumentException("O agendamento deve ser informado.");
        }
        if (inicio != null && !inicio.agendamento.getData().equals(agendamento.getData())) {
            throw new IllegalArgumentException("A fila só pode conter agendamentos do mesmo dia.");
        }
        No atual = inicio;
        while (atual != null) {
            if (atual.agendamento.getId() == agendamento.getId()) {
                throw new IllegalArgumentException("Já existe um agendamento com esse ID.");
            }
            if (atual.agendamento.getHorario().equals(agendamento.getHorario())) {
                throw new IllegalArgumentException("Esse horário já está ocupado.");
            }
            atual = atual.proximo;
        }
        No novo = new No(agendamento);
        if (inicio == null) {
            inicio = novo;
            fim = novo;
        } else if (agendamento.getHorario().isBefore(inicio.agendamento.getHorario())) {
            novo.proximo = inicio;
            inicio = novo;
        } else {
            No anterior = inicio;
            while (anterior.proximo != null
                    && anterior.proximo.agendamento.getHorario().isBefore(agendamento.getHorario())) {
                anterior = anterior.proximo;
            }
            novo.proximo = anterior.proximo;
            anterior.proximo = novo;
            if (novo.proximo == null) {
                fim = novo;
            }
        }
        quantidade++;
    }

    public Agendamento buscar(int id) {
        No atual = inicio;
        while (atual != null) {
            if (atual.agendamento.getId() == id) {
                return atual.agendamento;
            }
            atual = atual.proximo;
        }
        return null;
    }

    public Agendamento remover(int id) {
        No anterior = null;
        No atual = inicio;
        while (atual != null && atual.agendamento.getId() != id) {
            anterior = atual;
            atual = atual.proximo;
        }
        if (atual == null) {
            return null;
        }
        if (anterior == null) {
            inicio = atual.proximo;
        } else {
            anterior.proximo = atual.proximo;
        }
        if (atual == fim) {
            fim = anterior;
        }
        quantidade--;
        return atual.agendamento;
    }

    public Agendamento atenderProximo() {
        if (inicio == null) {
            return null;
        }
        Agendamento atendido = inicio.agendamento;
        inicio = inicio.proximo;
        if (inicio == null) {
            fim = null;
        }
        quantidade--;
        return atendido;
    }

    public String listar() {
        StringBuilder resultado = new StringBuilder();
        No atual = inicio;
        while (atual != null) {
            resultado.append(atual.agendamento).append(System.lineSeparator());
            atual = atual.proximo;
        }
        return resultado.toString();
    }

    public boolean possuiCliente(int idCliente) {
        No atual = inicio;
        while (atual != null) {
            if (atual.agendamento.getCliente().getId() == idCliente) {
                return true;
            }
            atual = atual.proximo;
        }
        return false;
    }

    public int quantidade() {
        return quantidade;
    }
}
