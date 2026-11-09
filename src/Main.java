import java.time.Duration;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Menu de console. Só lê/escreve no console; as regras ficam em ServicoClinica.
 */
public class Main {

    private static final int LIMITE_FILA_CONSULTAS = 5;

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.print("Ativar atendimento prioritário na fila de consultas (PriorityQueue)? (s/n): ");
        boolean prioridade = in.hasNextLine() && in.nextLine().trim().equalsIgnoreCase("s");
        ServicoClinica clinica = new ServicoClinica(LIMITE_FILA_CONSULTAS, prioridade);
        System.out.println(prioridade
                ? "Modo prioritário: Fila 2 = PriorityQueue (prioritários primeiro, depois por senha)."
                : "Modo FIFO: Fila 2 = ArrayDeque.");

        while (true) {
            System.out.println();
            System.out.println("===== CLÍNICA - FILAS =====");
            System.out.println("1 - Retirar senha");
            System.out.println("2 - Chamar próximo na recepção (Fila 1 -> Fila 2)");
            System.out.println("3 - Chamar próximo para consulta");
            System.out.println("4 - Listar fila de atendimento");
            System.out.println("5 - Listar fila de consultas");
            System.out.println("6 - Ver próximo de cada fila (peek)");
            System.out.println("7 - Relatório");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            if (!in.hasNextLine()) {
                return; // entrada encerrada (Ctrl+Z / fim de arquivo)
            }
            switch (in.nextLine().trim()) {
                case "1" -> retirarSenha(clinica, in);
                case "2" -> chamarNaRecepcao(clinica);
                case "3" -> chamarParaConsulta(clinica);
                case "4" -> listar("Fila de atendimento", clinica.getFilaAtendimento());
                case "5" -> listar("Fila de consultas (limite " + clinica.getLimiteConsultas() + ")",
                        clinica.getFilaConsultas());
                case "6" -> verProximos(clinica);
                case "7" -> relatorio(clinica);
                case "0" -> {
                    System.out.println("Encerrando.");
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void retirarSenha(ServicoClinica clinica, Scanner in) {
        System.out.print("Nome: ");
        String nome = in.nextLine().trim();
        System.out.print("CPF: ");
        String cpf = in.nextLine().trim();
        if (nome.isEmpty() || cpf.isEmpty()) {
            System.out.println("Nome e CPF são obrigatórios.");
            return;
        }
        boolean prioritario = false;
        if (clinica.isAtendimentoPrioritario()) {
            System.out.print("Prioritário (idoso, gestante...)? (s/n): ");
            prioritario = in.nextLine().trim().equalsIgnoreCase("s");
        }
        Paciente p = clinica.retirarSenha(nome, cpf, prioritario);
        System.out.println("Senha retirada: " + p);
    }

    private static void chamarNaRecepcao(ServicoClinica clinica) {
        try {
            Paciente p = clinica.chamarNaRecepcao();
            if (p == null) {
                System.out.println("Fila de atendimento vazia.");
            } else {
                System.out.println("Encaminhado para consulta: " + p);
            }
        } catch (IllegalStateException e) {
            System.out.println("AVISO: " + e.getMessage());
        }
    }

    private static void chamarParaConsulta(ServicoClinica clinica) {
        try {
            Paciente p = clinica.chamarParaConsulta();
            System.out.println("Chamado para consulta: " + p);
        } catch (NoSuchElementException e) {
            // remove() lança exceção com a fila vazia; tratamos aqui para o programa não quebrar.
            System.out.println("Fila de consultas vazia. Nenhum paciente para chamar.");
        }
    }

    private static void listar(String titulo, Collection<Paciente> fila) {
        System.out.println(titulo + " - " + fila.size() + " paciente(s):");
        if (fila.isEmpty()) {
            System.out.println("  (vazia)");
        }
        int pos = 1;
        for (Paciente p : fila) { // o iterador percorre na ordem FIFO, sem remover ninguém
            System.out.println("  " + pos++ + ". " + p);
        }
    }

    private static void verProximos(ServicoClinica clinica) {
        Paciente a = clinica.proximoAtendimento();
        Paciente c = clinica.proximoConsulta();
        System.out.println("Próximo na recepção: " + (a == null ? "(fila vazia)" : a));
        System.out.println("Próximo na consulta: " + (c == null ? "(fila vazia)" : c));
    }

    private static void relatorio(ServicoClinica clinica) {
        Duration media = clinica.tempoMedioEspera();
        System.out.println("===== RELATÓRIO =====");
        System.out.println("Pacientes atendidos: " + clinica.getAtendidos().size());
        for (Paciente p : clinica.getAtendidos()) {
            System.out.println("  " + p);
        }
        System.out.printf("Tempo médio de espera (chegada até a consulta): %d min %d s%n",
                media.toMinutes(), media.toSecondsPart());
        System.out.println("Tamanho da fila de atendimento: " + clinica.getFilaAtendimento().size());
        System.out.println("Tamanho da fila de consultas: " + clinica.getFilaConsultas().size()
                + "/" + clinica.getLimiteConsultas());
    }
}
