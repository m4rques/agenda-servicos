package agenda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

public final class TestesConsole {
    private static int verificacoes;

    private interface Acao {
        void executar() throws Exception;
    }

    private static final class Leitura {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private final EntradaConsole entrada;

        private Leitura(String dados) throws Exception {
            entrada = new EntradaConsole(new ByteArrayInputStream(dados.getBytes(StandardCharsets.UTF_8)),
                    new PrintStream(bytes, true, "UTF-8"));
        }

        private String saida() throws Exception {
            return bytes.toString("UTF-8");
        }
    }

    public static void main(String[] args) throws Exception {
        testarTexto();
        testarInteiros();
        testarDatas();
        testarHorarios();
        testarValores();
        testarFimDaEntrada();
        testarRecuperacaoDoMenu();
        testarFluxoCompleto();
        testarEofNoPrograma();
        System.out.println("Todos os testes de console passaram: " + verificacoes + " verificacoes.");
    }

    private static void testarTexto() throws Exception {
        Leitura leitura = new Leitura(" \n\n  Joao da Silva  \n");
        igual("Joao da Silva", leitura.entrada.texto("Nome: "), "Texto remove espacos externos");
        igual(2, ocorrencias(leitura.saida(), "Preencha este campo."), "Texto vazio pede nova entrada");
        Leitura acento = new Leitura("  Jo\u00e3o  \n");
        igual("Jo\u00e3o", acento.entrada.texto("Nome: "), "Texto UTF-8 preserva acentos");
    }

    private static void testarInteiros() throws Exception {
        Leitura leitura = new Leitura("abc\n2147483648\n-2147483649\n0\n-3\n12\n");
        igual(12, leitura.entrada.inteiro("ID: ", true), "Inteiro valido apos entradas invalidas");
        igual(3, ocorrencias(leitura.saida(), "Entrada invalida:"), "Letras e overflow sao recusados");
        igual(2, ocorrencias(leitura.saida(), "Informe um inteiro positivo."), "ID zero e negativo sao recusados");
        Leitura opcao = new Leitura("-2\n0\n2147483647\n-2147483648\n");
        igual(-2, opcao.entrada.inteiro("Opcao: ", false), "Parser permite menu tratar opcao negativa");
        igual(0, opcao.entrada.inteiro("Opcao: ", false), "Opcao zero permite sair");
        igual(Integer.MAX_VALUE, opcao.entrada.inteiro("Opcao: ", false), "Limite superior de int");
        igual(Integer.MIN_VALUE, opcao.entrada.inteiro("Opcao: ", false), "Limite inferior de int");
    }

    private static void testarDatas() throws Exception {
        Leitura leitura = new Leitura("31/02/2026\n29/02/2025\n1/02/2026\n2026-02-01\n00/01/2026\n01/01/0000\n29/02/2024\n");
        igual(LocalDate.of(2024, 2, 29), leitura.entrada.data("Data: "), "Data bissexta aceita apos datas invalidas");
        igual(6, ocorrencias(leitura.saida(), "Data invalida."), "Datas inexistentes e formatos invalidos recusados");
        Leitura seculo = new Leitura("29/02/1900\n29/02/2000\n");
        igual(LocalDate.of(2000, 2, 29), seculo.entrada.data("Data: "), "Regra bissexta de seculos");
        igual(1, ocorrencias(seculo.saida(), "Data invalida."), "Ano 1900 nao e bissexto");
    }

    private static void testarHorarios() throws Exception {
        Leitura leitura = new Leitura("24:00\n08:60\n8:00\n08:00:00\n08:30\n");
        igual(LocalTime.of(8, 30), leitura.entrada.horario("Hora: "), "Horario valido apos recuperacao");
        igual(4, ocorrencias(leitura.saida(), "Horario invalido."), "Horas e formatos invalidos recusados");
        Leitura limites = new Leitura("00:00\n23:59\n");
        igual(LocalTime.MIDNIGHT, limites.entrada.horario("Hora: "), "Parser aceita inicio do dia");
        igual(LocalTime.of(23, 59), limites.entrada.horario("Hora: "), "Parser aceita fim do dia");
    }

