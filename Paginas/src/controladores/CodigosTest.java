package controladores;

import modelos.Pagina;
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
   
    private ArrayList<Pagina> requestQueue;
    private int failAmount;
    private int amountOfPagesInMarco = 0;
    private Pagina[] marco;
    private Pagina[] blackList;

    private Boolean isInMarcoAlredy(Pagina paginaNueva){
        for(Pagina paginaVieja : this.marco){
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
    
    private int ArraySize(Pagina[] array){
       int count=0;
       for(Pagina pagina : array){
           if (pagina != null)
                count++;
       }
       return count;
    }
    
    private Boolean isInBlackList(Pagina paginaNueva){
        //System.out.println("Lista negra: " + Arrays.toString(blackList));
        for(Pagina paginaVieja : blackList){
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
    
    
    private int getIndexOfPaginaInArray(Pagina pagina, Pagina[] marco, int Size){
        for(int i = 0; i < Size; i++){
            if(marco[i] !=null)
                if(pagina.getCodigo() == marco[i].getCodigo()){
                    return(i);
            }
        }
        return -1;
    }
    
    private Pagina[] KILL(Pagina pagina, Pagina[] marco, int Size) {
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
        Pagina[] newMarco = new Pagina[Size];
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
        
        requestQueue = new ArrayList<Pagina>();
        marco = new Pagina[frameSize];
        
        // --- Paso 1, Llenar la lista con codigos de ejemplo. ---
        for(int i = 0; i < amountOfPages; i++){
            Pagina pagina = new Pagina();
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
            
            Pagina pagina = requestQueue.remove(0);
            if(ArraySize(marco) == frameSize)//El marco está lleno, aquí va el óptimo
            {
                //System.out.println(" --- REEEMPLAZO VA A A OCURRIR!!!---");
                //Si el codigo ya está en el marco, solo quitela de la lista luego.
                if(isInMarcoAlredy(pagina));
                else{
                    //--- Copiar un arraylist con otro ---
                    blackList = new Pagina[frameSize];
                    System.arraycopy(marco, 0, blackList, 0, frameSize);

                    // --- ¿Cual es el ultimo que aparece?---
                    for(Pagina paginaDelResto : requestQueue){
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
                        for(Pagina muerto : blackList){
                            
                            KILL(muerto, marco, frameSize);
                        }
                    }
                    
                    //Ahora introducir el nuevo elemento
                    int index = ArraySize(marco);
                    Pagina nuevaPagina = new Pagina(pagina);
                    marco[index] = nuevaPagina;
                    //Aumentó la cantidad de fallos por 1.
                    failAmount++;
                }
                //requestQueue.remove(pagina);
            }
            
            else if (!isInMarcoAlredy(pagina))//Meter en el marco la pagina
            {   
                int index = ArraySize(marco);
                Pagina nuevaPagina = new Pagina(pagina);
                marco[index] = nuevaPagina;
                //requestQueue.remove(0);
                
                failAmount++;
                //System.out.println("Done 1");
            }
            
        }
        System.out.println("La cantidad de fallos es de "+ failAmount);
    }
    
}
