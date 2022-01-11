package application.WindowCtrl;

import static application.MainWindowController.server;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.sun.javafx.PlatformUtil;
import com.sun.media.jfxmediaimpl.platform.Platform;

import application.MainWindowController;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class RemoteDesktopCtrl implements Initializable {

	@FXML
	private JFXButton startButton;

	@FXML
	private JFXComboBox<String> sending_method;

	@FXML
	private JFXCheckBox check_mouse;

	@FXML
	private JFXCheckBox check__keyboard;

	@FXML
	private Spinner<Integer> interval;

	@FXML
	private ImageView Desktop;
	@FXML
	private Canvas Desktop2;
	public boolean stop=false;
	volatile int sharpSocket;
	int socketNumber;
	private Object lock=new Object();
	public volatile long lastcheck;
	public RemoteDesktopCtrl(int sn) {
		socketNumber = sn;
		Thread th=new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(700);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long now=System.currentTimeMillis();
				while(true){
					synchronized (lock)
					{
						if(stop) break;
					}
						
					
					try {
						Thread.sleep(1200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					long deffre=now-lastcheck;
					if(deffre>2000){
						System.out.println("error2");
						MainWindowController.server.close(sharpSocket);
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					now=System.currentTimeMillis();
				
				}
			}
		});
		th.start();
	}

	double w, h, hi, wi;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ImageIO.setUseCache(false);
		Pane pa = (Pane) Desktop2.getParent();
		Desktop2.widthProperty().bind(pa.widthProperty());
		Desktop2.heightProperty().bind(pa.heightProperty());
		Desktop2.heightProperty().addListener((evt, oldVal, newVal) -> {
			draw(null, 0, 0, Desktop2.getWidth(), newVal.doubleValue(), 1);
		});
		Desktop2.widthProperty().addListener((evt, oldVal, newVal) -> {
			draw(null, 0, 0, newVal.doubleValue(), Desktop2.getHeight(), 1);
		});
		graphicsContext = Desktop2.getGraphicsContext2D();
		Desktop2.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if(state)
				if (check_mouse.isSelected()) {
					double x = event.getX();
					double y = event.getY();

					x *= wi;
					y *= hi;

					int xCord = (int) x;
					int yCord = (int) y;
					application.MainWindowController.server.send(socketNumber,
							"mousemove" + application.MainWindowController.server.SPL + xCord
									+ application.MainWindowController.server.SPL + yCord);
				}
			}
		});
        SpinnerValueFactory<Integer> valueFactory = //
                new SpinnerValueFactory.IntegerSpinnerValueFactory(25, 1000, 70);
 
        interval.setValueFactory(valueFactory);
        interval.valueProperty().addListener((obs, oldValue, newValue) -> {
        	if(!newValue.equals(oldValue)){
        		if(newValue>=25 & newValue <=1000)
        			application.MainWindowController.server.send(socketNumber,
							"Interval" + application.MainWindowController.server.SPL + newValue);
        	}
            
        });
        Desktop2.setFocusTraversable(true);
        sending_method.getItems().add("PNG");
        sending_method.getItems().add("JPEG");
        sending_method.getSelectionModel().select(0);
	}

	Image globImage = null;

	 void draw(byte[] data, int x, int y, double w, double h, int cm) {
		 
		if (cm == 1) {
			
			if (bf != null) {
				Image image = SwingFXUtils.toFXImage(bf, null);

				graphicsContext.clearRect(0, 0, w, h);
				graphicsContext.drawImage(image, 0, 0, w, h);
				wi = image.getWidth() / w;
				hi = image.getHeight() / h;

			}
		} else if (cm == 2) {
		
			ByteArrayInputStream ar = new ByteArrayInputStream(data); 
			Image img = new Image(ar);

			graphicsContext.drawImage(img, 0, 0, w, h);
			wi = img.getWidth() / w;
			hi = img.getHeight() / h;
			try {
				ar.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}                                                           
			ar = new ByteArrayInputStream(data);

			try {
				bf = ImageIO.read(ar);
				System.out.println(bf.getWidth());
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			try {
				ar.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
		
			ByteArrayInputStream ar = new ByteArrayInputStream(data);
			

			
			Graphics gr = bf.getGraphics();
			BufferedImage bimage;
			
			try {
				bimage = ImageIO.read(ar);
				gr.drawImage(bimage, x, y, null);
				gr.dispose();
				try {
					ar.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ar = new ByteArrayInputStream(data);
				Image img = new Image(ar);

				graphicsContext.drawImage(img, x / wi, y / hi, img.getWidth() / wi, img.getHeight() / hi);
				try {
					ar.close();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			} catch (IOException e1) {
				System.out.println("8p");
				e1.printStackTrace();

			}
			/*Image img = new Image(ar);

			graphicsContext.drawImage(img, x / wi, y / hi, img.getWidth() / wi, img.getHeight() / hi);
			try {
				ar.close();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			ar = new ByteArrayInputStream(data);
			Graphics gr = bf.getGraphics();
			BufferedImage bimage;
			try {
				bimage = ImageIO.read(ar);
				gr.drawImage(bimage, x, y, null);
				gr.dispose();

			} catch (IOException e1) {
				e1.printStackTrace();

			}*/

			

		}
	}

	BufferedImage bf;
	GraphicsContext graphicsContext;

	public void setData(byte[] data,int sharpSocket) {
		lastcheck=System.currentTimeMillis();
		this.sharpSocket=sharpSocket;
		javafx.application.Platform.runLater(()->{
			draw(data, 0, 0, Desktop2.getWidth(), Desktop2.getHeight(), 2);
		});
	

	}

	public void setData2(byte[] data, int x, int y,int sharpSocket) {
		lastcheck=System.currentTimeMillis();
		this.sharpSocket=sharpSocket;
		javafx.application.Platform.runLater(()->{
		draw(data, x, y, Desktop2.getWidth(), Desktop2.getHeight(), 3);
	});
	}

	public void setCursor(byte cursor) {
	
		switch (cursor) {
		
		case 4:
			Desktop2.setCursor(javafx.scene.Cursor.TEXT);
			break;
		case 3:
			Desktop2.setCursor(javafx.scene.Cursor.WAIT);
			break;
		case 1:
			Desktop2.setCursor(javafx.scene.Cursor.H_RESIZE);
			break;
		case 2:
			Desktop2.setCursor(javafx.scene.Cursor.V_RESIZE);
			break;
		case 6:
			Desktop2.setCursor(javafx.scene.Cursor.NE_RESIZE);
			break;
		case 5:
			Desktop2.setCursor(javafx.scene.Cursor.NW_RESIZE);
			break;
		case 7:
			Desktop2.setCursor(javafx.scene.Cursor.HAND);
			break;
		default:
			Desktop2.setCursor(javafx.scene.Cursor.DEFAULT);
			break;
		}
	}

	@FXML
	void MouseMove(MouseEvent e) {
		if(state)
		if (check_mouse.isSelected()) {
			double x = e.getX();
			double y = e.getY();

			x *= wi;
			y *= hi;

			int xCord = (int) x;
			int yCord = (int) y;
			application.MainWindowController.server.send(socketNumber,
					"mousemove" + application.MainWindowController.server.SPL + xCord
							+ application.MainWindowController.server.SPL + yCord);
		}
	}
	public void Stop(){
		synchronized (lock) {
			stop=true;
			server.send(socketNumber, "ScreenC" + server.SPL + socketNumber);
		}
	}
	private boolean state=true;
    @FXML
    void control_client(ActionEvent event) {
    	Button bu=(Button)event.getSource();
    	if(bu.getText().equals("Stop")){
    		server.send(socketNumber,"pause" + application.MainWindowController.server.SPL );
    		state=false;
    		bu.setText("Start");
    		
    		
    	}else{
    		server.send(socketNumber,"resume" + application.MainWindowController.server.SPL );
    		state=true;
    		bu.setText("Stop");
    		
    	}
    }
	@FXML
	void MousePress(MouseEvent event) {
		
		if(state)
		if (check_mouse.isSelected()) {
			if (event.isPrimaryButtonDown())
				application.MainWindowController.server.send(socketNumber,
						"mousepress1" + application.MainWindowController.server.SPL);
			else if (event.isSecondaryButtonDown())
				application.MainWindowController.server.send(socketNumber,
						"mousepress2" + application.MainWindowController.server.SPL);
		}
	}
    @FXML
    void method(ActionEvent event) {
    	String drv=sending_method.getSelectionModel().getSelectedItem();
		server.send(socketNumber, "ImageF"+server.SPL+drv);
	
    }
	@FXML
	void MouseRelease(MouseEvent event) {
		
		if(state)
		if (check_mouse.isSelected()) {
			if (event.getButton() == MouseButton.PRIMARY)
				application.MainWindowController.server.send(socketNumber,
						"mouserelease1" + application.MainWindowController.server.SPL);
			else if (event.getButton() == MouseButton.SECONDARY) {
				System.out.println("sec");
				application.MainWindowController.server.send(socketNumber,
						"mouserelease2" + application.MainWindowController.server.SPL);
			}
		}
	}
    @FXML
    void KeyPres(KeyEvent event) {
    	System.out.println("sdfsdf");
    	application.MainWindowController.server.send(socketNumber,
				"keypress" + application.MainWindowController.server.SPL+event.getCode().hashCode());
    	
    }

    @FXML
    void KeyRel(KeyEvent event) {
    	application.MainWindowController.server.send(socketNumber,
				"keyrelease" + application.MainWindowController.server.SPL+event.getCode().hashCode());
    }
}
