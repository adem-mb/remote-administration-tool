package application;

import java.net.URL;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

public class StatusController implements Initializable{

    @FXML
    private JFXTreeTableView<statusItem> serverList;

    @FXML
    private TreeTableColumn<statusItem, String> servers_column;

    @FXML
    private TreeTableColumn<statusItem, String> status_column;
    
    TreeItem<statusItem> root;
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		servers_column.setCellValueFactory(new TreeItemPropertyValueFactory("server"));
		status_column.setCellValueFactory(new TreeItemPropertyValueFactory("status"));
		root=new TreeItem<statusItem>(new statusItem("--", "--"));
		serverList.setShowRoot(false);
		serverList.setRoot(root);
	}
    public void addItem(String server){
    	Platform.runLater(()->{
    		TreeItem item=new TreeItem<statusItem>(new statusItem(server, "offline"));
    		root.getChildren().add(item);
    	});
    	
    }
    public void clear(){
    	Platform.runLater(()->{
    		
    		root.getChildren().clear();
    	});
    }
    public void notify(String server,String status){
    	final String s=server;
    	final String st=status;
    	Platform.runLater(()->{
    		
    		if(root.getChildren().size()>0){
    			root.getChildren().forEach(a->{
    				if(a.getValue().getServer().equals(s)){
    					/*System.out.println(a.getValue().getServer()+" "+s);
    					System.out.println(st);*/
    					a.getValue().setStatus(st);
    				}
    			});
    		}
    		serverList.refresh();
    	});
    }
    
    public static class statusItem extends RecursiveTreeObject<statusItem>{
    	private String server,status;
    	
		public String getServer() {
			return server;
		}

		public void setServer(String server) {
			this.server = server;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public statusItem(String server, String status) {
			super();
			this.server = server;
			this.status = status;
		}
    }





}
