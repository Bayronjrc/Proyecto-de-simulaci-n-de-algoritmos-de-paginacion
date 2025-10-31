package modelos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representa un proceso en el sistema con su lista de instrucciones
 * y tabla de simbolos (punteros asignados).
 */

public class Process {
    private int pid;
    private List<Instruction> instructionList;
    private Map<Integer, Boolean> symbolTableMap; //ptr -> existe o no
    private boolean active;
    private int instructionPointer;

    public Process(int pid) {
        this.pid = pid;
        this.instructionList = new ArrayList<>();
        this.symbolTableMap = new HashMap<>();
        this.active = true;
        this.instructionPointer = 0;
    }

    /**
     * Agrega una instrución al proceso.
     */
    public void addInstruction(Instruction instruction) {
        //pid coincide?
        if (instruction.getPid() != this.pid) {
            throw new IllegalArgumentException("La instrucción no pertenece a este proceso");
        }

        //si cae Kill, al final
        if(!instructionList.isEmpty() && instructionList.get(instructionList.size()-1) instanceof Kill) {
            throw new IllegalStateException("No se pueden agregar instrucciones despues de kill");
        }

        instructionList.add(instruction);
    }

    /**
     * Obtiene una lista de todos los punteros (ptrs) que este proceso posee actualmente.
     * Para simplificar la función de Kill()
     * @return Una lista de IDs de punteros.
     */
    public List<Integer> getOwnedPointers() {
        // Devuelve una nueva lista de todas las llaves (ptrs) en el mapa
        return new ArrayList<>(symbolTableMap.keySet());
    }
    
    /**
     * Registra un puntero en la tabla de simbolos.
     */
    public void registerPointer(int ptr) {
        symbolTableMap.put(ptr, true);
    }
    
    /**
     * Elimina un puntero de la tabla de simbolos.
     */
    public void removePointer(int ptr) {
        symbolTableMap.remove(ptr);
    }

    /**
     * Verifica si un puntero existe en la tabla de simbolos.
     */
    public boolean pointerExists(int ptr) {
        return symbolTableMap.containsKey(ptr);
    }

    /**
     * Obtiene la siguiente instrucción a ejecutar.
     */
    public Instruction getNextInstruction() {
        if (instructionPointer < instructionList.size()) {
            return instructionList.get(instructionPointer++);
        }
        return null;
    }

    /**
     * Verifica si el proceso ha terminado todas sus instrucciones.
     */
    public boolean isFinished() {
        return instructionPointer >= instructionList.size() || !active;
    }

    /**
     * Finaliza el proceso( usado en Kill).
     */
    public void terminate() {
        this.active = false;
        this.symbolTableMap.clear();
    }

    /*
     * Reinicia el proceso para una nueva simulación.
     */
    public void reset() {
        this.instructionPointer = 0;
        this.active = true;
        this.symbolTableMap.clear();
    }

    /**
     * Genera el contenido del archivo de instrucciones para este proceso.
     */
    public String toFileFormat() {
        StringBuilder sb = new StringBuilder();
        for (Instruction inst : instructionList) {
            sb.append(inst.toFileFormat()).append("\n");
        }
        return sb.toString();
    }

    //Getters
    public int getPid() {
        return pid;
    }

    public List<Instruction> getInstructionList() {
        return new ArrayList<>(instructionList); // copia defensiva
    }

    public int getTotalInstructions() {
        return instructionList.size();
    }

    public Map<Integer, Boolean> getSymbolTableMap() {
        return new HashMap<>(symbolTableMap); // copia defensiva
    }

    public boolean isActive() {
        return active;
    }

    public int getInstructionPointer() {
        return instructionPointer;
    }

    @Override
    public String toString() {
        return String.format ("Proceso[pid=%d, instrucciones=%d, ptrs=%d, activo=%s]", pid, instructionList.size(), symbolTableMap.size(), active);
    }
}