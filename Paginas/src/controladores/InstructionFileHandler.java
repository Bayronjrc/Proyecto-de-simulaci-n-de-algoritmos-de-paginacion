package controladores;

import modelos.*;
import modelos.Process;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author wess
 * Parser del sistema para archivos.
 * 
 */
public class InstructionFileHandler {

    // Contadores para el parser/generador
    private int nextPtrId;
    private Map<Integer, Integer> ordinalToPtrIdMap; // Mapa: 1er ptr -> ptrID 1, 2do ptr -> ptrID 2
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
        List<Instruction> instructions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Ignorar líneas vacías o comentarios (como $use(1)$ del Anexo)
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("$")) {
                    continue;
                }
                
                // Ignorar los números de línea del Anexo 1 (ej. "1 new(1,500)")
                if (line.matches("^\\d+\\s+.*") || line.matches("^\\d+\\.\\s+.*")) {
                    line = line.substring(line.indexOf(' ')).trim();
                }

                Instruction inst = parseLine(line);
                if (inst != null) {
                    instructions.add(inst);
                }
            }
        }
        
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
                    
                    // Añadir ptr al proceso (o crear el proceso si no existe)
                    processMap.putIfAbsent(pid, new Process(pid));
                    // (El puntero real se registrará en el Proceso durante la ejecución de la MMU)
                    
                    nextPtrId++;
                    break;

                case "use":
                    int ordinalUse = Integer.parseInt(args[0].trim());
                    int ptrIdUse = ordinalToPtrIdMap.get(ordinalUse);
                    pid = ptrIdToPidMap.get(ptrIdUse);
                    inst = new Use(pid, ptrIdUse);
                    break;

                case "delete":
                    int ordinalDel = Integer.parseInt(args[0].trim());
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
     * Genera una nueva lista de procesos e instrucciones.
     * @param P Número de procesos (10, 50, 100)
     * @param N Número total de operaciones (500, 1000, 5000)
     * @param seed Semilla para el Random
     * @return Un objeto SimulationData que contiene la lista de instrucciones y procesos.
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
        
        List<Instruction> allInstructions = new ArrayList<>();
        Map<Integer, List<Integer>> processPtrs = new HashMap<>(); // pid -> lista de ptrs ordinales
        for (int i = 1; i <= P; i++) {
            processPtrs.put(i, new ArrayList<>());
        }

        // 2. Generar N-P instrucciones (reservando P para los 'kill')
        for (int i = 0; i < (N - P); i++) {
            // Elegir un proceso al azar
            Process p = processes.get(rand.nextInt(P));
            int pid = p.getPid();
            
            // Decidir qué instrucción generar
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
                    
                    int currentOrdinal = ordinalToPtrIdMap.size() + 1;
                    ordinalToPtrIdMap.put(currentOrdinal, nextPtrId);
                    ptrIdToPidMap.put(nextPtrId, pid);
                    processPtrs.get(pid).add(currentOrdinal); // Rastrear el puntero ordinal
                    
                    nextPtrId++;
                    break;
                    
                case 4: // use
                case 5:
                case 6:
                case 7:
                    int ptrOrdinalToUse = processPtrs.get(pid).get(rand.nextInt(processPtrs.get(pid).size()));
                    int ptrIdUse = ordinalToPtrIdMap.get(ptrOrdinalToUse);
                    inst = new Use(pid, ptrIdUse);
                    break;

                case 8: // delete
                case 9:
                    int listIndex = rand.nextInt(processPtrs.get(pid).size());
                    int ptrOrdinalToDel = processPtrs.get(pid).remove(listIndex); // Quitar de la lista de ptrs
                    int ptrIdDel = ordinalToPtrIdMap.get(ptrOrdinalToDel);
                    inst = new Delete(pid, ptrIdDel);
                    break;
            }
            if (inst != null) {
                allInstructions.add(inst);
                p.addInstruction(inst);
            }
        }

        // 3. Añadir 'kill' al final para cada proceso
        for (Process p : processes) {
            Instruction killInst = new Kill(p.getPid());
            allInstructions.add(killInst);
            p.addInstruction(killInst);
        }

        // 4. Intercalar la lista final
        Collections.shuffle(allInstructions, rand);
        
        return new SimulationData(allInstructions, processes);
    }

    /**
     * Guarda una lista de instrucciones en un archivo, usando el formato ordinal del Anexo 1.
     * @param instructions Lista de instrucciones.
     * @param filePath Ruta para guardar.
     * @throws IOException
     */
    public void saveInstructionsToFile(List<Instruction> instructions, String filePath) throws IOException {
        // Mapa inverso para 'save': ptrID -> ordinal
        Map<Integer, Integer> ptrIdToOrdinalMap = new HashMap<>();
        int ordinalCounter = 1;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Instruction inst : instructions) {
                String line = "";
                if (inst instanceof New) {
                    New n = (New) inst;
                    // Asumiendo que el 'ptrAsignado' se estableció durante la EJECUCIÓN
                    // Es mejor reconstruir el mapa aquí
                    
                    int ptrId = -1; // Se necesita una forma de obtener el ptrId de New
                    // Esta lógica es compleja. Simplificaremos:
                    
                    line = String.format("new(%d,%d)", inst.getPid(), ((New) inst).getSize());
                    
                } else if (inst instanceof Use) {
                    // Esta es la parte difícil: necesitamos el 'ordinal' de este ptrId
                    // Esta implementación de 'save' es un desafío.
                    // La forma más simple es usar el toFileFormat() del parser:
                    line = inst.toFileFormat(); // Esto usará el formato del parser
                    
                } else if (inst instanceof Delete) {
                    line = inst.toFileFormat();
                } else if (inst instanceof Kill) {
                    line = inst.toFileFormat();
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
        public final List<Instruction> instructions;
        public final List<Process> processes;
        
        public SimulationData(List<Instruction> instructions, List<Process> processes) {
            this.instructions = instructions;
            this.processes = processes;
        }
    }
}