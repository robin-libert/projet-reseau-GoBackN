package reso.examples.gobackn;


import reso.ip.Datagram;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

/** 
 *Classe parent d'un protocol.
 */
public class Protocol implements IPInterfaceListener{
    public static final int IP_PROTO_GOBACKN = Datagram.allocateProtocolNumber("GO-BACK-N");
    protected final IPHost host;
   /**
    * Constructeur parent d'un protocol.
    * @param host
    */
    public Protocol(IPHost host) {
        this.host = host;
    }
    
   /** 
    *@throws Exception
    */
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
    }
	
}
