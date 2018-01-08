/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raft;

/**
 *
 * @author joao
 */
public class LogMessageProtocol {
    
    RaftMember member;
    
    public LogMessageProtocol(RaftMember member){
        this.member = member;
    }
    
    public void processMessage(String message) {
        String[] messageSplit = message.split("-");
        
        //VERIFIES IF RECEIVED MESSAGE ORIGINATED IN CURRENT TERM AND IN CURRENT LEADER
        if(/*(Integer.parseInt(messageSplit[0]) != member.leaderId)  || */(Integer.parseInt(messageSplit[1]) != member.currentTerm)){
            return;
        }

        if(Integer.parseInt(messageSplit[2]) == 101){
            appendRequestReceived(messageSplit[3], messageSplit[4]);
        }else if(Integer.parseInt(messageSplit[2]) == 102){
            appendConfirmationReceived(Integer.parseInt(messageSplit[0]), messageSplit[3]);
        }else if(Integer.parseInt(messageSplit[2]) == 103){
            appendRejectionReceived(Integer.parseInt(messageSplit[0]), messageSplit[3]);
        }
    }
    
    /**
     * called when asked to append log entry
     * @param command1 previous command
     * @param command2 command to be appended
     */
    public void appendRequestReceived(String command1, String command2){
        String[] split1 =  command1.split(":");
        String[] split2 =  command2.split(":");
        
        if(member.logManager.checkIfEntryExists(Integer.parseInt(split1[0]), Integer.parseInt(split1[1]), Integer.parseInt(split1[2]), split1[3])){
            member.logManager.addLogEntry(Integer.parseInt(split2[1]), split2[3] ,Integer.parseInt(split2[2]));
            sendAppendConfirmation(command2);
        }else{
            sendAppendRejection(command2);
        }
    }
    
    /**
     * send Message to leader confirming log entry has been successfully appended
     * @param logEntry 
     */
    public void sendAppendConfirmation(String logEntry){
        String message = member.id + "-" + member.currentTerm + "-" + "102" + "-" + logEntry + "-";
        member.sendMessage(member.leaderId, message.getBytes());
    }
    
    /**
     * send message to leader rejecting log entry
     * @param logEntry 
     */
    public void sendAppendRejection(String logEntry){
        String message = member.id + "-" + member.currentTerm + "-" + "103" + "-" + logEntry + "-";
        member.sendMessage(member.leaderId, message.getBytes());
    }
    
    
    public void appendConfirmationReceived(int id, String message){
        String[] split = message.split(":");
        member.logManager.updateFollowersNextIndex(id, Integer.parseInt(split[0]) + 1);
        
        int serialNumber = member.logManager.addReplica(Integer.parseInt(split[0]) - 1);
        
        if (serialNumber == -1){
            return;
        }
        
        String response = "Accepted-" + Integer.toString(serialNumber) + "-";
        member.sendMessage(4100, response.getBytes());
    }
    
    public void appendRejectionReceived(int id, String message){
        String[] split = message.split(":");
        member.logManager.updateFollowersNextIndex(id, Integer.parseInt(split[0]) - 1);
    }
}
