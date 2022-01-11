package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import application.MainController.busyObject;
import application.connection.Client;

public class ScreenProcess {
	// private int sockNumber,port;
	private String port, ip, sockNumber;
	Client client;
	private OutputStream out;
	private InputStream inp;
	private Process process;
	private processHandler ph;
	private MainController hnd;

	public ScreenProcess(Client client, String sockNumber, MainController hnd) {
		String adrs = client.getSocketAdress().toString();
		ip = adrs.substring(1, adrs.length()).split(":")[0];
		port = adrs.substring(1, adrs.length()).split(":")[1];
		this.sockNumber = sockNumber;
		this.hnd = hnd;
		this.client = client;
		srt();
	}

	private void srt() {
		/*
		 * if(ph!=null) ph.destroy();
		 */
		// File file = new File(System.getProperty("user.dir") + "\\server
		// config");
		// ProcessBuilder builder = new
		// ProcessBuilder("C:\\Users\\Adem\\Documents\\Visual Studio
		// 2017\\Projects\\ConsoleApp1\\ConsoleApp1\\bin\\Release\\ConsoleApp1.exe");
		ProcessBuilder builder = new ProcessBuilder(MainController.APP_PATH + "\\ConsoleApp1.exe");
		builder.redirectErrorStream(true);

		try {
			process = builder.start();
			inp = process.getInputStream();
			out = process.getOutputStream();

			ph = new processHandler();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void Renew(Client client, String sockNumber) {

		String adrs = client.getSocketAdress().toString();
		String ip = adrs.substring(1, adrs.length()).split(":")[0];
		// String port=adrs.substring(1, adrs.length()).split(":")[1];

		String ip_port = ip + port;

		this.ip = ip;
		this.port = port;
		this.sockNumber = sockNumber;

		process.destroy();
		srt();

	}

	private boolean stop = false;

	public void stop_and_Renew(Client client, String sockNumber) {
		stop = true;
		Renew(client, sockNumber);
	}

	public void Dispose() {
		process.destroy();
	}

	public void send(String s) {

		try {
			out.write((s + "\n").getBytes());
			out.flush();
		} catch (IOException e) {
			process.destroy();
			e.printStackTrace();
		}

	}

	public boolean isRunning() {
		return process.isAlive();
	}

	class processHandler extends Thread {
		public processHandler() {
			start();
		}

		@Override
		public void run() {
			try {
				int value = -1;
				String s = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(inp));

				send("info||" + ip + "||" + port + "||" + String.valueOf(sockNumber));
				// System.out.println("info||"+ip+"||"+port+"||"+String.valueOf(sockNumber));
				while ((s = in.readLine()) != null) {
					System.out.println(s);
					switch (s) {

					case "done":

						send("start");

						break;
					case "":

						break;

					}
				}

				System.out.println("end");
			} catch (IOException exp) {
				exp.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (!stop)
				hnd.NotBusy();
			else
				stop = false;

		}
	}

}
