package reso.examples.gobackn;


import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppReceiver extends AbstractApplication{
    private final IPLayer ip;
    public AppReceiver(IPHost host) {
        super(host, "receiver");
        ip = host.getIPLayer();
    }

    @Override
    public void start() throws Exception {
        ip.addListener(Protocol.IP_PROTO_GOBACKN, new ProtocolReceiverSide((IPHost) host));
    }

    @Override
    public void stop() {
    }
	
}