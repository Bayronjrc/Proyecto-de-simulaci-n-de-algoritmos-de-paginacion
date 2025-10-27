package modelos;

/**
 * Instruccion DELETE: Elimina un puntero de la tabla de simbolos
 * y libera la memoria asignada.
 * Formato: delete(ptr)
 */

public class Delete extends Instruction {
    private int ptr;

    public Delete(int pid, int ptr) {
        super(pid, "delete");
        this.ptr = ptr;
    }

    @Override
    public String toFileFormat() {
        return String.format("delete(%d)", ptr);
    }

    @Override
    public Object execute() {
        //Se maneja en el MMU
        return null;
    }

    // Getter y Setter
    public int getPtr() {
        return ptr;
    }

    public void setPtr(int ptr) {
        this.ptr = ptr;
    }
    @Override
    public String toString() {
        return String.format("delete(%d) [pid=%d]", ptr, pid);
    }
    
}
