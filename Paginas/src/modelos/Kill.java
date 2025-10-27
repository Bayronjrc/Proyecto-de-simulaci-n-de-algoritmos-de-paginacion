package modelos;

/**
 * Instrucción KILL: Libera toda la memoria asignada a un proceso
 * Debe ser la última instrucción de un proceso.
 * Formato: kill(pid)
 */

public class Kill extends Instruction {
    public Kill(int pid) {
        super(pid, "kill");
    }

    @Override
    public String toFileFormat() {
        return String.format("kill(%d)", pid);
    }

    @Override
    public Object execute() {
        // Se maneja en el MMU
        return null;
    }

    @Override
    public String toString() {
        return String.format("kill(%d)", pid);
    }
}
