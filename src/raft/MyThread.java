package raft;


import gui.RAFTGui;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MyThread implements Runnable {
    private int id;
    private RaftMember member;
    private RAFTGui raftGui;
    private ClientMessage clientMessages;
    private LogMessageProtocol logMessageProtocol;

    public MyThread(int id1, RAFTGui raftGui) {
        id = id1;
        
        this.raftGui = raftGui;

        try {
            member = new RaftMember(id, raftGui);
        } catch (SocketException | UnknownHostException ex) {
            Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        clientMessages = new ClientMessage(member);
        logMessageProtocol = new LogMessageProtocol(member);
        
    }
	
    public void run() {
        String receiveCommand ;
        int count = 0;
       
        while(true){
            
            //HALTS SERVER EXECUTION WHEN USER PRESSES PAUSE IN GUI
            refreshGui();
            while (this.raftGui.statusLabel.getText().equals("Stoped")){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if(member.state == 1){ 
                //System.out.println("Eu fui eleito Lider!! ->" + id);
                if(count == 10000 ){ // O CODIGO DENTRO DESTE IF É SÓ PARA DEITAR O LIDER ATUAL A BAIXO E FORÇAR REELEIÇÃO - PODES APAGAR SE NÃO PRECISARES
                    count = 0;
                    try {
                        //System.out.println("Thread: "+id+" DORMIR");
                        Thread.sleep(500);
                        //System.out.println("Thread: "+id+" acordei");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                count ++;
                if( (System.nanoTime() - member.lastHeartBeat) > 5e7 ){ // MANDA HEARTBEATS A CADA 5e-2 segundos
                    Messages.heartBeat(member,member.clusterSize);
                }
            }
            
            for(int i = 0; (i < member.clusterSize + 3) && (member.state == 1); i++){
                receive();
            }
            
            receive();
            
            if(member.state == 1){
                member.logManager.updateFollowersLogs();
            }
        }
    }
    
    public void receive(){
        String receiveCommand ;
        receiveCommand = member.receiveMessage(); // OS COMANDO TÊM A FORMA id(de quem enviou)-term-tipoMsg- METE "-" no final na mesma, faz melhor cat
        if(receiveCommand != null){
            String[] parts = receiveCommand.split("-");
                
            if(Integer.parseInt(parts[2]) < 100){
                receiveCommand = Messages.processMessage(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), member); // SE FOR UM COMANDO QUE REQUER RESPOSTA ESTA VAI ESTAR CONTIDA EM receiveCommand, CASO CONTRARIO ELA É NULL 
                if(receiveCommand != null){
                    String[] portMsgSplit = receiveCommand.split(":"); // A RESPSOTA É FEITA NO SEGUINTE FORMATO id(para quem enviamos):id(o nosso)-term-type-
                    member.sendMessage(Integer.parseInt(portMsgSplit[0]),portMsgSplit[1].getBytes());
                }
            }else if(Integer.parseInt(parts[2]) == 100){
                receiveCommand = clientMessages.processMessage(Integer.parseInt(parts[0]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), parts[4].toString());
                if(receiveCommand != null){
                    member.sendMessage(Integer.parseInt(parts[0]), receiveCommand.getBytes());
                }
            }else {
                System.out.println(receiveCommand);
                logMessageProtocol.processMessage(receiveCommand);
            }
        }
    }
    
    
    /**
     * updates the label in the gui that displays the current state of the server
     */
    public void refreshGui(){
        raftGui.currentLeader.setText(Integer.toString(member.leaderId));
        switch (member.state) {
            case 0:
                raftGui.currentState.setText("FOLLOWER");
                break;
            case 1:
                raftGui.currentState.setText("LIDER");
                break;
            default:
                raftGui.currentState.setText("CANDIDATE");
                break;
        }
        raftGui.currentTerm.setText(Integer.toString(member.currentTerm));
    }
}