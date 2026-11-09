import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * Testes do trabalho, sem bibliotecas externas: cada teste lança AssertionError se falhar.
 * Executar: java -cp out TesteClinica
 */
public class TesteClinica {

    private static int passou = 0;
    private static int falhou = 0;

    public static void main(String[] args) {
        rodar("1. Filas vazias não quebram o programa", TesteClinica::filasVazias);
        rodar("2. Fila 2 cheia: paciente fica na Fila 1", TesteClinica::fila2Cheia);
        rodar("3. Fluxo completo de um paciente", TesteClinica::fluxoCompleto);
        rodar("4. remove() em fila vazia lança e é tratada", TesteClinica::removeEmFilaVazia);
        rodar("5. Ordem FIFO preservada nas duas filas", TesteClinica::ordemFifo);
        rodar("6. Diferença add/offer, remove/poll, element/peek", TesteClinica::metodosQueue);
        rodar("7. Bônus: prioritário passa na frente na Fila 2", TesteClinica::prioridade);

        System.out.println();
        System.out.println(passou + " passaram, " + falhou + " falharam.");
        if (falhou > 0) {
            System.exit(1);
        }
    }

    static void filasVazias() {
        ServicoClinica c = new ServicoClinica(5);
        verificar(c.chamarNaRecepcao() == null, "recepção com Fila 1 vazia deve retornar null");
        verificar(c.proximoAtendimento() == null, "peek na Fila 1 vazia deve ser null");
        verificar(c.proximoConsulta() == null, "peek na Fila 2 vazia deve ser null");
        verificar(c.tempoMedioEspera().isZero(), "tempo médio sem atendidos deve ser zero");
    }

    static void fila2Cheia() {
        ServicoClinica c = new ServicoClinica(2);
        c.retirarSenha("A", "1");
        c.retirarSenha("B", "2");
        Paciente terceiro = c.retirarSenha("C", "3");
        c.chamarNaRecepcao();
        c.chamarNaRecepcao();
        try {
            c.chamarNaRecepcao();
            throw new AssertionError("deveria avisar que a Fila 2 está cheia");
        } catch (IllegalStateException esperado) {
            // ok
        }
        verificar(c.getFilaConsultas().size() == 2, "Fila 2 deve continuar com 2");
        verificar(c.proximoAtendimento() == terceiro, "o terceiro deve continuar na frente da Fila 1");

        c.chamarParaConsulta(); // abre uma vaga
        verificar(c.chamarNaRecepcao() == terceiro, "com vaga, o terceiro deve ser encaminhado");
    }

    static void fluxoCompleto() {
        ServicoClinica c = new ServicoClinica(5);
        Paciente p = c.retirarSenha("Maria", "123");
        verificar(c.getFilaAtendimento().contains(p), "após a senha, está na Fila 1");

        verificar(c.chamarNaRecepcao() == p, "recepção deve encaminhar Maria");
        verificar(c.getFilaAtendimento().isEmpty(), "saiu da Fila 1");
        verificar(c.getFilaConsultas().contains(p), "entrou na Fila 2");

        verificar(c.chamarParaConsulta() == p, "médico deve chamar Maria");
        verificar(c.getFilaConsultas().isEmpty(), "saiu da Fila 2");
        verificar(c.getAtendidos().contains(p), "está na lista de atendidos");
        verificar(p.getHoraConsulta() != null, "hora da consulta registrada");
        verificar(!c.tempoMedioEspera().isNegative(), "tempo de espera não pode ser negativo");
    }

    static void removeEmFilaVazia() {
        ServicoClinica c = new ServicoClinica(5);
        try {
            c.chamarParaConsulta();
            throw new AssertionError("remove() em fila vazia deveria lançar NoSuchElementException");
        } catch (NoSuchElementException esperado) {
            // ok: é exatamente o que o Main captura para não quebrar
        }
        verificar(c.getAtendidos().isEmpty(), "nada deve ir para atendidos");
    }

    static void ordemFifo() {
        ServicoClinica c = new ServicoClinica(5);
        Paciente a = c.retirarSenha("A", "1");
        Paciente b = c.retirarSenha("B", "2");
        Paciente d = c.retirarSenha("D", "3");
        verificar(a.getSenha() < b.getSenha() && b.getSenha() < d.getSenha(), "senhas sequenciais");

        verificar(c.chamarNaRecepcao() == a, "Fila 1: A primeiro");
        verificar(c.chamarNaRecepcao() == b, "Fila 1: B segundo");
        verificar(c.chamarNaRecepcao() == d, "Fila 1: D terceiro");
        verificar(c.chamarParaConsulta() == a, "Fila 2: A primeiro");
        verificar(c.chamarParaConsulta() == b, "Fila 2: B segundo");
        verificar(c.chamarParaConsulta() == d, "Fila 2: D terceiro");
    }

    static void metodosQueue() {
        Queue<String> q = new ArrayDeque<>();
        verificar(q.poll() == null, "poll() em vazia retorna null");
        verificar(q.peek() == null, "peek() em vazia retorna null");
        lancaNoSuchElement(q::remove, "remove()");
        lancaNoSuchElement(q::element, "element()");
        verificar(q.offer("x") && q.add("y"), "offer/add retornam true quando inserem");
        verificar(q.element().equals("x") && q.peek().equals("x"), "element/peek veem o primeiro");
    }

    static void prioridade() {
        ServicoClinica c = new ServicoClinica(5, true);
        Paciente comum1 = c.retirarSenha("Comum 1", "1");
        Paciente idoso = c.retirarSenha("Idoso", "2", true);
        Paciente comum2 = c.retirarSenha("Comum 2", "3");
        Paciente gestante = c.retirarSenha("Gestante", "4", true);

        // Fila 1 continua FIFO: a recepção chama na ordem de chegada.
        verificar(c.chamarNaRecepcao() == comum1, "Fila 1 segue FIFO mesmo no modo prioritário");
        c.chamarNaRecepcao();
        c.chamarNaRecepcao();
        c.chamarNaRecepcao();

        verificar(c.getFilaConsultas().equals(java.util.List.of(idoso, gestante, comum1, comum2)),
                "listagem da Fila 2 deve mostrar a ordem real de chamada");
        verificar(c.proximoConsulta() == idoso, "peek deve mostrar o prioritário de menor senha");
        // Prioritários primeiro; entre iguais, quem tem a menor senha.
        verificar(c.chamarParaConsulta() == idoso, "1º: Idoso (prioritário, senha menor)");
        verificar(c.chamarParaConsulta() == gestante, "2º: Gestante (prioritária)");
        verificar(c.chamarParaConsulta() == comum1, "3º: Comum 1");
        verificar(c.chamarParaConsulta() == comum2, "4º: Comum 2");
    }

    // ---------- utilitários ----------

    private static void lancaNoSuchElement(Runnable acao, String nome) {
        try {
            acao.run();
            throw new AssertionError(nome + " em fila vazia deveria lançar NoSuchElementException");
        } catch (NoSuchElementException esperado) {
            // ok
        }
    }

    private static void verificar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    private static void rodar(String nome, Runnable teste) {
        try {
            teste.run();
            passou++;
            System.out.println("[OK]    " + nome);
        } catch (AssertionError | RuntimeException e) {
            falhou++;
            System.out.println("[FALHA] " + nome + " -> " + e.getMessage());
        }
    }
}
