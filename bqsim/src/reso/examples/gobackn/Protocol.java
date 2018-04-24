package reso.examples.gobackn;


import java.util.Random;
import reso.ip.Datagram;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class Protocol implements IPInterfaceListener{
    public static final int IP_PROTO_GOBACKN = Datagram.allocateProtocolNumber("GO-BACK-N");
    protected final IPHost host;
    protected Random r;

    public Protocol(IPHost host) {
        this.host = host;
        this.r = new Random();
    }
    
    
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        /*GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        System.out.println(msg);
        if(!msg.isAck && r.nextInt(10) != 11){//si on reçoit un message, on renvoi un acquittement
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(msg.seqNum, true));
        }else if(msg.isAck && msg.seqNum < Protocol.packages.size()){//si on reçoit un acquittement, on envoi le message suivant si il y a encore des messages dans la liste.
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(Protocol.packages.get(msg.seqNum),msg.seqNum + 1, false));
        }*/
    }
	
}