    private static void testarValores() throws Exception {
        Leitura leitura = new Leitura("-1\n0\n0,00\n30.001\nNaN\n1.000,00\n2e2\n35,50\n");
        igual(new BigDecimal("35.50"), leitura.entrada.valor("Valor: "), "Valor com virgula aceito");
        igual(7, ocorrencias(leitura.saida(), "Valor invalido."), "Valores negativos, zero e formatos invalidos recusados");
        Leitura valores = new Leitura("25.75\n8\n9,5\n");
        igual(new BigDecimal("25.75"), valores.entrada.valor("Valor: "), "Valor com ponto aceito");
        igual(new BigDecimal("8"), valores.entrada.valor("Valor: "), "Valor inteiro aceito");
        igual(new BigDecimal("9.5"), valores.entrada.valor("Valor: "), "Uma casa decimal aceita");
    }

    private static void testarFimDaEntrada() throws Exception {
        Leitura vazio = new Leitura("");
        falha(EOFException.class, () -> vazio.entrada.texto("Texto: "));
        falha(EOFException.class, () -> vazio.entrada.inteiro("Inteiro: ", true));
        falha(EOFException.class, () -> vazio.entrada.data("Data: "));
        falha(EOFException.class, () -> vazio.entrada.horario("Hora: "));
        falha(EOFException.class, () -> vazio.entrada.valor("Valor: "));
        Leitura invalida = new Leitura("abc\n");
        falha(EOFException.class, () -> invalida.entrada.inteiro("Inteiro: ", true));
        igual(1, ocorrencias(invalida.saida(), "Entrada invalida:"), "Entrada invalida antes do EOF e informada");
    }

    private static void testarRecuperacaoDoMenu() throws Exception {
        String dados = "31/02/2026\n16/11/2026\nabc\n2147483648\n-1\n99\n"
                + "1\n0\n-3\n1\n\nAna\n123\n"
                + "1\n1\nAna\n71999991234\n"
                + "1\n1\nOutra Ana\n71988881234\n"
                + "2\n1\n"
                + "8\n1\nCorte\n31/02/2026\n16/11/2026\n24:00\n08:60\n08:00\n-1\n0\nNaN\n30.001\n35,50\n"
                + "9\n1\n0\n";
        String saida = executarPrograma(dados);
        contem(saida, "Data invalida.", "Main informa data invalida");
        igual(2, ocorrencias(saida, "Entrada invalida:"), "Main recupera letras e overflow no menu");
        igual(2, ocorrencias(saida, "Opcao invalida."), "Main recupera opcoes negativas e inexistentes");
        igual(2, ocorrencias(saida, "Informe um inteiro positivo."), "Main valida ID positivo");
        contem(saida, "Preencha este campo.", "Main recupera nome vazio");
        igual(2, ocorrencias(saida, "Operacao recusada:"), "Main recupera telefone invalido e cliente duplicado");
        contem(saida, "Cliente localizado:", "Main permanece utilizavel apos operacoes recusadas");
        igual(2, ocorrencias(saida, "Horario invalido."), "Main recupera horario invalido");
        igual(4, ocorrencias(saida, "Valor invalido."), "Main recupera valor invalido");
        contem(saida, "Agendamento criado:", "Main conclui agendamento apos corrigir entradas");
        contem(saida, "Agendamento localizado:", "Main recupera menu apos agendamento");
        contem(saida, "Programa encerrado.", "Main encerra normalmente apos entradas invalidas");
        semFalhaInesperada(saida);
    }

    private static void testarFluxoCompleto() throws Exception {
        String dados = "16/11/2026\n"
                + "1\n10\nAna Silva\n(71) 99999-1234\n"
                + "8\n10\nCorte\n16/11/2026\n09:00\n35,50\n"
                + "10\n"
                + "8\n10\nBarba\n16/11/2026\n09:00\n25,00\n"
                + "9\n1\n"
                + "12\n1\nViagem\n"
                + "10\n13\n14\n1\n15\n13\n15\n0\n";
        String saida = executarPrograma(dados);
        igual(1, ocorrencias(saida, "Cliente cadastrado:"), "Fluxo cadastra um cliente");
        contem(saida, "Ana Silva", "Fluxo apresenta cliente cadastrado");
        contem(saida, "71999991234", "Fluxo normaliza telefone");
        igual(1, ocorrencias(saida, "Agendamento criado:"), "Conflito nao cria segundo agendamento");
        igual(1, ocorrencias(saida, "Operacao recusada:"), "Fluxo recusa conflito de horario");
        contem(saida, "Agendamento localizado:", "Agendamento original permanece apos conflito");
        contem(saida, "Agendamento cancelado:", "Fluxo cancela agendamento");
        contem(saida, "Nenhum agendamento pendente.", "Cancelamento esvazia agenda do dia");
        contem(saida, "Viagem", "Historico contem motivo de cancelamento");
        contem(saida, "Cancelamento localizado:", "Fluxo busca cancelamento");
        contem(saida, "Registro removido do historico:", "Fluxo desempilha cancelamento");
        contem(saida, "Nenhum cancelamento registrado.", "Historico fica vazio apos desempilhar");
        contem(saida, "Pilha de cancelamentos vazia.", "Fluxo trata desempilhamento em pilha vazia");
        contem(saida, "Programa encerrado.", "Fluxo termina pela opcao sair");
        verifica(saida.indexOf("Agendamento criado:") < saida.indexOf("Agendamento cancelado:"), "Fluxo cria antes de cancelar");
        verifica(saida.indexOf("Agendamento cancelado:") < saida.indexOf("Registro removido do historico:"), "Fluxo cancela antes de desempilhar");
        semFalhaInesperada(saida);
    }

