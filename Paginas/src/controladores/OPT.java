package controladores;

import modelos.Instruction;
import modelos.Page;
import modelos.Use;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 *Algoritmo OPT (Optimal Page Replacement)
 * Funcionamiento:
 * - Requiere conocimiento completo de la secuencia  futura de instrcucciones
 * - Selecciona como víctima la página que será usada más tarde en el futuro
 * - Si una página nunca más será usada, se selecciona primerp
 * * Este algoritmo es teóricamente óptimo pero imposible de implementar
 * en un sistema real (requiere conocer el futuro).
 * * Se usa como benchmark para comparar otros algoritmos
 * @author wess y bayron
 */
public class OPT implements PageReplacementAlgorithm{

    /**
     * Secuencia completa de instrucciones.
     */
    private List<Instruction> instructionSequence;

    /**
     * Indice actual de la secuencia de instrucciones.
     */
    private int currentInstructionIndex;

    /**
     * Tabla de símbolos para saber qué páginas corresponden a cada ptr.
     * Key: ptr
     * Value: Lista de IDs de páginas
     */
    private Map<Integer, List<Integer>> ptrToPageIdsMap;

    /**
     * Constructor sin secuencia (se estbablece luego).
     */
    public OPT(){
        this.instructionSequence = null;
        this.currentInstructionIndex = 0;
        this.ptrToPageIdsMap = new HashMap<>();
    }

    /**
     * Constructor con secuencia de instrucciones.
     * @param sequence Lista completa de instrucciones
     */
    public OPT(List<Instruction> sequence) {
        this.instructionSequence = sequence;
        this.currentInstructionIndex = 0;
        this.ptrToPageIdsMap = new HashMap<>();
    }

    /**
     * Establece la secuencia de instrucciones
     * @param sequence Lista de instrucciones
     */
    public void setInstructionSequence(List<Instruction> sequence) {
        this.instructionSequence = sequence;
        this.currentInstructionIndex = 0;
    }

    /**
     * Establece el mapeo de ptr a páginas (para saber que páginas se van a usar).
     * * @param map Mapa de ptr -> Lista de IDs de páginas
     */
    public void setPtrToPageIdsMap(Map<Integer, List<Integer>> map) {
        this.ptrToPageIdsMap = map;
    }

    /**
     * Registra que un ptr está asociado a ciertas páginas.
     * @param ptr ID del puntero
     * @param pageIds Lista de IDs de páginas
     */
    public void registerPtrToPages(int ptr, List<Integer> pageIds) {
        this.ptrToPageIdsMap.put(ptr, pageIds);
    }

    @Override
    public Page selectPageToReplace(List<Page> pagesInRAM) {
        if (pagesInRAM == null || pagesInRAM.isEmpty()) {
            return null;
        }

        if (instructionSequence == null || instructionSequence.isEmpty()){
            System.err.println("OPT: Secuencia de instrucciones no establecida.");
            return pagesInRAM.get(0);
        }

        Map<Integer, Integer> nextUse = new HashMap<>();
    
        for (Page page : pagesInRAM) {
            int pageId = page.getId();
            int distance = findNextUse(pageId, currentInstructionIndex);
            nextUse.put(pageId, distance);
        }

        Page victimPage = null;
        int maxDistance = -1;

        for (Page page : pagesInRAM) {
            int distance = nextUse.get(page.getId());
            if (distance > maxDistance) {
                maxDistance = distance;
                victimPage = page;
            }
        }
        return victimPage;
    }

    /**
     * Busca cuándo será usado próximamente un ID de página
     * @param pageId ID de la página a buscar
     * @param startIndex Índice desde dodne empezar a buscar
     * @return Distancia hasta el proximo usos, o Integer.MAX_VALUE si nunca más se usa
     */
    private int findNextUse(int pageId, int startIndex) {
        for (int i = startIndex; i < instructionSequence.size(); i++){
            Instruction instr = instructionSequence.get(i);

            if (instr instanceof Use) {
                Use useInstr = (Use) instr;
                int ptr = useInstr.getPtr();

                List<Integer> pageIds = ptrToPageIdsMap.get(ptr);
                if(pageIds != null && pageIds.contains(pageId)) {
                    return i - startIndex;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public void updateMetadata(Page page, String event) {
        if ("use".equals(event.toLowerCase())) {
            currentInstructionIndex++;
        }
    }

    /**
     * Avanza el indice de instruccion actual
     * Debe ser llamado por la MMU despues de ejecutar cada instrucción
     */
    public void advanceInstructionIndex() {
        currentInstructionIndex++;
    }

    /**
     * Establece manualmente el indice actual
     * @param index Nuevo indice
     */
    public void setCurrentInstructionIndex(int index) {
        this.currentInstructionIndex = index;
    }

    @Override
    public void reset() {
        this.currentInstructionIndex = 0;
        this.ptrToPageIdsMap.clear();
    }

    @Override
    public String getAlgorithmName() {
        return "OPT";
    }

    @Override
    public String getDetailedState() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== OPT - Estado Actual ===\n");
        
        if (instructionSequence != null) {
            sb.append("Total de instrucciones: ").append(instructionSequence.size()).append("\n");
            sb.append("Índice actual: ").append(currentInstructionIndex).append("\n");
            sb.append("Instrucciones restantes: ")
              .append(instructionSequence.size() - currentInstructionIndex).append("\n");
        } else {
            sb.append("Sin secuencia de instrucciones cargada\n");
        }
        
        sb.append("Punteros rastreados: ").append(ptrToPageIdsMap.size()).append("\n");

        if (!ptrToPageIdsMap.isEmpty()) {
            sb.append("\nMapeo ptr -> páginas:\n");
            for (Map.Entry<Integer, List<Integer>> entry : ptrToPageIdsMap.entrySet()) {
                sb.append(String.format("  ptr %d: %s\n", entry.getKey(), entry.getValue()));
            }
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        String estado = (instructionSequence != null)
            ? String.format("inst=%d/%d", currentInstructionIndex, instructionSequence.size())
            : "sin-secuencia";
        return String.format("OPT[%s]", estado);
    }

}