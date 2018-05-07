package reso.examples.gobackn;


import java.util.Random;
import reso.ip.Datagram;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

/* Je ne comprend pas a quoi sert cette classe.
*
*/
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
    }
	
}
