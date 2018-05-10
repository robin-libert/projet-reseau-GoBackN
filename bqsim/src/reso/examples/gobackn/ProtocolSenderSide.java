package reso.examples.gobackn;

import java.io.BufferedWriter;
import java.io.File;
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

/**
 * Dans cette classe est implémenté le protocol gobackn et le contrôle de congestion du côté de celui qui envoi les messages.
 * C'est à dire que cette classe définit quoi faire quand l'application envoi un message, reçoit un acquittement ou quand un time out est déclenché.
 */
public class ProtocolSenderSide extends Protocol{
    private int sendBase, nextSeqNum;
    private double cwnd;
    private ArrayList<Integer> packages;
    private AbstractTimer timer;
    private Scheduler scheduler;
    private Random r;
    private IPAddress dst;
    private int lastAck, proba;
    
    private double ssthresh, cwndTemp;
    private int duplicated, expected;
    private double RTO, SRTT, RTTVAR, alpha, beta;
    private double time1, time2;//variables utiles pour le contrôle de congestion
    private boolean computeRTO;//c'est un flag qui permet de savoir quand démarrer le timer pour le calcul de RTO
    
    private int[] congestionTest;//on stocke dans ce tableau les 3 derniers acks recus, utile pour detecter de la congestion (3 acks duppliques)
    private int flagCongestion=0;//l indice du tableau ou je vais stocker le dernier ack recu

    private ArrayList<String> plots; //je stocke les plots a afficher dans cette arrayList
    private  File file;
    BufferedWriter writer = null;

    private double lastedCwnd=0;//la derniere taille de fenetre de congestion  recue
    private double newCwnd=0;//la nouvelle taille de fenetre de congestion recue


    /**
     * Constructeur de la classe.
     * @param host
     * @param proba probabilité de perte d'un message et d'un acquittement.
     * @throws IOException 
     */     
    public ProtocolSenderSide(IPHost host, int proba) throws IOException{

        super(host);
        this.scheduler = (Scheduler)host.getNetwork().getScheduler();
        this.cwnd = 1;
        this.r=new Random();
        this.congestionTest= new int[3];
        this.proba = proba;
        this.ssthresh = Double.MAX_VALUE;//initialement un grand nombre
        this.cwndTemp = cwnd;//utile pour additive increase
        this.duplicated = -1;//permet de ne pas prend en compte les ack dupliqué plus de 3 fois
        
        //Valeurs des slides
        this.RTO = 3.0;
        this.alpha = 0.125;
        this.beta = 0.25;
        //flag qui empêche de calculer RTO si le calcul est déjà en cours pour un autre message de la fenêtre.
        this.computeRTO = true;
        
        plots=new ArrayList<String>();
    }
    
    /**
     * Le protocol charge tous les messages que l'application doit transmettre.
     * @param messages la liste des messages a transmettre.
     */ 
    public void loadMessages(ArrayList<Integer> messages){
        packages = messages;
    }
    
    /**
     * Decris le fonctionnement du protocol lorsque l'on reçoit un acquittement. 
     * Dans cette methode nous implementons le slow start, l additive increase, la multiplicative decrease et la reaction lors d'un timeOut.
     * @param src
     * @param datagram
     * @throws Exception 
     */
    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        GoBackNMsg msg = (GoBackNMsg) datagram.getPayload();
        //On initialise la connexion.
        Double currentTime = scheduler.getCurrentTime();
        if(msg.isAck && msg.seqNum == -1){//Ack initial
            System.out.println("Connexion établie.");
            this.dst = datagram.src;
            this.sendBase = 0;
            this.nextSeqNum = 0;
            this.timer = new MyTimer(host.getNetwork().getScheduler(),RTO);
            this.lastAck = -1;
            plots.add("Nombre total de message : "+packages.size());
            plots.add("Probabilite d'avoir des pertes : "+proba);
            send();
        }

