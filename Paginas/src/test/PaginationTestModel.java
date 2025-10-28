/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;
import java.util.Random;
/**
 *
 * @author wesleyesquivel
 */
public class PaginationTestModel {

     private int codigo;
     private String pid;
     private String mark;
     
     
     private int getRandomPositiveNumber(int limit){
         Random rand = new Random();
         return rand.nextInt(limit);
     }
     
     public PaginationTestModel (){
        long seedNano = System.nanoTime();
        this.codigo = getRandomPositiveNumber(50);
        this.pid = "Test " + codigo;
        this.mark = "this could be used later";
     }
     
    public PaginationTestModel (PaginationTestModel copy){
        this.codigo = copy.codigo;
        this.pid = copy.pid;
        this.mark = copy.mark;
     }
     
    public String toString() {
        return " "+codigo+" ";
    }
    /**
     * @return the codigo
     */
    public int getCodigo() {
        return codigo;
    }

    /**
     * @param codigo the codigo to set
     */
    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    /**
     * @return the pid
     */
    public String getPid() {
        return pid;
    }

    /**
     * @param pid the pid to set
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    /**
     * @return the mark
     */
    public String getMark() {
        return mark;
    }

    /**
     * @param mark the mark to set
     */
    public void setMark(String mark) {
        this.mark = mark;
    }
}
