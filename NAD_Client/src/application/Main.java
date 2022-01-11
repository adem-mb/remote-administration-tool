package application;

import javafx.application.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import insidefx.undecorator.UndecoratorScene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.*;
import java.util.*;

// Java 8 code
public class Main extends Application {

   // private static final String iconImageLoc =
   //         "file:"+System.getProperty("user.dir")+"\\ico.png";

   
    private Stage stage;
    public StatusController statusController;
    private Stage statusStage;
    @Override 
    public void start(Stage primaryStage) {
        String temp=getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        int la=temp.lastIndexOf('/');
        MainController.APP_PATH=temp.substring(1, la);
    	File file =new File(MainController.APP_PATH+"\\config");
    	String sfile = null;
    	boolean hidden=false;
    	boolean notConfig=false;
    	try {
    		if(!file.exists()){
    			file.createNewFile();
    			notConfig=true;
    		}
    		FileReader fileReader = new FileReader(file);
    		BufferedReader bufferedReader = new BufferedReader(fileReader);
    		StringBuilder sb=new StringBuilder();
    		String line = bufferedReader.readLine();
    		while (line != null) {
    			sb.append(line + "\n");

    			line = bufferedReader.readLine();

    		}
    		fileReader.close();
    		if(sb.length()<=1){
    			notConfig=true;
    		}else{
    			if(sb.toString().split("\n")[0].equals("true"))
    				hidden=true;
    		}
    	sfile=sb.toString();
    	} catch (IOException e) {
			
			System.exit(0);
		}
    	statusStage=new Stage();
    	statusStage.setTitle("Status");
    	
    	FXMLLoader fxmlLoader1 = new FXMLLoader(getClass().getResource("FXML/statusWindow.fxml"));
    	

        Region root1;
		try {
			 statusController=new StatusController();
			fxmlLoader1.setController(statusController);
			root1 = (Region) fxmlLoader1.load();

        final UndecoratorScene undecoratorScene1 = new UndecoratorScene(statusStage,StageStyle.UTILITY, root1,null);
        undecoratorScene1.getStylesheets().add(getClass().getResource("FXML/application.css").toExternalForm());

        undecoratorScene1.setFadeInTransition();

        statusStage.setScene(undecoratorScene1);
        statusStage.setResizable(false);
       
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        this.stage = primaryStage;
        
        Platform.setImplicitExit(false);

        if(!hidden )
        javax.swing.SwingUtilities.invokeLater(this::addAppToTray);

        primaryStage.setTitle("Settings");


        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXML/ClientSettings.fxml"));
	

        Region root;
		try {
			MainController controller=new MainController(sfile,this);
			fxmlLoader.setController(controller);
			root = (Region) fxmlLoader.load();

        final UndecoratorScene undecoratorScene = new UndecoratorScene(primaryStage,StageStyle.UTILITY, root,null);
        undecoratorScene.getStylesheets().add(getClass().getResource("FXML/application.css").toExternalForm());

        undecoratorScene.setFadeInTransition();

        primaryStage.setScene(undecoratorScene);
        primaryStage.setResizable(false);
        if(notConfig)
        	primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
    public void closeStage(){
    	stage.hide();
    }
    public void showStatus(){
    	statusStage.show();
    	statusStage.toFront();
    }
    public void hideme(){
    	tray.remove(trayIcon);
    }
    private Node createContent() {
        Label hello = new Label("hello, world");
        hello.setStyle("-fx-font-size: 40px; -fx-text-fill: forestgreen;");
        Label instructions = new Label("(click to hide)");
        instructions.setStyle("-fx-font-size: 12px; -fx-text-fill: orange;");

        VBox content = new VBox(10, hello, instructions);
        content.setAlignment(Pos.CENTER);

        return content;
    }

    /**
     * Sets up a system tray icon for the application.
     */
    java.awt.TrayIcon trayIcon ;
    java.awt.SystemTray tray;
    private void addAppToTray() {
        try {
        
            java.awt.Toolkit.getDefaultToolkit();

           
            if (!java.awt.SystemTray.isSupported()) {
                System.out.println("No system tray support, application exiting.");
                Platform.exit();
            }

            
           tray = java.awt.SystemTray.getSystemTray();
            URL imageLoc = new URL(
            		MainController.class.getResource("FXML/ico.png").toExternalForm()
            );
            java.awt.Image image = ImageIO.read(imageLoc);
           trayIcon = new java.awt.TrayIcon(image);

           
            trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

      
            java.awt.MenuItem openItem = new java.awt.MenuItem("Settings");
            openItem.addActionListener(event -> Platform.runLater(this::showStage));

            java.awt.Font defaultFont = java.awt.Font.decode(null);
            java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
            openItem.setFont(boldFont);
            
            java.awt.MenuItem statusItem = new java.awt.MenuItem("Status");
            statusItem.addActionListener(event -> Platform.runLater(this::showStatus));

         
      
            java.awt.MenuItem exitItem = new java.awt.MenuItem("Exit");
            exitItem.addActionListener(event -> {
           
               System.exit(0);
            });


            final java.awt.PopupMenu popup = new java.awt.PopupMenu();
            popup.add(openItem);
            popup.add(statusItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            tray.add(trayIcon);
        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }


    private void showStage() {
        if (stage != null) {
            stage.show();
            stage.toFront();
        }
    }

    public static void main(String[] args) throws IOException, java.awt.AWTException {
    	
        launch(args);
    }
}