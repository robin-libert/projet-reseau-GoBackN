package reso.examples.gobackn;

import reso.common.Message;

public class MessageGoBackN implements Message{
    private int seqNumber;
    public int num;
    public boolean isAck;
    public MessageGoBackN(int msg, boolean isAck) {
        this.seqNumber = 42;
        this.num = msg;
        this.isAck = isAck;
    }
	
    public String toString() {
	return (this.isAck)?"This is an ack for " + seqNumber:"This is message number " + seqNumber + ". It contains: " + num;
    }
    
    @Override
    public int getByteLength() {
        return Integer.SIZE / 8;
    }
	
}