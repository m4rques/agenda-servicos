package agenda;

public final class ArvoreClientes {
    private static final class No {
        private Cliente cliente;
        private No esquerda;
        private No direita;

        private No(Cliente cliente) {
            this.cliente = cliente;
        }
    }

    private No raiz;
    private int quantidade;

    public void inserir(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("O cliente deve ser informado.");
        }
        if (raiz == null) {
            raiz = new No(cliente);
            quantidade++;
            return;
        }
        No atual = raiz;
        while (true) {
            if (cliente.getId() == atual.cliente.getId()) {
                throw new IllegalArgumentException("Já existe um cliente com esse ID.");
            }
            if (cliente.getId() < atual.cliente.getId()) {
                if (atual.esquerda == null) {
                    atual.esquerda = new No(cliente);
                    quantidade++;
                    return;
                }
                atual = atual.esquerda;
            } else {
                if (atual.direita == null) {
                    atual.direita = new No(cliente);
                    quantidade++;
                    return;
                }
                atual = atual.direita;
            }
        }
    }

    public Cliente buscar(int id) {
        No atual = raiz;
        while (atual != null) {
            if (id == atual.cliente.getId()) {
                return atual.cliente;
            }
            atual = id < atual.cliente.getId() ? atual.esquerda : atual.direita;
        }
        return null;
    }

    public Cliente remover(int id) {
        No pai = null;
        No atual = raiz;
        while (atual != null && atual.cliente.getId() != id) {
            pai = atual;
            atual = id < atual.cliente.getId() ? atual.esquerda : atual.direita;
        }
        if (atual == null) {
            return null;
        }
        Cliente removido = atual.cliente;
        if (atual.esquerda != null && atual.direita != null) {
            No paiSucessor = atual;
            No sucessor = atual.direita;
            while (sucessor.esquerda != null) {
                paiSucessor = sucessor;
                sucessor = sucessor.esquerda;
            }
            atual.cliente = sucessor.cliente;
            pai = paiSucessor;
            atual = sucessor;
        }
        No filho = atual.esquerda != null ? atual.esquerda : atual.direita;
        if (pai == null) {
            raiz = filho;
        } else if (pai.esquerda == atual) {
            pai.esquerda = filho;
        } else {
            pai.direita = filho;
        }
        quantidade--;
        return removido;
    }

    public String listarEmOrdem() {
        StringBuilder resultado = new StringBuilder();
        emOrdem(raiz, resultado);
        return resultado.toString();
    }

    public String listarPreOrdem() {
        StringBuilder resultado = new StringBuilder();
        preOrdem(raiz, resultado);
        return resultado.toString();
    }

    public String listarPosOrdem() {
        StringBuilder resultado = new StringBuilder();
        posOrdem(raiz, resultado);
        return resultado.toString();
    }

    public int quantidade() {
        return quantidade;
    }

    private void emOrdem(No no, StringBuilder resultado) {
        if (no != null) {
            emOrdem(no.esquerda, resultado);
            resultado.append(no.cliente).append(System.lineSeparator());
            emOrdem(no.direita, resultado);
        }
    }

    private void preOrdem(No no, StringBuilder resultado) {
        if (no != null) {
            resultado.append(no.cliente).append(System.lineSeparator());
            preOrdem(no.esquerda, resultado);
            preOrdem(no.direita, resultado);
        }
    }

    private void posOrdem(No no, StringBuilder resultado) {
        if (no != null) {
            posOrdem(no.esquerda, resultado);
            posOrdem(no.direita, resultado);
            resultado.append(no.cliente).append(System.lineSeparator());
        }
    }
}
