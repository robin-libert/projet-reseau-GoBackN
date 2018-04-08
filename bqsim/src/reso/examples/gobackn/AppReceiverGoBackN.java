package reso.examples.gobackn;


import reso.common.AbstractApplication;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppReceiverGoBackN extends AbstractApplication{
    private final IPLayer ip;
    public AppReceiverGoBackN(IPHost host) {
        super(host, "receiver");
        ip = host.getIPLayer();
    }

    @Override
    public void start() throws Exception {
        ip.addListener(ProtocolGoBackN.IP_PROTO_GOBACKN, new ProtocolGoBackN());
    }

    @Override
    public void stop() {
    }
	
}