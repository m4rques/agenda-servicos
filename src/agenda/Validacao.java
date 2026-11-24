package agenda;

final class Validacao {
    private Validacao() {
    }

    static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " deve ser informado.");
        }
        return valor.trim();
    }

    static void id(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O ID deve ser um inteiro positivo.");
        }
    }
}
