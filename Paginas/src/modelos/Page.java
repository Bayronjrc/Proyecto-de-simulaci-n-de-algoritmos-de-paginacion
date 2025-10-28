/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

/**
 *
 * @author wess
 */
public class Page {
    private int id;
    private int indexOfPage;
    private boolean isInVirtualMemory;
    private int extraValue;
    
    
    /**
     * This is the constructor to set a page directly
     * @param id
     * @param indexOfPage
     * @param isInVirtualMemory
     * @param extraValue
     */
    public Page(int id, int indexOfPage, boolean isInVirtualMemory, int extraValue){
        this.id = id;
        this.extraValue = extraValue;
        this.indexOfPage = indexOfPage;
        this.isInVirtualMemory = isInVirtualMemory;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the indexOfPage
     */
    public int getIndexOfPage() {
        return indexOfPage;
    }

    /**
     * @return the isInVirtualMemory
     */
    public boolean isIsInVirtualMemory() {
        return isInVirtualMemory;
    }

    /**
     * @return the extraValue
     */
    public int getExtraValue() {
        return extraValue;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param indexOfPage the indexOfPage to set
     */
    public void setIndexOfPage(int indexOfPage) {
        this.indexOfPage = indexOfPage;
    }

    /**
     * @param isInVirtualMemory the isInVirtualMemory to set
     */
    public void setIsInVirtualMemory(boolean isInVirtualMemory) {
        this.isInVirtualMemory = isInVirtualMemory;
    }

    /**
     * @param extraValue the extraValue to set
     */
    public void setExtraValue(int extraValue) {
        this.extraValue = extraValue;
    }
    
}
