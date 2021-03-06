package raft;

import gui.RAFTGui;
import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

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
    public int clusterSize = 5; // numero de nós
    private long startTime; // tempo em que se começou a votação
    public int voteGranted; // Em vez de ser booleano, este valor tem o term maximo em que votou
    public long lastHeartBeat;
    public LogManager logManager;
    public RAFTGui raftGui;
    
    public FileWriter electionTimeFile;
    
    public long electionTimeMeasurement;
    private double errorPercentage = 0.05;
    
    public RaftMember(int newid, RAFTGui raftGui) throws SocketException, UnknownHostException {
        id = newid;
        
        Random randomGenerator = new Random();
        IPAddress = InetAddress.getByName("localhost");
        receiveSocket = new DatagramSocket(newid,IPAddress);
        float randomVal = randomGenerator.nextFloat();
        float timeout = (float) (timeOutMean + ((randomVal)*0.8 - 0.4)*timeOutMean);
        receiveSocket.setSoTimeout((int)timeout/1);
        System.out.println("Nasci ID: "+id+" com timeout: "+timeout);
        sendSocket = new DatagramSocket();
        state = 0;
        leaderId = -1;
        currentTerm = 0;
        voteCount = 0;
        voteGranted = 0;
        
        
        //START GUI AND LOG MANAGER
        this.raftGui = raftGui;
        logManager = new LogManager(raftGui, clusterSize, this);
        logManager.setUpFolloersInfo();
        
        //CREAT FILE TO SAVE MEASUREMENTS
        //openFileElectionTime();
    }
    
    public void sendMessage(int port,byte sendData[]) {
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        
        Random randomGenerator = new Random();
        float randomVal = randomGenerator.nextFloat();
        if(randomVal < errorPercentage){
            return;
        }
        
        try {
            sendSocket.send(sendPacket);
        } catch (IOException ex) {
            Logger.getLogger(RaftMember.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
	
    public String receiveMessage() {
        byte receiveData[] = new byte[1024];
        String commandReceived;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        Random randomGenerator = new Random();
        float randomVal = randomGenerator.nextFloat();
        float timeout = (float) (timeOutMean+ ((randomVal)*0.8 - 0.4)*timeOutMean); // TIMEOUT ENTRE 150ms e 350ms
		
        if(id == leaderId)
            timeout = 2;
		
        try {
            receiveSocket.setSoTimeout((int)timeout/1);
            receiveSocket.receive(receivePacket);
            commandReceived = new String(receivePacket.getData());
            //System.out.println(commandReceived);
            return commandReceived;    
        } catch (IOException ex) {
            if(leaderId != id) {
                if(state == 0) // TIMEOUT DE HEARTBEATS
                    startVote();
              
                else if(state == 2) 
                    if((System.nanoTime() - startTime) > (long)((randomVal/2.0 + 0.5) * 1e8)/1) // timeout à espera de voto
                        startVote();
            }else if(state == 2) 
                    if((System.nanoTime() - startTime) > (long)((randomVal/2.0 + 0.5) * 1e8)/1) // timeout à espera de voto
                        startVote();
        }
        return null ;       
    }
    
    public void broadCast(byte sendData[],int number) {
        for(int i=4000; i < 4000 + clusterSize; i++){
            if(i != id) {
                Random randomGenerator = new Random();
                float randomVal = randomGenerator.nextFloat();
                if(randomVal < errorPercentage){
                    return;
                }   
                
                
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, i);
                try {
                    sendSocket.send(sendPacket);
                } catch (IOException ex) {
                    Logger.getLogger(RaftMember.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void startVote() { // INICIA VOTAÇãO
        state = 2; // candidate
        currentTerm ++; // INCREMENTA MANDATO
        voteCount = 1; // VOTA NELE PROPRIO
        voteGranted = currentTerm; // DIZ QUE JÁ VOTOU NO MANDATO ATUAL CASO RECEBA OUTROS PEDIDOS DE VOTO
        System.out.println("Eu "+id+" Vou Iniciar Votação, currente term ->" + currentTerm);
        String tmp = new String(id+"-"+currentTerm+"-2-" + logManager.entryNumber + "-");
        broadCast(tmp.getBytes(), clusterSize);
        leaderId = id; // METE O ID DO LIDER IGUAL AO SEU
        startTime = System.nanoTime(); // INICIA TIMEOUT DE VOTAÇÃO
        //electionTimeMeasurement = System.nanoTime();
    }
    
    public void clearReceiveSocket(){
        byte receiveData[] = new byte[1024];
        String commandReceived;
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        Random randomGenerator = new Random();
        int timeout = 1;
	
        while(true){
            try {
                receiveSocket.setSoTimeout((int)timeout/1);
                receiveSocket.receive(receivePacket);
                commandReceived = new String(receivePacket.getData());  
            } catch (IOException ex) {
                return;
            }
        }
    }
    
    public void openFileElectionTime(){
        try {
            File file = new File("electionTime" + Integer.toString(id) + ".txt");
            electionTimeFile = new FileWriter(file, true);
        } catch (IOException ex) {
            Logger.getLogger(RaftMember.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveElectionTime(long time){
        BufferedWriter out;
        out = new BufferedWriter(electionTimeFile);
        try {
            out.write(Long.toString(time) + "\n");
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(RaftMember.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
