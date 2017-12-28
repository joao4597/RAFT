/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import javax.swing.SwingUtilities;
import raft.MyThread;

/**
 *
 * @author joao
 */
public class RAFTGuiThread implements Runnable{
    private RAFTGui raftGui = null;
    private int id;
    
    public RAFTGuiThread(int id){
        this.id = id;
    }
    
    @Override
    public void run(){
        //SETUP GRAPHICAL USER INTERFACE
        raftGui = new RAFTGui();
        raftGui.setVisible(true);
        raftGui.myID.setText(Integer.toString(id));
        
        Thread server = new Thread(new MyThread(id, raftGui));
        server.start();
        
        raftGui.setThread(server);
    }

}
