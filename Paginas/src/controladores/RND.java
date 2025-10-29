package controladores;

import modelos.Page;
import java.util.List;
import java.util.Random;

/**
 * Algoritmo Random (RND)
 * Selecciona una página víctima de forma completamente aleatoria.
 * 
 * Características:
 * - Simple y sin overhead de metadatos
 * - No considera el uso o antigüedad de las páginas
 * - Útil como baseline para comparar otros algoritmos
 * - Puede usar una semilla para reproducibilidad en tests
 * Implemntación:
 * - Usa java.util.Random para seleccionar un índice aleatorio
 * @author Bayron
 */

public class RND implements PageReplacementAlgorithm {
    
    /**
     * Generador de números aleatorios.
     */
    private Random randomGenerator;

    /**
     * Semilla utilzada (para debuggin).
     */
    private Long seed;

    /**
     * Constructor con semilla aleatoria.
     */
    public RND() {
        this.randomGenerator = new Random();
        this.seed = null;
    }

    /**
     * Constructor con semilla específica (para tests).
     */
    public RND(Long seed) {
        this.randomGenerator = new Random(seed);
        this.seed = seed;
    }

    /**
     * Establece una nueva semilla
     * 
     * @param seed Nueva semilla
     */
    public void setSeed(Long seed) {
        this.seed = seed;
        this.randomGenerator = new Random(seed);
    }

    @Override
    public Page selectPageToReplace(List<Page> pagesInRAM) {
        if (pagesInRAM == null || pagesInRAM.isEmpty()){
            return null; 
        }

        int randomIndex = randomGenerator.nextInt(pagesInRAM.size());

        return pagesInRAM.get(randomIndex);
    }

    @Override
    public void updateMetadata(Page page, String event) {
        // RND no necesita metadatos, este método es un no-op.
    }

    @Override
    public void reset() {
        if (seed != null) {
            this.randomGenerator = new Random(seed);
        } else {
            this.randomGenerator = new Random();
        }
    }

    @Override
    public String getAlgorithmName() {
        return "RND";
    }

    @Override
    public String getDetailedState() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Random - Estado Actual ===\n");

        if (seed != null) {
            sb.append("Semilla: ").append(seed).append("\n");
            sb.append("Modo: Reproducible\n");
        } else {
            sb.append("Modo: Aleatorio (sin semilla)\n");
        }
        
        sb.append("\nNota: Este algoritmo no mantiene metadatos.\n");
        sb.append("Cada selección es independiente y aleatoria.\n");
        
        return sb.toString();
    }

    @Override
    public String toString() {
        String seedStr = (seed != null) ? ", seed=" + seed : "";
        return String.format("RND[%s]", seedStr.isEmpty() ? "random" : seedStr);
    }
}
