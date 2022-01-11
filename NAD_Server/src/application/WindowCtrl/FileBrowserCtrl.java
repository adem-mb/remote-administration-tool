package application.WindowCtrl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javax.swing.filechooser.FileSystemView;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import application.MainWindowController;
import application.Association.*;
import application.Association.BrowserItem.NameType;
import application.connection.Server;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import sun.awt.shell.ShellFolder;
import static application.Utils.Functions.*;
import static application.MainWindowController.*;
public class FileBrowserCtrl implements Initializable{
	@FXML
    private JFXTextField path_rm;
    @FXML
    private JFXTextField path_lo;
    @FXML
    private JFXTreeTableView<BrowserItem> remoteList;

    @FXML
    private TreeTableColumn<BrowserItem, NameType> fileName_rm;

    @FXML
    private TreeTableColumn<BrowserItem, String> date_rm;

    @FXML
    private TreeTableColumn<BrowserItem, String> size_rm;
    @FXML
    private JFXTreeTableView<BrowserItem> localList;

    @FXML
    private TreeTableColumn<BrowserItem, NameType> fileName_lo;

    @FXML
    private TreeTableColumn<BrowserItem, String> date_lo;

    @FXML
    private TreeTableColumn<BrowserItem, String> size_lo;
    @FXML
    Label user_label;
	@FXML
	TableView<BrowserItem> FileTable; 
	@FXML
	TableColumn<BrowserItem,String> FileNameColumn; 
	@FXML
	JFXListView DriveList;
	@FXML
	Label status;
	int socketNumber;
    @FXML
    private JFXComboBox<String> drives_rm;
    @FXML
    private JFXComboBox<String> drives_lo;
    
    private ObservableList<BrowserItem> itemList = FXCollections.observableArrayList();
    String local_patth;
    String rm_path;
    TreeItem<BrowserItem> root;
    TreeItem<BrowserItem> root1;
    TreeItem<BrowserItem> ret;
    BrowserItem retI;
    String user;
	public FileBrowserCtrl(int sn,String user){
		socketNumber=sn;
		this.user=user;
	}
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		user_label.setText(user);
		initLists();
		getDrives();
		drives_lo.getSelectionModel().select(0);
		String drv=drives_lo.getSelectionModel().getSelectedItem();
		int index =drv.lastIndexOf('(');
		
		String drive=drv.substring(index+1, index+3)+"\\";
	
		getFiles(drive);
		
