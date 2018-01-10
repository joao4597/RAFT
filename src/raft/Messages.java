package raft;

public final class Messages {

    public static String processMessage(int id, int term, int type, int logEntry, RaftMember raft) {

        String response;

        if(type == 0) { // RECEBI HEARTBEAT         
            if((id != raft.leaderId) && (term >= raft.currentTerm)) {
                //System.out.println("EU : "+raft.id+" MUDEI O LIDER DE : "+raft.leaderId+" PARA: "+id);
                raft.currentTerm = term;
                raft.leaderId = id;
                raft.state = 0;
                response = id+":"+raft.id+"-"+raft.currentTerm+"-1-";
            }
            else if((id == raft.leaderId) && (term == raft.currentTerm))
                response = id+":"+raft.id+"-"+raft.currentTerm+"-1-";
            else
                response = null;          
            //System.out.println("EU : "+raft.id+" RECEBI HEARTBEAT DE: "+id+" TENHO COMO LIDER: "+raft.leaderId+" MEU MANDATO: "+raft.currentTerm+" DO MANDATO: "+term+" RESPONSE: "+response);
            return response;
        }
        else if((type == 2) && (raft.currentTerm < term) ) { // RECEBI VOTE REQUEST
            if((raft.voteGranted < term) ){
                if(logEntry < raft.logManager.entryNumber){
                    raft.startVote();
                    return null;
                }
                raft.state = 0;
                raft.currentTerm = term;
                raft.leaderId = id;
                response = id+":"+raft.id+"-"+raft.currentTerm+"-3-";
                raft.voteGranted = term;
                return response;
            }
            return null;
        }
        else if ((type == 1) && ( raft.state == 1)) {// RECEBI RESPOSTA HEARTBEAT
            //System.out.println("Eu "+raft.id+"Recebi resposta de HEARTBEAT do :"+id);
            return null;
        }
        else if(type == 3) {
            raft.voteCount ++ ;
            if((raft.state == 2) && (raft.voteCount > raft.clusterSize/2)) {
                System.out.println("I " + raft.id + " was just elected leader vote count->" + raft.voteCount);
                heartBeat(raft, raft.clusterSize);
                raft.state = 1;
                raft.voteCount = 0;
                //raft.saveElectionTime(System.nanoTime() - raft.electionTimeMeasurement);
                raft.logManager.setUpFolloersInfo();/////////////////////////////////////////////
            }
        }
        ////////////////////////END OF LIDER ELECTION///////////////////////////
        return null;
    }

    public static void heartBeat(RaftMember raft,int number) {
        raft.lastHeartBeat = System.nanoTime();
        String response = new String(raft.id+"-"+raft.currentTerm+"-0-");
        raft.broadCast(response.getBytes(), number);
    }   
}
