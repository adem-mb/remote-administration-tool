using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.IO.Pipes;
using System.Windows.Forms;
using System.Text;
using MirrorDriver;
using System.Collections.Generic;
using Rectangle = System.Drawing.Rectangle;
using System.Timers;
using System.Threading;

namespace ConsoleApp1
{
    class ImageProvider
    {
        public  DesktopMirror MirrorDriver { get; set; }
        private  List<Rectangle> DesktopChanges { get; set; }
        private Connection conn;
        bool DR_state=true;
      
        private  void MirrorDriver_DesktopChange(object sender, DesktopMirror.DesktopChangeEventArgs e)
        {
            var rectangle = new Rectangle(e.Region.x1, e.Region.y1, e.Region.x2 - e.Region.x1,
                                                e.Region.y2 - e.Region.y1);
            if ((rectangle.X == 0) & (rectangle.Y == 0) & (rectangle.Width == 0) & (rectangle.Height == 0)) ;
            else {
                lock (DesktopChanges) {
                    DesktopChanges.Add(rectangle);
                }
            }
                    

            
            
        }
        Thread thread;
        
        public ImageProvider(ref Connection conn)
        {
 
            MirrorDriver = new DesktopMirror();
            DesktopChanges = new List<Rectangle>();
            this.conn = conn;
            MirrorDriver.Load();
            MirrorDriver.Connect();
            MirrorDriver.Start();
            MirrorDriver.DesktopChange += new EventHandler<DesktopMirror.DesktopChangeEventArgs>(MirrorDriver_DesktopChange);
            thread = new Thread(T_Tick);

            thread.Start();

        }
        public void stop()
        {
            thread.Abort();
            MirrorDriver.Dispose();
        }
        public int interval =30;
        private void T_Tick()
        {
            Thread.CurrentThread.IsBackground = true;
           
            bool ft = true;
            Bitmap lastScreenshot=null;
            while (true)
            {
                Bitmap screenshot = MirrorDriver.GetScreen();
                if (lastScreenshot==null)
                {

                    var stream = new MemoryStream();
                    screenshot.Save(stream, ImageFormat.Jpeg);
                    MemoryStream trm = new MemoryStream();
                    byte[] comm = { 0 };

                    byte[] img = stream.ToArray();
                    trm.Write(comm, 0, comm.Length);

                    trm.Write(img, 0, img.Length);
                    byte[] tram = trm.ToArray();
                    conn.send(tram);



                }
                else if(lastScreenshot.Width!=screenshot.Width || lastScreenshot.Height != screenshot.Height)
                {
                    var stream = new MemoryStream();
                    screenshot.Save(stream, ImageFormat.Jpeg);
                    MemoryStream trm = new MemoryStream();
                    byte[] comm = { 0 };

                    byte[] img = stream.ToArray();
                    trm.Write(comm, 0, comm.Length);

                    trm.Write(img, 0, img.Length);
                    byte[] tram = trm.ToArray();
                    conn.send(tram);
                }
                else
                {
                    // Send them the list of queued up changes
                    var regions = (List<Rectangle>)GetOptimizedRectangleRegions();
                    // var regions = new List<Rectangle>(DesktopChanges);

                    // var watch = System.Diagnostics.Stopwatch.StartNew();
                
                    /*  watch.Stop();
                      var elapsedMs = watch.ElapsedMilliseconds;
                      Console.WriteLine(elapsedMs);*/

                    for (int i = 0; i < regions.Count; i++)
                    {
                        if (regions[i].IsEmpty) continue;

                        Bitmap regionShot = null;

                        try
                        {
                            regionShot = screenshot.Clone(regions[i], PixelFormat.Format16bppRgb565);
                            //regionShot = MirrorDriver.GetScreen().Clone(regions[i], PixelFormat.Format16bppRgb565) ;
                        }
                        catch (OutOfMemoryException ex)
                        {

                        }

                        var stream = new MemoryStream();
                        regionShot.Save(stream, ImageFormat.Jpeg);
                        MemoryStream trm = new MemoryStream();
                        byte[] comm = { 1};

                        byte[] x = BitConverter.GetBytes(regions[i].X);
                        Array.Reverse(x);
                        byte[] y = BitConverter.GetBytes(regions[i].Y);
                        Array.Reverse(y);

                        byte[] img = stream.ToArray();
                        trm.Write(comm, 0, comm.Length);
                        trm.Write(x, 0, x.Length);
                        trm.Write(y, 0, y.Length);

                        trm.Write(img, 0, img.Length);
                        byte[] tram = trm.ToArray();
                        conn.send(tram);




                    }
                    
                }
              lastScreenshot = screenshot;
                Thread.Sleep(interval);
            }
        }
        public void ProvideImage()
        {
            if (MirrorDriver.State != DesktopMirror.MirrorState.Running)
            {
                // Most likely first time
                // Start the mirror driver
                MirrorDriver.Load();

                MirrorDriver.Connect();
                MirrorDriver.Start();

                Bitmap screenshot = MirrorDriver.GetScreen();
                var stream = new MemoryStream();
                screenshot.Save(stream, ImageFormat.Jpeg);
                MemoryStream trm = new MemoryStream();
                byte[] x = BitConverter.GetBytes(0);
                byte[] y = BitConverter.GetBytes(0);
                byte[] width = BitConverter.GetBytes(Screen.PrimaryScreen.Bounds.Width);
                byte[] height = BitConverter.GetBytes(Screen.PrimaryScreen.Bounds.Height);
                byte[] img = stream.ToArray();
                trm.Write(x, 0, x.Length);
                trm.Write(y, 0, y.Length);
                trm.Write(width, 0, width.Length);
                trm.Write(height, 0, height.Length);
                trm.Write(img, 0, img.Length);
                byte[] tram = trm.ToArray();


                //SendFragmentedBitmap(stream.ToArray(), Screen.PrimaryScreen.Bounds);

            }
            /*else
            {

                Bitmap screenshot = MirrorDriver.GetScreen();
                var stream = new MemoryStream();
                screenshot.Save(stream, ImageFormat.Png);
                MemoryStream trm = new MemoryStream();
                byte[] x = BitConverter.GetBytes(0);
                byte[] y = BitConverter.GetBytes(0);
                byte[] width = BitConverter.GetBytes(Screen.PrimaryScreen.Bounds.Width);
                byte[] height = BitConverter.GetBytes(Screen.PrimaryScreen.Bounds.Height);
                byte[] img = stream.ToArray();
                trm.Write(x, 0, x.Length);
                trm.Write(y, 0, y.Length);
                trm.Write(width, 0, width.Length);
                trm.Write(height, 0, height.Length);
                trm.Write(img, 0, img.Length);
                byte[] tram = trm.ToArray();

                sw.WriteLine(tram.Length);
                pipeServer.Write(tram, 0, tram.Length);
                Console.WriteLine("done");
                

            }*/
          else if (MirrorDriver.State == DesktopMirror.MirrorState.Running)
            {
                Console.WriteLine("done");
                // Send them the list of queued up changes
                var regions = (List<Rectangle>)GetOptimizedRectangleRegions();
               // var regions = new List<Rectangle>(DesktopChanges);

                
                Bitmap screenshot = MirrorDriver.GetScreen();



                for (int i = 0; i < regions.Count; i++)
                {
                    if (regions[i].IsEmpty) continue;

                    Bitmap regionShot = null;

                    try
                    {
                        regionShot = screenshot.Clone(regions[i], PixelFormat.Format16bppRgb565);
                    }
                    catch (OutOfMemoryException ex)
                    {
                       
                    }

                    var stream = new MemoryStream();
                    regionShot.Save(stream, ImageFormat.Jpeg);
                    MemoryStream trm = new MemoryStream();
                    byte[] x = BitConverter.GetBytes(regions[i].X);
                    Array.Reverse(x);
                    byte[] y = BitConverter.GetBytes(regions[i].Y);
                    Array.Reverse(y);

                 
                    byte[] img = stream.ToArray();
                    trm.Write(x, 0, x.Length);
                    trm.Write(y, 0, y.Length);
             
                    trm.Write(img, 0, img.Length);
                    byte[] tram = trm.ToArray();
                    byte[] x2=new byte[4];

                    Array.Copy(tram, x2, 4);
                    int x8 = BitConverter.ToInt32(x2, 0);
                    Console.WriteLine(x8 + " " + regions[i].X+" "+BitConverter.IsLittleEndian);

                    
                    //SendFragmentedBitmap(stream.ToArray(), regions[i]);

                }
               

            }
        }
        public IList<Rectangle> GetOptimizedRectangleRegions()
        {
            var desktopChangesCopy = new List<Rectangle>(DesktopChanges);
            DesktopChanges.Clear();
            lock (DesktopChanges)
            {
                desktopChangesCopy.ForEach((x) => desktopChangesCopy.ForEach((y) =>
            {
                if (x != y && x.Contains(y))
                {
                    desktopChangesCopy.Remove(y);
                }
            }));

                return desktopChangesCopy;
            }
        }

    }
}
