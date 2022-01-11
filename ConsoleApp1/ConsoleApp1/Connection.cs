using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;

namespace ConsoleApp1
{

    class Connection
    {
        private String ip;
        private int port, socketNumber;
        object lo = new Double();
        private Socket clientSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
        Socket sendSocket;
        public Connection(String ip, int port, int socketNumber)
        {

            this.ip = ip;
            this.port = port;
            this.socketNumber = socketNumber;
         /* sendSocket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, ProtocolType.Udp);
            sendSocket.SendBufferSize = 1000;*/
            IPEndPoint serverAddress = new IPEndPoint(IPAddress.Parse(ip), port);

              clientSocket.Connect(serverAddress);
              clientSocket.SendTimeout = 700;
              clientSocket.SendBufferSize = 50000;

            /*   Thread thread = new Thread(RECV);

               thread.Start();*/



        }
        public void reconnect()
        {
            IPEndPoint serverAddress = new IPEndPoint(IPAddress.Parse(ip), port);
            int i = 1;
            bool notconn = true;
            clientSocket= new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

            while (i<3 && notconn) { 
            try {
                   
                    clientSocket.Connect(serverAddress);
            clientSocket.SendTimeout = 700;
            clientSocket.SendBufferSize = 50000;
                    notconn = false;
                    Thread.Sleep(500);
                    
            }
            catch(Exception ex)
            {
                    
                    if (i == 2)
                        Environment.Exit(0);
            }
                i++;
                if(i>3)
                    Environment.Exit(0);
            }
            if (i > 3)
                Environment.Exit(0);
        }
        public void sendUdp(byte[] data,int lengh)
        {
            
            IPAddress serverIPAddress = IPAddress.Parse(ip);


            //byte[] ty = Encoding.ASCII.GetBytes("BYTE");
            /* byte[] imglen = System.BitConverter.GetBytes(lengh);

             Array.Reverse(imglen);
             byte[] socketNumberbytes = System.BitConverter.GetBytes(socketNumber);
             Array.Reverse(socketNumberbytes);
             MemoryStream memoryStream = new MemoryStream();
             //  memoryStream.Write(ty, 0, ty.Length);
             memoryStream.Write(imglen, 0, imglen.Length);
             memoryStream.Write(socketNumberbytes, 0, socketNumberbytes.Length);
             memoryStream.Write(data, 0, data.Length);
             byte[] message = memoryStream.ToArray();*/
            byte[] message = new byte[1000];
            for(int i = 0; i < 1000;i++)
            {
                message[i] = 0;
            }
            IPEndPoint serverEP = new IPEndPoint(serverIPAddress, 87);


            sendSocket.SendTo(message, serverEP);
        }
        public uint endian_sensitiveConversion(int d, bool cvtToLittleEndian)
        {
            // determine endian-type of this machine
            bool thisMachineLittleEndian = BitConverter.IsLittleEndian;

            // receives result of conversion from decimal to int
            uint i = Convert.ToUInt32(d);

            // result found here
            uint j = i;

            if (cvtToLittleEndian != thisMachineLittleEndian)
            {
                int low = 0;
                int hi = 3;
                byte spare;
                byte[] x = BitConverter.GetBytes(i);

                while (low < hi)
                {
                    spare = x[hi];
                    x[hi--] = x[low];
                    x[low++] = spare;
                }
                j = BitConverter.ToUInt32(x, 0);
            }

            return j;
        }
       
        public void send(byte[] buffer)
        {
        


                byte[] ty = Encoding.ASCII.GetBytes("BYTE");

                byte[] socketNumberbytes = System.BitConverter.GetBytes(socketNumber);
                Array.Reverse(socketNumberbytes);
                MemoryStream memoryStream = new MemoryStream();
                memoryStream.Write(ty, 0, ty.Length);
                memoryStream.Write(socketNumberbytes, 0, socketNumberbytes.Length);
                memoryStream.Write(buffer, 0, buffer.Length);
                byte[] lo = memoryStream.ToArray();

                byte[] toSendLenBytes = System.BitConverter.GetBytes(lo.Length);

                Array.Reverse(toSendLenBytes);
                lock (lo)
                {
                    try
                    {
                        clientSocket.Send(toSendLenBytes);

                        /*myStream.Write(toSendLenBytes, 0, toSendLenBytes.Length);
                        myStream.Write(lo, 0, lo.Length);*/
                        clientSocket.Send(lo);
                    }
                    catch (Exception ex)
                    {
                    Console.WriteLine("it happend");
                        Thread.Sleep(120);
                        reconnect();
                    }
                }
            
        }
        void RECV()
        {
            bool conn = true;
            while (conn)
            {
                try
                {
                    byte[] rcvLenBytes = new byte[4];
                    clientSocket.Receive(rcvLenBytes);
                    int rcvLen = System.BitConverter.ToInt32(rcvLenBytes, 0);
                    byte[] rcvBytes = new byte[rcvLen];
                    clientSocket.Receive(rcvBytes);
                    String rcv = System.Text.Encoding.ASCII.GetString(rcvBytes);


                }
                catch (Exception ex)
                {
                    conn = false;
                }
            }
        }
    }
}
