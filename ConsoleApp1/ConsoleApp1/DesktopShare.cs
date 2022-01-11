using DesktopDuplication;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;

namespace ConsoleApp1
{
    class DesktopShare
    {
        private Connection conn;
        private object interval = 70;
        private object ImgFormat = 0;
        private Thread thread;
        private Boolean pause_state = false;
        Object lock2 = new Object();
        public DesktopDuplication.DesktopDuplicator desktop;
        public DesktopShare(ref Connection conn)
        {
            this.conn = conn;
            thread = new Thread(share);
            thread.Start();

        }
        public void setInterval(int interval)
        {
            lock (this.interval)
            {
                this.interval = interval;
            }
        }
        public void restart(bool value)
        {
            /* Console.WriteLine("restart");
             thread.Abort();
             thread.Start();*/
            lock (rest)
            {
                rest = value;
            }
        }
        public void SetFormat(int format)
        {
            lock (ImgFormat)
            {
                ImgFormat = format;
            }
        }
        public void pause()
        {
            lock (lock2)
            {
                pause_state = true;
            }
        }
        public void resume()
        {
            lock (lock2)
            {
                pause_state = false;
            }
        }
        public ImageFormat GetFormat()
        {
            lock (ImgFormat)
            {
                if ((int)ImgFormat == 0)
                    return ImageFormat.Png;
                else
                    return ImageFormat.Jpeg;
            }
        }
        object rest = false;
        private void share()
        {
            desktop = new DesktopDuplicator(0);

            int width = 0, hight = 0;
            while (true)
            {
                lock (lock2)
                {
                    if (!pause_state)
                    {
                        try
                        {
                            DesktopFrame frame = desktop.GetLatestFrame();
                            if (frame != null)
                            {
                                lock (rest)
                                {
                                    Bitmap image = frame.DesktopImage;
                                    if (width == 0)
                                    {

                                        var stream = new MemoryStream();
                                        image.Save(stream, GetFormat());
                                        MemoryStream trm = new MemoryStream();
                                        byte[] comm = { 0 };

                                        byte[] img = stream.ToArray();
                                        trm.Write(comm, 0, comm.Length);

                                        trm.Write(img, 0, img.Length);
                                        byte[] tram = trm.ToArray();
                                        conn.send(tram);
                                       // conn.sendUdp(tram, img.Length);
                                    }

                                    else if ((width != image.Width || hight != image.Height) || (bool)rest == true)
                                    {
                                        var stream = new MemoryStream();
                                        image.Save(stream, GetFormat());
                                        MemoryStream trm = new MemoryStream();
                                        byte[] comm = { 0 };

                                        byte[] img = stream.ToArray();
                                        trm.Write(comm, 0, comm.Length);

                                        trm.Write(img, 0, img.Length);
                                        byte[] tram = trm.ToArray();
                                        conn.send(tram);
                                        //conn.sendUdp(tram, img.Length);
                                        rest = false;
                                    }

                                    else
                                    {

                                        if (frame.UpdatedRegions != null)
                                            foreach (var updated in frame.UpdatedRegions)
                                            {

                                                var stream = new MemoryStream();

                                                image.Clone(updated, PixelFormat.Format32bppRgb).Save(stream, GetFormat());
                                                MemoryStream trm = new MemoryStream();
                                                byte[] comm = { 1 };

                                                byte[] x = BitConverter.GetBytes(updated.X);
                                                Array.Reverse(x);
                                                byte[] y = BitConverter.GetBytes(updated.Y);
                                                Array.Reverse(y);

                                                byte[] img = stream.ToArray();
                                                trm.Write(comm, 0, comm.Length);
                                                trm.Write(x, 0, x.Length);
                                                trm.Write(y, 0, y.Length);

                                                trm.Write(img, 0, img.Length);
                                                byte[] tram = trm.ToArray();
                                               conn.send(tram);
                                                //conn.sendUdp(tram, img.Length);
                                            }

                                    }
                                    if (image != null)
                                    {
                                        width = image.Width;
                                        hight = image.Height;
                                    }
                                }
                            }
                        }
                        catch (Exception ex)
                        {


                            Console.WriteLine(ex);
                        }
                    }
                }
                lock (interval)
                {
                    Thread.Sleep((int)interval);
                }

            }
        }

        public void Dispose()
        {
            thread.Abort();
        }

    }
}
