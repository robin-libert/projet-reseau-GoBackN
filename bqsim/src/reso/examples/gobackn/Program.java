package reso.examples.gobackn;

import reso.common.*;
import reso.ethernet.*;
import reso.ip.*;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;
import reso.utilities.NetworkBuilder;

/**
 * Cette classe est la classe utilisée pour lancer la simulation du protocol GoBackN et le contrôle de congestion. Pour pouvoir correctement l'utiliser il faut
 * fournir 2 arguments en ligne de commande : le premier étant le nombre de messages à envoyer et le second doit être la probabilité de perte de messages.
 */
public class Program{

    public static void main(String [] args) {
        
        int numberOfMessages=Integer.parseInt(args[0]); // le premier argument doit être le nombre de message
        int probability=Integer.parseInt(args[1]); // le deuxiemre argument doit être la probabilité de perte
        if((args.length==2)&&(probability>=0&&probability<100)){
 
        
	AbstractScheduler scheduler= new Scheduler();
	Network network= new Network(scheduler);
            try {
                final EthernetAddress MAC_ADDR1= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x28);
                final EthernetAddress MAC_ADDR2= EthernetAddress.getByAddress(0x00, 0x26, 0xbb, 0x4e, 0xfc, 0x29);
                final IPAddress IP_ADDR1= IPAddress.getByAddress(192, 168, 0, 1);
                final IPAddress IP_ADDR2= IPAddress.getByAddress(192, 168, 0, 2);

                IPHost host1= NetworkBuilder.createHost(network, "H1", IP_ADDR1, MAC_ADDR1);
                host1.getIPLayer().addRoute(IP_ADDR2, "eth0");

                //host1.addApplication(new AppSender(host1, IP_ADDR2,1000,5));//envoi de x messages avec proba y
                host1.addApplication(new AppSender(host1, IP_ADDR2,numberOfMessages,probability));

                IPHost host2= NetworkBuilder.createHost(network,"H2", IP_ADDR2, MAC_ADDR2);
                host2.getIPLayer().addRoute(IP_ADDR1, "eth0");
                host2.addApplication(new AppReceiver(host2,probability));//réception de messages avec proba x

                EthernetInterface h1_eth0= (EthernetInterface) host1.getInterfaceByName("eth0");
                EthernetInterface h2_eth0= (EthernetInterface) host2.getInterfaceByName("eth0");

                // Connect both interfaces with a 5000km long link
                new Link<EthernetFrame>(h1_eth0, h2_eth0, 5000000, 100000);

                host1.start();
                host2.start();

                scheduler.run();
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace(System.err);
            }
        }
        else{
            System.out.println("Il faut 2 arguments, le premier est le nombre de messages a envoyer et le deuxieme est la probabilite de perte, un entier compris entre 0 et 100");
        }
    }
}
