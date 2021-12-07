/*
 * This file is (c) Shish 2002
 * See the GNU GPL for details
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class SendMessage extends Thread {
    StringTokenizer st=new StringTokenizer("");
    String message="",to="",tosend="";
    MultiPager parent;
    Socket sock;
    int i,count,tport,port;
/*
    public void finalize() {
	System.out.println("SendMessage.finalize() called");
	st=null;
	message=null;to=null;tosend=null;
    }
*/
    //"To" in string form, can be several addresses in one string
    public SendMessage(String s,String tos,MultiPager m) {
	message = s;
	port = m.netadd.port; //default port, replaced later if neccesary
	parent = m;
	to = tos.replace('\\','/');
	st = new StringTokenizer(to,"/");
	count = st.countTokens();
	setPriority(parent.SMP);
	start();
    }
    //"To" in NetAddress form, can only be one address
    public SendMessage(String s,NetAddress toaddr,MultiPager m) {
	message = s;
	port = toaddr.port;
	tport = toaddr.port;
	parent = m;
	to = toaddr.all;
	st = new StringTokenizer(to,"/");
	count = st.countTokens();
	setPriority(parent.SMP);
	start();
    }
    public void run() {
	try {
	    setName("SendMessage"); //Thread name for debugging
	    for(i=0;i<count;i++) {
		to = st.nextToken();
		to = to.trim();
		if(to.indexOf(":") == -1) {
		     tport = parent.netadd.port;
		} else {
		     tport = Integer.parseInt(to.substring(to.indexOf(":")+1,to.length()).trim());
		     to = to.substring(0,to.indexOf(":")).trim();
		}
		//add stuff to send here
		NetAddress na = parent.netadd;
		tosend = ""+
		    na.name+
		    " "+na.host.substring(na.host.indexOf("/")+1,na.host.length())+
		    " "+na.port+
		    " "+message;
		new SendMessage(to,tport,tosend);
	    }
	}
	//catch(UnsupportedEncodingException e) {parent.error("SendMessage.run() (Different system) <UnsupportedEncoding>",e);}
	//catch(UnknownHostException e) {parent.error("SendMessage.run() (Can't find host "+to+") <UnknownHost>",e);}
	//catch(ConnectException e) {parent.error("SendMessage.run() (Can't find socket "+tport+") <Connect>",e);}
	//catch(BindException e) {parent.error("SendMessage.run() (Can't bind socket "+to+":"+tport+") <Bind>",e);}
	//catch(IOException e) {parent.error("SendMessage.run() (Reading/Writing something) <IO>",e);}
	catch(Exception e) {parent.error("SendMessage.run() (Other) <Runtime>",e);}
    }
    public SendMessage(String to,int port,String data) {
	try {
	    DatagramSocket DS = new DatagramSocket();
	    InetAddress ia = InetAddress.getByName(to);
	    byte[] buf = data.getBytes();
	    DatagramPacket packet = new DatagramPacket(buf, buf.length, ia, port+1);
	    DS.send(packet);
	    if(parent.console) 
		System.out.println("================================"+
		"\nSent ["+data+"]"+
		"\nTo   ["+to+":"+port+"]");
	}
	catch(Exception e) {}
    }
}
