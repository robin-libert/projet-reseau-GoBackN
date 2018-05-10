package reso.examples.gobackn;


import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

/** 
 * Cette classe représente une application qui va recevoir les messages de AppSender.
 */
public class AppReceiver extends AbstractApplication{
    private final IPLayer ip;
    private int proba;
    
    /** 
     *Constructeur de l'application chargée de recevoir les messages.
     *@param host
     *@param proba probabilité de perte d'un acquittement.
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
