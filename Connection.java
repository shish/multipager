/*
 * This file is (c) Shish 2002
 * See the GNU GPL for details
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class Connection extends Thread {
    MultiPager parent;
    Socket sock;
    DatagramPacket packet;
    NetAddress from;
    String request="",type="",args="",name="Connection";
    int tbreak,i;
/*
    public void finalize() {
	System.out.println("Connection.finalize() called");
	request=null;type=null;args=null;name=null;
    }
*/
    public Connection(Socket s,MultiPager m) {
	sock = s;
	parent = m;
	from = new NetAddress(sock);
	setPriority(parent.CP);

	try {
	    BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream(), "8859_1"));
	    BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
	    PrintWriter pout = new PrintWriter(new OutputStreamWriter(out, "8859_1"));
	    request = in.readLine().toUpperCase();
	    sock.close();
	}
	catch(NullPointerException e) {} //port is opened with nothing sent (scan)
	catch(SocketException e) {parent.error("Connection.<init> (Read request) <Socket>",e);}
	catch(IOException e) {}
	catch(Exception e) {parent.error("Connection.<init> (Read request) <Runtime>",e);}

	start();
    }
    public Connection(DatagramPacket p,MultiPager m) {
	packet = p;
	parent = m;
	from = new NetAddress(packet);
	setPriority(parent.CP);
	request = new String(packet.getData());
	start();
    }
    public void run() {
	try {
	    do {
		try {
		    if(request == null) break;
		    StringTokenizer rtokens = new StringTokenizer(request);
		    tbreak = rtokens.countTokens();

		    if(tbreak <= 0) { //Nothing
			break;
		    }
		    else if(tbreak == 4) { //Command
			from.name = rtokens.nextToken();
			from.host = rtokens.nextToken();
			from.port = Integer.parseInt(rtokens.nextToken());
			type = rtokens.nextToken();
			args = "";
		    }
		    else { //Command + args
			from.name = rtokens.nextToken();
			from.host = rtokens.nextToken();
			from.port = Integer.parseInt(rtokens.nextToken());
			type = rtokens.nextToken();
			for(i=0;i<tbreak-4;i++) { //-4 = -name,host,port,type
			    args += " "+rtokens.nextToken();
			}
		    }
		}
		catch(StringIndexOutOfBoundsException e) {parent.error("Connection.run() (Edit request) <StringIndexOutOfBounds>",e);break;}
		catch(Exception e) {parent.error("Connection.run() (Edit request) <Runtime>",e);break;}

		try {
		/*
		    System.out.println("===========================");
		    System.out.println("from.name : "+from.name);
		    System.out.println("from.host : "+from.host);
		    System.out.println("from.port : "+from.port);
		    System.out.println("type      : "+type);
		    System.out.println("args      : "+args);
		*/
		    if(type.equals("SAY")) {
			args = args.replace('/','\n');
			args = args.replace('\\','\n');
			if(parent.echo) new SendMessage("SAY "+args,parent.toBox.getText(),parent);
			parent.print(from.name+":"+args);
			parent.beep();
			break;
		    }
		    else if(type.equals("FINGER")) {
			parent.print(from.host+": is FINGERing you");
			parent.beep();
			new SendMessage("SAY User info follows:\n"+parent.userInfo,from.host+":"+from.port,parent);
			break;
		    }
		    else if(type.equals("LURK")) {
			String newto = parent.toBox.getText()+"/"+from.host+":"+from.port;
			parent.toBox.setText(newto);
			break;
		    }
		    else if(type.equals("CRASH")) {
			System.out.println("Sysadmin says no talking");
			System.out.println("bye bye "+parent.netadd.name);
			parent.beep();
			new SendMessage("SAY "+from.host+" was crashed",from,parent);
			System.exit(0);
		    }
		    else if(type.equals("EXIT")) {
			break;
		    }
		    else {
			//pout.println("ERROR bad request");
			//pout.println("EXIT");
			//pout.flush();
			break;
		    }
		}
		catch(Exception e) {parent.error("Connection.run() (Execute request) <Runtime>",e);break;}
            } while(false); //Execute once
	}
	catch(Exception e) {parent.error("Connection.run() (Unknown) <Runtime>",e);}
    }
}

