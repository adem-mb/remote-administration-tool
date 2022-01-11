package application;

import static application.Functions.calculatesize;

import java.awt.AWTException;

import java.awt.Robot;

import java.awt.event.InputEvent;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;

import application.connection.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import static application.connection.Client.*;

public class MainController implements Handler, Initializable {
	@FXML
	private JFXCheckBox startup;

	@FXML
	private JFXCheckBox permission_ch;

	@FXML
	private JFXButton cn_btn;

	@FXML
	private JFXButton ok_btn;

	@FXML
	private TextArea servers_text;

	@FXML
	private JFXCheckBox hidden;
	@FXML
	private TextField group_text;

	private boolean permission = false;

	String group = "work";
	private Object lock = new Object();
	private Socket sock;
	private Client client[] = null;
	private Robot rb;
	private String sfile;
	private ScreenProcess screen;
	private Main stage;
	private String servlist;
	public static busyObject busy;
	public static String APP_PATH;

	public MainController(String sfile, Main mystage) {
		this.sfile = sfile;
		this.stage = mystage;
		busy = new busyObject(false, -1);
		try {
			rb = new Robot();
		} catch (AWTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void connector() {

		if (servlist != null) {
			String servsers[] = servlist.split("\n");

			if (client != null) {
				if (screen != null)
					screen.Dispose();

				for (Client cc : client) {

					cc.Stop_client();

				}
			} else {
				client = null;
				client = new Client[servsers.length];
			}
			stage.statusController.clear();
			client = new Client[servsers.length];
			for (int i = 0; i < servsers.length; i++) {
				final int j = i;
				String param[] = servsers[i].split(":");
				System.out.println(i);
				// Thread connn = new Thread(new Runnable() {
				stage.statusController.addItem(servsers[i]);
				// @Override
				// public void run() {
				client[j] = new Client(param[0], Integer.valueOf(param[1]), MainController.this, j);

				// }
				// });
				// connn.start();
			}
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (sfile != null)
			if (sfile.length() >= 5) {
				String settings[] = sfile.split("\n");
				if (settings[1].equals("true")) {
					startup.setSelected(true);
				}
				if (settings[2].equals("true")) {
					permission_ch.setSelected(true);
					permission = true;
				}
				group = settings[3];
				group_text.setText(settings[3]);
				StringBuilder sb = new StringBuilder();
				for (int i = 4; i < settings.length; i++) {
					sb.append(settings[i]);
					sb.append("\n");
				}
				servers_text.setText(sb.toString());
				servlist = sb.toString();
			}
		hidden.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// TODO Auto-generated method stub

				permission_ch.setDisable(newValue);

			}
		});
		connector();
		System.out.println(servlist);
	}

	@FXML
	void close(ActionEvent event) {
		stage.closeStage();

	}

	@FXML
	void save_and_close(ActionEvent event) {
		StringBuilder sb = new StringBuilder();
		String servers[] = servers_text.getText().split("\n");
		String newServers = null;
		String hidden1, permission1, startu;
		String gro = null;
		boolean restart = true;
		if (!servers_text.getText().equals(servers_text)) {
			for (String server : servers) {
				if (server != null)
					if (!server.equals("")) {
						String svs[] = server.split(":");
						if (svs.length == 2) {
							if (svs[0].equals("")) {
								Platform.runLater(() -> {
									Alert alert = new Alert(AlertType.ERROR, "Error parsing IP in: " + server,
											ButtonType.OK);
									alert.showAndWait();
								});
								return;
							} else {
								sb.append(server);
								sb.append("\n");
							}
						} else {
							Platform.runLater(() -> {
								Alert alert = new Alert(AlertType.ERROR, "Error parsing IP in: " + server,
										ButtonType.OK);
								alert.showAndWait();
							});
							return;
						}
					}

			}
			newServers = sb.toString();
		} else {
			restart = false;
			newServers = servlist;
		}
		if (permission_ch.isDisabled()) {
			permission = false;

			hidden1 = "true";

			permission1 = "false";
		} else {
			if (permission_ch.isSelected()) {
				permission = true;
				permission1 = "true";
			} else {
				permission = false;
				permission1 = "false";
			}
			hidden1 = "false";

		}
		if (startup.isSelected()) {
			startu = "true";
		} else {
			startu = "false";
		}
		if (group_text.getText().equals("")) {
			gro = "NOT NAMED";
		} else {
			gro = group_text.getText();
		}
		group = gro;
		File file = new File(MainController.APP_PATH + "\\config");
		String settings = hidden1 + "\n" + startu + "\n" + permission1 + "\n" + gro + "\n" + newServers;
		FileOutputStream writer;
		try {
			writer = new FileOutputStream(file);
			writer.write(settings.getBytes());
			writer.flush();
			writer.close();

		} catch (FileNotFoundException e) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stage.closeStage();
		if (hidden1.equals("true"))
			stage.hideme();
		if (restart) {

			servlist = newServers;
			connector();
		}

	}

