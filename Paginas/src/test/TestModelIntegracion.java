/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import modelos.Computer;
import modelos.New;
import modelos.Page;
import modelos.SymbolTable;
import java.util.ArrayList;
import java.util.List;

//Code done by gemini to test my methods, have to addmit that honestly.


/**
 * Prueba la integración de las clases principales del Modelo:
 * 1. Computer.java (Hardware, RAM, Disco)
 * 2. SymbolTable.java (Mapa de Memoria Global de la MMU)
 * 3. Page.java (Unidad de memoria)
 *
 * Simula las operaciones lógicas de la MMU para verificar que el estado
 * del modelo se actualiza correctamente.
 */
public class TestModelIntegracion {

    // Instancias compartidas para las pruebas secuenciales
    private static Computer computer;
    private static SymbolTable symTable;

    // Constantes del proyecto
    private static final int PAGE_SIZE_BYTES = 4096;
    private static final int RAM_SIZE_PAGES = 100;

    public static void main(String[] args) {
        System.out.println("=== INICIO DE PRUEBAS DE INTEGRACIÓN DEL MODELO ===");

        // Instanciar los componentes principales
        computer = new Computer(); // Usa 400KB RAM, 4KB Pagina por defecto
        symTable = new SymbolTable();

        // Ejecutar pruebas en orden
        runTest("Test 01: Estado Inicial", TestModelIntegracion::test01_InitialState);
        runTest("Test 02: Asignación y Fragmentación", TestModelIntegracion::test02_Allocation);
        runTest("Test 03: Llenado de RAM y Paginación (Swap)", TestModelIntegracion::test03_RamFullAndPaging);
        runTest("Test 04: Desasignación (Delete)", TestModelIntegracion::test04_Deletion);
        runTest("Test 05: Reseteo del Sistema", TestModelIntegracion::test05_Reset);

        System.out.println("\n=== FIN DE PRUEBAS ===");
    }

    /**
     * Prueba el estado inicial por defecto del Computador y la SymbolTable.
     */
    private static boolean test01_InitialState() {
        assertEq(computer.getRam().size(), RAM_SIZE_PAGES, "RAM debe tener 100 marcos");
        assertEq(computer.getRealMemoryUsed(), 0, "RAM usada debe ser 0 KB");
        assertEq(computer.getVirtualMemoryUsed(), 0, "V-RAM usada debe ser 0 KB");
        assertEq(computer.isRamFull(), false, "RAM no debe estar llena");
        assertEq(computer.findFreeFrameInRam(), 0, "Primer marco libre debe ser el 0");
        assertEq(symTable.getAllActivePointers().size(), 0, "Tabla de Símbolos debe estar vacía");
        return true;
    }

    /**
     * Simula dos operaciones 'new' para probar la asignación de páginas,
     * el registro de punteros y el cálculo de fragmentación.
     */
    private static boolean test02_Allocation() {
        // --- Escenario 1: new(1, 8192) -> 2 páginas exactas
        New inst1 = new New(1, 8192); //
        int pagesNeeded1 = inst1.calcularPaginasNecesarias(PAGE_SIZE_BYTES);
        assertEq(pagesNeeded1, 2, "Cálculo de 8192B debe ser 2 páginas");

        List<Page> list1 = new ArrayList<>();
        Page p1 = new Page(1, -1, false, 0); //
        Page p2 = new Page(1, -1, false, 0);
        list1.add(p1);
        list1.add(p2);

        // Prueba de SymbolTable
        int ptr1 = symTable.registerNewPointer(list1);
        assertEq(ptr1, 1, "Primer ptr debe ser 1");
        assertEq(symTable.pointerExists(1), true, "ptr 1 debe existir");
        assertEq(symTable.getPages(1).size(), 2, "ptr 1 debe tener 2 páginas");

        // Prueba de Computer (colocación en RAM)
        computer.placePageInMemory(p1, 0);
        computer.placePageInMemory(p2, 1);
        assertEq(computer.getRealMemoryUsed(), 8, "RAM usada debe ser 8 KB (2 pag * 4KB)");
        assertEq(p1.getIndexOfPage(), 0, "Página p1 debe estar en marco 0");
        assertEq(p2.getIndexOfPage(), 1, "Página p2 debe estar en marco 1");
        assertEq(computer.isPageInRam(p1), true, "p1 debe estar en RAM");
        assertEq(computer.getTotalRamFragmentation(), 0, "Fragmentación debe ser 0");

        // --- Escenario 2: new(2, 5000) -> 2 páginas, con fragmentación
        New inst2 = new New(2, 5000);
        int pagesNeeded2 = inst2.calcularPaginasNecesarias(PAGE_SIZE_BYTES);
        assertEq(pagesNeeded2, 2, "Cálculo de 5000B debe ser 2 páginas");

        List<Page> list2 = new ArrayList<>();
        Page p3 = new Page(2, -1, false, 0);
        Page p4 = new Page(2, -1, false, 0);

        // Calcular fragmentación
        int bytesUsadosEnUltimaPag = 5000 - PAGE_SIZE_BYTES; // 5000 - 4096 = 904
        int fragmentacion = PAGE_SIZE_BYTES - bytesUsadosEnUltimaPag; // 4096 - 904 = 3192
        p4.setFragmentationInBytes(fragmentacion);

        list2.add(p3);
        list2.add(p4);

        int ptr2 = symTable.registerNewPointer(list2);
        assertEq(ptr2, 2, "Segundo ptr debe ser 2");
        assertEq(symTable.getPages(2).get(1), p4, "ptr 2 debe contener p4");

        computer.placePageInMemory(p3, 2); // Coloca en marco 2
        computer.placePageInMemory(p4, 3); // Coloca en marco 3
        assertEq(computer.getRealMemoryUsed(), 16, "RAM usada debe ser 16 KB (4 pag * 4KB)");
        assertEq(computer.getTotalRamFragmentation(), 3192, "Fragmentación total debe ser 3192B");
        assertEq(computer.findFreeFrameInRam(), 4, "Siguiente marco libre debe ser 4");

        return true;
    }

