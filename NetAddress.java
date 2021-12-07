/*
 * This file is (c) Shish 2002
 * See the GNU GPL for details
 */

import java.net.*;

public class NetAddress {
    String name = "127.0.0.1";
    String host = "127.0.0.1";
    int port = 1;
    String all = "127.0.0.1:1";
/*
    public void finalize() {
	System.out.println("NetAddress.finalize() called");
	name=null;host=null;all=null;
    }
*/
    public NetAddress() {}
    public NetAddress(Socket s) {
	name = s.getInetAddress().getHostName();
	host = s.getInetAddress().getHostName();
	port = s.getPort();
	all = host+":"+port;
    }
    public NetAddress(DatagramPacket p) {
	name = p.getAddress()+"";
	host = p.getAddress()+"";
	port = p.getPort();
	all = host+":"+port;
    }
    public NetAddress(String h,int p) {
	name = h;
	host = h;
	port = p;
	all = host+":"+port;
    }
    public String toString() {return all;}
}
