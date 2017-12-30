
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
    public int entryNumber = 0;
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
        
        addLogEntry(0, "I was Just Born", 0);

    }
    
    public boolean checkIfEntryExists(int index, int term, int serial, String command){
        for(int i = logList.size() - 1; i >= 0; i--){
            if(logList.get(i).entryNumber == index){
                if(logList.get(i).termNumber == term){
                    if(logList.get(i).serialNumber() == serial){
                        if(command.equals(logList.get(i).command)){
                            
                            for(int x = logList.size() - 1; x > i; x--){
                                logList.remove(x);
                                entryNumber--;
                                System.out.println(i + "elemente deleted");
                            }
                                System.out.println(serial + "-> serial Exists");
                            return true;
                        }
                    }
                }
            }   
        }
        return false;
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
        followers.clear();
        for(int i = 0; i < clusterSize; i++){
            if(member.id != (firstPort + i)){
                followers.add(new FollowersInfo(firstPort + i, entryNumber + 1));
            }
        }
    }
    
    public void updateFollowersNextIndex(int id, int nextIndex){
        for(int i = 0; i < clusterSize - 1; i++){
            if(followers.get(i).id  == id){
                followers.get(i).nextLogIndex = nextIndex;
                return;
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
            if(followers.get(i).nextLogIndex <= (entryNumber)){
                sendAppendRequestToFollower(followers.get(i).nextLogIndex, followers.get(i).id);
            }
        }
    }
    
    public void sendAppendRequestToFollower(int entryToSend, int followerID){
        String myID = Integer.toString(member.id);
        String currentTerm = Integer.toString(member.currentTerm );
        String type = "101";
                
        //INFORMATION RELATIVE TO THE PRIVIOUS COMMAND, ASSUMED TO BE ALREADY STORED IN THE FOLLOWERS LOG
        String prevEntryIndex = Integer.toString(entryToSend - 1);
        String prevEntryTerm = Integer.toString(logList.get(entryToSend - 2).termNumber);
        String prevSerialNumber = Integer.toString(logList.get(entryToSend - 2).serialNumber());
        String prevCommand = logList.get(entryToSend - 2).command;
                
        //LOG ENTRY TO BE APPENDED IN FOLLOWERS LOG
        String entryIndex = Integer.toString(entryToSend);
        String entryTerm = Integer.toString(logList.get(entryToSend - 1).termNumber);
        String serialNumber = Integer.toString(logList.get(entryToSend - 1).serialNumber());
        String command = logList.get(entryToSend - 1).command;
                
        //SETUP STRING TO SEND TO FOLLOWER
        String message = myID + "-" + currentTerm + "-" + type + "-" 
            + prevEntryIndex + ":" + prevEntryTerm + ":" + prevSerialNumber + ":" + prevCommand + ":-"
            + entryIndex + ":" + entryTerm + ":" + serialNumber + ":" + command + ":-";
                
        //SEND STRING TO FOLLOWER
        member.sendMessage(followerID, message.getBytes());
    }
    
    /**
     * verifies if serial number exists in log
     * to avoid entry repetition
     * @param serial serial to check
     * @return entry number if serial exists, -1 otherwise
     */
    public int verifySerialExistance(int serial){
        for(int i = 0; i < logList.size(); i++){
            if (logList.get(i).serialNumber() == serial)
                return logList.get(i).entryNumber;
        }
        return -1;
    }
    
}