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
public class ClientMessage {
    
    private RaftMember member;
    
    public ClientMessage(RaftMember raftMember){
        this.member = raftMember;
    }

    public String processMessage(int id, int type, int serialNumber, String command) {

        if((type == 100) && (member.state == 1)) { // RECEBI HEARTBEAT
            if (member.logManager.verifySerialExistance(serialNumber) == -1){
                member.logManager.addLogEntry(member.currentTerm, command, serialNumber);
            }else{
                String response =  "Accepted-" + serialNumber + "-";
                member.sendMessage(4100, response.getBytes());
            }
        }

        return null;
    }
}
