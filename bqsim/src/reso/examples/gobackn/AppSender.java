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
    private int msg;
    private ArrayList<Integer> messages = new ArrayList<>();;
    private Random r;
    private ProtocolSenderSide protocol;
    private int sent;
    
    public AppSender(IPHost host, IPAddress dst, int num) {
        super(host, "sender");
        this.dst = dst;
        this.ip = host.getIPLayer();
        this.msg = -1;//message initial
        this.r = new Random();
        this.sent = 0;
    }

    @Override
    public void start() throws Exception {
        //On crée notre liste de messages
        for(int i = 0; i < 1000;i++){
            this.messages.add(i+100);
        }
        this.protocol = new ProtocolSenderSide((IPHost) host);
        this.protocol.loadMessages(this.messages);//On envoi la liste de messages au protocol
        ip.addListener(Protocol.IP_PROTO_GOBACKN, this.protocol);
        ip.send(IPAddress.ANY, dst, Protocol.IP_PROTO_GOBACKN, new GoBackNMsg(this.msg,-1, false));
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
}