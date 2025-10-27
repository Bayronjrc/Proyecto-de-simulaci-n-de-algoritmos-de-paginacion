package modelos;

/**
 * Instrucción NEW: Solicita memoria de tamaño size en bytes.
 * Formato: new(pid,size)
 */

public class New extends Instruction {
    private int size;
    private Integer ptrAsignado;

    public New(int pid, int size) {
        super(pid, "new");
        this.size = size;
        this.ptrAsignado = null;
    }

    @Override
    public String toFileFormat() {
        return String.format("new(%d,%d)", pid, size);
    }
    @Override
    public Object execute() {
        //Se maneja en el MMU
        return null;
    }

    /**
     * Calcula cuántas páginas se necesitan para este tamaño.
     * @param pageSize tamaño de página en bytes(4KB por defecto).
     * @return número de páginas necesarias.
     */
    public int calcularPaginasNecesarias(int pageSize) {
        return (int) Math.ceil((double) size / pageSize);
    }

    // Getters y Setters
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Integer getPtrAsignado() {
        return ptrAsignado;
    }

    public void setPtrAsignado(Integer ptrAsignado) {
        this.ptrAsignado = ptrAsignado;
    }

    @Override
    public String toString() {
        if (ptrAsignado != null) {
            return String.format("new(%d, %d) -> prt=%d [%d páginas]", pid, size, ptrAsignado, calcularPaginasNecesarias(4096));
        }
        return String.format("new(%d, %d)",pid, size);
    }
}
