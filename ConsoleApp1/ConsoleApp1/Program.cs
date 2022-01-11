using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.IO.Pipes;

using System.Text;
using MirrorDriver;
using System.Collections.Generic;
using Rectangle = System.Drawing.Rectangle;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.Threading;

namespace ConsoleApp1
{
    class Program
    {
        public static DesktopMirror MirrorDriver { get; set; }
        private static List<Rectangle> DesktopChanges { get; set; }
        private static void MirrorDriver_DesktopChange(object sender, DesktopMirror.DesktopChangeEventArgs e)
        {
            var rectangle = new Rectangle(e.Region.x1, e.Region.y1, e.Region.x2 - e.Region.x1,
                                                e.Region.y2 - e.Region.y1);
            DesktopChanges.Add(rectangle);
        }
        static Connection c = null;
        static void Main(string[] args)
        {

          
            /* c = new Connection("197.207.161.25", 84, 0);
              Console.WriteLine(BitConverter.IsLittleEndian);*/
           // ImageProvider mp ;
            // String message = Console.ReadLine();
            DesktopShare ds=null;
              String ip;
            int lascur = 0;
                int port, socketNumber;
                char[] sep = { '|', '|' };
                while (true)
                {
                    String message = Console.ReadLine();

                    String[] param = message.Split(new string[] { "||" }, StringSplitOptions.None);
                //     Socket clientSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

                switch (param[0])
                {
                    case "info":
                        {
                                Console.WriteLine(param[1]+ param[2]+ param[3]);
                                ip = param[1];
                            port = Int32.Parse(param[2]);
                            socketNumber = Int32.Parse(param[3]);
                            c = new Connection(ip, port,socketNumber);
                            Console.WriteLine("done");

                            break;
                        }
                    case "start":
                        {
                            if(ds!= null)
                                ds.Dispose();
                            //mp = new ImageProvider(ref c);
                            ds = new DesktopShare(ref c);
                           // ds.share1();
                            break;
                        }
                    case "interval":
                        {
                            if (ds != null)
                                ds.setInterval(Int32.Parse(param[1]));
                            break;
                        }
                    case "format":
                        {
                            if (ds != null) { 
                                ds.SetFormat(Int32.Parse(param[1]));
                                ds.restart(true);
                            }
                            break;
                        }
                    /*case "sendIM":
                        {
                            ds.share1();
                            break;
                        }*/
                    case "MouseMove":
                        {
                           // Console.WriteLine("mouseMove");
                           // VirtualMouse.MoveTo(Int32.Parse(param[1]), Int32.Parse(param[2]));
                            int cur = VirtualMouse.GetCursorState();

                            if (lascur != cur) {
                                byte curs;
                                if (cur == Cursors.Default.Handle.ToInt32())
                                    curs = 0;
                                else if (cur == Cursors.SizeWE.Handle.ToInt32())
                                    curs = 1;
                                else if (cur == Cursors.SizeNS.Handle.ToInt32())
                                    curs = 2;
                                else if (cur == Cursors.WaitCursor.Handle.ToInt32())
                                    curs = 3;
                                else if (cur == Cursors.IBeam.Handle.ToInt32())
                                    curs = 4;
                                else if (cur == Cursors.SizeNWSE.Handle.ToInt32())
                                    curs = 5;
                                else if (cur == Cursors.SizeNESW.Handle.ToInt32())
                                    curs = 6;
                                else if (cur == Cursors.Hand.Handle.ToInt32())
                                    curs = 7;
                                else
                                    curs=0;
                                /*byte[] x = BitConverter.GetBytes(cur);
                            Array.Reverse(x);*/
                            MemoryStream ms = new MemoryStream();
                                lascur = cur;
                            ms.WriteByte(2);
                            ms.WriteByte(curs);
                            c.send(ms.ToArray());
                            }
                            break;
                        }
                    case "pause":
                        {
                            ds.pause();
                            break;
                        }
                    case "resume":
                        {
                            ds.resume();
                            break;
                        }
                }
                Thread.Sleep(15);
            }
            /*    pipeServer =new NamedPipeServerStream("NADpipe"+param[1], PipeDirection.InOut, 4);
                           StreamReader sr = new StreamReader(pipeServer);

                           StreamWriter sw = new StreamWriter(pipeServer);
                           sw.AutoFlush = true;*/
            /*  while (true)
              {
                  MirrorDriver = new DesktopMirror();
                  DesktopChanges = new List<Rectangle>();

                  MirrorDriver.DesktopChange += new EventHandler<DesktopMirror.DesktopChangeEventArgs>(MirrorDriver_DesktopChange);

                  //Create pipe instance
                  NamedPipeServerStream pipeServer =
                  new NamedPipeServerStream("testpipe", PipeDirection.InOut, 4);
                  Console.WriteLine("[ECHO DAEMON] NamedPipeServerStream thread created.");

                  //wait for connection
                  Console.WriteLine("[ECHO DAEMON] Wait for a client to connect");
                  pipeServer.WaitForConnection();

                  Console.WriteLine("[ECHO DAEMON] Client connected.");

                      // Stream for the request. 
                      StreamReader sr = new StreamReader(pipeServer);
                      // Stream for the response. 
                      StreamWriter sw = new StreamWriter(pipeServer);
                      sw.AutoFlush = true;
                  String s = "hello منيتبليبليبليبيبل";

                  byte[] data = Encoding.ASCII.GetBytes(s);
                  byte[] datalen= BitConverter.GetBytes(data.Length);
                  ImageProvider mp = new ImageProvider(ref pipeServer, ref sw,ref sr);

                      Console.WriteLine("next");


                  }*/
            // pipeServer.Disconnect();

        }

