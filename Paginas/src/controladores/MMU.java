/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import modelos.*;
import modelos.Process; // Importa su clase Process
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wess
 * La MMU (Memory Management Unit) es el controlador central de la simulación.
 * Ejecuta las instrucciones, administra la memoria física (Computer) y
 * la memoria lógica (SymbolTable), y utiliza un algoritmo de reemplazo
 * de páginas cuando es necesario.
 */
public class MMU {

    // --- Componentes del Modelo ---
    private Computer computer;
    private SymbolTable symbolTable; // El mapa de memoria GLOBAL (ptr -> List<Page>)
    private Map<Integer, Process> processMap; // Mapa de PID -> Objeto Process

    // --- Algoritmo de Paginación ---
    private PageReplacementAlgorithm algorithm;
    
    // --- Estadísticas de Simulación ---
    private long totalTime;
    private long thrashingTime;
    
    // Constantes de tiempo del PDF
    private static final int TIME_HIT = 1;      // 1s por hit de página
    private static final int TIME_FAULT = 5;    // 5s por fallo de página
    private static final int PAGE_SIZE_BYTES = 4096; // 4KB por página

    /**
     * Constructor para una instancia de simulación.
     * @param algorithm El algoritmo de paginación a utilizar (FIFO, SC, MRU, RND).
     * @param processes La lista de todos los procesos que participarán en la simulación.
     */
    public MMU(PageReplacementAlgorithm algorithm, List<Process> processes) {
        this.computer = new Computer(); // Crea el hardware (400KB RAM, 100 marcos)
        this.symbolTable = new SymbolTable();
        this.algorithm = algorithm;
        this.totalTime = 0;
        this.thrashingTime = 0;
        
        // Registrar todos los procesos en un mapa para acceso rápido por PID
        this.processMap = new HashMap<>();
        if (processes != null) {
            for (Process p : processes) {
                this.processMap.put(p.getPid(), p);
            }
        }
    }

    /**
     * Ejecuta una única instrucción en la MMU.
     * Este es el método principal que la simulación debe llamar en cada paso.
     * @param inst La instrucción a ejecutar (New, Use, Delete, Kill)
     * @return El tiempo que tomó ejecutar esta instrucción.
     */
    public long executeInstruction(Instruction inst) {
        long instructionTime = 0;
        if (inst == null) return 0;

        if (inst instanceof New) {
            instructionTime = executeNew((New) inst);
        } else if (inst instanceof Use) {
            instructionTime = executeUse((Use) inst);
        } else if (inst instanceof Delete) {
            instructionTime = executeDelete((Delete) inst);
        } else if (inst instanceof Kill) {
            instructionTime = executeKill((Kill) inst);
        }
        
        // Actualiza el tiempo total
        this.totalTime += instructionTime;
        return instructionTime;
    }
    
    // --- LÓGICA DE INSTRUCCIONES ---

    /**
     * Ejecuta una instrucción 'new(pid, size)'.
     */
private long executeNew(New inst) {
        long timeElapsed = 0;
        int pid = inst.getPid();
        int size = inst.getSize();
        Process currentProcess = processMap.get(pid);

        if (currentProcess == null || !currentProcess.isActive()) {
            System.err.println("Error: Proceso " + pid + " no existe o está inactivo. Ignorando 'new'.");
            return 0;
        }
        
        int pagesNeeded = inst.calcularPaginasNecesarias(PAGE_SIZE_BYTES);
        List<Page> newPages = new ArrayList<>();

        for (int i = 0; i < pagesNeeded; i++) {
            // Usamos el PID en el constructor de Page
            Page newPage = new Page(pid, -1, false, 0); 
            newPages.add(newPage);

            int freeFrameIndex = computer.findFreeFrameInRam();
            
            if (freeFrameIndex == -1) {
                // --- FALLO DE PÁGINA (RAM LLENA) ---
                timeElapsed += TIME_FAULT;
                this.thrashingTime += TIME_FAULT;
                
                Page victimPage = algorithm.selectPageToReplace(computer.getRam());
                if (victimPage == null) {
                    throw new RuntimeException("RAM está llena pero el algoritmo no seleccionó víctima.");
                }

                int victimFrameIndex = victimPage.getIndexOfPage();
                computer.sendPageToDisk(victimFrameIndex);
                algorithm.updateMetadata(victimPage, "evict");
                
                computer.placePageInMemory(newPage, victimFrameIndex);
                algorithm.updateMetadata(newPage, "load");

            } else {
                // --- HIT DE PÁGINA (ESPACIO DISPONIBLE) ---
                timeElapsed += TIME_HIT;
                computer.placePageInMemory(newPage, freeFrameIndex);
                algorithm.updateMetadata(newPage, "load");
            }

            // Asignar fragmentación interna (solo a la última página)
            if (i == pagesNeeded - 1) {
                int bytesInLastPage = size % PAGE_SIZE_BYTES;
                if (bytesInLastPage > 0 && bytesInLastPage != size) {
                    int fragmentation = PAGE_SIZE_BYTES - bytesInLastPage;
                    newPage.setFragmentationInBytes(fragmentation);
                } else if (size < PAGE_SIZE_BYTES) {
                     int fragmentation = PAGE_SIZE_BYTES - size;
                    newPage.setFragmentationInBytes(fragmentation);
                }
            }
        }
        
        // Registrar en ambas tablas de símbolos
        int newPtr = symbolTable.registerNewPointer(newPages);
        currentProcess.registerPointer(newPtr); // Rastrear en el proceso
        
        inst.setPtrAsignado(newPtr); // Para logging

        // --- INICIO DEL FIX: Registrar el mapeo de ptr a pageIds en OPT ---
        if (algorithm instanceof OPT) {
            // 1. Extraer solo los IDs de página
            List<Integer> pageIds = newPages.stream()
                                            .map(Page::getId)
                                            .collect(Collectors.toList());
            // 2. Registrar en la instancia del algoritmo OPT
            ((OPT) algorithm).registerPtrToPages(newPtr, pageIds);
        }
        // --- FIN DEL FIX ---

        return timeElapsed;
    }

