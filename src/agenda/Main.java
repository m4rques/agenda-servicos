package agenda;

import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class Main {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    private final EntradaConsole entrada = new EntradaConsole(System.in, System.out);
    private AgendaService sistema;

    public static void main(String[] args) {
        new Main().executar();
    }

    private void executar() {
        System.out.println("AGENDA DE SERVICOS - BARBEARIA");
        System.out.println("Um profissional; servicos de 30 minutos, de 08:00 a 18:00.");
        System.out.println("Os dados ficam na memoria e sao perdidos ao sair.");
        try {
            sistema = new AgendaService(entrada.data("Dia da agenda (dd/MM/aaaa): "));
            while (true) {
                menu();
                int opcao = entrada.inteiro("Opcao: ", false);
                if (opcao == 0) {
                    System.out.println("Programa encerrado.");
                    return;
                }
                try {
                    executarOpcao(opcao);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.out.println("Operacao recusada: " + e.getMessage());
                }
            }
        } catch (EOFException e) {
            System.out.println("\nEntrada encerrada. Programa finalizado.");
        } catch (IOException e) {
            System.out.println("Falha na leitura da entrada: " + e.getMessage());
        }
    }

    private void menu() {
        System.out.println("\nDia selecionado: " + DATA.format(sistema.getDiaSelecionado()));
        System.out.println("1 - Cadastrar cliente");
        System.out.println("2 - Buscar cliente por ID");
        System.out.println("3 - Listar clientes em in-ordem");
        System.out.println("4 - Excluir cliente");
        System.out.println("5 - Exibir clientes em pre-ordem");
        System.out.println("6 - Exibir clientes em pos-ordem");
        System.out.println("7 - Selecionar outro dia (fila deve estar vazia)");
        System.out.println("8 - Criar agendamento");
        System.out.println("9 - Buscar agendamento por ID");
        System.out.println("10 - Listar agenda do dia");
        System.out.println("11 - Atender proximo agendamento");
        System.out.println("12 - Cancelar agendamento");
        System.out.println("13 - Listar cancelamentos do topo para a base");
        System.out.println("14 - Buscar cancelamento por ID do agendamento");
        System.out.println("15 - Remover cancelamento mais recente");
        System.out.println("0 - Sair");
    }

    private void executarOpcao(int opcao) throws IOException {
        switch (opcao) {
            case 1:
                cadastrarCliente();
                break;
            case 2:
                exibir(sistema.buscarCliente(id("ID do cliente: ")), "Cliente nao encontrado.", "Cliente localizado:");
                break;
            case 3:
                listar(sistema.listarClientesEmOrdem(), "Nenhum cliente cadastrado.");
                break;
            case 4:
                exibir(sistema.excluirCliente(id("ID do cliente: ")), "Cliente nao encontrado.", "Cliente excluido:");
                break;
            case 5:
                listar(sistema.listarClientesPreOrdem(), "Nenhum cliente cadastrado.");
                break;
            case 6:
                listar(sistema.listarClientesPosOrdem(), "Nenhum cliente cadastrado.");
                break;
            case 7:
                sistema.selecionarDia(entrada.data("Novo dia (dd/MM/aaaa): "));
                System.out.println("Dia selecionado.");
                break;
            case 8:
                criarAgendamento();
                break;
            case 9:
                exibir(sistema.buscarAgendamento(id("ID do agendamento: ")), "Agendamento nao encontrado.", "Agendamento localizado:");
                break;
            case 10:
                listar(sistema.listarAgenda(), "Nenhum agendamento pendente.");
                break;
            case 11:
                exibir(sistema.atenderProximo(), "Nenhum agendamento pendente.", "Atendimento realizado:");
                break;
            case 12:
                cancelarAgendamento();
                break;
            case 13:
                listar(sistema.listarCancelamentos(), "Nenhum cancelamento registrado.");
                break;
            case 14:
                exibir(sistema.buscarCancelamento(id("ID do agendamento cancelado: ")), "Cancelamento nao encontrado.", "Cancelamento localizado:");
                break;
            case 15:
                exibir(sistema.removerCancelamentoRecente(), "Pilha de cancelamentos vazia.", "Registro removido do historico:");
                break;
            default:
                System.out.println("Opcao invalida. Escolha uma opcao de 0 a 15.");
        }
    }

    private int id(String mensagem) throws IOException {
        return entrada.inteiro(mensagem, true);
    }

    private void cadastrarCliente() throws IOException {
        int id = id("ID do cliente: ");
        String nome = entrada.texto("Nome: ");
        String telefone = entrada.texto("Telefone com DDD: ");
        exibir(sistema.cadastrarCliente(id, nome, telefone), "", "Cliente cadastrado:");
    }

    private void criarAgendamento() throws IOException {
        int idCliente = id("ID do cliente: ");
        String servico = entrada.texto("Servico: ");
        LocalDate data = entrada.data("Data (dd/MM/aaaa): ");
        LocalTime horario = entrada.horario("Horario (HH:mm; 08:00 a 17:30): ");
        BigDecimal valor = entrada.valor("Valor (ex.: 35,50): ");
        exibir(sistema.criarAgendamento(idCliente, servico, data, horario, valor), "", "Agendamento criado:");
    }

    private void cancelarAgendamento() throws IOException {
        int id = id("ID do agendamento: ");
        String motivo = entrada.texto("Motivo: ");
        exibir(sistema.cancelarAgendamento(id, motivo), "Agendamento nao encontrado.", "Agendamento cancelado:");
    }

    private void exibir(Object registro, String ausente, String sucesso) {
        if (registro == null) {
            System.out.println(ausente);
        } else {
            System.out.println(sucesso);
            System.out.println(registro);
        }
    }

    private void listar(String registros, String mensagemVazia) {
        if (registros.isEmpty()) {
            System.out.println(mensagemVazia);
        } else {
            System.out.print(registros);
        }
    }
}
