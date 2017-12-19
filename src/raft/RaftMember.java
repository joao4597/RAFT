package raft;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
/**
 *
 * @author Rafaz
 */
public class RaftMember {
    public int id;
    public DatagramSocket receiveSocket;
    public DatagramSocket sendSocket;
    public int leaderId;
    public InetAddress IPAddress;
    public int voteCount; //variavel para contar votos
    public int state; // 0 - follower , 1 - leaderId , 2 - candidate 
    public int currentTerm; // Mandato atual
    private float timeOutMean = 250; // Media do tempo de espera
    public int clusterSize = 10; // numero de nós
    private long startTime; // tempo em que se começou a votação
    public int voteGranted; // Em vez de ser booleano, este valor tem o term maximo em que votou
    public long lastHeartBeat;
    
    public RaftMember(int newid) throws SocketException, UnknownHostException
    {
        id = newid;
        
        Random randomGenerator = new Random();
        IPAddress = InetAddress.getByName("localhost");
        receiveSocket = new DatagramSocket(newid,IPAddress);
        float randomVal = randomGenerator.nextFloat();
        float timeout = (float) (timeOutMean+ ((randomVal)*0.8 - 0.4)*timeOutMean);
        receiveSocket.setSoTimeout((int)timeout/1);
        System.out.println("Nasci ID: "+id+" com timeout: "+timeout);
        sendSocket = new DatagramSocket();
        state = 0;
        leaderId = -1;
        currentTerm = 0;
        voteCount = 0;
        voteGranted = 0;
    }
    
    /**
     *
     * @param port
     * @param sendData
     * @throws UnknownHostException
     * @throws SocketException
     */
    public void sendMessage(int port,byte sendData[]) 
    {
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        
        try {
            sendSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(RaftMember.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public String receiveMessage()
    {
        byte receiveData[] = new byte[1024];
        String commandReceived;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        Random randomGenerator = new Random();
        float randomVal = randomGenerator.nextFloat();
        float timeout = (float) (timeOutMean+ ((randomVal)*0.8 - 0.4)*timeOutMean); // TIMEOUT ENTRE 150ms e 300ms
        if(id == leaderId)
            timeout = 10;
        try {
            receiveSocket.setSoTimeout((int)timeout/1);
            receiveSocket.receive(receivePacket);
            commandReceived = new String(receivePacket.getData());
            return commandReceived; 
            
        } 
        catch (IOException ex) 
        {
          if(leaderId != id)   
          {
              if(state == 0) // TIMEOUT DE HEARTBEATS
                startVote();
              
              else if(state == 2)
                  if((System.nanoTime() - startTime) > (long)((randomVal/2.0 + 0.5) * 1e8)/1) // timeout à espera de voto
                      startVote();
          }
          
        }
        return null ;
        
    }
    
    public void broadCast(byte sendData[],int number)
    {
        for(int i=1;i<=number;i++)
        {
            if(i != id)
            {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, i);
                try {
                    sendSocket.send(sendPacket);
                } catch (IOException ex) {
                    Logger.getLogger(RaftMember.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    private void startVote() // INICIA VOTAÇãO
    {
        state = 2; // candidate
        currentTerm ++; // INCREMENTA MANDATO
        voteCount ++; // VOTA NELE PROPRIO
        voteGranted = currentTerm; // DIZ QUE JÁ VOTOU NO MANDATO ATUAL CASO RECEBA OUTROS PEDIDOS DE VOTO
        System.out.println("Eu "+id+" Vou Iniciar Votação ");
        String tmp = new String(id+"-"+currentTerm+"-2-");
        broadCast(tmp.getBytes(),clusterSize);
        leaderId = id; // METE O ID DO LIDER IGUAL AO SEU
        startTime = System.nanoTime(); // INICIA TIMEOUT DE VOTAÇÃO
    }

    
}
