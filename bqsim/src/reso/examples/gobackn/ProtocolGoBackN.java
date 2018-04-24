package reso.examples.gobackn;


import java.util.ArrayList;
import java.util.Random;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class ProtocolGoBackN implements IPInterfaceListener{
    public static final int IP_PROTO_GOBACKN = Datagram.allocateProtocolNumber("GO-BACK-N");
    private final IPHost host;
    private Random r;
    private static ArrayList<Integer> packages = new ArrayList<>();

    public ProtocolGoBackN(IPHost host) {
        this.host = host;
        this.r = new Random();
    }
    
    public void loadMessages(ArrayList<Integer> messages){
        ProtocolGoBackN.packages = messages;
    }
    
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        MessageGoBackN msg = (MessageGoBackN) datagram.getPayload();
        System.out.println(msg);
        if(!msg.isAck && r.nextInt(10) != 11){//si on reçoit un message, on renvoi un acquittement
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new MessageGoBackN(msg.seqNum, true));
        }else if(msg.isAck && msg.seqNum < ProtocolGoBackN.packages.size()){//si on reçoit un acquittement, on envoi le message suivant si il y a encore des messages dans la liste.
            System.out.println("Numéro de séquence vaut: "+msg.seqNum);
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new MessageGoBackN(ProtocolGoBackN.packages.get(msg.seqNum),msg.seqNum + 1, false));
        }
    }
	
}