		local_patth=drive;
	}

	private void initLists(){
		remoteList.widthProperty().addListener((obs, oldVal, newVal) -> {
            int w =newVal.intValue();
            int half=w/2;
            int h2=(int) ((w/2)*0.7);
            int h3=(int) ((w/2)*0.3);
            fileName_rm.setPrefWidth(half);
            date_rm.setPrefWidth(h2);
            size_rm.setPrefWidth(h3);
            
        });
		fileName_rm.setCellValueFactory(new TreeItemPropertyValueFactory("nameType"));
		
		fileName_rm.setCellFactory(new Callback <TreeTableColumn<BrowserItem,NameType>, TreeTableCell<BrowserItem,NameType>>() {
			
				@Override
				public TreeTableCell<BrowserItem, NameType> call(TreeTableColumn<BrowserItem, NameType> arg0) {
					TreeTableCell<BrowserItem, NameType> cell=new TreeTableCell<BrowserItem, NameType>(){
						@Override
						public void updateItem(NameType item, boolean empty) {
							if(item!=null){                            
	                            HBox box= new HBox();
	                            box.setSpacing(10) ;
	                       
	                            Label o=new Label(item.getName());
	                                            
	                            ImageView imageview = new ImageView();
	                            
	                            imageview.setFitHeight(30);
	                            imageview.setFitWidth(30);
	                            if(item.getType().equals("DIR"))
	                            	imageview.setImage(new Image(MainWindowController.class.getResourceAsStream("/Images/folder.png"))); 
	                            else {
	                            	try {
	                            	String path=MainWindowController.APP_PATH+"\\tmp\\"+user+Integer.toString(socketNumber)+o.getText();
	                            	File file=new File(path);
	                            	if(!file.exists())
	                            		file.createNewFile();
	                            	
	                            	 BufferedImage img = (BufferedImage) ShellFolder.getShellFolder(file).getIcon(true);
		                        	   if(img!=null)
		                        	   imageview.setImage(SwingFXUtils.toFXImage(img, null)); 
		                        	   file.delete();
										
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
	                            }
	                            box.getChildren().addAll(imageview,o); 
	                            
	                            setGraphic(empty ? null : box);
	                       
	                        }
						}
						
						
					};
					   
					return cell;
				}
			});
		date_rm.setCellValueFactory(new TreeItemPropertyValueFactory("date"));
		size_rm.setCellValueFactory(new TreeItemPropertyValueFactory("size"));
		
		
		localList.widthProperty().addListener((obs, oldVal, newVal) -> {
            int w =newVal.intValue();
            int half=w/2;
            int h2=(int) ((w/2)*0.7);
            int h3=(int) ((w/2)*0.3);
            fileName_lo.setPrefWidth(half);
            date_lo.setPrefWidth(h2);
            size_lo.setPrefWidth(h3);
         
        });
		localList.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
            	if(localList.getSelectionModel().getSelectedItem()!=null){
                BrowserItem selected=localList.getSelectionModel().getSelectedItem().getValue();
                if(selected.getType().equals("DIR"))
                	getFiles(local_patth+"\\"+selected.getFileName());
             
            	}
               
            }
            localList.refresh();
        });
		retI=new BrowserItem(".."," "," ","DIR");
		ret=new TreeItem<BrowserItem>(retI);
		localList.sortPolicyProperty().set(
                new Callback<TreeTableView<BrowserItem>, Boolean>() {

                    @Override
                    public Boolean call(TreeTableView<BrowserItem> param) {
                    	   Comparator<TreeItem<BrowserItem>> comparator = new Comparator<TreeItem<BrowserItem>>() {
                               @Override
                               public int compare(TreeItem<BrowserItem> r1, TreeItem<BrowserItem> r2) {
                                   if (r1.getValue() == retI) {
                                       return -root.getChildren().size();
                                   } else if (r2.getValue() == retI) {
                                       return root.getChildren().size();
                                   } else if (param.getComparator() == null) {
                                       return 0;
                                   } else {
                                       return param.getComparator()
                                               .compare(r1, r2);
                                   }
                               }
                           };
                           if(localList.getRoot() != null)
                        	    FXCollections.sort(localList.getRoot().getChildren(), comparator);

                        return true;
                    }
                });
		remoteList.sortPolicyProperty().set(
                new Callback<TreeTableView<BrowserItem>, Boolean>() {

                    @Override
                    public Boolean call(TreeTableView<BrowserItem> param) {
                    	   Comparator<TreeItem<BrowserItem>> comparator = new Comparator<TreeItem<BrowserItem>>() {
                               @Override
                               public int compare(TreeItem<BrowserItem> r1, TreeItem<BrowserItem> r2) {
                                   if (r1.getValue() == retI) {
                                       return -root1.getChildren().size();
                                   } else if (r2.getValue() == retI) {
                                       return root1.getChildren().size();
                                   } else if (param.getComparator() == null) {
                                       return 0;
                                   } else {
                                       return param.getComparator()
                                               .compare(r1, r2);
                                   }
                               }
                           };
                           if(remoteList.getRoot() != null)
                        	    FXCollections.sort(remoteList.getRoot().getChildren(), comparator);

                        return true;
                    }
                });
		fileName_lo.setCellValueFactory(new TreeItemPropertyValueFactory("nameType"));
		
		
		fileName_lo.setCellFactory(new Callback <TreeTableColumn<BrowserItem,NameType>, TreeTableCell<BrowserItem,NameType>>() {
			
				@Override
				public TreeTableCell<BrowserItem, NameType> call(TreeTableColumn<BrowserItem, NameType> arg0) {
					TreeTableCell<BrowserItem, NameType> cell=new TreeTableCell<BrowserItem, NameType>(){
						@Override
						public void updateItem(NameType item, boolean empty) {
							if(item!=null){                            
	                            HBox box= new HBox();
	                            box.setSpacing(10) ;
	                       
	                            Label o=new Label(item.getName());
	                      
	                       
	                            
	                            ImageView imageview = new ImageView();
	                            
	                            imageview.setFitHeight(30);
	                            imageview.setFitWidth(30);
	                            
	                            try {
									
						
	                           if(item.getType().equals("DIR"))
	                            	imageview.setImage(new Image(MainWindowController.class.getResourceAsStream("/Images/folder.png"))); 
	                           else {
	                        	   BufferedImage img = (BufferedImage) ShellFolder.getShellFolder(new File(local_patth+"\\"+item.getName())).getIcon(true);
	                        	   if(img !=null)
	                        	   imageview.setImage(SwingFXUtils.toFXImage(img, null)); 
	                           }
	                            box.getChildren().addAll(imageview,o); 
	                            
	                            setGraphic(empty ? null : box);
	                    		} catch (FileNotFoundException e) {
									
									e.printStackTrace();
								}
	                           
	                        }
						} 
						
						
					};
					   
					return cell;
				}
			});
		remoteList.setOnMouseClicked((MouseEvent event) -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2){
            	if(remoteList.getSelectionModel().getSelectedItem()!=null){
                BrowserItem selected=remoteList.getSelectionModel().getSelectedItem().getValue();
                if(selected.getType().equals("DIR"))
                	//getFiles(rm_path+"\\"+selected.getFileName());
                	server.send(socketNumber, "GetFiles"+server.SPL+server.Quote(rm_path+"\\"+selected.getFileName()));
             
            	}
               
            }
            localList.refresh();
        });
		date_lo.setCellValueFactory(new TreeItemPropertyValueFactory("date"));
		size_lo.setCellValueFactory(new TreeItemPropertyValueFactory("size"));
		root = new TreeItem<BrowserItem>(new BrowserItem("--", "--", "--", "--"));
		root1=new TreeItem<BrowserItem>(new BrowserItem("--", "--", "--", "--"));
		root.getChildren().add(ret);
		localList.setRoot(root);
		localList.setShowRoot(false);
		remoteList.setRoot(root1);
		remoteList.setShowRoot(false);
	}
	

	private void getDrives(){
		FileSystemView fsv = FileSystemView.getFileSystemView();
		File[] roots =File.listRoots();
		for(File root:roots)
		{
			if(root.canRead() && root.canWrite())
				drives_lo.getItems().add(fsv.getSystemDisplayName(root));
		    
		}
	}
	 private void getFiles(String String_path){

		 try {
			 root.getChildren().clear();
			 root.getChildren().add(ret);
			 
		 		     File folder = new File(String_path);
		 		     File files[]=folder.listFiles();
		 		    local_patth=folder.getCanonicalPath();
		 		    path_lo.setText(folder.getCanonicalPath());
		 		    
		 		     StringBuilder fls =new StringBuilder();
		 		     if(files!=null)
		 		     for(File file:files){
		 		    	 String type = null;
		 		     	java.nio.file.Path p =file.toPath();
		 		        BasicFileAttributes view;
		 			
		 					view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
		 					String size;
		 		    	 if(file.isDirectory()){
		 		    		 type="DIR";
		 		    		 size=" ";
		 		    	 }else{
		 		    		
                              type="file";
                              size=calculatesize(file.length());
		                                 
		 		    		
		 		    	 }
		 		    	   DateFormat df=new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss ");
				             String date=df.format(view.creationTime().toMillis());
		 		    	 TreeItem<BrowserItem> item=new TreeItem<BrowserItem>(new BrowserItem(file.getName(),date,size,type));
		 		     //fls.append(file.getName()+"**?"+type+"**?"+view.creationTime()+"\\?");
		 		    	
		 		    	 root.getChildren().add(item);
		 		     }
		 	
		 		 	} catch (IOException e) {
		 				
		 				e.printStackTrace();
		 				}
		 	localList.refresh();
		  }
	@FXML
	void select_rm(ActionEvent event) {
    	String drv=drives_rm.getSelectionModel().getSelectedItem();
		int index =drv.lastIndexOf('(');
		
		String drive=drv.substring(index+1, index+3)+"\\";
		server.send(socketNumber, "GetFiles"+server.SPL+server.Quote(drive));
	}
	@FXML
    void select_local(ActionEvent event) {
	    	String drv=drives_lo.getSelectionModel().getSelectedItem();
			int index =drv.lastIndexOf('(');
			
			String drive=drv.substring(index+1, index+3)+"\\";
			getFiles(drive);
    }
	public void setDrives(String data){
		String[] files=data.split(Pattern.quote("\\?"));
		
		for(String file :files){
			String[] filespro=file.split(Pattern.quote("**?"));
			drives_rm.getItems().add(filespro[0]);
				
			
		}
		Platform.runLater(() -> {
			drives_rm.getSelectionModel().select(0);
			String drv=drives_rm.getSelectionModel().getSelectedItem();
			int index =drv.lastIndexOf('(');
			
			String drive=drv.substring(index+1, index+3)+"\\";
			rm_path=drive;
		});
	
	}
	public void setData(String data,String data2){
		path_rm.setText(data2);
		rm_path=data2;
		String[] files=data.split(Pattern.quote("\\?"));
		Platform.runLater(() -> {
		root1.getChildren().clear();
		root1.getChildren().add(ret);
		if(files.length>1)
		for(String file :files){
			String[] filespro=file.split(Pattern.quote("**?"));
			TreeItem<BrowserItem>item;
			if(filespro.length==4)
				item=new TreeItem<BrowserItem>(new BrowserItem(filespro[0],filespro[2],filespro[1],filespro[3]));
			else
				item=new TreeItem<BrowserItem>(new BrowserItem(filespro[0],filespro[1]," ",filespro[2]));
			root1.getChildren().add(item);
			
		}
		remoteList.refresh();
		});
	}
}
