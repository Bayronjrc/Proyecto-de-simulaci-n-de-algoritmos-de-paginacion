/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the symbol table (Memory Map) of the MMU.
 * This class implements the requirement from the PDF: "The MMU must have a memory
 * map that relates each (ptr) to a list of pages."
 *
 *
 * this class tracks which pages make up a pointer in a full omniscient way.
 */
public class SymbolTable {

    /**
     * The core memory map.
     * Key (Integer): The pointer ID (ptr).
     * Value (List<Page>): The list of pages assigned to that pointer.
     */
    private final Map<Integer, List<Page>> pointerPageMap;

    /**A global counter to ensure pointer IDs are unique. */
    private int nextPointerId;

    
    
    /**
     * Constructor.
     */
    public SymbolTable() {
        this.pointerPageMap = new HashMap<>();
        // We start pointers at 1 (or any number > 0) for clarity.
        this.nextPointerId = 1;
    }

    /**
     * Registers a new set of pages and assigns it a new and
     * unique pointer ID.
     * This method is called during a new operation.
     *
     * @param pages The list of pages that were created for this request.
     * @return The ID of the newly assigned pointer (ptr).
     */
    public int registerNewPointer(List<Page> pages) {
        int newPtrId = this.nextPointerId;
        this.nextPointerId++; // Increment for the next request.

        this.pointerPageMap.put(newPtrId, new ArrayList<>(pages));

        return newPtrId;
    }

    /**
     * Gets the list of pages associated with a pointer (ptr).
     * This method is called during a 'use' or 'delete' operation.
     *
     * @param ptr The ID of the pointer to look up.
     * @return The list of pages (List<Page>) or null if the pointer does not exist.
     */
    public List<Page> getPages(int ptr) {
        return this.pointerPageMap.get(ptr);
    }

    /**
     * Removes a pointer from the memory map.
     * This method is called during a 'delete' operation.
     *
     * @param ptr The ID of the pointer to remove.
     * @return The list of pages that were associated with that pointer (so that
     * the MMU can free them), or null if the pointer did not exist.
     */
    public List<Page> removePointer(int ptr) {
        return this.pointerPageMap.remove(ptr);
    }

    /**
     * Checks if a pointer (ptr) exists in the map.
     *
     * @param ptr The ID of the pointer to check.
     * @return true if the pointer is registered, false otherwise.
     */
    public boolean pointerExists(int ptr) {
        return this.pointerPageMap.containsKey(ptr);
    }

    /**
     * Clears the entire symbol table.
     * Useful for resetting the simulation later.
     */
    public void clear() {
        this.pointerPageMap.clear();
        this.nextPointerId = 1;
    }

    /**
     * Returns a list of all currently active pointers (ptr).
     *
     * @return A list of pointer IDs (Integers).
     */
    public List<Integer> getAllActivePointers() {
        return new ArrayList<>(this.pointerPageMap.keySet());
    }
}