        if(r.nextInt(100)>=proba){//Pourcentage de chance de ne pas recevoir le ack
            //msg.seqNum != duplicated pour dire que si on reçoit 3 fois le même ack, on ne les prends plus en compte.
            if(msg.isAck && msg.seqNum != -1 && msg.seqNum != duplicated){//Quand on reçoit un ack normal
                System.out.println(msg + " RTO = "+RTO+" ssthresh : "+ssthresh);
                //C'est ici que l'on s'occupe de modifier la valeur de RTO
                if(this.expected == msg.seqNum && this.computeRTO == false){
                    this.time2 = scheduler.getCurrentTime();
                    this.computeRTO = true;
                    double R = Math.abs(this.time2 - this.time1);
                    if(this.SRTT == 0){
                        this.SRTT = R;
                        this.RTTVAR = R/2;
                    }else{
                        this.SRTT = (1 - this.alpha) * this.SRTT + this.alpha * R;
                        this.RTTVAR = (1 - this.beta) * this.RTTVAR + this.beta * Math.abs(this.SRTT - R);
                    }
                    this.RTO = this.SRTT + 4*this.RTTVAR;
                }else if(this.expected < msg.seqNum && this.computeRTO == false){
                    this.computeRTO = true;
                }
                
                if(cwnd < ssthresh){//si on est en slowStart on augmente la taille de cwnd de 1 à chaque ack reçu
                    lastedCwnd=cwnd;
                    cwnd += 1;
                    newCwnd=cwnd;
                    cwndTemp = cwnd;
                    System.out.println("----------> Taille de la fenêtre en slowstart : " + cwnd);
                    plots.add("(SS)Temps ecoule  : "+currentTime+"                          fenetre de congestion  : " +cwnd);
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
                    System.out.println("----------> Taille de la fenêtre en additiveincrease : " + cwnd);
                    plots.add("(AI)Temps ecoule  : "+currentTime+"                          fenetre de congestion  : " +cwnd);
                } 
                
                congestionTest[flagCongestion]=msg.seqNum;
                flagCongestion=(flagCongestion+1)%3;
                
                if(congestionTest[0]==congestionTest[1]&&congestionTest[0]==congestionTest[2]&&congestionTest[0]!=0){//si on a de la congestion
                    System.out.println("");
                    System.out.println("==========CONGESTION========");
                    System.out.println("");
                    ssthresh = Math.ceil(cwnd/2);
                    duplicated = msg.seqNum;
                    cwnd = Math.ceil(cwnd/2.); //Math.ceil comme ça la fenêtre ne vaut jamais 0
                    newCwnd=cwnd;
                    cwndTemp = cwnd;
                    stopTimer();

                    if(lastAck+1 < packages.size()){
                        this.sendBase = this.lastAck + 1;
                        this.nextSeqNum = this.lastAck + 1;
                        send();
                    }
                }

                if(!(congestionTest[0]==congestionTest[1]&&congestionTest[0]==congestionTest[2]&&congestionTest[0]!=0)){ //pas de congestion
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
                if(msg.seqNum==packages.size()-1){ // si le dernier message est recu, je peux ecrire dans mon ficher .txt les valeurs de l'arrayList plots
                    writePlots();
                }
            }
        }
    }
    
    
    /**
     * Cette methode envoie des messages comme définit dans le protocol gobackn.
     * @throws Exception 
     */
    public void send() throws Exception{
        GoBackNMsg msg = new GoBackNMsg(packages.get(this.nextSeqNum),this.nextSeqNum, false);
        if(nextSeqNum < sendBase + cwnd && this.nextSeqNum < packages.size()){//si nextSeqNum est dans la fenêtre et qu'il reste des messages dans la liste
            //Si on est dans la phase de calcul de RTO, on envoi un message et  on enregistre la valeur du timer.
            if(this.computeRTO){
                this.computeRTO = false;
                this.expected = msg.seqNum;
                this.time1 = scheduler.getCurrentTime();
            }
            host.getIPLayer().send(IPAddress.ANY, dst, IP_PROTO_GOBACKN, msg);
            if(this.sendBase == this.nextSeqNum){
                startTimer();
            }
            this.nextSeqNum ++;
            send();
        }
    }
    
   /**
    * Cette methode stoppe le timer si il etait en marche, en cree un autre et lance ce nouveau timer. 
    */
    public void startTimer(){
        stopTimer();
        this.timer = new MyTimer(host.getNetwork().getScheduler(),RTO);
        this.timer.start();
    }
   /**
    * Cette methode verifie si le timer est en marche et si oui, le stoppe.
    */
    public void stopTimer(){
        if(this.timer.isRunning())
            this.timer.stop();
    }
    
   /**
    * Cette methode ecrit les plots dans le fichier nomme "Plots.txt".
    * @throws IOException
    */
    public void writePlots() throws IOException{
        try {

            file = new File("Plots.txt");
            writer = new BufferedWriter(new FileWriter(file));
            for(int i=0;i<plots.size();i++){
                    writer.write(plots.get(i));
                    writer.write("\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        writer.close();
    }
    
    
   /**
    * Cette methode decrit comment reagir lors de la reception d un timeOut.
    * @throws Exception
    */
    public void timeout() throws Exception{
        if(lastAck+1 < packages.size()){
            ssthresh = Math.ceil(cwnd/2);
            cwnd = 1;
            cwndTemp = cwnd;
            this.sendBase = this.lastAck + 1;
            this.nextSeqNum = this.lastAck + 1;
            startTimer();
            send();
        }
    }
    
    /**
     * Cette classe interne représente un timer.
     */
    private class MyTimer extends AbstractTimer{
      /*
       * @param scheduler
       * @param interval
       */
        public MyTimer(AbstractScheduler scheduler, double interval){
            super(scheduler, interval, false);
        }

        
      /**
       * @throws Exception
       */
        @Override
        protected void run() throws Exception {
            this.stop();
            System.out.println("");
            System.out.println("---------Timeout---------");
            System.out.println("time = " + scheduler.getCurrentTime());
            System.out.println("");
            timeout();
        }
    }
    
}
