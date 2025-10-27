package modelos;

/**
 * Clase abstracta base para todas las instrucciones del sistema.
 */

 public abstract class Instruction {
    protected int pid;
    protected String tipo;

    public Instruction(int pid, String tipo) {
        this.pid = pid;
        this.tipo = tipo;
    }

    /**
     * Método abstracto que cada instrucción debe implementar
     * @return String representando la instruccion
     */
    public abstract String toFileFormat();

    /**
     * Ejectua la instrucción.
     * @return resultado de la ejecución.
     */
    public abstract Object execute();
    public int getPid() {
        return pid;
    }
    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return String.format("%s(pid=%d)", tipo, pid);
    }
 }