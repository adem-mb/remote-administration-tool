package application.connection;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import application.Utils.Functions;

public class Server extends Thread {
	 boolean stop=false;
	private Handler Controller;
	private Integer sock = -1;
	private Socket serverSocket[] = new Socket[999999];
	public static String SPL = "\\\\+*-";
	public static String ANSPL = "\\\\#+*-";
	private List<Integer> Olist = new ArrayList<Integer>();
	private int port;
	
	public  void close(int sn){
		try {
			
			serverSocket[sn].close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static String Quote(String s){
		if(s.contains(SPL)){      
			s="QUT"+s;
			String rx ="(\\\\\\\\\\+)";
			StringBuffer sb = new StringBuffer();
			Pattern p=Pattern.compile(rx);
			Matcher m=p.matcher(s);
			while(m.find()){
				m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "#"));
			}
			m.appendTail(sb);
			return sb.toString();
		}
		
		return s;
	}
	public static String Unquote(String s){
		if(s.startsWith("QUT")){
			s=s.substring(3);
			String rx ="(\\\\\\\\\\+)(#)";
			StringBuffer sb = new StringBuffer();
			Pattern p=Pattern.compile(rx);
			Matcher m=p.matcher(s);
			while(m.find()){
				m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1)));
			}
			m.appendTail(sb);
			return sb.toString();
		}
		
		return s;
	}
	public Server(int port,Handler hnd){
		this.port=port;
		this.Controller=hnd;
		start();
		
	}
	public void set(Handler l) {

		Controller = l;
	}
	public synchronized void send(int sn, String data) {
		try {
			
			DataOutputStream dOut = new DataOutputStream(serverSocket[sn].getOutputStream());
			byte[] b=Functions.Str2Byte(data);
			dOut.writeInt(b.length); 
			dOut.write(b);
			dOut.flush();
			/*OutputStream dOut = serverSocket[sn].getOutputStream();
			byte[] b=Functions.Str2Byte(data);
	        int toSendLen = b.length;
	        byte[] toSendLenBytes = new byte[4];*/
	      /*  toSendLenBytes[0] = (byte)(toSendLen & 0xff);
	        toSendLenBytes[1] = (byte)((toSendLen >> 8) & 0xff);
	        toSendLenBytes[2] = (byte)((toSendLen >> 16) & 0xff);
	        toSendLenBytes[3] = (byte)((toSendLen >> 24) & 0xff);*/
	       /* final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	        bb.order(ByteOrder.BIG_ENDIAN);
	        bb.putInt(toSendLen);
	      
	        dOut.write(bb.array());

	        dOut.write(b);*/
	        
		} catch (IOException e) {
			Controller.Diconnected(sn);
		}
	}

	public void send(int sn, byte data[]) {
		try {
			
					DataOutputStream dOut = new DataOutputStream(serverSocket[sn].getOutputStream());

					dOut.writeInt(data.length); 
					dOut.write(data);
					
					dOut.flush();
			/*OutputStream dOut = serverSocket[sn].getOutputStream();
			
	        int toSendLen = data.length;
	        byte[] toSendLenBytes = new byte[4];

	        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	        bb.order(ByteOrder.BIG_ENDIAN);
	        bb.putInt(toSendLen);
	      
	        dOut.write(bb.array());
	        dOut.write(data);*/


		} catch (IOException e) {
			
			Controller.Diconnected(sn);
		}
	}
	private Integer getID() {
		re: while (true) {
			sock += 1;
			if (sock == 99999) {
				sock = 0;
			}
			synchronized (this) {
				if (Olist.contains(sock))
					break re;
				else
					Olist.add(sock);
			}
				
			return sock;
		}
		return sock;
	}
	public void removeClient(Integer sn){
		synchronized (this) {
			if(stop==false)
			Olist.remove(sn);
		}
	}
	
	public void dispose() {
		synchronized (this) {
			stop=true;
			try {
				if(ListenerSocket!=null)
				ListenerSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(Integer i:Olist){
				try {
					serverSocket[i].close();
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Olist.clear();
		
			Controller.setStatus(false);
			
		}
	}
	ServerSocket ListenerSocket;
	@Override
	public void run() {

		try {
			ListenerSocket = new ServerSocket(port);
			Controller.setStatus(true);
		} catch (IOException e) {
			Controller.Exceptions("Port already "+port+ " in use");
			
			return;
		}Socket s = null;
		try {
			//ListenerSocket.setReceiveBufferSize(5242880);
			
			while (true) {
				
				
					s = ListenerSocket.accept();
		
				Integer i = getID();
				s.setReceiveBufferSize(50000);
				serverSocket[i] = s;
				

				new ServerFromClient(s, Controller, i);
	
				
				
				
				send(i, "NewC" + SPL);
			}
		} catch(java.net.SocketException e2){
			e2.printStackTrace();
			try {
				if(s!=null)
				s.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}

/*class ServerToClient extends Thread {

	Socket clientSocket;

	ServerToClient(Socket cs) {

		clientSocket = cs;
		start();
	}

	public void run() {

		try {
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String s;

			while ((s = in.readLine()) != null) {
				out.println(s);
			}
		} catch (IOException e) {
		}

	}
}*/

class ServerFromClient extends Thread {

	Socket socket;
	Handler Controller;
	int sn;
	
	ServerFromClient(Socket ss, Handler a, int sn) {
		this.Controller = a;
		socket = ss;
		this.sn = sn;
		start();
		
	}

	public void run() {

		DataInputStream dIn;
		try {
			dIn = new DataInputStream(socket.getInputStream());
	
			int time=0;
		int length;
		while (((length = dIn.readInt()) > 0) ) {
			
	           try{
			if(length>0) {
		    byte[] message = new byte[length];
		    dIn.readFully(message, 0, message.length); // read the message
	

		    Controller.Data(message, sn);
		
		  /*  if(time==20){
		    	System.gc();
		    	time=0;
		    }else{
		    	time++;
		    }*/
		    
			}}
			catch (java.lang.OutOfMemoryError e2) {
				System.gc();
				System.out.println("err");
			}
		}
		} catch (Exception e1) {
			Controller.Diconnected(sn);
		}  
		finally {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Controller.Diconnected(sn);
		}
		

		/*InputStream dIn;
		try {
			dIn = socket.getInputStream();
			int length ;
			do {
				byte le[] = new byte[4];
				dIn.read(le, 0, 4); // read the message
				ByteBuffer wrapped = ByteBuffer.wrap(le); // big-endian by
				//wrapped.order(ByteOrder.LITTLE_ENDIAN)	;										// default
				length = wrapped.getInt();
				

				if (length > 0) {
					byte[] message = new byte[length];
					dIn.read(message, 0, length); // read the message
			
					System.out.println(length);
					Controller.Data(message, sn);
					
				}
			} while (length != 0);

		} catch (IOException e1) {
	
		}    
		Controller.Diconnected(sn);*/
	}

}