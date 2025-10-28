package test;

import test.PaginationTestModel;
import java.util.ArrayList;
import java.util.Arrays;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author wesleyesquivel
 */
public class CodigosTest {
   
    private ArrayList<PaginationTestModel> requestQueue;
    private int failAmount;
    private int amountOfPagesInMarco = 0;
    private PaginationTestModel[] marco;
    private PaginationTestModel[] blackList;

    private Boolean isInMarcoAlredy(PaginationTestModel paginaNueva){
        for(PaginationTestModel paginaVieja : this.marco){
            if(paginaVieja != null){
                int codigoViejo = paginaVieja.getCodigo();
                int codigoNuevo = paginaNueva.getCodigo();
                if(codigoViejo == codigoNuevo)
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    private int ArraySize(PaginationTestModel[] array){
       int count=0;
       for(PaginationTestModel pagina : array){
           if (pagina != null)
                count++;
       }
       return count;
    }
    
    private Boolean isInBlackList(PaginationTestModel paginaNueva){
        //System.out.println("Lista negra: " + Arrays.toString(blackList));
        for(PaginationTestModel paginaVieja : blackList){
            if ( paginaVieja == null )continue;//???
            
            int codigoViejo = paginaVieja.getCodigo();
            int codigoNuevo = paginaNueva.getCodigo();
            if(codigoViejo == codigoNuevo)
            {
                //System.out.println("Codigo "+ codigoViejo+" Es igual a " + codigoNuevo);
                return true;
            }
        }
        return false;
    }
    
    
    private int getIndexOfPaginaInArray(PaginationTestModel pagina, PaginationTestModel[] marco, int Size){
        for(int i = 0; i < Size; i++){
            if(marco[i] !=null)
                if(pagina.getCodigo() == marco[i].getCodigo()){
                    return(i);
            }
        }
        return -1;
    }
    
    private PaginationTestModel[] KILL(PaginationTestModel pagina, PaginationTestModel[] marco, int Size) {
        if(pagina == null)
            return marco;
        
        // 1. Encontrar el índice del elemento a eliminar.
        int index = getIndexOfPaginaInArray(pagina, marco, Size);
        //System.out.println("Se evaluara "+pagina.getCodigo() +" en la ubicacion"+ index);
        // 2. Si no se encuentra la página, devolver el array original sin cambios.
        if (index == -1) {
            //System.out.println("Página no encontrada, no se puede eliminar.");
            return marco;
        }

        marco[index] = null;
        PaginationTestModel[] newMarco = new PaginationTestModel[Size];
        for(int i = 0, j=0; i < Size; i++){
            if(marco[i]!=null){
                newMarco[j]=marco[i];
                j++;
            }   
        }
        return newMarco;
    }
        
    
    //Agrego N páginas en la lista
    /**
     * 
     * @param amountOfPages Es la cantidad de páginas en la fila de ejemplo
     * @param frameSize Es la cantidad total de páginas en el marco
     * @param printSteps Un booleano si se quiere imprimir cada paso
     */
    public void Test(int amountOfPages, int frameSize, boolean printSteps){
        //--- Paso 0, declarar las Estructuras de datos ---
        
        requestQueue = new ArrayList<PaginationTestModel>();
        marco = new PaginationTestModel[frameSize];
        
        // --- Paso 1, Llenar la lista con codigos de ejemplo. ---
        for(int i = 0; i < amountOfPages; i++){
            PaginationTestModel pagina = new PaginationTestModel();
            requestQueue.add(pagina);
        }
        // Marco = [1,2,3,54]
        // 23 12 1 2 1 3....
        
        
        
        // -- Paso 2, el algoritmo de reemplazo ---
        while(!requestQueue.isEmpty()){
            
            if(printSteps){
                System.out.println("----------------------------------------------");
                System.out.println("The array size is" + ArraySize(marco));
                System.out.println(Arrays.toString(marco));
                System.out.println(requestQueue);
            }
            
            PaginationTestModel pagina = requestQueue.remove(0);
            if(ArraySize(marco) == frameSize)//El marco está lleno, aquí va el óptimo
            {
                //System.out.println(" --- REEEMPLAZO VA A A OCURRIR!!!---");
                //Si el codigo ya está en el marco, solo quitela de la lista luego.
                if(isInMarcoAlredy(pagina));
                else{
                    //--- Copiar un arraylist con otro ---
                    blackList = new PaginationTestModel[frameSize];
                    System.arraycopy(marco, 0, blackList, 0, frameSize);

                    // --- ¿Cual es el ultimo que aparece?---
                    for(PaginationTestModel paginaDelResto : requestQueue){
                        if (isInBlackList(paginaDelResto)){
                            //Quitar esa página del arreglo blacklist
                            blackList = KILL(paginaDelResto,blackList,frameSize);
                            /// BUSCAR LA PAGINA, Y BAM
                            
                        }
                        if(ArraySize(blackList) == 1){
                            //Solo queda 1, terminar, el que quedó aparece ultimo.
                            break;
                        }
                    }
                    if(ArraySize(blackList) > 0){
                        //Eliminar todas las páginas que siguen en el blacklist
                        //Del marco.
                        for(PaginationTestModel muerto : blackList){
                            
                            KILL(muerto, marco, frameSize);
                        }
                    }
                    
                    //Ahora introducir el nuevo elemento
                    int index = ArraySize(marco);
                    PaginationTestModel nuevaPagina = new PaginationTestModel(pagina);
                    marco[index] = nuevaPagina;
                    //Aumentó la cantidad de fallos por 1.
                    failAmount++;
                }
                //requestQueue.remove(pagina);
            }
            
            else if (!isInMarcoAlredy(pagina))//Meter en el marco la pagina
            {   
                int index = ArraySize(marco);
                PaginationTestModel nuevaPagina = new PaginationTestModel(pagina);
                marco[index] = nuevaPagina;
                //requestQueue.remove(0);
                
                failAmount++;
                //System.out.println("Done 1");
            }
            
        }
        System.out.println("La cantidad de fallos es de "+ failAmount);
    }
    
}
