package controladores;

import modelos.Page;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Algoritmo FIFO (First In, First Out)
 * Reemplaza la página que fue cargada primero en RAM
 * 
 * Implemntación:
 * - Usa un contador global que se incrementa en cada carga
 * - Cada página tien un "timestamp" de cuando fue cargada
 * - La victima es la página con el timestamp más antiguo
 */

public class FIFO implements PageReplacementAlgorithm {
    
    /**
     * Mapa que asocia cada págian con su orden de llegada.
     * KEy: ID de la pagina
     * Value: Timestamp de carga( menor = más antiguo)
     */
    private Map<Integer, Integer> pageLoadOrder;

    /**
     * Contador global que se incrementa en cada página cargada.
     */
    private int globalLoadCounter;

    public FIFO() {
        this.pageLoadOrder = new HashMap<>();
        this.globalLoadCounter = 0;
    }

    @Override
    public Page selectPageToReplace(List<Page> pagesInRAM) {
        if (pagesInRAM == null || pagesInRAM.isEmpty()) {
            return null; 
        }

        Page victimPage = null;
        int oldestLoadTime = Integer.MAX_VALUE;

        for (Page page : pagesInRAM) {
            Integer timestamp = pageLoadOrder.get(page.getId());

            if (timestamp == null) {
                timestamp = Integer.MAX_VALUE;
            }

            if (timestamp < oldestLoadTime) {
                oldestLoadTime = timestamp;
                victimPage = page;
            }
        }

        return victimPage;
    }

    @Override
    public void updateMetadata(Page page, String event) {
        if (page == null) return;

        switch (event.toLowerCase()) {
            case "load":
                // Asignar timestamp de carga
                pageLoadOrder.put(page.getId(), globalLoadCounter);
                globalLoadCounter++;
                break;
                
            case "evict":
                // Remover la página del mapa cuando se envía a disco
                pageLoadOrder.remove(page.getId());
                break;
                
            case "use":
                // FIFO no hace nada cuando se usa una página
                // (el orden de llegada no cambia)
                break;
                
            default:
                System.err.println("FIFO: Evento desconocido: " + event);

        }

    }

    @Override
    public void reset() {
        this.pageLoadOrder.clear();
        this.globalLoadCounter = 0;
    }

    @Override
    public String getAlgorithmName() {
        return "FIFO";
    }

    @Override
    public String getDetailedState() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FIFO - Estado Actual ===\n");
        sb.append("Contador Global: ").append(globalLoadCounter).append("\n");
        sb.append("Páginas rastreadas: ").append(pageLoadOrder.size()).append("\n");

        if (!pageLoadOrder.isEmpty()) {
            sb.append("\nOrden de carga (ID -> Timestamp):\n");
            pageLoadOrder.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()) // Ordenar por timestamp
                .forEach(entry -> sb.append(String.format("  Page %d: cargada en t=%d\n", 
                    entry.getKey(), entry.getValue())));
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("FIFO[páginas=%d, contador=%d]", 
            pageLoadOrder.size(), globalLoadCounter);
    }  
}
