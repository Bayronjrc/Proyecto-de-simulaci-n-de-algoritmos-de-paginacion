package controladores;

import modelos.Page;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Algoritmo MRU (Most Recently Used)
 * Reemplaza la página que fue usada más recientemente en RAM
 * 
 * Implemntación:
 * - Usa un contador global que se incrementa en cada uso
 * - Cada página tien un "timestamp" de cuando fue usada
 * - La victima es la página con el timestamp más reciente(mayor)
 * 
 * Nota: MRU es poco común en sistemas reales pero útil para casos especificos
 * donde las paginas recien usadas probablemente no se volveran a usar pronto.
 */

public class MRU implements PageReplacementAlgorithm {
    
    /**
     * Mapa que asocia cada página con su ultimo timestamp de uso-
     * Key: ID de la pagina
     * Value: Timestamp de ultimo uso (mayor = más reciente)
     */
    private Map<Integer, Integer> lastUsedTimestamps;

    /**
     * Contador global que se incrementa con cada operación..
     */
    private int globalCounter;

    public MRU() {
        this.lastUsedTimestamps = new HashMap<>();
        this.globalCounter = 0;
    }

    @Override
    public Page selectPageToReplace(List<Page> pagesInRAM) {
        if (pagesInRAM == null || pagesInRAM.isEmpty()) {
            return null; 
        }

        Page victimPage = null;
        int mostRecentUseTime = Integer.MIN_VALUE;

        for (Page page: pagesInRAM) {
            Integer timestamp = lastUsedTimestamps.get(page.getId());

            if(timestamp == null) {
                timestamp = Integer.MIN_VALUE;
            }

            if (timestamp > mostRecentUseTime) {
                mostRecentUseTime = timestamp;
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
                // Cuando se carga, actualizar timestamp
                lastUsedTimestamps.put(page.getId(), globalCounter);
                globalCounter++;
                break;
                
            case "use":
                // Actualizar timestamp cuando se usa
                lastUsedTimestamps.put(page.getId(), globalCounter);
                globalCounter++;
                break;
                
            case "evict":
                // Remover la página del mapa cuando se envía a disco
                lastUsedTimestamps.remove(page.getId());
                break;
                
            default:
                System.err.println("MRU: Evento desconocido: " + event);
        }
    }

    @Override
    public void reset() {
        this.lastUsedTimestamps.clear();
        this.globalCounter = 0;
    }

    @Override
    public String getAlgorithmName() {
        return "MRU";
    }

    @Override
    public String getDetailedState() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MRU - Estado Actual ===\n");
        sb.append("Contador Global: ").append(globalCounter).append("\n");
        sb.append("Páginas rastreadas: ").append(lastUsedTimestamps.size()).append("\n");

        if (!lastUsedTimestamps.isEmpty()) {
            sb.append("\nÚltimo uso (ID -> Timestamp):\n");
            lastUsedTimestamps.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())) // Ordenar por más reciente
                .forEach(entry -> sb.append(String.format("  Page %d: último uso en t=%d\n", 
                    entry.getKey(), entry.getValue())));
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("MRU[páginas=%d, contador=%d]", 
            lastUsedTimestamps.size(), globalCounter);
    }
}
