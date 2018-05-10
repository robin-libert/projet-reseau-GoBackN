package reso.examples.gobackn;


import java.util.ArrayList;
import reso.common.AbstractApplication;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPLayer;

/**
 * Cette classe représente une application qui va envoyer des messages. Chaque message contiendra un unique entier valant 42.
 */
public class AppSender extends AbstractApplication{
    private final IPLayer ip;
    private final IPAddress dst;
    private ArrayList<Integer> messages = new ArrayList<>();;
    private ProtocolSenderSide protocol;
    private int proba;
    
    /**
     * Constructeur de l'application chargée d'envoyer des messages.
     * @param host adresse ip de l'hote
     * @param dst adresse ip du destinataire
     * @param n nombre de messages à envoyer
     * @param p probabilités de pertes dans l'envoi d'un message
     */
    public AppSender(IPHost host, IPAddress dst, int n, int p) {
        super(host, "sender");
        this.dst = dst;
        this.ip = host.getIPLayer();
        this.proba = p;
        this.numberToSend(n);
    }

    /**
     * Cette methode permet d'instancier une instance de la classe ProtocolSenderSide qui represente un sender dans le protocol GoBackN.
     * @throws Exception
     */ 
    @Override
    public void start() throws Exception {
        this.protocol = new ProtocolSenderSide((IPHost) host, this.proba);
        this.protocol.loadMessages(this.messages);//On envoi la liste de messages au protocol
        ip.addListener(Protocol.IP_PROTO_GOBACKN, this.protocol);
        ip.send(IPAddress.ANY, dst, Protocol.IP_PROTO_GOBACKN, new GoBackNMsg(-1,-1, false));
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    /**
     * Cette methode ajoute n entier à l'arraylist "messages".
     * @param n 
     */
    private void numberToSend(int n){
        for(int i = 0; i < n;i++){
            this.messages.add(42);
        }
    }
	
}
