import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Regras de negócio da clínica e as duas filas.
 *
 * Chegada -> [Fila 1: Atendimento] -> recepção -> [Fila 2: Consultas] -> médico
 *
 * Fluxo normal usa offer/poll/peek (retornam valor especial).
 * A chamada para consulta usa remove(), que lança NoSuchElementException com a fila vazia.
 */
public class ServicoClinica {

    /** Bônus: prioritários primeiro; entre iguais, a menor senha (quem chegou antes). */
    public static final Comparator<Paciente> ORDEM_PRIORIDADE =
            Comparator.comparing(Paciente::isPrioritario).reversed()
                    .thenComparingInt(Paciente::getSenha);

    // Declaradas pela interface Queue (polimorfismo): o resto do código não depende da implementação.
    private final Queue<Paciente> filaAtendimento = new LinkedList<>();
    private final Queue<Paciente> filaConsultas;
    private final List<Paciente> atendidos = new ArrayList<>();
    private final int limiteConsultas;
    private final boolean atendimentoPrioritario;

    /** Versão obrigatória: as duas filas são FIFO puras. */
    public ServicoClinica(int limiteConsultas) {
        this(limiteConsultas, false);
    }

    /**
     * @param atendimentoPrioritario true = bônus: a Fila 2 vira PriorityQueue e prioritários passam na frente.
     *                               A Fila 1 continua FIFO (a prioridade só vale na fila de consultas).
     */
    public ServicoClinica(int limiteConsultas, boolean atendimentoPrioritario) {
        if (limiteConsultas <= 0) {
            throw new IllegalArgumentException("O limite da fila de consultas deve ser maior que zero");
        }
        this.limiteConsultas = limiteConsultas;
        this.atendimentoPrioritario = atendimentoPrioritario;
        // Única linha que muda entre as versões: o resto só usa métodos de Queue.
        this.filaConsultas = atendimentoPrioritario
                ? new PriorityQueue<>(ORDEM_PRIORIDADE)
                : new ArrayDeque<>();
    }

    /** Regra 1: o paciente retira a senha e entra no fim da Fila de Atendimento. */
    public Paciente retirarSenha(String nome, String cpf) {
        return retirarSenha(nome, cpf, false);
    }

    public Paciente retirarSenha(String nome, String cpf, boolean prioritario) {
        Paciente p = new Paciente(nome, cpf, prioritario);
        filaAtendimento.offer(p);
        return p;
    }

    /**
     * Regras 2 e 3: move o primeiro da Fila 1 para a Fila 2.
     *
     * @return o paciente encaminhado, ou null se a Fila 1 estiver vazia
     * @throws IllegalStateException se a Fila 2 estiver cheia (o paciente continua na Fila 1)
     */
    public Paciente chamarNaRecepcao() {
        if (filaAtendimento.peek() == null) {
            return null;
        }
        // Queue não tem capacidade fixa, então o limite é controlado com size().
        if (filaConsultas.size() >= limiteConsultas) {
            throw new IllegalStateException("Fila de consultas cheia (" + limiteConsultas
                    + " pacientes). O paciente continua na fila de atendimento.");
        }
        Paciente p = filaAtendimento.poll();
        filaConsultas.offer(p);
        return p;
    }

    /**
     * Regra 4: o médico chama o primeiro da Fila 2, que vai para a lista de atendidos.
     *
     * @throws NoSuchElementException se a Fila 2 estiver vazia (comportamento de remove())
     */
    public Paciente chamarParaConsulta() {
        Paciente p = filaConsultas.remove();
        p.registrarConsulta();
        atendidos.add(p);
        return p;
    }

    /** Próximo da Fila 1 sem remover (peek: null se vazia). */
    public Paciente proximoAtendimento() { return filaAtendimento.peek(); }

    /** Próximo da Fila 2 sem remover (peek: null se vazia). */
    public Paciente proximoConsulta() { return filaConsultas.peek(); }

    /** Tempo médio entre a chegada e a chamada do médico, só dos atendidos. */
    public Duration tempoMedioEspera() {
        if (atendidos.isEmpty()) {
            return Duration.ZERO;
        }
        Duration total = Duration.ZERO;
        for (Paciente p : atendidos) {
            total = total.plus(Duration.between(p.getHoraChegada(), p.getHoraConsulta()));
        }
        return total.dividedBy(atendidos.size());
    }

    // Visões somente leitura: quem chama pode listar, mas não alterar as filas por fora das regras.
    public Collection<Paciente> getFilaAtendimento() { return Collections.unmodifiableCollection(filaAtendimento); }

    /** Cópia da Fila 2 na ordem em que os pacientes serão chamados. */
    public List<Paciente> getFilaConsultas() {
        List<Paciente> copia = new ArrayList<>(filaConsultas);
        if (atendimentoPrioritario) {
            // O iterador da PriorityQueue segue o heap, não a ordem de saída; por isso ordenamos a cópia.
            copia.sort(ORDEM_PRIORIDADE);
        }
        return copia;
    }

    public List<Paciente> getAtendidos() { return Collections.unmodifiableList(atendidos); }
    public int getLimiteConsultas() { return limiteConsultas; }
    public boolean isAtendimentoPrioritario() { return atendimentoPrioritario; }
}
