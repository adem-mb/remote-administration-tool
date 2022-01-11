package application;

import java.awt.Label;
import java.io.File;

import javax.swing.filechooser.FileSystemView;


import insidefx.undecorator.UndecoratorScene;
import application.Association.*;
import application.connection.Server;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;

public class Main extends Application {

	public Scene scene;

	@Override

	public void start(Stage primaryStage) {
		try {
	       
	        primaryStage.setTitle("NAD server");
	        String temp=getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	        int la=temp.lastIndexOf('/');
	        MainWindowController.APP_PATH=temp.substring(1, la);
	        
	        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML/MainWindow.fxml"));
	        
	        MainWindowController mc =new MainWindowController();
	        
	        fxmlLoader.setController(mc);
	        
	        Region root = (Region) fxmlLoader.load();
	        
	        final UndecoratorScene undecoratorScene = new UndecoratorScene(primaryStage, root);
	        undecoratorScene.getStylesheets().add(getClass().getResource("FXML/application.css").toExternalForm());

	        undecoratorScene.setFadeInTransition();

	        primaryStage.setScene(undecoratorScene);

	        primaryStage.toFront();
	        primaryStage.show();
	        mc.check();
	    	
	        //undecoratorScene.addStylesheet("application.css");
	        
	        /*
	         * Fade out transition on window closing request
	         */
	      /*  primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	            @Override
	            public void handle(WindowEvent we) {
	                we.consume();   // Do not hide yet
	                undecoratorScene.setFadeOutTransition();
	            }
	        });*/

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
