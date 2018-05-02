package reso.examples.gobackn;


import java.util.ArrayList;
import java.util.Random;
import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

public class AppSender extends AbstractApplication{
    private final IPLayer ip;
    private final IPAddress dst;
    private ArrayList<Integer> messages = new ArrayList<>();;
    private ProtocolSenderSide protocol;
    
    public AppSender(IPHost host, IPAddress dst, int n) {
        super(host, "sender");
        this.dst = dst;
        this.ip = host.getIPLayer();
        this.numberToSend(n);
    }

 
    @Override
    public void start() throws Exception {
        this.protocol = new ProtocolSenderSide((IPHost) host);
        this.protocol.loadMessages(this.messages);//On envoi la liste de messages au protocol
        ip.addListener(Protocol.IP_PROTO_GOBACKN, this.protocol);
        ip.send(IPAddress.ANY, dst, Protocol.IP_PROTO_GOBACKN, new GoBackNMsg(-1,-1, false));
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private void numberToSend(int n){
        for(int i = 0; i < n;i++){
            this.messages.add(42);
        }
    }
	
}
