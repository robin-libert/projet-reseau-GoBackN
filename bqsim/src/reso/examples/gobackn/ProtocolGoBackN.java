package reso.examples.gobackn;


import reso.ip.Datagram;
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
        System.out.println("GoBackN (" + (int) (host.getNetwork().getScheduler().getCurrentTime() * 1000) + "ms)"
                + " host=" + host.name + ", dgram.src=" + datagram.src + ", dgram.dst="
                + datagram.dst + ", iif=" + src + ", counter=" + msg.msg);
    }
	
}