        //  pipeServer.Close();
    }

    public static class VirtualMouse
    {
        public static int GetCursorState()
        {
           

            CURSORINFO pci;
            pci.cbSize = Marshal.SizeOf(typeof(CURSORINFO));
            GetCursorInfo(out pci);


            return pci.hCursor.ToInt32();
        }

        [StructLayout(LayoutKind.Sequential)]
        struct POINT
        {
            public Int32 x;
            public Int32 y;
        }

        [StructLayout(LayoutKind.Sequential)]
        struct CURSORINFO
        {
            public Int32 cbSize;        // Specifies the size, in bytes, of the structure. 
                                        // The caller must set this to Marshal.SizeOf(typeof(CURSORINFO)).
            public Int32 flags;         // Specifies the cursor state. This parameter can be one of the following values:
                                        //    0             The cursor is hidden.
                                        //    CURSOR_SHOWING    The cursor is showing.
            public IntPtr hCursor;          // Handle to the cursor. 
            public POINT ptScreenPos;       // A POINT structure that receives the screen coordinates of the cursor. 
        }

     
   
        [DllImport("user32.dll")]
        static extern bool GetCursorInfo(out CURSORINFO pci);
        [DllImport("user32.dll")]
        static extern void mouse_event(int dwFlags, int dx, int dy, int dwData, int dwExtraInfo);
        private const int MOUSEEVENTF_MOVE = 0x0001;
        private const int MOUSEEVENTF_LEFTDOWN = 0x0002;
        private const int MOUSEEVENTF_LEFTUP = 0x0004;
        private const int MOUSEEVENTF_RIGHTDOWN = 0x0008;
        private const int MOUSEEVENTF_RIGHTUP = 0x0010;
        private const int MOUSEEVENTF_MIDDLEDOWN = 0x0020;
        private const int MOUSEEVENTF_MIDDLEUP = 0x0040;
        private const int MOUSEEVENTF_ABSOLUTE = 0x8000;
        public static void Move(int xDelta, int yDelta)
        {
            mouse_event(MOUSEEVENTF_MOVE, xDelta, yDelta, 0, 0);
        }
        public static void MoveTo(float x, float y)
        {
            float min = 0;
            float max = 65535;

            int mappedX = (int)Remap(x, 0.0f, System.Windows.Forms.Screen.PrimaryScreen.Bounds.Width, min, max);
            int mappedY = (int)Remap(y, 0.0f, System.Windows.Forms.Screen.PrimaryScreen.Bounds.Height, min, max);

            mouse_event(MOUSEEVENTF_ABSOLUTE | MOUSEEVENTF_MOVE, mappedX, mappedY, 0, 0);
        }


        public static float Remap(float value, float from1, float to1, float from2, float to2)
        {
            return (value - from1) / (to1 - from1) * (to2 - from2) + from2;
        }
        public static void LeftClick()
        {
            mouse_event(MOUSEEVENTF_LEFTDOWN, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
            mouse_event(MOUSEEVENTF_LEFTUP, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
        }

        public static void LeftDown()
        {
            mouse_event(MOUSEEVENTF_LEFTDOWN, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
        }

        public static void LeftUp()
        {
            mouse_event(MOUSEEVENTF_LEFTUP, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
        }

        public static void RightClick()
        {
            mouse_event(MOUSEEVENTF_RIGHTDOWN, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
            mouse_event(MOUSEEVENTF_RIGHTUP, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
        }

        public static void RightDown()
        {
            mouse_event(MOUSEEVENTF_RIGHTDOWN, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
        }

        public static void RightUp()
        {
            mouse_event(MOUSEEVENTF_RIGHTUP, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
        }
    }

}
    

