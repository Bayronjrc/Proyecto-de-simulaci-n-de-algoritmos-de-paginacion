/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package paginas;
import test.CodigosTest;
/**
 *
 * @author wesleyesquivel
 */
public class Paginas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CodigosTest TestObject = new CodigosTest();
        int amountOfPages = 2000;
        int sizeOfFrame = 4;
        boolean printSteps = false;
        TestObject.Test(amountOfPages, sizeOfFrame, printSteps);
    }
    
}
