package application.connection;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer extends Thread{
    DatagramSocket socket;
    Handler controller;
    public UdpServer(int port,Handler controller) {
    	   try {
			socket = new DatagramSocket (port);
			this.controller=controller;
			start();
		} catch (SocketException e) {
			
			e.printStackTrace();
		}
	}
    @Override
    public void run() {
    	 try

         {

             DatagramPacket packet;

             byte[] buf = new byte[1000];
            socket.setReceiveBufferSize(1000);
             while (true)

             {

                 packet = new DatagramPacket (buf, buf.length);

                 socket.receive (packet);
                
                 System.out.println ("Received packet");
                // controller.Data(packet.getData(), -1);
   
             }

         }

         catch (IOException e)

         {
        	e.printStackTrace();
         }
    }

}
