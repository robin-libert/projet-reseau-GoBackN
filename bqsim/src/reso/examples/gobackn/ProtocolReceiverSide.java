package reso.examples.gobackn;

import java.util.Random;
import static reso.examples.gobackn.Protocol.IP_PROTO_GOBACKN;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;

 /**
  * Dans cette classe est implémenté le protocol gobackn du côté de celui qui reçoit les messages.
  * Cette classe représente le receveur dans le protocole GoBackN : quand elle recoit un message, elle renvoie un ack pour transmettre a la classe ProtocolSenderSide
  * que le message a bien eté recu. Pour pouvoir expérimenter le problème de congestion un certain nombre de ack ne sera pas renvoyé.
  */
public class ProtocolReceiverSide extends Protocol{
    private int currentSeqNum;
    private int expectedSeqNum;
    private Random r;
    private int proba;
    
    /**
     * Constructeur de la classe.
     * @param host
     * @param proba
     */
    public ProtocolReceiverSide(IPHost host, int proba){
        super(host);
        this.currentSeqNum = -1;
        this.expectedSeqNum = 0;
        this.r=new Random();
        this.proba = proba;
    }
    
    
    /**
     * Décris les différents comportements du receveur lorsque cette classe recoit un message. 
     * @param src
     * @param datagram
     * @throws Exception
     */ 
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        if(!msg.isAck){//Du coté du receveur, nous allons seulement recevoir des messages
            //Quand on reçoit un message, on renvois un ack.
            if(msg.seqNum == -1){//Quand le numéro de séquence vaut -1, on établit la connexion.
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
