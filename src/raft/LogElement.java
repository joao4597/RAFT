/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raft;

import javax.swing.JLabel;

/**
 * This class stores each element of the log
 * stores the element in array format used
 * to add line to table
 *
 * @author joao
 */
public class LogElement {
    public int entryNumber = 0;
    public int termNumber = 0;
    public String command = null;
    private int serialNumber;
    private int replicas = 0;
    
    private Object[] element = null;
    
    public LogElement(int entryNumber, int termNumber, String command, int serialNumber){
        this.entryNumber = entryNumber;
        this.termNumber = termNumber;
        this.command = command;
        this.serialNumber = serialNumber;
        
        Object[] auxObject = {Integer.toString(entryNumber), Integer.toString(termNumber), command};
        
        this.element = auxObject;
    }
    
    /**
     * 
     * @return element in array form;
     */
    public Object[] returnLogElement(){
        return element;
    }
    
    public void addReplica(){
        replicas++;
    }
    
    public int replicas(){
        return replicas;
    }
    
    public int serialNumber(){
        return serialNumber;
    }
}


