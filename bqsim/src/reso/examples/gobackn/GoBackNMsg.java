package reso.examples.gobackn;

import reso.common.Message;

public class GoBackNMsg implements Message{
    public int seqNum;
    public int num;
    public boolean isAck;
    
    public GoBackNMsg(int seqNum, boolean isAck) {
        this.seqNum = seqNum;
        this.isAck = isAck;
    }
    
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