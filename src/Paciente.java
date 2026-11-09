import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Paciente da clínica. A senha é gerada automaticamente e em sequência.
 */
public class Paciente {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static int proximaSenha = 1; // contador compartilhado por todos os pacientes

    private final int senha;
    private final String nome;
    private final String cpf;
    private final LocalDateTime horaChegada;
    private final boolean prioritario; // idosos, gestantes etc. (bônus)
    private LocalDateTime horaConsulta; // preenchida quando o médico chama (usada no tempo de espera)

    public Paciente(String nome, String cpf) {
        this(nome, cpf, false);
    }

    public Paciente(String nome, String cpf, boolean prioritario) {
        this.senha = proximaSenha++;
        this.nome = nome;
        this.cpf = cpf;
        this.prioritario = prioritario;
        this.horaChegada = LocalDateTime.now();
    }

    public int getSenha() { return senha; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public LocalDateTime getHoraChegada() { return horaChegada; }
    public LocalDateTime getHoraConsulta() { return horaConsulta; }
    public boolean isPrioritario() { return prioritario; }

    void registrarConsulta() { this.horaConsulta = LocalDateTime.now(); }

    @Override
    public String toString() {
        return String.format("Senha %03d | %-20s | CPF %s | chegou %s%s",
                senha, nome, cpf, horaChegada.format(HORA), prioritario ? " | PRIORITÁRIO" : "");
    }
}
