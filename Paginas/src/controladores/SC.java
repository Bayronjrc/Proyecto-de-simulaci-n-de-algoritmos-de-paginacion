package controladores;

import modelos.Page;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;

/**
 * Algoritmo Second Chance (SC) - Mejora de FIFO
 * 
 * Funcionamiento:
 * - Mantiene una cola circular (simulada con Queue)
 * - Cada página tien un "bit de referencia"
 * - Al buscar víctima:
 *   - Si el bit = 1: poner bit = 0 y enviar al final de la cola
 *   - Si el bit = 0: seleccionar como víctima
 * @author Bayron
 */
public class SC implements PageReplacementAlgorithm {
    
    /**
     * Cola circular que mantiene el orden de las páginas
     * Las páginas se agregan al final y se procesan desde el inicio.
     */
    private Queue<Integer> pageQueue;

    /**
     * Mapa que asocia cada página con su bit de referencia.
     * Key: ID de la página
     * Value: true (1) si fue referenciada recientemente, false (0) si no)
     */
    private Map<Integer, Boolean> referenceBits;
    
    public SC() {
        this.pageQueue = new LinkedList<>();
        this.referenceBits = new HashMap<>();
    }

    @Override
    public Page selectPageToReplace(List<Page> pagesInRAM) {
        if (pagesInRAM == null || pagesInRAM.isEmpty()) {
            return null; 
        }

        Map<Integer, Page> pageMap = new HashMap<>();
        for (Page page : pagesInRAM) {
            pageMap.put(page.getId(), page);
        }

        int iterations = 0;
        int maxIterations = pageQueue.size() * 2; //Evitamos el bucle infinito

        while(iterations < maxIterations) {
            Integer pageId = pageQueue.poll();

            if (pageId == null) {
                break; 
            }

            Boolean refBit = referenceBits.get(pageId);
            if (refBit == null) {
                refBit = false;
            }

            if (refBit) {
                //refBit = 1 -> dar segunda oportunidad
                //poner bit = 0 y enviar al final de la cola
                referenceBits.put(pageId, false);
                pageQueue.offer(pageId);
            } else {
                //refBit = 0 -> seleccionar como víctima
                return pageMap.get(pageId);
            }
            iterations++;
        }

        System.err.println("SecondChance: No se encontró víctima tras " + maxIterations + " iteraciones, devolviendo primera página.");
        return pagesInRAM.get(0);
    }

    @Override
    public void updateMetadata(Page page, String event) {
        if (page == null) return;

        int pageId = page.getId();

        switch (event.toLowerCase()) {
            case "load":
                // Al cargar, agregar a la cola y poner bit = 0
                pageQueue.offer(pageId);
                referenceBits.put(pageId, false);
                break;
                
            case "use":
                // Al usar, poner bit = 1 (dar segunda oportunidad)
                referenceBits.put(pageId, true);
                break;
                
            case "evict":
                // Al evictar, remover de la cola y del mapa
                pageQueue.remove(pageId);
                referenceBits.remove(pageId);
                break;
                
            default:
                System.err.println("SC: Evento desconocido: " + event);
        }
    }

    @Override
    public void reset() {
        this.pageQueue.clear();
        this.referenceBits.clear();
    }

    @Override
    public String getAlgorithmName() {
        return "SC";
    }

    @Override
    public String getDetailedState() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Second Chance - Estado Actual ===\n");
        sb.append("Páginas en cola: ").append(pageQueue.size()).append("\n");
        sb.append("Páginas rastreadas: ").append(referenceBits.size()).append("\n");

        if (!pageQueue.isEmpty()) {
            sb.append("\nCola circular (frente -> final):\n");
            for (Integer pageId : pageQueue) {
                Boolean bit = referenceBits.get(pageId);
                String bitStr = (bit != null && bit) ? "1" : "0";
                sb.append(String.format("  Page %d: bit=%s\n", pageId, bitStr));
            }
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("SC[páginas=%d, cola=%d]", 
            referenceBits.size(), pageQueue.size());
    }

}
