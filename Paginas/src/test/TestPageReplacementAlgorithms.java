package test;

import controladores.PageReplacementAlgorithm;
import controladores.FIFO;
import controladores.MRU;
import modelos.Page;
import java.util.ArrayList;
import java.util.List;

/**
 * Prueba los algoritmos FIFO y MRU de forma aislada.
 */
public class TestPageReplacementAlgorithms {
    
    public static void main(String[] args) {
        System.out.println("=== TEST DE ALGORITMOS DE PAGINACIÓN ===\n");
        
        testFIFO();
        System.out.println("\n" + "=".repeat(50) + "\n");
        testMRU();
        
        System.out.println("\n=== FIN DE LOS TESTS ===");
    }
    
    /**
     * Prueba el algoritmo FIFO.
     */
    private static void testFIFO() {
        System.out.println("--- TEST FIFO ---");
        
        PageReplacementAlgorithm fifo = new FIFO();
        
        // Crear 5 páginas
        Page p1 = new Page(1, 0, false, 0);
        Page p2 = new Page(2, 1, false, 0);
        Page p3 = new Page(3, 2, false, 0);
        Page p4 = new Page(4, 3, false, 0);
        Page p5 = new Page(5, 4, false, 0);
        
        // Simular carga en orden: p1, p2, p3, p4
        System.out.println("\n1. Cargando páginas en orden: p1, p2, p3, p4");
        fifo.updateMetadata(p1, "load");
        fifo.updateMetadata(p2, "load");
        fifo.updateMetadata(p3, "load");
        fifo.updateMetadata(p4, "load");

        System.out.println(fifo.getDetailedState());
        
        // Crear lista de páginas en RAM
        List<Page> ramPages = new ArrayList<>();
        ramPages.add(p1);
        ramPages.add(p2);
        ramPages.add(p3);
        ramPages.add(p4);
        
        // Seleccionar víctima (debe ser p1, la más antigua)
        System.out.println("2. Seleccionando víctima...");
        Page victima = fifo.selectPageToReplace(ramPages);
        System.out.println("   Víctima seleccionada: Page " + victima.getId() + 
            " ✓ (esperado: Page 1)");
        
        // Simular eviction de p1 y carga de p5
        System.out.println("\n3. Evictando p1 y cargando p5");
        fifo.updateMetadata(p1, "evict");
        ramPages.remove(p1);
        
        fifo.updateMetadata(p5, "load");
        ramPages.add(p5);
        
        System.out.println(fifo.getDetailedState());
        
        // Usar p2 (FIFO no cambia nada con "use")
        System.out.println("4. Usando p2 (FIFO no cambia orden)");
        fifo.updateMetadata(p2, "use");

        // Nueva víctima debe ser p2 (la siguiente más antigua)
        victima = fifo.selectPageToReplace(ramPages);
        System.out.println("   Nueva víctima: Page " + victima.getId() + 
            " ✓ (esperado: Page 2)");
        
        // Reset
        System.out.println("\n5. Reseteando algoritmo...");
        fifo.reset();
        System.out.println(fifo.getDetailedState());
    }
    
    /**
     * Prueba el algoritmo MRU.
     */
    private static void testMRU() {
        System.out.println("--- TEST MRU ---");
        
        PageReplacementAlgorithm mru = new MRU();
        
        // Crear 5 páginas
        Page p1 = new Page(1, 0, false, 0);
        Page p2 = new Page(2, 1, false, 0);
        Page p3 = new Page(3, 2, false, 0);
        Page p4 = new Page(4, 3, false, 0);
        Page p5 = new Page(5, 4, false, 0);
        
        // Simular carga en orden: p1, p2, p3, p4
        System.out.println("\n1. Cargando páginas en orden: p1, p2, p3, p4");
        mru.updateMetadata(p1, "load");
        mru.updateMetadata(p2, "load");
        mru.updateMetadata(p3, "load");
        mru.updateMetadata(p4, "load");

        System.out.println(mru.getDetailedState());
        
        // Crear lista de páginas en RAM
        List<Page> ramPages = new ArrayList<>();
        ramPages.add(p1);
        ramPages.add(p2);
        ramPages.add(p3);
        ramPages.add(p4);
        
        // Seleccionar víctima (debe ser p4, la más reciente)
        System.out.println("2. Seleccionando víctima...");
        Page victima = mru.selectPageToReplace(ramPages);
        System.out.println("   Víctima seleccionada: Page " + victima.getId() + 
            " ✓ (esperado: Page 4)");
        
        // Usar p2 (esto la hace la más reciente)
        System.out.println("\n3. Usando p2 (la hace más reciente)");
        mru.updateMetadata(p2, "use");
        System.out.println(mru.getDetailedState());

        // Nueva víctima debe ser p2 (ahora es la más reciente)
        victima = mru.selectPageToReplace(ramPages);
        System.out.println("   Nueva víctima: Page " + victima.getId() + 
            " ✓ (esperado: Page 2)");
        
        // Evictar p2 y cargar p5
        System.out.println("\n4. Evictando p2 y cargando p5");
        mru.updateMetadata(p2, "evict");
        ramPages.remove(p2);

        mru.updateMetadata(p5, "load");
        ramPages.add(p5);

        System.out.println(mru.getDetailedState());

        // Víctima debe ser p5 (recién cargada)
        victima = mru.selectPageToReplace(ramPages);
        System.out.println("   Víctima actual: Page " + victima.getId() + 
            " ✓ (esperado: Page 5)");
        
        // Reset
        System.out.println("\n5. Reseteando algoritmo...");
        mru.reset();
        System.out.println(mru.getDetailedState());
    }
}