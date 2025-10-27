package test;

import modelos.*;
import modelos.Process;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase para probar las instrucciones y procesos
 */
public class TestProcesosInstrucciones {
    
    public static void main(String[] args) {
        System.out.println("=== TEST DE PROCESOS E INSTRUCCIONES ===\n");
        
        // Crear dos procesos
        Process p1 = new Process(1);
        Process p2 = new Process(2);
        
        // Proceso 1: secuencia del ejemplo del PDF
        p1.addInstruction(new New(1, 250));
        p1.addInstruction(new New(1, 50));
        p1.addInstruction(new Use(1, 1));
        p1.addInstruction(new Use(1, 2));
        p1.addInstruction(new Use(1, 1));
        p1.addInstruction(new Delete(1, 1));
        p1.addInstruction(new Kill(1));
        
        // Proceso 2: secuencia simple
        p2.addInstruction(new New(2, 5320));
        p2.addInstruction(new New(2, 345));
        p2.addInstruction(new Use(2, 3));
        p2.addInstruction(new New(2, 50));
        p2.addInstruction(new Use(2, 4));
        p2.addInstruction(new Kill(2));
        
        // Mostrar información de los procesos
        System.out.println("--- PROCESO 1 ---");
        System.out.println(p1);
        System.out.println("\nInstrucciones:");
        for (Instruction inst : p1.getInstructionList()) {
            System.out.println("  " + inst);
            if (inst instanceof New) {
                New newInst = (New) inst;
                System.out.println("    └─> Requiere " + 
                    newInst.calcularPaginasNecesarias(4096) + " páginas");
            }
        }
        
        System.out.println("\n--- PROCESO 2 ---");
        System.out.println(p2);
        System.out.println("\nInstrucciones:");
        for (Instruction inst : p2.getInstructionList()) {
            System.out.println("  " + inst);
            if (inst instanceof New) {
                New newInst = (New) inst;
                System.out.println("    └─> Requiere " + 
                    newInst.calcularPaginasNecesarias(4096) + " páginas");
            }
        }
        
        // Generar archivo de salida intercalado
        System.out.println("\n--- ARCHIVO DE OPERACIONES (intercalado) ---");
        List<Instruction> todasInstrucciones = new ArrayList<>();
        todasInstrucciones.addAll(p1.getInstructionList());
        todasInstrucciones.addAll(p2.getInstructionList());
        
        // Simulación simple de intercalado
        List<Instruction> intercaladas = intercalarInstrucciones(
            p1.getInstructionList(),
            p2.getInstructionList()
        );
        
        int lineNum = 1;
        for (Instruction inst : intercaladas) {
            System.out.println(lineNum + "\t" + inst.toFileFormat());
            lineNum++;
        }
        
        // Test de validaciones
        System.out.println("\n--- TEST DE VALIDACIONES ---");
        try {
            Process p3 = new Process(3);
            p3.addInstruction(new Kill(3));
            p3.addInstruction(new New(3, 100)); // Debería fallar
            System.out.println("ERROR: No debería permitir instrucciones después de kill");
        } catch (IllegalStateException e) {
            System.out.println("✓ Validación correcta: " + e.getMessage());
        }
        
        try {
            Process p4 = new Process(4);
            p4.addInstruction(new New(5, 100)); // PID diferente
            System.out.println("ERROR: No debería permitir instrucciones de otro proceso");
        } catch (IllegalArgumentException e) {
            System.out.println("✓ Validación correcta: " + e.getMessage());
        }
        
        System.out.println("\n=== FIN DEL TEST ===");
    }
    
    /**
     * Método auxiliar para intercalar instrucciones de dos procesos
     */
    private static List<Instruction> intercalarInstrucciones(
            List<Instruction> lista1, List<Instruction> lista2) {
        List<Instruction> resultado = new ArrayList<>();
        int i = 0, j = 0;
        
        while (i < lista1.size() && j < lista2.size()) {
            resultado.add(lista1.get(i++));
            resultado.add(lista2.get(j++));
        }
        
        while (i < lista1.size()) resultado.add(lista1.get(i++));
        while (j < lista2.size()) resultado.add(lista2.get(j++));
        
        return resultado;
    }
}