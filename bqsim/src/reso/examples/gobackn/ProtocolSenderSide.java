package reso.examples.gobackn;

import java.util.ArrayList;
import reso.common.AbstractTimer;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;

public class ProtocolSenderSide extends Protocol{
    private int sendBase, nextSeqNum;
    private int sendingWindowSize = 4;
    private static ArrayList<Integer> packages = new ArrayList<>();
    private MyTimer timer;
    private Scheduler scheduler;
    
    public ProtocolSenderSide(IPHost host){
        super(host);
        this.scheduler = (Scheduler)host.getNetwork().getScheduler();
        this.timer = new MyTimer(scheduler,1000);
    }
    
    public void loadMessages(ArrayList<Integer> messages){
        ProtocolSenderSide.packages = messages;
    }
    
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        System.out.println(msg);
        if(msg.isAck && msg.seqNum == -1){//Ack initial
            this.sendBase = 0;
            this.nextSeqNum = 0;
        }
        
        if(msg.isAck && msg.seqNum != -1){//Quand on reçoit un ack normal, on incrémente sendBase
            this.sendBase ++;
        }
        
        while(nextSeqNum < sendBase + sendingWindowSize){//si nextSeqNum est dans la fenêtre
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(ProtocolSenderSide.packages.get(this.nextSeqNum),this.nextSeqNum, false));
            /*if(this.sendBase == this.nextSeqNum){
                timer.start();
            }*/
            this.nextSeqNum ++;
        }
        
    }
    
    private class MyTimer extends AbstractTimer{
        public MyTimer(AbstractScheduler scheduler, int interval){
            super(scheduler, interval, false);
        }

        @Override
        protected void run() throws Exception {
            System.out.println("Timer expire");
        }
    }
    
}
