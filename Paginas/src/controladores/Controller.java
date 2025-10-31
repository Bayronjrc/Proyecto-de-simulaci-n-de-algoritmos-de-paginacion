/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controladores;

import modelos.Instruction;
import modelos.Process;
import java.util.List;

/**
 *
 * @author wess
 *
 */
public class Controller {

    // --- Componentes ---
    private InstructionFileHandler fileHandler;
    private MMU mmuOpt;
    private MMU mmuUser;
    
    // --- Estado de Simulación ---
    private List<Process> processes;
    private List<Instruction> fullInstructionSequence;
    private int currentInstructionIndex;
    private boolean isPaused;
    
    // Necesario para la interfaz
    private javax.swing.Timer uiRefreshTimer;

    public Controller() {
        this.fileHandler = new InstructionFileHandler();
        this.currentInstructionIndex = 0;
        this.isPaused = true;
        
        // Configurar el Timer de la GUI.
        // Se disparará cada 200ms (5 veces por segundo) para refrescar la GUI.
        // El 'action listener' llama a stepSimulation()
        this.uiRefreshTimer = new javax.swing.Timer(200, e -> stepSimulation());
    }

    /**
     * @param algorithm El algoritmo que el usuario seleccionó (FIFO, MRU, etc.)
     * @param seed La semilla para la generación (o para RND)
     * @param filePath (Opcional) Ruta al archivo a cargar
     * @param P (Opcional) Número de procesos a generar
     * @param N (Opcional) Número de operaciones a generar
     */
    public void setupSimulation(PageReplacementAlgorithm algorithm, long seed, String filePath, int P, int N) {
        
        InstructionFileHandler.SimulationData data;
        
        // 1. Cargar o Generar Instrucciones
        try {
            if (filePath != null && !filePath.isEmpty()) {
                data = fileHandler.loadInstructionsFromFile(filePath);
            } else {
                data = fileHandler.generateProcesses(P, N, seed);
            }
            this.fullInstructionSequence = data.instructions;
            this.processes = data.processes;
        } catch (Exception e) {
            // (Informar a la GUI sobre el error)
            System.err.println("Error al preparar la simulación: " + e.getMessage());
            return;
        }

        // 2. Preparar Algoritmo OPT
        OPT optAlgorithm = new OPT(this.fullInstructionSequence);
        // (El parser ya no necesita pasar el mapa, la MMU de OPT lo construirá)
        
        // 3. Preparar Algoritmo de Usuario
        if (algorithm instanceof RND) {
            ((RND) algorithm).setSeed(seed); // Asegurar repetibilidad
        }

        // 4. Crear las dos MMUs
        this.mmuOpt = new MMU(optAlgorithm, this.processes);
        this.mmuUser = new MMU(algorithm, this.processes);
        
        // 5. Reiniciar el estado
        this.currentInstructionIndex = 0;
        
    }
    
    /**
     * Ejecuta un solo paso (una instrucción) en ambas simulaciones.
     * Llamado por el Timer de la GUI.
     */
    public void stepSimulation() {
        if (isPaused) {
            return; // No hacer nada si está en pausa
        }

        if (currentInstructionIndex >= fullInstructionSequence.size()) {
            pauseSimulation(); // Pausar al acabar
            // (Informar a la GUI: Simulación terminada)
            // view.showFinalStats();
            return;
        }

        Instruction inst = fullInstructionSequence.get(currentInstructionIndex);
        
        // Ejecutar la misma instrucción en ambas MMUs
        mmuOpt.executeInstruction(inst);
        mmuUser.executeInstruction(inst);
        
        currentInstructionIndex++;
        
        // (Actualizar la GUI con los nuevos estados)
        // view.updateStatistics(mmuOpt, mmuUser);
    }
    
    /**
     * Inicia o reanuda el bucle de simulación.
     * Llamado por el botón "Play" de la GUI.
     */
    public void resumeSimulation() {
        this.isPaused = false;
        this.uiRefreshTimer.start();
        System.out.println("Simulación Reanudada.");
    }
    
    /**
     * Pausa el bucle de simulación.
     * Llamado por el botón "Pause" de la GUI.
     */
    public void pauseSimulation() {
        this.isPaused = true;
        this.uiRefreshTimer.stop();
        System.out.println("Simulación Pausada.");
    }
    
    /**
     * Permite a la GUI descargar el archivo de operaciones generado.
     * @param savePath La ruta donde el usuario eligió guardar.
     */
    public void saveGeneratedInstructions(String savePath) {
        if (this.fullInstructionSequence == null || this.fullInstructionSequence.isEmpty()) {
            // (Informar a la GUI: No hay nada que guardar)
            return;
        }
        try {
            fileHandler.saveInstructionsToFile(this.fullInstructionSequence, savePath);
        } catch (Exception e) {
            // (Informar a la GUI sobre el error)
        }
    }

    // (Getters para que la GUI consulte las MMUs)
    public MMU getMmuOpt() { return mmuOpt; }
    public MMU getMmuUser() { return mmuUser; }
    public boolean isPaused() { return isPaused; }
}