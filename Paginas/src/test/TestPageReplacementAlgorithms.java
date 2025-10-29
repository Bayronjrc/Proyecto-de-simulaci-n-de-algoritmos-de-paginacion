package test;

import controladores.*;
import modelos.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Prueba los algoritmos FIFO y MRU de forma aislada.
 */
public class TestPageReplacementAlgorithms {
    
    public static void main(String[] args) {
        System.out.println("=== TEST DE ALGORITMOS DE PAGINACIÓN ===\n");
        
        testFIFO();
        System.out.println("\n" + "=".repeat(50) + "\n");
        testMRU();

        testSecondChance();
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        testRandom();
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        testOPT();
        
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

    /**
     * Prueba Second Chance.
     */
    private static void testSecondChance() {
        System.out.println("--- TEST SECOND CHANCE ---");
        
        PageReplacementAlgorithm sc = new SC();
        
        // Crear páginas
        Page p1 = new Page(1, 0, false, 0);
        Page p2 = new Page(2, 1, false, 0);
        Page p3 = new Page(3, 2, false, 0);
        Page p4 = new Page(4, 3, false, 0);
        
        // Cargar páginas
        System.out.println("1. Cargando p1, p2, p3, p4");
        sc.updateMetadata(p1, "load");
        sc.updateMetadata(p2, "load");
        sc.updateMetadata(p3, "load");
        sc.updateMetadata(p4, "load");
        
        System.out.println(sc.getDetailedState());
        
        List<Page> ramPages = new ArrayList<>(Arrays.asList(p1, p2, p3, p4));
        
        // Sin uso, víctima debe ser p1 (primera en cola, bit=0)
        System.out.println("2. Seleccionando víctima (sin usos previos)");
        Page victima = sc.selectPageToReplace(ramPages);
        System.out.println("   Víctima: Page " + victima.getId() + " ✓ (esperado: Page 1)");
        
        // Resetear
        sc.reset();
        sc.updateMetadata(p1, "load");
        sc.updateMetadata(p2, "load");
        sc.updateMetadata(p3, "load");
        sc.updateMetadata(p4, "load");
        
        // Usar p1 y p2 (poner sus bits en 1)
        System.out.println("\n3. Usando p1 y p2 (bits=1)");
        sc.updateMetadata(p1, "use");
        sc.updateMetadata(p2, "use");
        System.out.println(sc.getDetailedState());

        // Ahora la víctima debe ser p3 (primera con bit=0)
        System.out.println("4. Seleccionando víctima");
        victima = sc.selectPageToReplace(ramPages);
        System.out.println("   Víctima: Page " + victima.getId() + " ✓ (esperado: Page 3)");
        System.out.println("   (p1 y p2 recibieron segunda oportunidad)");
        
        // Reset final
        System.out.println("\n5. Reset");
        sc.reset();
        System.out.println(sc.getDetailedState());
    }
    
    /**
     * Prueba Random.
     */
    private static void testRandom() {
        System.out.println("--- TEST RANDOM ---");
        
        // Crear con semilla para reproducibilidad
        long semilla = 12345L;
        PageReplacementAlgorithm rnd = new RND(semilla);
        
        System.out.println("1. Inicializado con semilla: " + semilla);
        System.out.println(rnd.getDetailedState());
        
        // Crear páginas
        List<Page> ramPages = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            ramPages.add(new Page(i, i-1, false, 0));
        }
        
        System.out.println("\n2. Páginas en RAM: 1, 2, 3, 4, 5");
        
        // Seleccionar víctimas múltiples veces
        System.out.println("\n3. Seleccionando 10 víctimas aleatorias:");
        int[] conteo = new int[6]; // índices 0-5
        
        for (int i = 0; i < 10; i++) {
            Page victima = rnd.selectPageToReplace(ramPages);
            System.out.println("   Iteración " + (i+1) + ": Page " + victima.getId());
            conteo[victima.getId()]++;
        }
        
        System.out.println("\n4. Distribución:");
        for (int i = 1; i <= 5; i++) {
            System.out.println("   Page " + i + ": " + conteo[i] + " veces");
        }
        
        // Reset con misma semilla
        System.out.println("\n5. Reset con misma semilla (reproducible)");
        rnd.reset();
        Page primera = rnd.selectPageToReplace(ramPages);
        System.out.println("   Primera selección después de reset: Page " + primera.getId());
    }
    
