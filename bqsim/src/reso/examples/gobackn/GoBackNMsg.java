package reso.examples.gobackn;

import reso.common.Message;


 /**
  * Cette classe represente la structure d un message, un message contient un numero de sequence, un booleen "isAck" pour savoir si ce message est un ack ainsi que
  *l'entier contenu dans le message.
  */
public class GoBackNMsg implements Message{
    public int seqNum;
    public int num;
    public boolean isAck;
    
     /**
      *@param seqNum
      * @param isAck
      */
    public GoBackNMsg(int seqNum, boolean isAck) {
        this.seqNum = seqNum;
        this.isAck = isAck;
    }
    
     /**
      *@param seqNum
      * @param isAck
      * @param msg
      */
    public GoBackNMsg(int msg, int seqNum, boolean isAck) {
        this.seqNum = seqNum;
        this.num = msg;
        this.isAck = isAck;
    }
	
    public String toString() {
	return (this.isAck)?"This is an ack for " + seqNum:"This is message number " + seqNum + ". It contains: " + num;
    }
    
    @Override
    public int getByteLength() {
        return Integer.SIZE / 8;
    }
	
}
