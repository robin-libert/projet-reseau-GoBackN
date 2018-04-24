package reso.examples.gobackn;

import static reso.examples.gobackn.Protocol.IP_PROTO_GOBACKN;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class ProtocolReceiverSide extends Protocol{
    
    public ProtocolReceiverSide(IPHost host){
        super(host);
    }
    
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        if(!msg.isAck){//Du coté du receveur, nous allons seulement recevoir des messages
            System.out.println(msg);
            //Quand on reçoit un message, on renvois un ack.
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new GoBackNMsg(msg.seqNum, true));
        }
    }
    
}