    /**
     * Prueba OPT.
     */
    private static void testOPT() {
        System.out.println("--- TEST OPT ---");
        
        // Crear secuencia de instrucciones simulada
        List<Instruction> secuencia = new ArrayList<>();
        secuencia.add(new Use(1, 1)); // usa ptr 1
        secuencia.add(new Use(1, 2)); // usa ptr 2
        secuencia.add(new Use(1, 1)); // usa ptr 1
        secuencia.add(new Use(1, 3)); // usa ptr 3
        secuencia.add(new Use(1, 2)); // usa ptr 2
        
        OPT opt = new OPT(secuencia);
        
        System.out.println("1. Secuencia de instrucciones cargada");
        System.out.println(opt.getDetailedState());

        // Crear páginas (cada ptr tiene 1 página para simplificar)
        Page p1 = new Page(101, 0, false, 0); // asociada a ptr 1
        Page p2 = new Page(102, 1, false, 0); // asociada a ptr 2
        Page p3 = new Page(103, 2, false, 0); // asociada a ptr 3
        Page p4 = new Page(104, 3, false, 0); // NO será usada
        
        // Registrar mapeo ptr -> páginas
        opt.registerPtrToPages(1, Arrays.asList(101));
        opt.registerPtrToPages(2, Arrays.asList(102));
        opt.registerPtrToPages(3, Arrays.asList(103));

        System.out.println("\n2. Mapeo registrado:");
        System.out.println("   ptr 1 -> Page 101");
        System.out.println("   ptr 2 -> Page 102");
        System.out.println("   ptr 3 -> Page 103");
        System.out.println("   Page 104 no está mapeada (nunca se usa)");
        
        List<Page> ramPages = Arrays.asList(p1, p2, p3, p4);
        
        // En índice 0, la secuencia es: use(1), use(2), use(1), use(3), use(2)
        System.out.println("\n3. Seleccionando víctima en índice 0");
        System.out.println("   Próximos usos:");
        System.out.println("   - Page 101 (ptr 1): en posición 0 (distancia=0)");
        System.out.println("   - Page 102 (ptr 2): en posición 1 (distancia=1)");
        System.out.println("   - Page 103 (ptr 3): en posición 3 (distancia=3)");
        System.out.println("   - Page 104: NUNCA (distancia=∞)");
        
        opt.setCurrentInstructionIndex(0);
        Page victima = opt.selectPageToReplace(ramPages);
        System.out.println("   Víctima: Page " + victima.getId() +
            " ✓ (esperado: Page 104 - nunca se usa)");
        
        // Avanzar a índice 1 (después de usar ptr 1)
        System.out.println("\n4. Avanzando a índice 1 (después de use(1))");
        opt.setCurrentInstructionIndex(1);
        ramPages = Arrays.asList(p1, p2, p3); // p4 ya fue evicted
        
        System.out.println("   Próximos usos desde índice 1:");
        System.out.println("   - Page 101 (ptr 1): en posición 2 (distancia=1)");
        System.out.println("   - Page 102 (ptr 2): en posición 1 (distancia=0)");
        System.out.println("   - Page 103 (ptr 3): en posición 3 (distancia=2)");

        victima = opt.selectPageToReplace(ramPages);
        System.out.println("   Víctima: Page " + victima.getId() +
            " ✓ (esperado: Page 103 - se usa más tarde)");
        
        // Reset
        System.out.println("\n5. Reset");
        opt.reset();
        System.out.println(opt.getDetailedState());
    }
}