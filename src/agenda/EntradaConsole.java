package agenda;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class EntradaConsole {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private final BufferedReader leitor;
    private final PrintStream saida;

    public EntradaConsole(InputStream entrada, PrintStream saida) {
        leitor = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        this.saida = saida;
    }

    private String linha(String mensagem) throws IOException {
        saida.print(mensagem);
        saida.flush();
        String valor = leitor.readLine();
        if (valor == null) {
            throw new EOFException();
        }
        return valor.trim();
    }

    public String texto(String mensagem) throws IOException {
        while (true) {
            String valor = linha(mensagem);
            if (!valor.isEmpty()) {
                return valor;
            }
            saida.println("Preencha este campo.");
        }
    }

    public int inteiro(String mensagem, boolean positivo) throws IOException {
        while (true) {
            try {
                int valor = Integer.parseInt(linha(mensagem));
                if (!positivo || valor > 0) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                saida.println("Entrada invalida: informe um numero inteiro dentro do limite de int.");
                continue;
            }
            saida.println("Informe um inteiro positivo.");
        }
    }

    public LocalDate data(String mensagem) throws IOException {
        while (true) {
            String valor = linha(mensagem);
            try {
                if (valor.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
                    LocalDate data = LocalDate.parse(valor, DATA);
                    if (data.getYear() > 0) {
                        return data;
                    }
                }
            } catch (DateTimeParseException e) {
                saida.println("Data invalida. Use dd/MM/aaaa com uma data existente.");
                continue;
            }
            saida.println("Data invalida. Use dd/MM/aaaa com uma data existente.");
        }
    }

    public LocalTime horario(String mensagem) throws IOException {
        while (true) {
            String valor = linha(mensagem);
            try {
                if (valor.matches("[0-9]{2}:[0-9]{2}")) {
                    return LocalTime.parse(valor, HORA);
                }
            } catch (DateTimeParseException e) {
                saida.println("Horario invalido. Use HH:mm, de 00:00 a 23:59.");
                continue;
            }
            saida.println("Horario invalido. Use HH:mm, de 00:00 a 23:59.");
        }
    }

    public BigDecimal valor(String mensagem) throws IOException {
        while (true) {
            String texto = linha(mensagem);
            if (texto.matches("[0-9]+([.,][0-9]{1,2})?")) {
                BigDecimal valor = new BigDecimal(texto.replace(',', '.'));
                if (valor.signum() > 0) {
                    return valor;
                }
            }
            saida.println("Valor invalido. Informe um valor positivo, como 35,50, sem separador de milhar.");
        }
    }
}
