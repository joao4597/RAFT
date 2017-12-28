/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raft;
import gui.RAFTGui;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author joao
 */
public class LogManager {
    
    private int firstPort = 4000;
    private RAFTGui raftGui = null;
    private ArrayList<LogElement> logList = null;
    private JScrollPane scrollPane;
    private int entriesCommited;
    private RaftMember member;
    private int clusterSize;
    private int entryNumber = 0;
    private ArrayList<FollowersInfo> followers;
    
    DefaultTableModel model;
    JTable table;
    
    public LogManager(RAFTGui raftGui, int clusterSize, RaftMember member){
        
        entriesCommited = 0;
        
        //LIST CAONATINENG EACH LOG ENTRY
        this.logList = new ArrayList();
        
        this.clusterSize = clusterSize;
        this.member = member;
        
        //CONTAINS ID AND NEXT LOG INDEX OF EACH FOLLOWER
        followers = new ArrayList();
        
        //SAVE GUI OBJECT
        this.raftGui = raftGui;
        
        //CREAT TALBLE AND ADD IT TO GUI
        this.model = new DefaultTableModel();
        this.table = new JTable(model);

        model.addColumn("Index");
        model.addColumn("Term");
        model.addColumn("Command");
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setMaxWidth(40);
        
        scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        raftGui.logContainer.add(scrollPane);

    }
    
    /**
     * used to add new log entries
     * @param entryNumber 
     * @param termNumber term were entry was added 
     * @param command command to be executed
     * @param serialNumber serial Number of entry to avoid repetition
     */
    public void addLogEntry(int termNumber, String command, int serialNumber){
        entryNumber++;
        LogElement logElement = new LogElement(entryNumber, termNumber, command, serialNumber);
        logElement.addReplica();
        logList.add(logElement);
        this.updateTable();
    }
    
    /**
     * updates the index up to which log entries have been commited
     * @param index index to commit
     */
    public void updateCommitedIndex(int index){
        entriesCommited = index;
        this.updateTable();
    }
    
    /**
     * updates table in gui
     */
    public void updateTable(){
        int linesMissing = logList.size() - model.getRowCount();
        
        for(int i = logList.size() - linesMissing ; i < logList.size(); i++){
            model.addRow(logList.get(i).returnLogElement());
        }
    }
    
    /**
     * increments the number of replicas for a given log entry
     * @param index log entry index
     */
    public void addReplica(int index){
        logList.get(index).addReplica();
        if (logList.get(index).replicas() >= (clusterSize / 2)){
        } else {
            updateCommitedIndex(index);
        }
    }
    
    /**
     * stores each of the followers info
     * stores it's ID and stores the next index in the log
     */
    public void setUpFolloersInfo(){
        for(int i = 0; i < clusterSize - 1; i++){
            if(member.id != (firstPort + i)){
                followers.add(new FollowersInfo(firstPort + i, entryNumber + 1));
            }
        }
    }
    
    /**
     * if member state id leader
     * this method is used to check to update the followers logs
     * in case they refuse new entry due to having outdated logs
     */
    public void updateFollowersLogs(){
        
        for(int i = 0; i < clusterSize - 1; i++){
            
            //FALLS ON THIS IF IN CASE LIDER THINKS ONLY LAST ENTRY IS MISSING IN FOLLOWERS LOG
            ////////////This is to be put in a separete method/////////////////////////////////
            if(followers.get(i).nextLogIndex == (entryNumber)){
                String myID = Integer.toString(member.id);
                String currentTerm = Integer.toString(member.currentTerm );
                String type = "101";
                String entryIndex = Integer.toString(entryNumber - 1);
                String entryTerm = Integer.toString(logList.get(entryNumber).termNumber);
                String command = logList.get(entryNumber).command;
                
                String message = myID + "-" + currentTerm + "-" + type + "-" + entryIndex + ":" + entryTerm + ":" + command + ":-";
                member.sendMessage(followers.get(i).id, message.getBytes());
            }
        }
            
    }
    
    /**
     * verifies if serial number exists in log
     * to avoid entry repetition
     * @param serial serial to check
     * @return true if serial already exists, false otherwise
     */
    public boolean verifySerialExistance(int serial){
        for(int i = 0; i < logList.size(); i++){
            if (logList.get(i).serialNumber() == serial)
                return true;
        }
        return false;
    }
    
}
