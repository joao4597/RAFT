package raft;


import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Rafaz
 */
public class MyThread implements Runnable {

   private int id;
   
   private RaftMember member;
   public MyThread(int id1) {
       id = id1;
   }

   public void run() 
   {
       String receiveCommand ;
       int count = 0;
       try {
           member = new RaftMember(id);
       } catch (SocketException | UnknownHostException ex) {
           Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
       }
       
       while(true)
       {
            if(member.state == 1)
            {  
            if(count == 50 ) // O CODIGO DENTRO DESTE IF É SÓ PARA DEITAR O LIDER ATUAL A BAIXO E FORÇAR REELEIÇÃO - PODES APAGAR SE NÃO PRECISARES
            {

                count = 0;
                try 
                {
                    System.out.println("Thread: "+id+" DORMIRRRRRRRRRRRRRRRRRRRRR");
                    Thread.sleep(500);
                    System.out.println("Thread: "+id+" acordei");
                } catch (InterruptedException ex) 
                {
                    Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            count ++;
            if( (System.nanoTime() - member.lastHeartBeat) > 5e7 ) // MANDA HEARTBEATS A CADA 5e-2 segundos
                Messages.heartBeat(member,member.clusterSize);
            } 

            receiveCommand = member.receiveMessage(); // OS COMANDO TÊM A FORMA id(de quem enviou)-term-tipoMsg- METE "-" no final na mesma, faz melhor cat
            if(receiveCommand != null)
            {
                String[] parts = receiveCommand.split("-");

                receiveCommand = Messages.processMessage(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), member); // SE FOR UM COMANDO QUE REQUER RESPOSTA ESTA VAI ESTAR CONTIDA EM receiveCommand, CASO CONTRARIO ELA É NULL 
                if(receiveCommand != null)
                {
                    String[] portMsgSplit = receiveCommand.split(":"); // A RESPSOTA É FEITA NO SEGUINTE FORMATO id(para quem enviamos):id(o nosso)-term-type-
                    member.sendMessage(Integer.parseInt(portMsgSplit[0]),portMsgSplit[1].getBytes());

                }
            }
            

    
       }
   }
}