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
    private static ArrayList<Integer> packages = new ArrayList<>();
    private AbstractTimer timer;
    private Scheduler scheduler;
    private Random r;
    private int currentSeqNum;
    
    public ProtocolSenderSide(IPHost host){
        super(host);
        this.scheduler = (Scheduler)host.getNetwork().getScheduler();
        this.sendingWindowSize = 4;
        this.r=new Random();
        
    }
    
    public void loadMessages(ArrayList<Integer> messages){
        ProtocolSenderSide.packages = messages;
    }
    
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        
        
        if(msg.isAck && msg.seqNum == -1){//Ack initial
            System.out.println("Connexion établie.");
            this.sendBase = 0;
            this.nextSeqNum = 0;
            this.currentSeqNum=0;
            this.timer = new MyTimer(host.getNetwork().getScheduler(),1.0,src,datagram);
        }
        
        if(msg.isAck && msg.seqNum != -1){//Quand on reçoit un ack normal, on incrémente sendBase
            System.out.println(msg);
            //Quand on reçoit ack(0), ça veut dire que sendBase augmente et vaut 1.
            //Si on perd des ack, on risque de recevoir ack(3) directement après ack(0). Donc sendBase vaudra le seqNum de l'ack + 1.
            this.sendBase = msg.seqNum + 1;
            
           
            if(this.sendBase == this.nextSeqNum){
                this.timer.stop();
            }else{
                this.currentSeqNum=msg.seqNum;
                this.timer.start();
                
            }
        }
        
        while(nextSeqNum < sendBase + sendingWindowSize && this.nextSeqNum < ProtocolSenderSide.packages.size()){//si nextSeqNum est dans la fenêtre et qu'il reste des messages dans la liste
            
            if( r.nextInt(10)!=7){
                host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(ProtocolSenderSide.packages.get(this.nextSeqNum),this.nextSeqNum, false));
            }
                if(this.sendBase == this.nextSeqNum){
                    this.currentSeqNum=msg.seqNum;
                    this.timer.start();
                }
                this.nextSeqNum ++;
            
        }
        
    }
    
    public void timeout(IPInterfaceAdapter src, Datagram datagram) throws Exception{
        //nextSeqNum=currentSeqNum;
        currentSeqNum=nextSeqNum-1;
        nextSeqNum=currentSeqNum;
        
        this.timer.start();
        for(int i = this.sendBase; i < this.nextSeqNum;i++){
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(ProtocolSenderSide.packages.get(i),i, false));
        }
    }
    
    private class MyTimer extends AbstractTimer{
        private IPInterfaceAdapter src;
        private Datagram datagram;
        public MyTimer(AbstractScheduler scheduler, double interval,IPInterfaceAdapter src, Datagram datagram){
            super(scheduler, interval, false);
            this.src = src;
            this.datagram = datagram;
        }

        @Override
        protected void run() throws Exception {
            System.out.println( "time=" + scheduler.getCurrentTime());
            timeout(this.src, this.datagram);
        }
    }
    
}
