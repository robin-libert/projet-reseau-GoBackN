package reso.examples.gobackn;


import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppSenderGoBackN extends AbstractApplication{
    private final IPLayer ip;
    private final IPAddress dst;
    private int num;
    public AppSenderGoBackN(IPHost host, IPAddress dst, int num) {
        super(host, "sender");
        this.dst = dst;
        this.ip = host.getIPLayer();
        this.num = num;
    }

    @Override
    public void start() throws Exception {
        ip.addListener(ProtocolGoBackN.IP_PROTO_GOBACKN, new ProtocolGoBackN());
        ip.send(IPAddress.ANY, dst, ProtocolGoBackN.IP_PROTO_GOBACKN, new MessageGoBackN(num, false));
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
}