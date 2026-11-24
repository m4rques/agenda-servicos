package agenda;

public final class Cliente {
    private final int id;
    private final String nome;
    private final String telefone;

    public Cliente(int id, String nome, String telefone) {
        Validacao.id(id);
        this.nome = Validacao.texto(nome, "Nome");
        String numeros = Validacao.texto(telefone, "Telefone").replaceAll("[()\\s-]", "");
        if (!numeros.matches("[0-9]{10,11}")) {
            throw new IllegalArgumentException("Informe telefone com DDD e 10 ou 11 digitos.");
        }
        this.id = id;
        this.telefone = numeros;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    @Override
    public String toString() {
        return "Cliente #" + id + " | " + nome + " | Telefone: " + telefone;
    }
}
