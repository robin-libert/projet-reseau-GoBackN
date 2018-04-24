package reso.examples.gobackn;


import java.util.ArrayList;
import java.util.Random;
import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppSenderGoBackN extends AbstractApplication{
    private final IPLayer ip;
    private final IPAddress dst;
    private int num;
    private ArrayList<Integer> messages = new ArrayList<>();;
    private Random r;
    
    public AppSenderGoBackN(IPHost host, IPAddress dst, int num) {
        super(host, "sender");
        this.dst = dst;
        this.ip = host.getIPLayer();
        this.num = -1;//message initial
        this.r = new Random();
    }

    @Override
    public void start() throws Exception {
        for(int i = 0; i < 10;i++){
            this.messages.add(r.nextInt(100));
        }
        ProtocolGoBackN protocol = new ProtocolGoBackN((IPHost) host);
        protocol.loadMessages(this.messages);
        ip.addListener(ProtocolGoBackN.IP_PROTO_GOBACKN, protocol);
        ip.send(IPAddress.ANY, dst, ProtocolGoBackN.IP_PROTO_GOBACKN, new MessageGoBackN(this.num,0, false));
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
}