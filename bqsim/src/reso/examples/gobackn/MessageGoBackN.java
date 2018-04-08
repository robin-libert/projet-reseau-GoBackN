package reso.examples.gobackn;

import reso.common.Message;

public class MessageGoBackN implements Message{

    @Override
    public int getByteLength() {
        return Integer.SIZE / 8;
    }
	
}