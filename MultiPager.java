/*
 * This file is (c) Shish 2002
 * @version 1.4.2
 * See the GNU GPL for details
 */

import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class MultiPager extends Panel implements ActionListener, WindowListener {

    //User Info 709->332
    public static final String version = "1.4.5";
    public static NetAddress netadd = new NetAddress("127.0.0.1",1234);
    public static String userInfo = "<user has no details>";

    //Init args
    public static boolean console = false;
    public static boolean quiet = true;
    public static boolean echo = false;
    public static boolean root = false;

    //Priorities
           static final int np = Thread.NORM_PRIORITY; //NORM=5/MIN=1/MAX=10
    public static final int CLP = np - 3; // -3
    public static final int CP = np - 1;  // -1
    public static final int SMP = np - 2; // -2

    //GUI
    MultiPager mp = this;
    MenuBar menu;
    TextArea text;
    TextField toBox,messageBox;
    Frame container;
    Label title;

    //Misc
    int i;

    public static void main(String[] args) {new MultiPager(args);}
    public MultiPager() {}

    public MultiPager(String[] args) {
	try {
	    container = new Frame("MultiPager by Shish");
	    container.setLayout(new BorderLayout());
	    container.add(this,BorderLayout.CENTER);
	    container.addWindowListener(this);
 	    //container.setResizable(false); 
	    //container.pack();
	    //container.setVisible(true);
	    container.setSize(400,300);
	} catch(Exception e) {
	    Toolkit.getDefaultToolkit().beep();
	    System.out.println("ERROR MultiPager.<main> (Creation) <Runtime>:\n"+e);
	    e.printStackTrace();
	    System.exit(1);
	}

	try {
	    for(i=0;i<args.length;i++) {
		String arg = args[i].toUpperCase();
		     if(arg.equals("-P")) {if(args.length-1 >= i+1) netadd.port = Integer.parseInt(args[++i]);}
		else if(arg.equals("-R")) {if(args.length-1 >= i+1) root = args[++i].equals("xyzzy") ? true : false;}
		else if(arg.equals("-E")) {echo = true;}
		else if(arg.equals("-Q")) {quiet = true;}
		else if(arg.equals("-C")) {console = true;}
		else System.out.println(arg+" is not a valid argument");
	    }
	}
	catch(ArrayIndexOutOfBoundsException e) {error("MultiPager.<init>() (Args) <ArrayIndexOutOfBounds>",e);}
	catch(Exception e) {error("MultiPager.<init> (Args) <Runtime>",e);}

	try {
	    netadd.host = InetAddress.getLocalHost()+"";
	    netadd.name = System.getProperty("user.name");
	    setLayout(new BorderLayout());
	    text = new TextArea(); //Needed here for init stat printouts

	    Panel top = new Panel();
	    top.setLayout(new BorderLayout());
//====
	    menu = new MenuBar();
	    MenuItem mi;
	    Menu m;

	    m = new Menu("File");
	    mi = new MenuItem("Clear Screen");
	    mi.addActionListener(this);
	    m.add(mi);
	    mi = new MenuItem("Exit");
	    mi.addActionListener(this);
	    m.add(mi);
	    menu.add(m);

	    m = new Menu("Advanced");
	    mi = new MenuItem("Finger");
	    mi.addActionListener(this);
	    m.add(mi);
	    mi = new MenuItem("Lurk");
	    mi.addActionListener(this);
	    m.add(mi);
	    mi = new MenuItem("Change Details");
	    mi.addActionListener(this);
	    m.add(mi);
/*	    mi = new MenuItem("Send File");
	    mi.addActionListener(this);
	    m.add(mi);
*/	    menu.add(m);

	    m = new Menu("Help");
	    mi = new MenuItem("Help");
	    mi.addActionListener(this);
	    m.add(mi);
	    mi = new MenuItem("About");
	    mi.addActionListener(this);
	    m.add(mi);
	    menu.add(m);

	    container.setMenuBar(menu);
//====
	    title = new Label("EXTRA",Label.CENTER);
	    resetTitle();
	    title.setFont(new Font("monospaced", Font.PLAIN, 12));
	    top.add(title,BorderLayout.CENTER);
	    add(top,BorderLayout.NORTH);

	    text.setFont(new Font("monospaced", Font.PLAIN, 12));
	    text.setEditable(false);
	    add(text,BorderLayout.CENTER);

	    Panel bottom = new Panel();
	    bottom.setLayout(new BorderLayout());
	    toBox = new TextField();
	    toBox.setFont(new Font("monospaced", Font.PLAIN, 12));
	    toBox.addActionListener(this);
	    bottom.add(toBox,BorderLayout.NORTH);
	    messageBox = new TextField();
	    messageBox.setFont(new Font("monospaced", Font.PLAIN, 12));
	    messageBox.addActionListener(this);
	    bottom.add(messageBox,BorderLayout.CENTER);
	    Button quitb = new Button("Exit");
	    quitb.setFont(new Font("monospaced", Font.PLAIN, 12));
	    quitb.addActionListener(this);
	    bottom.add(quitb,BorderLayout.SOUTH);
	    add(bottom,BorderLayout.SOUTH);
	}
	catch(UnknownHostException e) {error("MultiPager.<init> (Window) <UnknownHost>",e);}
	catch(Exception e) {error("MultiPager.<init> (Window) <Runtime>",e);}

	try {
	    print("Version   : "+version);
	    print("Localhost : "+netadd.host);
	    new ConnectionListener(this,ConnectionListener.SOCKET);
	    new ConnectionListener(this,ConnectionListener.DATAGRAM);
	    container.show();
	}
	catch(Exception e) {error("MultiPager.<init> (Sockets) <Runtime>",e);}
    }

    public void print(String s) {
	try {
	    if(console) System.out.println(s.trim());
	    text.setText(s.trim()+"\n"+text.getText());
	} catch(Exception e) {}
    }
    public void beep() {if(!quiet) Toolkit.getDefaultToolkit().beep();}
    public void error(String info,Exception e) {
	beep();
	print("=========================================================="+
	" Brief Error Description"+
	" ERROR "+info+":\n "+e+
	"==========================================================");
	if(console) {
	    System.out.println(" Detailed Error Description");
	    System.out.print(" ");
	    e.printStackTrace();
	    System.out.println("==========================================================");
	}
    }
    public void resetTitle() {
	title.setText("IP Address: "+netadd.host+":"+netadd.port+" - name "+netadd.name);
    }
    public void actionPerformed(ActionEvent aev) {
	Object srcobj = aev.getSource();
	String src;

	if(srcobj instanceof MenuItem) {
	    MenuItem mi = (MenuItem)srcobj;
	    src = mi.getLabel().toUpperCase();

	    if(src.equals("CLEAR SCREEN")) {text.setText("");System.gc();}
	    else if(src.equals("EXIT")) {System.exit(0);}

	    else if(src.equals("FINGER")) {new SendMessage("FINGER",toBox.getText(),mp);}
	    else if(src.equals("LURK")) {new SendMessage("LURK",toBox.getText(),mp);}
	    else if(src.equals("CHANGE DETAILS")) {print("use INFO blahblah to set your description");}
	    else if(src.equals("SEND FILE")) {print("use FILE <filename> <ip> to send a file");}

	    else if(src.equals("HELP")) {
		    print("==========================================================="+
			  "\n Type the IP addresses of the people to mail in the top"+
			  "\n (thin) box (seperated by slashes '/') if they are on a"+
			  "\n different port to you use ':' after the address (spaces"+
			  "\n are optional) eg"+
			  "\n 123.45.67.89 / 987.54.34.3 : 1234"+
			  "\n "+
			  "\n Then type your message in the bottom (thin) box, hit enter"+
			  "\n (return) to send to everyone in the address box"+
			  "\n===========================================================");
	    }
	    else if(src.equals("ABOUT")) {
		    print("============================================================="+
			  "\n= By =                                                     "+
			  "\n ShishTheMoomin@yahoo.com   freeware.mektrix.2ya.com       "+
			  "\n DO mail me any questions                                  "+
			  "\n DO NOT add me to any more spam lists! I have enough!      "+
			  "\n===========================================================");
	    }
	} else 
	if(srcobj instanceof Button) {
	    Button b = (Button)srcobj;
	    src = b.getLabel().toUpperCase();
	    if(src.equals("EXIT")) {System.exit(0);}
	} else 
	if(srcobj instanceof TextField) {
	    String c = messageBox.getText();
	    if(c.toUpperCase().startsWith("INFO")) {
       		userInfo = c.substring(c.indexOf(" "),c.length()).trim();
	    } else if(c.toUpperCase().startsWith("NAME")) {
       		netadd.name = c.substring(c.indexOf(" "),c.length()).trim();
		resetTitle();
	    } else if(c.toUpperCase().startsWith("HOST") && root) {
       		netadd.host = c.substring(c.indexOf(" "),c.length()).trim();
	    } else if(c.toUpperCase().startsWith("PORT") && root) {
       		netadd.port = Integer.parseInt(c.substring(c.indexOf(" "),c.length()).trim());
	    } else if(root) {
       		new SendMessage(c,toBox.getText(),mp);
	    } else {
       		new SendMessage("SAY "+c,toBox.getText(),mp);
	    }
	    messageBox.select(0,messageBox.getText().length());
	}
    }

    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e) {System.exit(0);}
    public void windowClosed(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowMaximized(WindowEvent e) {}
}

