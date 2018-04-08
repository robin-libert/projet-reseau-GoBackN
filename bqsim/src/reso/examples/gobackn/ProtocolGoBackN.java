package reso.examples.gobackn;


import reso.ip.Datagram;
import reso.ip.IPInterfaceAdapter;
import reso.ip.IPInterfaceListener;

public class ProtocolGoBackN implements IPInterfaceListener{

    @Override
    public void receive(IPInterfaceAdapter src, Datagram datagram) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
	
}