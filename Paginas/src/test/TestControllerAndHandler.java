package test;

import controladores.*; // Importa Controller, MMU, InstructionFileHandler, etc.
import modelos.*; // Importa Page, New, Use, etc.
import modelos.Process; // <-- Corrección 1: Resuelve la ambigüedad

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Prueba extensiva para el Controller y el InstructionFileHandler.
 * 1. Prueba la generación de instrucciones y procesos (generateProcesses).
 * 2. Prueba la carga de archivos y el "parser inteligente" (loadInstructionsFromFile).
 * 3. Prueba la configuración de la simulación dual en el Controller (setupSimulation).
 * 4. Prueba una simulación completa de principio a fin (stepSimulation) y verifica
 * los tiempos finales.
 */
public class TestControllerAndHandler {

    // (Contenido del archivo de prueba para el "parser inteligente")
    private static final String TEST_FILE_CONTENT = 
            "new(1, 4096)\n" +  // P1, 1 pág. Ordinal 1 -> ptr 1.
            "new(1, 8000)\n" +  // P1, 2 págs. Ordinal 2 -> ptr 2.
            "new(2, 4096)\n" +  // P2, 1 pág. Ordinal 3 -> ptr 3.
            "use(1)\n" +        // P1 usa ptr 1.
            "use(3)\n" +        // P2 usa ptr 3.
            "use(2)\n" +        // P1 usa ptr 2 (2 págs).
            "delete(1)\n" +     // P1 borra ptr 1.
            "kill(1)\n" +       // P1 termina (borra ptr 2).
            "kill(2)\n";        // P2 termina (borra ptr 3).
    
    private static final String TEST_FILE_NAME = "test_parser_input.txt";

    public static void main(String[] args) {
        System.out.println("=== INICIO DE PRUEBAS DE CONTROLADOR Y MANEJADOR DE ARCHIVOS ===");

        runTest("Test 01: Generación de Instrucciones (File Handler)", 
                TestControllerAndHandler::test01_Generation);
        
        runTest("Test 02: Carga de Archivo (Parser Inteligente)", 
                TestControllerAndHandler::test02_FileLoadingParser);
        
        runTest("Test 03: Configuración del Controlador (Controller Setup)", 
                TestControllerAndHandler::test03_ControllerSetup);
        
        runTest("Test 04: Simulación Completa (End-to-End) y Estadísticas", 
                TestControllerAndHandler::test04_FullSimulationRun);

        System.out.println("\n=== FIN DE PRUEBAS ===");
    }

    /**
     * Prueba que InstructionFileHandler.generateProcesses funciona.
     */
    private static boolean test01_Generation() {
        InstructionFileHandler handler = new InstructionFileHandler();
        int P = 10;
        int N = 500;
        long SEED = 12345L;

        // Generar una simulación
        InstructionFileHandler.SimulationData data1 = handler.generateProcesses(P, N, SEED);

        assertEq(data1.processes.size(), P, "Debe generar P procesos");
        assertEq(data1.instructions.size(), N, "Debe generar N instrucciones totales");

        // Verificar la regla de 'kill'
        for (Process p : data1.processes) {
            List<Instruction> pInsts = p.getInstructionList();
            // <-- Corrección 2: Sintaxis de Assert
            assert pInsts.get(pInsts.size() - 1) instanceof Kill : 
                    "La última instrucción del proceso " + p.getPid() + " debe ser Kill";
        }

        // Verificar repetibilidad
        InstructionFileHandler.SimulationData data2 = handler.generateProcesses(P, N, SEED);
        assertEq(data1.instructions.get(0).toFileFormat(), 
                 data2.instructions.get(0).toFileFormat(), 
                 "La generación debe ser repetible con la misma semilla");
        
        return true;
    }