    private static void testarEofNoPrograma() throws Exception {
        String inicio = executarPrograma("");
        contem(inicio, "Entrada encerrada. Programa finalizado.", "EOF na data inicial encerra normalmente");
        semFalhaInesperada(inicio);
        String menu = executarPrograma("16/11/2026\n");
        contem(menu, "Entrada encerrada. Programa finalizado.", "EOF no menu encerra normalmente");
        semFalhaInesperada(menu);
        String cadastro = executarPrograma("16/11/2026\n1\n10\nAna\n");
        contem(cadastro, "Entrada encerrada. Programa finalizado.", "EOF no meio do cadastro encerra normalmente");
        verifica(!cadastro.contains("Cliente cadastrado:"), "Cadastro incompleto nao e confirmado");
        semFalhaInesperada(cadastro);
        String agendamento = executarPrograma("16/11/2026\n1\n10\nAna\n71999991234\n8\n10\nCorte\n16/11/2026\n09:00\n");
        contem(agendamento, "Entrada encerrada. Programa finalizado.", "EOF no meio do agendamento encerra normalmente");
        verifica(!agendamento.contains("Agendamento criado:"), "Agendamento incompleto nao e confirmado");
        semFalhaInesperada(agendamento);
    }

    private static String executarPrograma(String dados) throws Exception {
        InputStream entradaOriginal = System.in;
        PrintStream saidaOriginal = System.out;
        PrintStream erroOriginal = System.err;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream captura = new PrintStream(bytes, true, "UTF-8");
        try {
            System.setIn(new ByteArrayInputStream(dados.getBytes(StandardCharsets.UTF_8)));
            System.setOut(captura);
            System.setErr(captura);
            Main.main(new String[0]);
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(saidaOriginal);
            System.setErr(erroOriginal);
            captura.close();
        }
        mesmo(entradaOriginal, System.in, "Entrada padrao restaurada apos teste");
        mesmo(saidaOriginal, System.out, "Saida padrao restaurada apos teste");
        mesmo(erroOriginal, System.err, "Saida de erro restaurada apos teste");
        return bytes.toString("UTF-8");
    }

    private static int ocorrencias(String texto, String trecho) {
        int quantidade = 0;
        int inicio = 0;
        while ((inicio = texto.indexOf(trecho, inicio)) >= 0) {
            quantidade++;
            inicio += trecho.length();
        }
        return quantidade;
    }

    private static void semFalhaInesperada(String saida) {
        verifica(!saida.contains("Exception"), "Console nao apresenta excecao inesperada");
        verifica(!saida.contains("\tat agenda."), "Console nao apresenta stacktrace");
        verifica(!saida.contains("Falha na leitura"), "Console nao apresenta falha de leitura");
    }

    private static void contem(String texto, String trecho, String descricao) {
        verifica(texto.contains(trecho), descricao);
    }

    private static void igual(Object esperado, Object atual, String descricao) {
        verifica(esperado == null ? atual == null : esperado.equals(atual), descricao + ": esperado=" + esperado + ", atual=" + atual);
    }

    private static void mesmo(Object esperado, Object atual, String descricao) {
        verifica(esperado == atual, descricao);
    }

    private static void verifica(boolean condicao, String descricao) {
        verificacoes++;
        if (!condicao) {
            throw new AssertionError(descricao);
        }
    }

    private static void falha(Class<? extends Throwable> tipo, Acao acao) {
        Throwable obtida = null;
        try {
            acao.executar();
        } catch (Throwable erro) {
            obtida = erro;
        }
        verifica(obtida != null && tipo.isInstance(obtida), "Esperada excecao " + tipo.getSimpleName() + "; recebida " + obtida);
    }
}
