package raft;

import gui.RAFTGuiThread;

public class Raft {
    public static void main(String[] args){
        int i;
        int x;

        Thread gui[] = new Thread[5];
        
        for(i = 4000, x = 0; i < 4005; i++, x++){
            gui[x] = new Thread(new RAFTGuiThread(i));
            gui[x].start();
        }
    }
}
