package application.WindowCtrl;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.*;

import application.MainWindowController;
import application.connection.Server;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;

public class DosCtrl implements Initializable{
	@FXML
	JFXTextArea commands;
	@FXML
	JFXTextArea command;
	@FXML
	JFXButton clear;
	int skn;
	public DosCtrl(int sk) {
		this.skn = sk;

		
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		commands.setFont(Font.font("Consolas",15));
		
		command.setOnKeyReleased(new EventHandler<KeyEvent>() {

			public void handle(KeyEvent event) {

				if (event.getCode() == KeyCode.ENTER){
					String com=command.getText();
					if(com.endsWith("\n")){
						
						int po=com.lastIndexOf('\n');
						com=com.substring(0, po);
						
					}if(!com.equals(""))
					MainWindowController.server.send(skn, "Command" + Server.SPL +com);
					command.setText("");}
				
			};

		});
	}
	
	public void setSocketNumber(int sk) {
		this.skn = sk;
		/*Consolas*/


	}
	@FXML
	private void Clear() {

		commands.setText("");
		MainWindowController.server.send(skn, "Command" + Server.SPL +"cls");
	}


	synchronized public void setData(String data) {
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				//System.out.print((char)Integer.parseInt(data)/* + " " + data*/);
				commands.appendText(String.valueOf((char)Integer.parseInt(data)));
	
			}
		});
		/*Task task;
		task = new Task<Void>() {
			@Override
			public Void call() throws Exception {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						commands.appendText(data);
					}
				});

			

				return null;

			};

		};
		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();*/
	}


}