    /**
     * Prueba el "parser inteligente" de InstructionFileHandler.loadInstructionsFromFile.
     * Verifica que los 'use(ordinal)' del archivo se mapean a 'Use(pid, ptrId)'.
     */
    private static boolean test02_FileLoadingParser() throws IOException {
        createTestFile(TEST_FILE_NAME, TEST_FILE_CONTENT);
        
        InstructionFileHandler handler = new InstructionFileHandler();
        InstructionFileHandler.SimulationData data = handler.loadInstructionsFromFile(TEST_FILE_NAME);
        
        List<Instruction> insts = data.instructions;
        List<Process> procs = data.processes; // Esta línea ahora compilará

        assertEq(insts.size(), 9, "Debe cargar 9 instrucciones");
        assertEq(procs.size(), 2, "Debe detectar 2 procesos");
        
        // --- Verificar el "Parseo Inteligente" (con sintaxis de assert corregida) ---
        assert insts.get(0) instanceof New : "Inst 0 debe ser New";
        assertEq(insts.get(0).getPid(), 1, "Inst 0 PID debe ser 1");
        
        assert insts.get(2) instanceof New : "Inst 2 debe ser New";
        assertEq(insts.get(2).getPid(), 2, "Inst 2 PID debe ser 2");
        
        assert insts.get(3) instanceof Use : "Inst 3 debe ser Use";
        assertEq(insts.get(3).getPid(), 1, "Inst 3 (use(1)) PID debe ser 1");
        assertEq(((Use)insts.get(3)).getPtr(), 1, "Inst 3 (use(1)) PtrID debe ser 1");
        
        assert insts.get(4) instanceof Use : "Inst 4 debe ser Use";
        assertEq(insts.get(4).getPid(), 2, "Inst 4 (use(3)) PID debe ser 2");
        assertEq(((Use)insts.get(4)).getPtr(), 3, "Inst 4 (use(3)) PtrID debe ser 3");
        
        assert insts.get(5) instanceof Use : "Inst 5 debe ser Use";
        assertEq(insts.get(5).getPid(), 1, "Inst 5 (use(2)) PID debe ser 1");
        assertEq(((Use)insts.get(5)).getPtr(), 2, "Inst 5 (use(2)) PtrID debe ser 2");

        assert insts.get(6) instanceof Delete : "Inst 6 debe ser Delete";
        assertEq(insts.get(6).getPid(), 1, "Inst 6 (delete(1)) PID debe ser 1");
        assertEq(((Delete)insts.get(6)).getPtr(), 1, "Inst 6 (delete(1)) PtrID debe ser 1");

        assert insts.get(7) instanceof Kill : "Inst 7 debe ser Kill";
        assertEq(insts.get(7).getPid(), 1, "Inst 7 (kill(1)) PID debe ser 1");

        new File(TEST_FILE_NAME).delete(); // Limpiar
        return true;
    }
    
    /**
     * Prueba que el Controller.setupSimulation inicializa ambas MMUs correctamente.
     */
    private static boolean test03_ControllerSetup() throws IOException {
        createTestFile(TEST_FILE_NAME, TEST_FILE_CONTENT);
        
        Controller controller = new Controller();
        long SEED = 999L;
        
        // 1. Probar con FIFO
        PageReplacementAlgorithm fifo = new FIFO();
        controller.setupSimulation(fifo, SEED, TEST_FILE_NAME, 0, 0);

        assert controller.getMmuOpt() != null : "MMU Opt no debe ser nula";
        assert controller.getMmuUser() != null : "MMU User no debe ser nula";
        assert controller.getMmuOpt().getAlgorithm() instanceof OPT : "MMU Opt debe usar OPT";
        assert controller.getMmuUser().getAlgorithm() instanceof FIFO : "MMU User debe usar FIFO";
        assertEq(controller.getMmuUser().getProcessMap().size(), 2, "MMU User debe tener 2 procesos");
        
        // 2. Probar que la semilla se pasa a RND
        PageReplacementAlgorithm rnd = new RND();
        controller.setupSimulation(rnd, SEED, TEST_FILE_NAME, 0, 0);
        
        assert controller.getMmuUser().getAlgorithm() instanceof RND : "MMU User debe usar RND";
        // (Sería ideal añadir un getSeed() a RND para verificar controller.getMmuUser().getAlgorithm().getSeed() == SEED)

        new File(TEST_FILE_NAME).delete(); // Limpiar
        return true;
    }
    
