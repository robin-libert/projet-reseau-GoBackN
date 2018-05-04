package reso.examples.gobackn;

import java.util.Random;
import static reso.examples.gobackn.Protocol.IP_PROTO_GOBACKN;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;

public class ProtocolReceiverSide extends Protocol{
    private int currentSeqNum;
    private int expectedSeqNum;
    private Random r;
    private int proba;
    
    public ProtocolReceiverSide(IPHost host, int proba){
        super(host);
        this.currentSeqNum = -1;
        this.expectedSeqNum = 0;
        this.r=new Random();
        this.proba = proba;
    }
    
  @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        if(!msg.isAck){//Du coté du receveur, nous allons seulement recevoir des messages
            //Quand on reçoit un message, on renvois un ack.
            if(msg.seqNum == -1){//Quand le numéro de séquence vaut -1, on établi la connexion.
                System.out.println("Initialisation de la connexion ...");
                host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(msg.seqNum, true));
            }else{
                //Pourcentage des messages perdu.
                if(r.nextInt(100)>=proba){//On ne renvoit pas de ack pour simuler une perte de message
                    this.currentSeqNum = msg.seqNum;
                    if(this.currentSeqNum == this.expectedSeqNum){
                        System.out.println(msg);
                        host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(msg.seqNum, true));
                        this.expectedSeqNum++;
                    }else{
                        //Je renvois un ack pour dire que le dernier message reçu est le message avant celui attendu.
                        host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(this.expectedSeqNum-1, true));
                    }
                }
            }            
        }
    } 
}
