package controladores;

import modelos.*;
import modelos.Process; // Importar su clase

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Maneja la carga, generación y guardado de archivos de instrucciones.
 * GENERADOR ROBUSTO: Ahora genera secuencias lógicamente correctas por proceso.
 */
public class InstructionFileHandler {

    // Contadores para el parser/generador
    private int nextPtrId;
    private Map<Integer, Integer> ordinalToPtrIdMap; // Mapa: 1er ptr -> ptrID 1
    private Map<Integer, Integer> ptrIdToPidMap;     // Mapa: ptrID -> PID
    private Map<Integer, Process> processMap;        // Mapa: PID -> Objeto Process

    public InstructionFileHandler() {
        resetParserState();
    }
    
    private void resetParserState() {
        this.nextPtrId = 1;
        this.ordinalToPtrIdMap = new HashMap<>();
        this.ptrIdToPidMap = new HashMap<>();
        this.processMap = new HashMap<>();
    }

    /**
     * Carga una lista de instrucciones y procesos desde un archivo.
     * @param filePath Ruta al archivo.
     * @return Un objeto SimulationData que contiene la lista de instrucciones y procesos.
     * @throws IOException Si el archivo no se puede leer.
     */
    public SimulationData loadInstructionsFromFile(String filePath) throws IOException {
        resetParserState();
        List<Instruction> instructions = new ArrayList<>(); // Esta es la lista intercalada
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("$")) {
                    continue;
                }
                
                if (line.matches("^\\d+\\s+.*") || line.matches("^\\d+\\.\\s+.*")) {
                    line = line.substring(line.indexOf(' ')).trim();
                }