    /**
     * Llena el resto de la RAM y luego fuerza una paginación (swap-out).
     */
    private static boolean test03_RamFullAndPaging() {
        // Ya tenemos 4 páginas en RAM. Llenamos las 96 restantes.
        List<Page> dummyPages = new ArrayList<>(); // Para rastrearlos
        for (int i = 0; i < (RAM_SIZE_PAGES - 4); i++) {
            Page p = new Page(99, i, false, 0); // ID de proceso 99
            dummyPages.add(p);
            List<Page> tempList = new ArrayList<>();
            tempList.add(p);

            symTable.registerNewPointer(tempList); // Ptrs 3 al 98
            int freeFrame = computer.findFreeFrameInRam();
            computer.placePageInMemory(p, freeFrame);
        }

        // Verificar que la RAM está llena
        assertEq(computer.getRealMemoryUsed(), 400, "RAM usada debe ser 400 KB");
        assertEq(computer.isRamFull(), true, "RAM debe estar llena");
        assertEq(computer.findFreeFrameInRam(), -1, "No debe haber marcos libres");

        // --- Forzar Paginación (Swap-Out) ---
        // La MMU decide sacar la página del marco 0 (p1)
        Page victim = computer.sendPageToDisk(0);

        assertEq(victim.getId(), 1, "La víctima debe ser p1");
        assertEq(victim.isIsInVirtualMemory(), true, "p1 debe estar marcada como V-RAM");
        assertEq(computer.getDisk().size(), 1, "Disco debe tener 1 página");
        assertEq(computer.getDisk().get(0), victim, "p1 debe estar en Disco");
        assertEq(computer.getRam().get(0), null, "Marco 0 debe estar libre (null)");
        assertEq(computer.isRamFull(), false, "RAM ya no debe estar llena");
        assertEq(computer.findFreeFrameInRam(), 0, "Marco 0 debe ser el primero libre");
        assertEq(computer.getVirtualMemoryUsed(), 4, "V-RAM usada debe ser 4 KB");

        // La MMU ahora carga una nueva página (p_new) en el espacio libre
        Page p_new = new Page(100, -1, false, 0);
        computer.placePageInMemory(p_new, 0);
        assertEq(computer.getRam().get(0), p_new, "p_new debe estar en marco 0");
        assertEq(computer.isRamFull(), true, "RAM debe estar llena de nuevo");
        assertEq(computer.getRealMemoryUsed(), 400, "RAM usada debe ser 400 KB");

        return true;
    }

    /**
     * Simula una operación 'delete' sobre un puntero.
     */
    private static boolean test04_Deletion() {
        // Vamos a borrar ptr 2 (páginas p3 y p4, en marcos 2 y 3).
        // p4 tiene la fragmentación de 3192B.
        assertEq(symTable.pointerExists(2), true, "ptr 2 debe existir antes de borrar");
        assertEq(computer.getTotalRamFragmentation(), 3192, "Fragmentación debe ser 3192B");

        // 1. MMU elimina el puntero de la tabla
        List<Page> pagesToRemove = symTable.removePointer(2);
        assertEq(symTable.pointerExists(2), false, "ptr 2 no debe existir después de borrar");
        assertEq(pagesToRemove.size(), 2, "Debe devolver las 2 páginas (p3, p4)");

        // 2. MMU libera los marcos de RAM asociados
        // (En una simulación real, la MMU iteraría sobre pagesToRemove)
        assertEq(computer.getRam().get(2).getId(), 2, "Marco 2 debe tener p3");
        assertEq(computer.getRam().get(3).getId(), 2, "Marco 3 debe tener p4");
        computer.getRam().set(2, null);
        computer.getRam().set(3, null);

        // 3. Verificar estado
        assertEq(computer.isRamFull(), false, "RAM no debe estar llena post-delete");
        assertEq(computer.findFreeFrameInRam(), 2, "Marco 2 debe ser el primero libre");
        // RAM tenía 100 páginas, quitamos 2. Quedan 98. 98 * 4KB = 392KB
        assertEq(computer.getRealMemoryUsed(), 392, "RAM usada debe ser 392 KB");
        
        // La página (p4) con la fragmentación fue eliminada
        assertEq(computer.getTotalRamFragmentation(), 0, "Fragmentación debe ser 0 post-delete");

        return true;
    }

    /**
     * Prueba los métodos de reseteo.
     */
    private static boolean test05_Reset() {
        // Ejecutar reseteo
        computer.reset();
        symTable.clear();

        // Re-ejecutar las mismas pruebas del Test 01
        System.out.println("  > Re-probando estado inicial post-reset...");
        return test01_InitialState();
    }

    // --- Métodos Ayudantes para Pruebas ---

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
     * Afirmación de igualdad simple.
     */
private static <T> void assertEq(T actual, T expected, String message) {
        // Si ambos son null, son iguales.
        if (actual == null && expected == null) {
            return;
        }
        
        // Si solo uno es null, son diferentes.
        if (actual == null || expected == null) {
             throw new AssertionError(
                String.format("%s. [Esperado: %s, Recibido: %s]", message, expected, actual)
            );
        }
        
        // Si ninguno es null, usar .equals()
        if (!actual.equals(expected)) {
            throw new AssertionError(
                String.format("%s. [Esperado: %s, Recibido: %s]", message, expected, actual)
            );
        }
    }
    
    
    
}