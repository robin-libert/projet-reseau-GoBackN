package reso.examples.gobackn;


import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class ProtocolGoBackN implements IPInterfaceListener{
    public static final int IP_PROTO_GOBACKN = Datagram.allocateProtocolNumber("GO-BACK-N");
    private final IPHost host;

    public ProtocolGoBackN(IPHost host) {
        this.host = host;
    }
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        MessageGoBackN msg = (MessageGoBackN) datagram.getPayload();
        if(!msg.isAck && msg.num > 0){
            System.out.println("GoBackN message, counter=" + msg.num);
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new MessageGoBackN(msg.num - 1, true));
        }else if(msg.num > 0){
            System.out.println("GoBackN ack");
            host.getIPLayer().send(IPAddress.ANY, datagram.src, IP_PROTO_GOBACKN, new MessageGoBackN(msg.num, false));
        }
    }
	
}