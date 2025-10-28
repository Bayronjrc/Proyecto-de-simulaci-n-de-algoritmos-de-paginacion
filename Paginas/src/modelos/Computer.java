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
    
    private int amountOfCores;
    private int amountOfRamInKB; 
    private ArrayList<Page> Disk;//Virtual memory
    private int pageKBSize;
    
    // This one is for the amount of instructions doable per second.
    private int instructionsPerSecond;
    
    /*-------------------------------
    /
    /           Methods
    /
    /-------------------------------*/
    
    /**
     *This is the default for this project.
     */
    public Computer(){
        this.Disk = new ArrayList<>(); 
        this.amountOfCores = 1;
        this.instructionsPerSecond = 1;
        this.pageKBSize = 4;
        this.amountOfRamInKB = 400;
        
        //int ensures no funny bussiness happens here.
        int amountOfPagesInRAM = amountOfRamInKB/pageKBSize;
        this.Ram = new ArrayList<>(amountOfPagesInRAM);
        
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
    
    
    
    
    // More functions can go around this area later...
    
    
    
    
    
    /*-------------------------------
    /
    /         Gets and sets
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
     * @param Ram the Ram to set
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
}
