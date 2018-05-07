package reso.examples.gobackn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import reso.common.AbstractTimer;
import reso.ip.Datagram;
import reso.ip.IPAddress;
import reso.ip.IPHost;
import reso.ip.IPInterfaceAdapter;
import reso.scheduler.AbstractScheduler;
import reso.scheduler.Scheduler;

/*Cette methode represente l'envoyeur dans le protocol goBackN. Elle se charge d'envoyer des messages a la classe ProtocolReceiverSide.On introduit une notion
*de probabilite que l'ack renvoye par la classe ProtocolReceiverSide ne soit pas recu pour pouvoir implementer le slowStart, l'additive increase et le multiply decrease.
*/

public class ProtocolSenderSide extends Protocol{
    private int sendBase, nextSeqNum;
    private double cwnd;
    private ArrayList<Integer> packages;
    private AbstractTimer timer;
    private Scheduler scheduler;
    private Random r;
    private IPAddress dst;
    private int lastAck;
    private int proba;
    
    private double ssthresh;
    private double cwndTemp;
    private int duplicated;
    
    private int[] congestionTest;
    private int flagCongestion=0;
    


    private ArrayList<String> test;
    private  File file;
    BufferedWriter writer = null;
    private int totalMsg;
    private double lastedCwnd=0;
    private double newCwnd=0;
            
    
    public ProtocolSenderSide(IPHost host, int proba,int n) throws IOException{

        super(host);
        this.scheduler = (Scheduler)host.getNetwork().getScheduler();
        this.cwnd = 1;
        this.r=new Random();
        this.congestionTest= new int[3];
        this.proba = proba;
        this.ssthresh = Double.MAX_VALUE;//initialement un grand nombre
        this.cwndTemp = cwnd;//utile pour additive increase
        this.duplicated = -1;//permet de ne pas prend en compte les ack dupliqué plus de 3 fois
        test=new ArrayList<String>();
        this.totalMsg=n;

    }
    
    public void loadMessages(ArrayList<Integer> messages){
        packages = messages;
    }
    
    /**
     * Quand l'application sender reçoit un ack.
     * @param src
     * @param datagram
     * @throws Exception 
     */
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        //On initialise la connexion.
        
        Double time=scheduler.getCurrentTime();
        if(msg.isAck && msg.seqNum == -1){//Ack initial
            System.out.println("Connexion établie.");
            this.dst = datagram.src;
            this.sendBase = 0;
            this.nextSeqNum = 0;
            this.timer = new MyTimer(host.getNetwork().getScheduler(),3.0);
            this.lastAck = -1;
            test.add("Nombre total de message : "+totalMsg);
            test.add(" "+time+"    " +cwnd);
            send();
      
        }
        
