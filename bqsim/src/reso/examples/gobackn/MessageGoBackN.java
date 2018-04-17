package reso.examples.gobackn;

import reso.common.Message;

public class MessageGoBackN implements Message{
    private int seqNumber;
    public int msg;
    private boolean isAck;
    public MessageGoBackN(int msg, boolean isAck) {
        this.seqNumber = 42;
        this.msg = msg;
        this.isAck = isAck;
    }
	
    public String toString() {
	return (this.isAck)?"This is an ack for " + seqNumber:"This is message number " + seqNumber + ". It contains: " + msg;
    }
    
    @Override
    public int getByteLength() {
        return Integer.SIZE / 8;
    }
	
}