    /**
     * Ejecuta una instrucción 'use(ptr)'.
     */
    private long executeUse(Use inst) {
        long timeElapsed = 0;
        int ptr = inst.getPtr();
        
        List<Page> pagesToUse = symbolTable.getPages(ptr);
        
        if (pagesToUse == null) {
             System.err.println("Error: Puntero " + ptr + " no existe (Segmentation Fault simulado).");
             return 0;
        }
        
        for (Page page : pagesToUse) {
            if (computer.isPageInRam(page)) {
                // --- HIT DE PÁGINA ---
                timeElapsed += TIME_HIT;
                algorithm.updateMetadata(page, "use");

            } else {
                // --- FALLO DE PÁGINA (PÁGINA EN DISCO) ---
                timeElapsed += TIME_FAULT;
                this.thrashingTime += TIME_FAULT;

                int freeFrameIndex = computer.findFreeFrameInRam();
                
                if (freeFrameIndex == -1) {
                    // RAM llena, seleccionar víctima
                    Page victimPage = algorithm.selectPageToReplace(computer.getRam());
                    int victimFrameIndex = victimPage.getIndexOfPage();
                    
                    computer.sendPageToDisk(victimFrameIndex);
                    algorithm.updateMetadata(victimPage, "evict");
                    freeFrameIndex = victimFrameIndex;
                }
                
                // "Swap-In" (Mover página del Disco a la RAM)
                computer.getDisk().remove(page); // Quitar de V-RAM
                computer.placePageInMemory(page, freeFrameIndex); // Poner en RAM
                algorithm.updateMetadata(page, "load"); 
            }
        }
        
        if (algorithm instanceof OPT) {
            ((OPT) algorithm).advanceInstructionIndex();
        }

        return timeElapsed;
    }

    /**
     * Ejecuta una instrucción 'delete(ptr)'.
     */
    private long executeDelete(Delete inst) {
        int ptr = inst.getPtr();
        Process currentProcess = processMap.get(inst.getPid());

        // 1. Quitar el puntero del mapa GLOBAL.
        List<Page> pagesToFree = symbolTable.removePointer(ptr);
        
        if (pagesToFree == null) {
             System.err.println("Error: Puntero " + ptr + " no existe, no se puede borrar.");
             return 0;
        }

        // 2. Quitar el puntero del mapa LOCAL del proceso
        if (currentProcess != null) {
            currentProcess.removePointer(ptr);
        }

        // 3. Liberar cada página
        for (Page page : pagesToFree) {
            if (page.isIsInVirtualMemory()) {
                computer.getDisk().remove(page);
            } else if (page.getIndexOfPage() >= 0) {
                computer.getRam().set(page.getIndexOfPage(), null); // Liberar el marco
            }
            // Notificar al algoritmo que la página fue "evictada" del sistema
            algorithm.updateMetadata(page, "evict");
        }
        
        return 0; // Operación lógica, sin costo de tiempo
    }
    
    /**
     * Ejecuta una instrucción 'kill(pid)'.
     */
    private long executeKill(Kill inst) {
        int pidToKill = inst.getPid();
        Process processToKill = processMap.get(pidToKill);
        
        if (processToKill == null) {
             System.err.println("Error: Proceso " + pidToKill + " no existe, no se puede 'matar'.");
             return 0;
        }
        
        // 1. Obtener todos los punteros que le pertenecen a este proceso
        // ¡Aquí usamos el nuevo método getOwnedPointers()!
        List<Integer> pointersToKill = processToKill.getOwnedPointers();

        // 2. Llamar a la lógica de 'delete' para cada puntero
        long timeElapsed = 0;
        for (int ptr : pointersToKill) {
            // Creamos una instrucción 'Delete' falsa para reutilizar la lógica
            timeElapsed += executeDelete(new Delete(pidToKill, ptr));
        }
        
        // 3. Marcar el proceso como terminado
        processToKill.terminate();
        
        return timeElapsed; // Costo es la suma de todas las eliminaciones lógicas (0)
    }
    
    
    // --- MÉTODOS PARA LA VISUALIZACIÓN ---
    
    public Computer getComputer() {
        return computer;
    }
    
    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getThrashingTime() {
        return thrashingTime;
    }
    
    public PageReplacementAlgorithm getAlgorithm() {
        return algorithm;
    }
    
    public Map<Integer, Process> getProcessMap() {
        return processMap;
    }
    
    /**
     * Resetea la MMU para una nueva simulación.
     */
    public void reset() {
        computer.reset();
        symbolTable.clear();
        algorithm.reset();
        
        // Resetear todos los procesos
        for (Process p : processMap.values()) {
            p.reset();
        }
        
        totalTime = 0;
        thrashingTime = 0;
    }
}