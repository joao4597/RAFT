package raft;

import gui.RAFTGuiThread;

public class Raft {
    public static void main(String[] args){
        int i;
        int x;
        int clusterSize = 31;

        Thread gui[] = new Thread[clusterSize];
        
        for(i = 4000, x = 0; i < 4000 + clusterSize; i++, x++){
            gui[x] = new Thread(new RAFTGuiThread(i));
            gui[x].start();
        }
    }
}
