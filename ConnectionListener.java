/*
 * This file is (c) Shish 2002
 * See the GNU GPL for details
 */

import java.io.*;
import java.net.*;

public class ConnectionListener extends Thread {
    public static final int DATAGRAM=0,SOCKET=1;
    int MODE=0;
    ServerSocket SS;
    DatagramSocket DS;
    MultiPager MP;
    String name = "ConnectionListener"; //Thread name for debugging

    public ConnectionListener(MultiPager m,int mode) {
	MP = m;
	setPriority(MP.CLP);
	setDaemon(true);
	MODE=mode;
	start();
    }
    public void run() {
	try {
	    if(MODE == SOCKET) SS = new ServerSocket(MP.netadd.port);
	    else if(MODE == DATAGRAM) DS = new DatagramSocket(MP.netadd.port+1);
	    else MP.print("INVALID MODE");
	}
	catch(BindException e) {MP.error("ConnectionListener.run() (Address in use) <Bind>",e);}
	catch(IOException e) {MP.error("ConnectionListener.run() (Sockets) <IO>",e);}

	while(true) {
	    if(MODE == SOCKET) {
		try{new Connection(SS.accept(),MP);}
		catch(IOException e) {MP.error("ConnectionListener.run() (Serving) <IO>",e);}
	    } else
	    if(MODE == DATAGRAM) {
		try {
		    byte[] buf = new byte[256];

		    // receive request
		    DatagramPacket packet = new DatagramPacket(buf, buf.length);
		    DS.receive(packet);
		    new Connection(packet,MP);

		} catch (IOException e) {
		    DS.close();
		    break;
		}
	    } else {
		MP.print("Invalid mode");
		MODE = 0;
	    }
	}
    }
}

