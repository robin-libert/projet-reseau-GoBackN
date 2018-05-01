package reso.examples.gobackn;

import java.util.ArrayList;
import java.util.Random;
import reso.common.AbstractTimer;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;

public class ProtocolSenderSide extends Protocol{
    private int sendBase, nextSeqNum;
    private int sendingWindowSize;
    private ArrayList<Integer> packages;
    private AbstractTimer timer;
    private Scheduler scheduler;
    private Random r;
    private IPAddress dst;
    private int lastAck;
    private int proba = 10;
    private int[] congestionTest;
    private int flagCongestion=0;
    
    public ProtocolSenderSide(IPHost host, int size){
        super(host);
        this.scheduler = (Scheduler)host.getNetwork().getScheduler();
        this.sendingWindowSize = size;
        this.r=new Random();
        this.congestionTest= new int[3];
    }
    
    public void loadMessages(ArrayList<Integer> messages){
        packages = messages;
    }
    
    /**
     * Quand l'application sender reçoit un ack.
     * @param src
     * @param datagram
     * @throws Exception 
     */
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        //On initialise la connexion.
        if(msg.isAck && msg.seqNum == -1){//Ack initial
            System.out.println("Connexion établie.");
            this.dst = datagram.src;
            this.sendBase = 0;
            this.nextSeqNum = 0;
            this.timer = new MyTimer(host.getNetwork().getScheduler(),3.0);
            this.lastAck = -1;
            send();
      
        }
        if(r.nextInt(100)>=proba){//Pourcentage de chance de ne pas recevoir le ack
            if(msg.isAck && msg.seqNum != -1){//Quand on reçoit un ack normal, on incrémente sendBase
                

                
                
                congestionTest[flagCongestion]=msg.seqNum;
                flagCongestion=(flagCongestion+1)%3;
                
                //for(int i=0;i<=2;i++)
                //    System.out.println("element d indice "+i+" = "+congestionTest[i]);
                
                if(congestionTest[0]==congestionTest[1]&&congestionTest[0]==congestionTest[2]&&congestionTest[0]!=0){
                    System.out.println("============CONGESTION========");
                    //System.out.println(5/2);
                    //sendingWindowSize=sendingWindowSize/2;
                    //System.out.println(sendingWindowSize);
                }
                if(!(congestionTest[0]==congestionTest[1]&&congestionTest[0]==congestionTest[2]&&congestionTest[0]!=0))//pas de signe de congestion
                    //additiveIncrease();
                
                
                
                System.out.println(msg);
                this.lastAck = msg.seqNum;
                //Quand on reçoit ack(0), ça veut dire que sendBase augmente et vaut 1.
                //Si on perd des ack, on risque de recevoir ack(3) directement après ack(0). Donc sendBase vaudra le seqNum de l'ack + 1.
                if(msg.seqNum + 1 < packages.size())
                    this.sendBase = msg.seqNum + 1;
                if(this.sendBase == this.nextSeqNum){
                    stopTimer();
                    send();
                }else{
                    startTimer();
                }
                
            }
        }
        
    }
    
 public void send() throws Exception{
        GoBackNMsg msg = new GoBackNMsg(packages.get(this.nextSeqNum),this.nextSeqNum, false);
        //System.out.println("On est dans la methode send() , sendingWindowSize = "+sendingWindowSize);
        if(nextSeqNum < sendBase + sendingWindowSize && this.nextSeqNum < packages.size()){//si nextSeqNum est dans la fenêtre et qu'il reste des messages dans la liste
            host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_GOBACKN, msg);
            if(this.sendBase == this.nextSeqNum){
                startTimer();
            }
            this.nextSeqNum ++;
            send();
        }
    }
 
 public void additiveIncrease(){
     
     sendingWindowSize=sendingWindowSize+1;
     System.out.println("additiveIncrease, le sendingWindowSize = " +sendingWindowSize);
 }
 
    
    public void startTimer(){
        //System.out.println("start");
        stopTimer();
        this.timer = new MyTimer(host.getNetwork().getScheduler(),3.0);
        this.timer.start();
    }
    
    public void stopTimer(){
        //System.out.println("stop");
        if(this.timer.isRunning())
            this.timer.stop();
    }
    
    public void timeout() throws Exception{
        if(lastAck+1 < packages.size()){
            this.sendBase = this.lastAck + 1;
            this.nextSeqNum = this.lastAck + 1;
            startTimer();
            send();
        }
    }
    
    private class MyTimer extends AbstractTimer{
        public MyTimer(AbstractScheduler scheduler, double interval){
            super(scheduler, interval, false);
        }

        @Override
        protected void run() throws Exception {
            this.stop();
            System.out.println("timeout=" + scheduler.getCurrentTime());
            timeout();
        }
    }
    
}