    /**
     * Prueba una simulación completa de principio a fin y verifica las estadísticas finales.
     * Esto prueba que el Controller.stepSimulation() funciona.
     */
    private static boolean test04_FullSimulationRun() throws IOException {
        createTestFile(TEST_FILE_NAME, TEST_FILE_CONTENT);
        
        Controller controller = new Controller();
        PageReplacementAlgorithm mru = new MRU();
        long SEED = 1L;
        
        // Configurar simulación con MRU
        controller.setupSimulation(mru, SEED, TEST_FILE_NAME, 0, 0);
        
        // Simular el bucle de la GUI llamando a stepSimulation() manualmente
        controller.resumeSimulation(); // Pone isPaused = false
        
        for (int i = 0; i < 100; i++) { // 100 pasos (más que las 9 instrucciones)
            controller.stepSimulation();
            if (controller.isPaused()) {
                break; // Simulación terminó y se pausó sola
            }
        }

        assert controller.isPaused() : "La simulación debe pausarse automáticamente al terminar";
        
        // --- Verificar Estadísticas Finales ---
        // La RAM es de 100 marcos.
        // La secuencia de prueba solo carga 4 páginas en total (1+2+1).
        // NUNCA debe haber un fallo de página (Thrashing).
        
        MMU mmuOpt = controller.getMmuOpt();
        MMU mmuUser = controller.getMmuUser();

        // Cálculo del tiempo esperado:
        // new(1, 4096) -> 1 pág. Hit.  +1s
        // new(1, 8000) -> 2 págs. Hit.  +2s (1s * 2)
        // new(2, 4096) -> 1 pág. Hit.  +1s
        // use(1)       -> 1 pág. Hit.  +1s
        // use(3)       -> 1 pág. Hit.  +1s
        // use(2)       -> 2 págs. Hit.  +2s (1s * 2)
        // delete(1)    -> Lógico.      +0s
        // kill(1)      -> Lógico.      +0s
        // kill(2)      -> Lógico.      +0s
        // Total Time = 1 + 2 + 1 + 1 + 1 + 2 = 8 segundos.
        // Thrashing Time = 0 segundos.
        
        long TIEMPO_ESPERADO = 8L;
        long THRASHING_ESPERADO = 0L;

        assertEq(mmuOpt.getTotalTime(), TIEMPO_ESPERADO, 
                 "OPT: El tiempo total debe ser 8s (puros hits)");
        assertEq(mmuOpt.getThrashingTime(), THRASHING_ESPERADO, 
                 "OPT: El tiempo de thrashing debe ser 0s");
        
        assertEq(mmuUser.getTotalTime(), TIEMPO_ESPERADO, 
                 "MRU: El tiempo total debe ser 8s (puros hits)");
        assertEq(mmuUser.getThrashingTime(), THRASHING_ESPERADO, 
                 "MRU: El tiempo de thrashing debe ser 0s");

        new File(TEST_FILE_NAME).delete(); // Limpiar
        return true;
    }
    

    // --- Métodos Ayudantes para Pruebas (copiados de TestModelIntegracion) ---

    private static void createTestFile(String name, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(name))) {
            writer.write(content);
        }
    }

    /**
     * Interfaz funcional simple para un método de prueba.
     */
    @FunctionalInterface
    interface TestRunner {
        boolean run() throws Exception;
    }

    /**
     * Contenedor para ejecutar una prueba y reportar su resultado.
     */
    private static void runTest(String testName, TestRunner test) {
        System.out.println("\n--- " + testName + " ---");
        try {
            if (test.run()) {
                System.out.println("[PASS] " + testName);
            }
        } catch (AssertionError e) {
            System.err.println("[FAIL] " + testName + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] " + testName + ": Ocurrió una excepción inesperada.");
            e.printStackTrace(System.err);

            // Imprimir la causa raíz si existe, que suele ser la 'AssertionError'
            Throwable cause = e.getCause();
            if (cause != null) {
                System.err.println("Causa raíz: " + cause.getMessage());
            }
        }
    }

    /**
     * Afirmación de igualdad simple.
     */
    private static <T> void assertEq(T actual, T expected, String message) {
        if (actual == null && expected == null) {
            return;
        }
        if (actual == null || !actual.equals(expected)) {
            throw new AssertionError(
                String.format("%s. [Esperado: %s, Recibido: %s]", message, expected, actual)
            );
        }
    }
}