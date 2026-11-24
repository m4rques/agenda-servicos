package agenda;

public final class PilhaCancelamentos {
    private static final class No {
        private final Cancelamento cancelamento;
        private No proximo;

        private No(Cancelamento cancelamento) {
            this.cancelamento = cancelamento;
        }
    }

    private No topo;
    private int quantidade;

    public void inserir(Cancelamento cancelamento) {
        if (cancelamento == null) {
            throw new IllegalArgumentException("O cancelamento deve ser informado.");
        }
        No novo = new No(cancelamento);
        novo.proximo = topo;
        topo = novo;
        quantidade++;
    }

    public Cancelamento buscar(int idAgendamento) {
        No atual = topo;
        while (atual != null) {
            if (atual.cancelamento.getAgendamento().getId() == idAgendamento) {
                return atual.cancelamento;
            }
            atual = atual.proximo;
        }
        return null;
    }

    public Cancelamento remover() {
        if (topo == null) {
            return null;
        }
        Cancelamento removido = topo.cancelamento;
        topo = topo.proximo;
        quantidade--;
        return removido;
    }

    public String listar() {
        StringBuilder resultado = new StringBuilder();
        No atual = topo;
        while (atual != null) {
            resultado.append(atual.cancelamento).append(System.lineSeparator());
            atual = atual.proximo;
        }
        return resultado.toString();
    }

    public int quantidade() {
        return quantidade;
    }
}
