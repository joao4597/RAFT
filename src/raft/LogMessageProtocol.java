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
    
}