	private void initfile(int clientNumber) {
		client[clientNumber].send("BrowserFilesInI" + SPL);
		String drives = getDrives();
		String drvs[] = drives.split(Pattern.quote("\\?"));
		int index = drvs[0].lastIndexOf('(');
		String drive = drvs[0].substring(index + 1, index + 3) + "\\";
		client[clientNumber].send("SetDrives" + SPL + drives);
		String files = getFiles(drive);
		if (files != null)
			client[clientNumber].send("BrowserFiles" + SPL + files);
	}

	private void initDos(int clientNumber) {

		client[clientNumber].send("commandInI" + SPL);
		try {
			client[clientNumber].startDos();
			InputStreamConsumer cons = new InputStreamConsumer(client[clientNumber].process.getInputStream(),
					clientNumber);
			cons.start();
		} catch (IOException ex) {
			Logger.getLogger(Reciver.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	private void INIRemoteDesktop(int clientNumber,String sk){
		client[clientNumber].send("ScreenInI" + SPL);
		

		if (screen == null)
			screen = new ScreenProcess(client[clientNumber], sk, this);
		else
			screen.Renew(client[clientNumber], sk);
		busy.busy = true;
		busy.clientNumber = clientNumber;
	}
	private void INIFroceDesktop(int clientNumber,String sk){
		busy.clientNumber = clientNumber;

		
		if (screen == null)
			screen = new ScreenProcess(client[clientNumber], sk, this);
		else
			screen.stop_and_Renew(client[clientNumber], sk);
	}

	@Override
	public void Data(byte[] data, int clientNumber) {

		String d[] = Functions.Byte2Str(data).split(Pattern.quote(SPL));

		switch (d[0]) {
		case "NewC": {
			String arch = System.getenv("PROCESSOR_ARCHITECTURE");
			String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

			String realArch = arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64") ? "64bit" : "32bit";
			client[clientNumber].send("NewC" + SPL
					+ client[clientNumber].getSocket().getLocalSocketAddress().toString()
							.substring(1, client[clientNumber].getSocket().getLocalSocketAddress().toString().length())
							.split(":")[0]
					+ SPL + System.getProperty("os.name") + " " + realArch + SPL + System.getProperty("user.name") + SPL
					+ group);
			break;

		}
		case "BrowsFilesINI": {

			Platform.runLater(() -> {
				if (permission) {
					ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
					ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
					Alert alert = new Alert(AlertType.INFORMATION, "Do you want to allow it ?", yes, no);
					alert.setHeaderText("A server want to browse files");
					alert.setTitle("Info");
					Optional<ButtonType> result = alert.showAndWait();

					if (result.isPresent() && result.get() == yes) {
						initfile(clientNumber);
					} else {
						client[clientNumber].send("refused" + SPL + "file");
					}

				} else {
					initfile(clientNumber);
				}

			});

			break;
		}
		case "GetFiles": {
			String files = getFiles(d[1]);
			if (files != null)
				client[clientNumber].send("BrowserFiles" + SPL + files);
			break;
		}
		case "BrowsFiles":
			String files = getFiles(d[1]);
			System.out.println(files);
			client[clientNumber].send("BrowserFiles" + SPL + files);
			break;
		case "CommandLineIn": {
			Platform.runLater(() -> {
				if (permission) {
					ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
					ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
					Alert alert = new Alert(AlertType.INFORMATION, "Do you want to allow it ?", yes, no);
					alert.setHeaderText("A server want to execute dos commands");
					alert.setTitle("Info");
					Optional<ButtonType> result = alert.showAndWait();

					if (result.isPresent() && result.get() == yes) {
						initDos(clientNumber);
					} else {
						client[clientNumber].send("refused" + SPL + "dos");
					}

				} else {
					initDos(clientNumber);
				}

			});
			break;
		}

		case "Command": {
			OutputStream wr = client[clientNumber].DosWriter;
			try {
				wr.write((d[1] + "\n").getBytes());
				wr.flush();
			} catch (IOException ex) {
				Logger.getLogger(Reciver.class.getName()).log(Level.SEVERE, null, ex);
			}
			break;
		}
		case "Screen": {
			Platform.runLater(() -> {
				synchronized (lock) {
					if (busy.busy == false) {
						if (permission) {
							ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
							ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
							Alert alert = new Alert(AlertType.INFORMATION, "Do you want to allow it ?", yes, no);
							alert.setHeaderText("A server want to control desktop");
							alert.setTitle("Info");
							Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
							stage.setAlwaysOnTop(true);
							stage.toFront();
							Optional<ButtonType> result = alert.showAndWait();

							if (result.isPresent() && result.get() == yes) {
								client[clientNumber].send("ScreenInI" + SPL);
								
							//	INIRemoteDesktop(clientNumber, d[1]);
							} else {
								client[clientNumber].send("refused" + SPL + "Desktop");
							}
						} else {
							client[clientNumber].send("ScreenInI" + SPL);
							//INIRemoteDesktop(clientNumber, d[1]);
						}

					} else {
						client[clientNumber].send("Busy" + SPL);
					}
				}
				/*
				 * if(permission){ ButtonType yes = new ButtonType("Yes",
				 * ButtonBar.ButtonData.YES); ButtonType no = new
				 * ButtonType("No", ButtonBar.ButtonData.NO); Alert alert = new
				 * Alert(AlertType.INFORMATION, "Do you want to allow it ?",
				 * yes, no);
				 * alert.setHeaderText("A server want to control desktop");
				 * alert.setTitle("Info"); Stage stage = (Stage)
				 * alert.getDialogPane().getScene().getWindow();
				 * stage.setAlwaysOnTop(true); stage.toFront();
				 * Optional<ButtonType> result = alert.showAndWait();
				 * 
				 * if (result.isPresent() && result.get() == yes) { synchronized
				 * (lock) { if (busy.busy == false) { String sk = d[1]; if
				 * (screen == null) screen = new
				 * ScreenProcess(client[clientNumber], sk,this); else
				 * screen.Renew(client[clientNumber], sk); busy.busy = true;
				 * busy.clientNumber = clientNumber; }else{
				 * client[clientNumber].send("Busy" + SPL); } } }else{
				 * client[clientNumber].send("refused" + SPL+"Desktop"); }
				 * 
				 * }else{ synchronized (lock) { if (busy.busy == false) { String
				 * sk = d[1]; if (screen == null) screen = new
				 * ScreenProcess(client[clientNumber], sk,this); else
				 * screen.Renew(client[clientNumber], sk); busy.busy = true;
				 * busy.clientNumber = clientNumber; }else{
				 * client[clientNumber].send("Busy" + SPL); } } }
				 */

			});

			break;
		}
		case "FScreen": {
			Platform.runLater(() -> {
				synchronized (lock) {
					if (permission) {
						System.out.println("yes");
						ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
						ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
						Alert alert = new Alert(AlertType.INFORMATION, "Do you want to allow it ?", yes, no);
						alert.setHeaderText("A server want to control desktop");
						alert.setTitle("Info");
						Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
						stage.setAlwaysOnTop(true);
						stage.toFront();
						Optional<ButtonType> result = alert.showAndWait();

						if (result.isPresent() && result.get() == yes) {
							client[busy.clientNumber].send("Interupted" + SPL);
							client[clientNumber].send("FScreenInI" + SPL);
						//	INIFroceDesktop(clientNumber, d[1]);
						} else {
							client[clientNumber].send("refused" + SPL + "Desktop");
						}

					} else {
						System.out.println("else");
						client[busy.clientNumber].send("Interupted" + SPL);
						client[clientNumber].send("FScreenInI" + SPL);
						// busy.busy=true;
					//	INIFroceDesktop(clientNumber, d[1]);
					}

				}
			});
			break;
		}
		case "mousepress1": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					rb.mousePress(InputEvent.BUTTON1_MASK);
				}
			}

			break;
		}
		case "startDeskt":{
			INIRemoteDesktop(clientNumber,d[1]);
			break;
		}
		case "FstartDeskt":{
			INIFroceDesktop(clientNumber,d[1]);
			break;
		}
		case "mouserelease1": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					rb.mouseRelease(InputEvent.BUTTON1_MASK);
				}
			}
			break;
		}
		case "mousepress2": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					rb.mousePress(InputEvent.BUTTON3_DOWN_MASK);
				}
			}

			break;
		}
		case "mouserelease2": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					rb.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
					
				}
			}
			break;
		}
		case "mousemove": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					rb.mouseMove(Integer.parseInt(d[1]), Integer.parseInt(d[2]));
					try {
						screen.send("MouseMove||" + d[1] + "||" + d[2]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			break;
		}
		case "keypress":{
			System.out.println("11");
			rb.keyPress(Integer.parseInt(d[1]));
			break;
		}
		case "keyrelease":{
			System.out.println("dfgdfg");
			rb.keyRelease(Integer.parseInt(d[1]));
			break;
		
		}
		case "Interval": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					try {
						screen.send("interval||" + d[1]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			break;
		}
		case "ImageF": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					String f = null;
					if (d[1].equals("PNG"))
						f = "0";
					else
						f = "1";
					try {
						screen.send("format||" + f);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			break;
		}
		case "ScreenC": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					screen.Dispose();
				}
			}
			break;
		}
		case "pause": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					screen.send("pause||");
				}
			}
			break;
		}
		case "resume": {
			synchronized (lock) {
				if (busy.clientNumber == clientNumber) {
					screen.send("resume||");
				}
			}
			break;
		}
		}

	}

	public void NotBusy() {
		synchronized (lock) {
			busy.busy = false;
		}
	}

	class InputStreamConsumer extends Thread {

		private InputStream is;
		private int clientNumber;

		public InputStreamConsumer(InputStream is, int clientNumber) {
			this.is = is;
			this.clientNumber = clientNumber;
		}

		@Override
		public void run() {

			try {
				int value = -1;
				DataOutputStream dOut = new DataOutputStream(client[clientNumber].getSocket().getOutputStream());
				while ((value = is.read()) != -1) {

					byte[] b = Functions.Str2Byte("command" + SPL + value);
					client[clientNumber].send(b);
					/*
					 * dOut.writeInt(b.length); dOut.write(b); dOut.flush();
					 */

				}
			} catch (IOException exp) {
				exp.printStackTrace();
			}

		}

	}

	@Override
	public void Diconnected(int clientNumber) {
		synchronized (lock) {
			if (busy.clientNumber == clientNumber) {
				screen.Dispose();
			}
		}
	}

	/*
	 * @Override public void Exceptions(String message) {
	 * 
	 * client.startconn(); }
	 */

	private String getFiles(String String_path) {

		try {
			File folder = new File(String_path);
			File files[] = folder.listFiles();
			String pa = folder.getCanonicalPath();
			StringBuilder fls = new StringBuilder();
			if (files != null)
				for (File file : files) {
					String type = null;
					java.nio.file.Path p = file.toPath();
					BasicFileAttributes view;

					view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
					String size = null;
					DateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss ");
					String date = df.format(view.creationTime().toMillis());
					if (file.isDirectory()) {
						type = "DIR";
						fls.append(file.getName() + "**?" + date + "**?" + type + "\\?");
					} else {
						type = "File";
						size = calculatesize(file.length());
						fls.append(file.getName() + "**?" + size + "**?" + date + "**?" + type + "\\?");
					}

				}
			return fls.toString() + SPL + pa;
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

	@Override
	public void Exceptions(String message) {

	}

	class busyObject {
		public boolean busy = false;
		public int clientNumber;

		public busyObject(boolean busy, int clientNumber) {

			this.busy = busy;
			this.clientNumber = clientNumber;
		}

	}

	@Override
	public void notify(int clinetNumber, String server, String message) {

		stage.statusController.notify(server, message);
	}
}
