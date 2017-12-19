/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package raft;

/**
 *
 * @author Rafaz
 */
public class Raft {
    public static void main(String[] args)
    {
        int i = 0;
        for(i = 4012;i <= 4021; i++)
        {
            //MyThread t = new MyThread(i);
            Thread t = new Thread(new MyThread(i));
            t.start();
        }
    }
    
}
