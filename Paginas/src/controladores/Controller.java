package controladores;

import modelos.Instruction;
import modelos.Process;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Controlador Central (MVC).
 * CORREGIDO: Sin Timer interno y con lógica de 'step' a prueba de nulos.
 */
public class Controller {

    // --- Componentes ---
    private InstructionFileHandler fileHandler;
    private MMU mmuOpt;
    private MMU mmuUser;
    
    // --- Referencia a la Vista ---
    // private VistaPrincipal view; 
    
    // --- Estado de Simulación ---
    private List<Process> processes;
    private List<Instruction> loadedInstructionSequence; // Para modo "Cargar Archivo"
    private int loadedInstructionIndex;
    
    private boolean isPaused;
    private boolean isFileMode; // true si cargamos desde archivo
    private boolean simulationEnded; // true si no hay más instrucciones
    private Random simulationRandom; // Random para elegir procesos en modo "Generar"
    
    // NO HAY TIMER AQUÍ. La VentanaSimulacion lo manejará.

    public Controller() {
        this.fileHandler = new InstructionFileHandler();
        this.isPaused = true;
        this.simulationEnded = false;
        this.loadedInstructionIndex = 0;
    }
    
    // (Este método será llamado por la VISTA)
    // public void setView(VistaPrincipal view) {
    //     this.view = view;
    // }

    /**
     * Llamado por la VISTA cuando el usuario presiona "Iniciar Simulación".
     */
    public void setupSimulation(PageReplacementAlgorithm algorithm, long seed, String filePath, int P, int N) {
        
        this.simulationRandom = new Random(seed);
        InstructionFileHandler.SimulationData data;
        
        try {
            if (filePath != null && !filePath.isEmpty()) {
                // --- MODO CARGAR DESDE ARCHIVO ---
                this.isFileMode = true;
                data = fileHandler.loadInstructionsFromFile(filePath);
                this.loadedInstructionSequence = data.instructions;
                this.processes = data.processes;

            } else {
                // --- MODO GENERAR NUEVO ---
                this.isFileMode = false;
                data = fileHandler.generateProcesses(P, N, seed);
                this.processes = data.processes;
                // loadedInstructionSequence permanece null, lo cual es correcto
            }
        } catch (Exception e) {
            System.err.println("Error al preparar la simulación: " + e.getMessage());
            // (Informar a la GUI sobre el error)
            return;
        }

        // --- Configuración de MMUs ---
        OPT optAlgorithm = new OPT();
        if (isFileMode) {
            // OPT necesita la lista pre-cargada para predecir el futuro
            optAlgorithm.setInstructionSequence(this.loadedInstructionSequence); 
        }
        
        if (algorithm instanceof RND) {
            ((RND) algorithm).setSeed(seed);
        }

        this.mmuOpt = new MMU(optAlgorithm, this.processes);
        this.mmuUser = new MMU(algorithm, this.processes);
        
        this.simulationEnded = false;
    }
    
    /**
     * Ejecuta un solo paso (una instrucción) en ambas simulaciones.
     * Llamado por el Timer de la VISTA.
     */
    public void stepSimulation() {
        if (isPaused || simulationEnded) {
            return; // No hacer nada
        }

        Instruction inst = null;

        if (isFileMode) {
            // --- MODO ARCHIVO: Leer la siguiente instrucción de la lista ---
            if (this.loadedInstructionSequence == null) { // Guarda de seguridad
                 simulationEnded = true;
                 return;
            }
            if (loadedInstructionIndex < loadedInstructionSequence.size()) {
                inst = loadedInstructionSequence.get(loadedInstructionIndex);
                loadedInstructionIndex++;
            } else {
                simulationEnded = true; // Se acabaron las instrucciones
            }
        } else {
            // --- MODO GENERADO: Elegir un proceso activo y tomar su instrucción ---
            if (this.processes == null) { // Guarda de seguridad
                simulationEnded = true;
                return;
            }
            
            List<Process> activeProcesses = processes.stream()
                                                     .filter(p -> p.isActive() && !p.isFinished())
                                                     .collect(Collectors.toList());
            
            if (activeProcesses.isEmpty()) {
                simulationEnded = true; // Se acabaron las instrucciones
            } else {
                Process chosenProcess = activeProcesses.get(simulationRandom.nextInt(activeProcesses.size()));
                inst = chosenProcess.getNextInstruction();
            }
        }

        // Si no hay más instrucciones, pausar y salir
        if (inst == null) {
            simulationEnded = true;
            pauseSimulation(); // Pausa la simulación (isPaused = true)
            return;
        }
        
        // Ejecutar la misma instrucción en ambas MMUs
        if (mmuOpt != null) mmuOpt.executeInstruction(inst);
        if (mmuUser != null) mmuUser.executeInstruction(inst);
    }
    
    /**
     * Inicia o reanuda el bucle de simulación.
     * Llamado por el botón "Play/Pause" de la VISTA.
     */
    public void resumeSimulation() {
        this.isPaused = false;
        System.out.println("Simulación Reanudada.");
    }
    
    /**
     * Pausa el bucle de simulación.
     * Llamado por el botón "Play/Pause" de la VISTA.
     */
    public void pauseSimulation() {
        this.isPaused = true;
        System.out.println("Simulación Pausada.");
    }
    
    public void saveGeneratedInstructions(String savePath) {
        // (lógica de guardado)
    }

    public MMU getMmuOpt() { return mmuOpt; }
    public MMU getMmuUser() { return mmuUser; }
    public boolean isPaused() { return isPaused; }
    public boolean isSimulationEnded() { return simulationEnded; }
}