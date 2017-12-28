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
public class FollowersInfo {
    public int id;
    public int nextLogIndex;
    
    public FollowersInfo(int id, int nextLogIndex){
        this.id = id;
        this.nextLogIndex = nextLogIndex;
    }
}
