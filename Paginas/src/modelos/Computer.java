/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

import java.util.ArrayList;

/**
 *
 * @author wess
 */
public class Computer {
    
    /*-------------------------------
    /
    /        Variables used
    /
    /-------------------------------*/
    
    //RAM is the memory, must be used as an array!! Even if it's an arrayList.
    //It's useful to have it in this type for it to come with more tools.
    private ArrayList<Page> Ram;
    private final int amountOfPagesInRAM;
    
    private int amountOfCores;
    private int amountOfRamInKB; 
    private ArrayList<Page> Disk;//Virtual limitless memory
    private int pageKBSize;
    
    // This one is for the amount of instructions doable per second.
    private int instructionsPerSecond;
    
    /*-------------------------------
    /
    /           Methods
    /
    /-------------------------------*/
    
    
    /**
     *This is the default constructor for this project.
     */
    public Computer(){
        this.Disk = new ArrayList<>(); 
        this.amountOfCores = 1;
        this.instructionsPerSecond = 1;
        this.pageKBSize = 4;
        this.amountOfRamInKB = 400;
        
        //int ensures no funny bussiness happens here.
        this.amountOfPagesInRAM = amountOfRamInKB/pageKBSize;
        this.Ram = new ArrayList<>(getAmountOfPagesInRAM());
        
        for(int i = 0; i < amountOfPagesInRAM; i++){
            Ram.add(null);
        }

    }
    
    
    /**
     * Gives the amount of real memory being used in the computer object.
     * @return amount of KB being used with the pages
     */
    public int getRealMemoryUsed(){
        int i = 0;
        for(Page page : getRam()){
            if( page != null ) 
                i++;
        }
        return i*getPageKBSize();
    }
    
    
    /**
     * Gives the amount of virtual memory being in use on the computer object.
     * @return amount of KB being used with the pages
     */
    public int getVirtualMemoryUsed(){
        int i = 0;
        for(Page page : getDisk()){
            if( page != null ) 
                i++;
        }
        return i*getPageKBSize();
    }
    
    
    /**
     * 
     * @param page the page you want to place in the disk
    */
    public void placePageInDisk(Page page){
        Disk.add(page);
    }
    
    
    /**
     * Place a page in a specific position in memory
     * @param page the page that will be placed
     * @param index the index the page will have in the directory.
     */
    public void placePageInMemory(Page page, int index) {
        if (index < 0 || index >= Ram.size()) {
            throw new IndexOutOfBoundsException("Índice de marco de RAM fuera de rango: " + index);
        }
        page.setIndexOfPage(index); // Asigna el marco a la página
        page.setIsInVirtualMemory(false); // Marca que está en RAM
        Ram.set(index, page); // Usa SET, no ADD
    }
    
    
    /**
     * Sends a page to Siberia (The disk) to be stored there in the meantime as
     * Virtual Memory. This frees the ram to be used for another page.
     * @param index 
     */
    public Page sendPageToDisk(int index) {
        if (index < 0 || index >= Ram.size()) {
            throw new IndexOutOfBoundsException("Índice de marco de RAM fuera de rango: " + index);
        }

        Page movingPage = Ram.get(index); // Obtiene la página de ese marco

        if (movingPage != null) {
            movingPage.setIndexOfPage(-1); // Indica que ya no tiene un marco de RAM
            movingPage.setIsInVirtualMemory(true); // Marca que está en Disco
            Disk.add(movingPage); // Agrega al disco (el tamaño del disco sí es variable)
        }

        Ram.set(index, null); // Libera el marco de RAM
        return movingPage;
    }
    
    //--------------------------------------------------------------------------
    
    
    /**
     * Gets the first free page possible.
     *
     * @return The index of the first free frame, -1 if the RAM is full
     */
    public int findFreeFrameInRam() {
        for (int i = 0; i < Ram.size(); i++) {
            if (Ram.get(i) == null) {
                return i;
            }
        }
        return -1; // No hay marcos libres
    }
    
    
    //Yay, reusing code is nice.
    public boolean isRamFull() {
        return findFreeFrameInRam() == -1;
    }
    
    
    /**
     * Gets if a page is already loaded.
     * @param page The page that is being searched
     * @return true if it's there.
     */
    public boolean isPageInRam(Page page) {
        return Ram.contains(page);//Yes this should be enough.
    }

    
    /**
     * Calcula la fragmentación interna total de todas las páginas cargadas en RAM.
     *
     * @return El total de bytes desperdiciados en la RAM.
     */
    public int getTotalRamFragmentation() {
        int totalFragmentation = 0;
        for (Page page : Ram) {
            if (page != null) {
                totalFragmentation += page.getFragmentationInBytes();
            }
        }
        return totalFragmentation;
    }

    
    /**
     * To reset the computer when a new simulation needs to be done later.
     */
    public void reset() {
        Disk.clear();
        Ram.clear();

        // Re-inicializar la RAM con 100 marcos nulos
        int amountOfPagesInRAM = this.amountOfRamInKB / this.pageKBSize;
        this.Ram = new ArrayList<>(amountOfPagesInRAM);
        for (int i = 0; i < amountOfPagesInRAM; i++) {
            Ram.add(null);
        }
    }
    
    
    /*-------------------------------
    /
    /      Standard Gets and sets
    /
    /-------------------------------*/

    /**
     * @return the Ram
     */
    public ArrayList<Page> getRam() {
        return Ram;
    }

    /**
     * @return the amountOfCores
     */
    public int getAmountOfCores() {
        return amountOfCores;
    }

    /**
     * @return the amountOfRamInKB
     */
    public int getAmountOfRamInKB() {
        return amountOfRamInKB;
    }

    /**
     * @return the Disk
     */
    public ArrayList<Page> getDisk() {
        return Disk;
    }

    /**
     * @return the pageKBSize
     */
    public int getPageKBSize() {
        return pageKBSize;
    }

    /**
     * @return the instructionsPerSecond
     */
    public int getInstructionsPerSecond() {
        return instructionsPerSecond;
    }

    /**
     * @param Ram the entire array... yeah, don't use this unless you have to.
     */
    public void setRam(ArrayList<Page> Ram) {
        this.Ram = Ram;
    }

    /**
     * @param amountOfCores the amountOfCores to set
     */
    public void setAmountOfCores(int amountOfCores) {
        this.amountOfCores = amountOfCores;
    }

    /**
     * @param amountOfRamInKB the amountOfRamInKB to set
     */
    public void setAmountOfRamInKB(int amountOfRamInKB) {
        this.amountOfRamInKB = amountOfRamInKB;
    }

    /**
     * @param Disk the Disk to set
     */
    public void setDisk(ArrayList<Page> Disk) {
        this.Disk = Disk;
    }

    /**
     * @param pageKBSize the pageKBSize to set
     */
    public void setPageKBSize(int pageKBSize) {
        this.pageKBSize = pageKBSize;
    }

    /**
     * @param instructionsPerSecond the instructionsPerSecond to set
     */
    public void setInstructionsPerSecond(int instructionsPerSecond) {
        this.instructionsPerSecond = instructionsPerSecond;
    }

    /**
     * @return the amountOfPagesInRAM
     */
    public int getAmountOfPagesInRAM() {
        return amountOfPagesInRAM;
    }
}