        System.out.println("time = " +time);
        if(r.nextInt(100)>=proba){//Pourcentage de chance de ne pas recevoir le ack
            //msg.seqNum != duplicated pour dire que si on reçoit 3 fois le même ack, on ne les prends plus en compte.
            if(msg.isAck && msg.seqNum != -1 && msg.seqNum != duplicated){//Quand on reçoit un ack normal, on incrémente sendBase

                
                System.out.println(msg);
                if(cwnd < ssthresh){//si on est en slowStart on augmente la taille de cwnd de 1 à chaque ack reçu
                    lastedCwnd=cwnd;
                    cwnd += 1;
                    newCwnd=cwnd;
                    System.out.println("Slow start : le time est = "+time+ " le cwnd = "+cwnd);
                    
                    //test.add(" "+time+"    " +cwnd);

                    
                    
                    cwndTemp = cwnd;
                    //System.out.println("slowStart : "+cwnd);
                }else{//additive increase cwnd = cwnd + MSS^2/cwnd ici MSS vaut 1
                    lastedCwnd=cwnd;
                    cwndTemp += 1./cwnd;
                    //On ajoute une petite fraction à cwndTemp à chaque ack reçu.
                    //Comme la taille de fenêtre doit être entière, cwnd vaut la borne inférieure de cwndTemp.
                    cwnd = Math.floor(cwndTemp);
                    if(cwnd == 0){
                        cwnd = 1;
                    }
                    newCwnd=cwnd;
                    //test.add(" "+time+"    " +cwnd);

                    System.out.println("additive increase newCwnd : "+newCwnd);
                    System.out.println("additive increase lastedCwnd: "+lastedCwnd);
                }    
                
                
                congestionTest[flagCongestion]=msg.seqNum;
                flagCongestion=(flagCongestion+1)%3;
                if(congestionTest[0]==congestionTest[1]&&congestionTest[0]==congestionTest[2]&&congestionTest[0]!=0){//si on a de la congestion
                    System.out.println("============CONGESTION========");
                    //lastedCwnd=cwnd;
                    lastedCwnd=newCwnd;
                    ssthresh = Math.ceil(cwnd/2);

                    duplicated = msg.seqNum;
                    cwnd = Math.ceil(cwnd/2.); //Math.ceil comme ça la fenêtre ne vaut jamais 0
                    
                    //test.add(" "+time+"    " +cwnd);
                    newCwnd=cwnd;
                    
                    cwndTemp = cwnd;
                    stopTimer();

                    if(lastAck+1 < packages.size()){
                        //test.add(" "+time+"    " +cwnd);
                        this.sendBase = this.lastAck + 1;
                        this.nextSeqNum = this.lastAck + 1;
                        //startTimer();
                        send();
                    }
                                        
                    
                    //test.add(" "+time+"    " +cwnd);
                }
                
                if(lastedCwnd!=newCwnd){
                        //test.add(" "+time+"    " +newCwnd);
                        test.add(" "+time+"    " +lastedCwnd);
                    }
                
                
                //else{//si on a pas de congestion
                if(!(congestionTest[0]==congestionTest[1]&&congestionTest[0]==congestionTest[2]&&congestionTest[0]!=0)){
                    this.lastAck = msg.seqNum;
                    //Quand on reçoit ack(0), ça veut dire que sendBase augmente et vaut 1.
                    //Si on perd des ack, on risque de recevoir ack(3) directement après ack(0). Donc sendBase vaudra le seqNum de l'ack + 1.
                    if(msg.seqNum + 1 < packages.size())
                        this.sendBase = msg.seqNum + 1;
                    if(this.sendBase == this.nextSeqNum){
                        stopTimer();
                        send();
                    }else{
                        startTimer();
                    }
                }
                if(msg.seqNum==totalMsg-1){
                    try {

                        file = new File("Plots.txt");

                        writer = new BufferedWriter(new FileWriter(file));
                        for(int i=0;i<test.size();i++){
                            writer.write(test.get(i));
                            writer.write("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    writer.close();
        
                }
            }
        }
    }
    
    public void send() throws Exception{
        GoBackNMsg msg = new GoBackNMsg(packages.get(this.nextSeqNum),this.nextSeqNum, false);
        //System.out.println("On est dans la methode send() , cwnd = "+cwnd);
        if(nextSeqNum < sendBase + cwnd && this.nextSeqNum < packages.size()){//si nextSeqNum est dans la fenêtre et qu'il reste des messages dans la liste
            host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_GOBACKN, msg);
            if(this.sendBase == this.nextSeqNum){
                startTimer();
            }
            this.nextSeqNum ++;
            send();
        }
    }
    
    public void startTimer(){
        //System.out.println("start");
        stopTimer();
        this.timer = new MyTimer(host.getNetwork().getScheduler(),3.0);
        this.timer.start();
    }
    
    public void stopTimer(){
        //System.out.println("stop");
        if(this.timer.isRunning())
            this.timer.stop();
    }
    
    public void timeout() throws Exception{
        if(lastAck+1 < packages.size()){
            ssthresh = Math.ceil(cwnd/2);
            System.out.println(ssthresh);
            cwnd = 1;
            cwndTemp = cwnd;
            this.sendBase = this.lastAck + 1;
            this.nextSeqNum = this.lastAck + 1;
            startTimer();
            send();
        }
    }
    
    private class MyTimer extends AbstractTimer{
        public MyTimer(AbstractScheduler scheduler, double interval){
            super(scheduler, interval, false);
        }

        @Override
        protected void run() throws Exception {
            this.stop();
            System.out.println("timeout=" + scheduler.getCurrentTime());
            timeout();
        }
    }
    
}