                Instruction inst = parseLine(line);
                if (inst != null) {
                    instructions.add(inst);
                }
            }
        }
        
        // El parser ya añade las instrucciones a los procesos
        return new SimulationData(instructions, new ArrayList<>(processMap.values()));
    }
    
    /**
     * Parsea una sola línea de instrucción y actualiza los mapas.
     */
    private Instruction parseLine(String line) {
        try {
            String command = line.substring(0, line.indexOf('('));
            String[] args = line.substring(line.indexOf('(') + 1, line.indexOf(')'))
                                .split(",");
            Instruction inst = null;
            int pid;

            switch (command.trim()) {
                case "new":
                    pid = Integer.parseInt(args[0].trim());
                    int size = Integer.parseInt(args[1].trim());
                    inst = new New(pid, size);
                    
                    // --- Lógica del "parser inteligente" ---
                    int currentOrdinal = ordinalToPtrIdMap.size() + 1;
                    ordinalToPtrIdMap.put(currentOrdinal, nextPtrId); // Mapea 1 -> ptrId 1
                    ptrIdToPidMap.put(nextPtrId, pid);                // Mapea ptrId 1 -> pid 1
                    
                    processMap.putIfAbsent(pid, new Process(pid));
                    
                    nextPtrId++;
                    break;

                case "use":
                    int ordinalUse = Integer.parseInt(args[0].trim());
                    // Manejar error si el puntero ordinal no existe
                    if (!ordinalToPtrIdMap.containsKey(ordinalUse)) {
                        throw new IllegalArgumentException("Error de Parseo: Puntero ordinal " + ordinalUse + " no existe.");
                    }
                    int ptrIdUse = ordinalToPtrIdMap.get(ordinalUse);
                    pid = ptrIdToPidMap.get(ptrIdUse);
                    inst = new Use(pid, ptrIdUse);
                    break;

                case "delete":
                    int ordinalDel = Integer.parseInt(args[0].trim());
                    if (!ordinalToPtrIdMap.containsKey(ordinalDel)) {
                        throw new IllegalArgumentException("Error de Parseo: Puntero ordinal " + ordinalDel + " no existe.");
                    }
                    int ptrIdDel = ordinalToPtrIdMap.get(ordinalDel);
                    pid = ptrIdToPidMap.get(ptrIdDel);
                    inst = new Delete(pid, ptrIdDel);
                    break;

                case "kill":
                    pid = Integer.parseInt(args[0].trim());
                    inst = new Kill(pid);
                    break;

                default:
                    System.err.println("Instrucción desconocida: " + line);
                    return null;
            }
            
            // Añadir la instrucción a la lista de su proceso
            if (inst != null) {
                 processMap.putIfAbsent(inst.getPid(), new Process(inst.getPid()));
                 processMap.get(inst.getPid()).addInstruction(inst);
            }
            return inst;

        } catch (Exception e) {
            System.err.println("Error parseando línea: " + line + " -> " + e.getMessage());
            return null;
        }
    }


    /**
     * Genera una nueva lista de procesos e instrucciones (LÓGICA ROBUSTA).
     * Ya no crea una lista intercalada, solo añade instrucciones lógicas a cada proceso.
     */
    public SimulationData generateProcesses(int P, int N, long seed) {
        resetParserState();
        List<Process> processes = new ArrayList<>();
        Random rand = new Random(seed);

        // 1. Crear P procesos
        for (int i = 1; i <= P; i++) {
            Process p = new Process(i);
            processes.add(p);
            processMap.put(i, p);
        }
        
        // Mapa para rastrear los punteros ORDINALES VÁLIDOS por proceso
        // Esto simula la "tabla de símbolos" de cada proceso
        Map<Integer, List<Integer>> processPtrs = new HashMap<>();
        for (int i = 1; i <= P; i++) {
            processPtrs.put(i, new ArrayList<>());
        }

        // 2. Generar N-P instrucciones (reservando P para los 'kill')
        for (int i = 0; i < (N - P); i++) {
            // Elegir un proceso al azar
            Process p = processes.get(rand.nextInt(P));
            int pid = p.getPid();
            
            int instructionType = rand.nextInt(10); // 0-3=new, 4-7=use, 8-9=delete
            
            // Regla: 'use' o 'delete' solo si el proceso tiene punteros
            if ((instructionType >= 4) && processPtrs.get(pid).isEmpty()) {
                instructionType = 0; // Forzar 'new' si no hay punteros
            }

            Instruction inst = null;
            switch (instructionType) {
                case 0: // new
                case 1:
                case 2:
                case 3:
                    int size = (rand.nextInt(10) + 1) * 512; // Tamaños de 512B a 5120B
                    inst = new New(pid, size);
                    
                    // --- Lógica de "parser inteligente" para generación ---
                    int currentOrdinal = ordinalToPtrIdMap.size() + 1;
                    ordinalToPtrIdMap.put(currentOrdinal, nextPtrId);
                    ptrIdToPidMap.put(nextPtrId, pid);
                    
                    // Añadir el *ptrID real* a la lista de punteros del proceso
                    processPtrs.get(pid).add(nextPtrId); // Rastrear el puntero real
                    
                    nextPtrId++;
                    break;
                    
                case 4: // use
                case 5:
                case 6:
                case 7:
                    // Elegir un ptrID aleatorio de la lista de punteros válidos de este proceso
                    int ptrIdToUse = processPtrs.get(pid).get(rand.nextInt(processPtrs.get(pid).size()));
                    inst = new Use(pid, ptrIdToUse);
                    break;

                case 8: // delete
                case 9:
                    // Elegir y *eliminar* un ptrID de la lista de punteros válidos
                    int listIndex = rand.nextInt(processPtrs.get(pid).size());
                    int ptrIdToDel = processPtrs.get(pid).remove(listIndex); // Quitar de la lista
                    inst = new Delete(pid, ptrIdToDel);
                    break;
            }
            
            if (inst != null) {
                p.addInstruction(inst); // Añadir al proceso
            }
        }

        // 3. Añadir 'kill' al final de la lista de cada proceso
        for (Process p : processes) {
            Instruction killInst = new Kill(p.getPid());
            p.addInstruction(killInst); 
        }

        // 4. NO devolver una lista intercalada. Devolver solo los procesos.
        // El Controller se encargará de intercalar en tiempo de ejecución.
        return new SimulationData(null, processes);
    }
    
    /**
     * Guarda una lista de instrucciones en un archivo.
     * (NOTA: Esta implementación es compleja y puede necesitar ajustes
     * para recrear el formato ordinal 1-a-1).
     */
    public void saveInstructionsToFile(List<Instruction> instructions, String filePath) throws IOException {
        
        // (Esta lógica es para MODO CARGA, no MODO GENERADO)
        // Para guardar el modo generado, tendríamos que re-intercalar
        
        System.out.println("Guardando " + instructions.size() + " instrucciones en " + filePath);
        
        // Reconstruir mapa de ptrID -> ordinal
        Map<Integer, Integer> ptrIdToOrdinalMap = new HashMap<>();
        int ordinalCounter = 1;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Instruction inst : instructions) {
                String line = "";
                if (inst instanceof New) {
                    New n = (New) inst;
                    // Asumir que el 'ptrAsignado' fue seteado por la MMU
                    int ptrId = n.getPtrAsignado() != null ? n.getPtrAsignado() : -1;
                    
                    if (ptrId != -1 && !ptrIdToOrdinalMap.containsKey(ptrId)) {
                        ptrIdToOrdinalMap.put(ptrId, ordinalCounter++);
                    }
                    line = String.format("new(%d,%d)", inst.getPid(), n.getSize());
                    
                } else if (inst instanceof Use) {
                    int ptrId = ((Use) inst).getPtr();
                    int ordinal = ptrIdToOrdinalMap.getOrDefault(ptrId, -1);
                    line = String.format("use(%d)", ordinal);
                    
                } else if (inst instanceof Delete) {
                    int ptrId = ((Delete) inst).getPtr();
                    int ordinal = ptrIdToOrdinalMap.getOrDefault(ptrId, -1);
                    line = String.format("delete(%d)", ordinal);
                    
                } else if (inst instanceof Kill) {
                    line = String.format("kill(%d)", inst.getPid());
                }
                
                writer.write(line);
                writer.newLine();
            }
        }
    }


    /**
     * Clase interna para devolver múltiples valores desde el handler.
     */
    public static class SimulationData {
        public final List<Instruction> instructions; // Usado para 'load' (lista intercalada)
        public final List<Process> processes;      // Usado para 'generate' (procesos con listas ordenadas)
        
        public SimulationData(List<Instruction> instructions, List<Process> processes) {
            this.instructions = instructions;
            this.processes = processes;
        }
    }
}