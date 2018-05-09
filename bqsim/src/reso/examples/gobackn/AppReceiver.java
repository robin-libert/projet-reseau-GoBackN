package reso.examples.gobackn;


import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

/** 
 *Je ne comprend pas a quoi sert cette classe.
 */
public class AppReceiver extends AbstractApplication{
    private final IPLayer ip;
    private int proba;
    
    /** 
     *@param host
     *@param proba
     */
    public AppReceiver(IPHost host, int proba) {
        super(host, "receiver");
        ip = host.getIPLayer();
        this.proba = proba;
    }

    /** 
     *@throws Exception
     */
    @Override
    public void start() throws Exception {
        ip.addListener(Protocol.IP_PROTO_GOBACKN, new ProtocolReceiverSide((IPHost) host, this.proba));
    }

    @Override
    public void stop() {
    }
	
}
