package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import com.jfoenix.controls.JFXTreeTableView;

import application.Utils.Functions;
import application.WindowCtrl.*;
import application.connection.Handler;
import application.connection.Server;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import application.Association.*;
import static application.Utils.Functions.*;
import application.Association.ConnectionItem.IsGroup;

public class MainWindowController implements Handler, Initializable {

	/*
	 * @FXML public TableView<Nitem> Connected;
	 * 
	 * @FXML public TableColumn wind, ip,users;
	 */
	@FXML
	private Label status_label;

	boolean startup = false;
	@FXML
	private JFXTreeTableView<ConnectionItem> mainList;

	@FXML
	private TreeTableColumn<ConnectionItem, IsGroup> groupsColumn;

	private TreeItem<ConnectionItem> root;
	@FXML
	private CheckMenuItem startup_menu;
	@FXML
	private TabPane tabPane;
	ObservableList<Nitem> itemList = FXCollections.observableArrayList();
	public static String APP_PATH;
	public static Server server;
	Integer port;
	Object lock =new Object();
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		mainList.widthProperty().addListener((obs, oldVal, newVal) -> {
			int w = newVal.intValue();
			groupsColumn.setPrefWidth(w);
		});
		groupsColumn.setCellValueFactory(new TreeItemPropertyValueFactory("ig"));

		root = new TreeItem<ConnectionItem>(new ConnectionItem("--", "--", "--", "--", "--", true));

		groupsColumn.setCellFactory(
				new Callback<TreeTableColumn<ConnectionItem, IsGroup>, TreeTableCell<ConnectionItem, IsGroup>>() {

					@Override
					public TreeTableCell<ConnectionItem, IsGroup> call(TreeTableColumn<ConnectionItem, IsGroup> arg0) {
						TreeTableCell<ConnectionItem, IsGroup> cell = new TreeTableCell<ConnectionItem, IsGroup>() {
							@Override
							public void updateItem(IsGroup item, boolean empty) {
								if (item != null) {
									HBox box = new HBox();
									box.setSpacing(10);
									VBox vbox = new VBox();

									Label o;
									if (item.isGroup()) {
										o = new Label(item.getGroup());
										vbox.getChildren().add(o);
									} else {
										o = new Label("User: " + item.getUser());
										vbox.getChildren().add(o);
										Label win = new Label("OS: " + item.getWindows());
										vbox.getChildren().addAll(win);
									}

									ImageView imageview = new ImageView();

									imageview.setFitHeight(30);
									imageview.setFitWidth(30);

									if (item.isGroup())
										/*
										 * imageview.setImage(new Image(
										 * MainWindowController.class.
										 * getResource("FXML") +
										 * "/Images/grp.png"));
										 */
										imageview
												.setImage(new Image(getClass().getResourceAsStream("/Images/grp.png")));
									else
										imageview
												.setImage(new Image(getClass().getResourceAsStream("/Images/usr.png")));
									box.getChildren().addAll(imageview, vbox);

									setGraphic(empty ? null : box);

									// mainList.refresh();

								}
							}

						};

						return cell;
					}
				});
		startup_menu.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				// TODO Auto-generated method stub
				startup = newValue;
				save_settings();
			}
		});
		mainList.setRoot(root);
		mainList.setShowRoot(false);
		System.out.println(getClass().getResource("FXML") + "/Images/usr.png");
		System.out.println(APP_PATH);
		// server = new Server(port, this);
	}

	public void check() {

		File file = new File(APP_PATH + "/server config");
		if (!file.exists()) {
			try {
				file.createNewFile();
				setPort();
				startlistning();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			FileReader fileReader;
			try {
				fileReader = new FileReader(file);

				BufferedReader bufferedReader = new BufferedReader(fileReader);
				StringBuilder sb = new StringBuilder();
				String line = bufferedReader.readLine();
				while (line != null) {
					sb.append(line + "\n");

					line = bufferedReader.readLine();

				}
				fileReader.close();

				String param[] = sb.toString().split("\n");
				startup = Boolean.parseBoolean(param[0]);
				port = Integer.parseInt(param[1]);
				startup_menu.setSelected(startup);
				startlistning();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void Exceptions(String message) {
		Platform.runLater(() -> {
			Alert alert = new Alert(AlertType.ERROR, message, ButtonType.OK);
			alert.showAndWait();
		});

	}

	public synchronized void Diconnected(int sn) {
		
		synchronized (lock) {

			String id = Integer.toString(sn);
			TreeItem<ConnectionItem> user = find_user(root, id);
			if (user != null) {
				TreeItem<ConnectionItem> parent = user.getParent();
				parent.getChildren().removeIf(a -> a == user);
				if (parent.getChildren().size() == 0) {
					root.getChildren().removeIf(a -> a == parent);
				}
			}

			Platform.runLater(() -> {
				ArrayList<Tab> tablist=new  ArrayList<>();
				tabPane.getTabs().forEach(a -> {
					if (a.getId().contains(String.valueOf(sn))) {
						tablist.add(a);
					}
				});
				tablist.forEach(a->{
					
					if (a.getId().contains("Desktop" + String.valueOf(sn))) {
						RemoteDesktopCtrl ctrl=(RemoteDesktopCtrl)a.getUserData();
						ctrl.Stop();
						a.getTabPane().getTabs().remove(a);
					} else {
						a.getTabPane().getTabs().remove(a);
					}
				});
			});
			server.removeClient(sn);
			mainList.refresh();
		}
	}

	synchronized void newConnection(byte data[], int sn) {

		synchronized (lock) {

			String d[] = Functions.Byte2Str(data).split(Pattern.quote(Server.SPL));
			String groupe = Server.Unquote(d[4]);
			TreeItem<ConnectionItem> groupitem = find_group(root, groupe);
			TreeItem<ConnectionItem> newUser = new TreeItem<ConnectionItem>(new ConnectionItem(Integer.toString(sn),
					Server.Unquote(d[1]), Server.Unquote(d[2]), Server.Unquote(d[3]), groupe, false));
			if (groupitem == null) {
				groupitem = new TreeItem<ConnectionItem>(new ConnectionItem("--", "--", "--", "--", groupe, true));
				root.getChildren().add(groupitem);
			}

			groupitem.getChildren().add(newUser);
			mainList.refresh();
			File file = new File(APP_PATH + "\\tmp\\" + Server.Unquote(d[3]) + Integer.toString(sn));

			if (!file.exists())
				file.mkdir();
		}
	}

	public void Data(byte data[], int sn) {

		byte BYTE[] = Arrays.copyOfRange(data, 0, 4);
		String sB;
		try {
			sB = new String(BYTE, "ASCII");

			if (sB.equals("BYTE")) {

				byte com[] = Arrays.copyOfRange(data, 8, 9);
				int socketNumber = Byte2Int(Arrays.copyOfRange(data, 4, 8));
				switch (com[0]) {

				case 0: {

					byte[] data2 = Arrays.copyOfRange(data, 9, data.length);
					SetImage(socketNumber, data2, sn);

					break;

				}
				case 1: {

					int x = Byte2Int(Arrays.copyOfRange(data, 9, 13));
					int y = Byte2Int(Arrays.copyOfRange(data, 13, 17));
					byte[] data2 = Arrays.copyOfRange(data, 17, data.length);
					SetSubImage(socketNumber, data2, x, y, sn);
					break;
				}
				case 2: {
					byte[] cursor = Arrays.copyOfRange(data, 9, 10);
					SetCursor(socketNumber, cursor[0]);
					break;
				}
				case 3: {
					System.out.println("nullll");
					application.MainWindowController.server.send(socketNumber,
							"shot" + application.MainWindowController.server.SPL);

					break;
				}
				}

			} else {
				String d[] = Functions.Byte2Str(data).split(Pattern.quote(Server.SPL));

				switch (d[0]) {

				case "NewC":

					newConnection(data, sn);
					break;
				case "SetDrives":
					setDrives(sn, Server.Unquote(d[1]));
					break;
				case "BrowserFiles":

					BrowserFiles(sn, Server.Unquote(d[1]), Server.Unquote(d[2]));
					break;
				case "command":

					command(sn, Server.Unquote(d[1]));

					break;
				case "Dosexit":
					DosExit(sn);

					break;
				case "Rec":

					Rec(sn, Functions.Str2Byte(Server.Unquote(d[1])));

					break;
				case "RecM":

					RecM(sn, Functions.Str2Byte(d[1]));

					break;
				case "ScreenInI":
					initViewer(sn,false);
					break;
				case "FScreenInI":
					initViewer(sn,true);
					break;
				case "BrowserFilesInI":

					initFileBrowser(sn);
					break;
				case "commandInI":

					initCommand(sn);

					break;
				case "Interupted": {
					String user = null;
					synchronized (lock) {
						TreeItem<ConnectionItem> useritem = find_user(root, String.valueOf(sn));
						if (useritem != null)
							user = useritem.getValue().getUser();
					

					if (user != null) {
						final String finaluser = user;
						Platform.runLater(() -> {
							ArrayList<Tab> tablist=new  ArrayList<>();
							tabPane.getTabs().forEach(a -> {
								if(a.getId().contains("Desktop" + String.valueOf(sn))){
								tablist.add(a);
								}
							
							}
							);
							
							tablist.forEach(a->{
								
								RemoteDesktopCtrl ctrl=(RemoteDesktopCtrl)a.getUserData();
								ctrl.Stop();
								a.getTabPane().getTabs().remove(a);
							});
							ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

							Alert alert = new Alert(AlertType.INFORMATION, "", ok);
							alert.setHeaderText("Controlling " + finaluser + " has been interrupted by another server");
							alert.setTitle("Info");
							Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
							stage.setAlwaysOnTop(true);
							stage.toFront();
							Optional<ButtonType> result = alert.showAndWait();

						});
					}}
					break;
				}
				case "refused": {
					String user = null;
					synchronized (lock) {
						TreeItem<ConnectionItem> useritem = find_user(root, String.valueOf(sn));
						if (useritem != null)
							user = useritem.getValue().getUser();
					}

					if (user != null) {
						final String finaluser = user;
						Platform.runLater(() -> {
							String type;
							String message;
							if (d[1].equals("Desktop")) {
								type = "Desktop";
								message = "controle desktop";
							} else if (d[1].equals("dos")) {
								type = "CommandLine";
								message = "start remote dos";
							} else {
								type = "FileExplorer";
								message = "browse files";
							}
							tabPane.getTabs().removeIf(a -> a.getId().contains(type + String.valueOf(sn)));
							ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

							Alert alert = new Alert(AlertType.INFORMATION, "", ok);
							Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
							stage.setAlwaysOnTop(true);
							stage.toFront();
							alert.setHeaderText("Client " + finaluser + " refused your demande to " + message);
							alert.setTitle("Info");
							Optional<ButtonType> result = alert.showAndWait();

						});
					}

					break;
				}
				case "Busy":
					String user = null;
					synchronized (lock) {
						TreeItem<ConnectionItem> useritem = find_user(root, String.valueOf(sn));
						if (useritem != null)
							user = useritem.getValue().getUser();
					}

					if (user != null) {
						final String finaluser = user;
						Platform.runLater(() -> {
							ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
							ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
							Alert alert = new Alert(AlertType.INFORMATION, "do you wanna force control it", yes, no);
							alert.setHeaderText("The client " + finaluser + " is being controlled by another server");
							alert.setTitle("Busy client");
							Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
							stage.setAlwaysOnTop(true);
							stage.toFront();
							Optional<ButtonType> result = alert.showAndWait();

							if (result.isPresent() && result.get() == yes) {
								server.send(sn, "FScreen" + server.SPL + sn);
							} else {
								Platform.runLater(() -> {
									tabPane.getTabs().removeIf(a -> a.getId().contains("Desktop" + String.valueOf(sn)));

								});
							}

						});
					}
					break;

				}
			}
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
	}

	private void DosExit(int skn) {
		Stage stage = Functions.getStage("CommandLine", skn);
		if (stage != null) {
			Platform.runLater(() -> {

				stage.close();
			}

			);
		}

	}

	private void RecM(int skn, byte[] data) {

		// System.out.print((char)Integer.parseInt(data));
		Stage stage = Functions.getStage("Recorder", skn);

		if (stage != null) {

			FXMLLoader loader = (FXMLLoader) (((UserData) (stage.getUserData())).getLoader());
			Recorder Controller = (Recorder) (loader.getController());

			Controller.setDataM(data);

		}

	}

	private void Rec(int skn, byte[] data) {

		Stage stage = Functions.getStage("Recorder", skn);

		if (stage != null) {

			FXMLLoader loader = (FXMLLoader) (((UserData) (stage.getUserData())).getLoader());
			Recorder Controller = (Recorder) (loader.getController());

			Controller.setData(data);

		}

	}

	private void setDrives(int skn, String data) {
		Tab tab = getTab(tabPane, "FileExplorer", String.valueOf(skn));
		if (tab != null) {

			FileBrowserCtrl Controller = (FileBrowserCtrl) tab.getUserData();

			Controller.setDrives(data);

		}
	}

	private void command(int skn, String data) {

		Tab tab = getTab(tabPane, "CommandLine", String.valueOf(skn));
		if (tab != null) {

			DosCtrl Controller = (DosCtrl) tab.getUserData();

			Controller.setData(data);

		}

	}

	private void BrowserFiles(int skn, String data, String data2) {

		Tab tab = getTab(tabPane, "FileExplorer", String.valueOf(skn));
		if (tab != null) {

			FileBrowserCtrl Controller = (FileBrowserCtrl) tab.getUserData();

			Controller.setData(data, data2);

		}

	}

	private void SetImage(int skn, byte[] data, int sharpSocket) {

		Tab tab = getTab(tabPane, "Desktop", String.valueOf(skn));
		if (tab != null) {

			RemoteDesktopCtrl Controller = (RemoteDesktopCtrl) tab.getUserData();

			Controller.setData(data, sharpSocket);

		}

	}

	private void SetSubImage(int skn, byte[] data, int x, int y, int sharpSocket) {

		Tab tab = getTab(tabPane, "Desktop", String.valueOf(skn));
		if (tab != null) {

			RemoteDesktopCtrl Controller = (RemoteDesktopCtrl) tab.getUserData();

			Controller.setData2(data, x, y, sharpSocket);
		}

	}

	private void SetCursor(int skn, byte cursor) {

		Tab tab = getTab(tabPane, "Desktop", String.valueOf(skn));
		if (tab != null) {

			RemoteDesktopCtrl Controller = (RemoteDesktopCtrl) tab.getUserData();

			Controller.setCursor(cursor);
		}

	}

	private void recINI(int skn, String name) {
		Stage stage = Functions.getStage("Recorder", skn);

		if (stage == null) {

			Stage primaryStage = new Stage();
			Scene scene;
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/DesktopRecorder.fxml"));
				scene = new Scene((Parent) loader.load());
				primaryStage.setTitle(name + " Remote Desktop");
				primaryStage.setScene(scene);
				primaryStage.setMinWidth(743);
				primaryStage.setMinHeight(540);
				primaryStage.show();
				Recorder c = loader.getController();
				c.sn = skn;
				UserData userdata = new UserData(loader, "Recorder", String.valueOf(skn));
				primaryStage.setUserData(userdata);

				server.send(skn, "RecSrt" + server.SPL);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {

			stage.show();
			stage.requestFocus();
		}

	}

	private void commandIN(int skn, String cmd, String name) {

		Stage stage = Functions.getStage("CommandLine", skn);

		if (stage == null) {

			Stage primaryStage = new Stage();
			Scene scene;
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/DosWindow.fxml"));
				scene = new Scene((Parent) loader.load());
				primaryStage.setTitle(name + " Command Line");
				primaryStage.setScene(scene);
				primaryStage.setMinWidth(743);
				primaryStage.setMinHeight(540);
				primaryStage.show();
				DosCtrl c = loader.getController();
				UserData userdata = new UserData(loader, "CommandLine", String.valueOf(skn));
				primaryStage.setUserData(userdata);

				server.send(skn, "CommandLineIn" + Server.SPL);

			} catch (IOException e) {

			}

		} else {

			stage.show();
			stage.requestFocus();
		}

	}

	private void Browse(int skn, String pth, String name) {

		Stage stage = Functions.getStage("FileBrowser", skn);

		if (stage == null) {

			Stage primaryStage = new Stage();
			Scene scene;
			try {
				FXMLLoader loader = new FXMLLoader(getClass().getResource("FXML/FileBrowserWindow.fxml"));
				scene = new Scene((Parent) loader.load());

				primaryStage.setTitle(name + " file Browser");
				primaryStage.setScene(scene);
				primaryStage.setMinWidth(743);
				primaryStage.setMinHeight(540);
				primaryStage.show();
				FileBrowserCtrl c = loader.getController();
				UserData userdata = new UserData(loader, "FileBrowser", String.valueOf(skn));
				primaryStage.setUserData(userdata);

				server.send(skn, "BrowsFilesINI" + server.SPL);
			} catch (IOException e) {

			}

		} else {

			stage.show();
			stage.requestFocus();
		}

	}

	@FXML
	public void FileBrowser() {
		synchronized (lock) {

			TreeItem<ConnectionItem> SlectedItem = mainList.getSelectionModel().getSelectedItem();

			if (SlectedItem != null) {

				String socketnubmer = SlectedItem.getValue().getSock();
				String username = SlectedItem.getValue().getUser();
				Tab tab = getTab(tabPane, "FileExplorer", socketnubmer);

				if (tab == null) {

					server.send(Integer.parseInt(socketnubmer), "BrowsFilesINI" + server.SPL);

				} else
					tabPane.getSelectionModel().select(tab);

			}
		}
	}

	private void initFileBrowser(int sn) {
		synchronized (lock) {

			TreeItem<ConnectionItem> SlectedItem = find_user(root, String.valueOf(sn));

			if (SlectedItem != null) {

				String socketnubmer = SlectedItem.getValue().getSock();
				String username = SlectedItem.getValue().getUser();
				Tab tab = getTab(tabPane, "FileExplorer", socketnubmer);
				if (tab == null) {
					try {
						Tab newTab = createTab(username + " File Explorer", "FileExplorer" + socketnubmer);
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("FXML/ExplorerWindow.fxml"));
						FileBrowserCtrl ctrl = new FileBrowserCtrl(Integer.parseInt(socketnubmer), username);

						loader.setController(ctrl);
						Parent parent = loader.load();
						newTab.setClosable(true);
						newTab.setUserData(ctrl);
						newTab.setContent(parent);
						final Waiter wait=new Waiter();
						Platform.runLater(() -> {
							tabPane.getTabs().add(newTab);
							tabPane.getSelectionModel().select(newTab);
							wait.setState(false);
						});
						while(wait.getState()){
							Thread.sleep(10);
						}
					} catch (IOException e) {

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				} else
					tabPane.getSelectionModel().select(tab);

			}
		}
	}

	@FXML
	public void CommandLine() {
		synchronized (lock) {
			TreeItem<ConnectionItem> SlectedItem = mainList.getSelectionModel().getSelectedItem();
			// System.out.println(SlectedItem.getValue().getSock());

			if (SlectedItem != null) {

				String socketnubmer = SlectedItem.getValue().getSock();
				String username = SlectedItem.getValue().getUser();
				Tab tab = getTab(tabPane, "CommandLine", socketnubmer);
				if (tab == null) {

					server.send(Integer.parseInt(socketnubmer), "CommandLineIn" + server.SPL);

				} else
					tabPane.getSelectionModel().select(tab);

			}
		}

	}

	private void initCommand(int sn) {
		synchronized (lock) {
			TreeItem<ConnectionItem> SlectedItem = find_user(root, String.valueOf(sn));
			// System.out.println(SlectedItem.getValue().getSock());

			if (SlectedItem != null) {

				String socketnubmer = SlectedItem.getValue().getSock();
				String username = SlectedItem.getValue().getUser();
				Tab tab = getTab(tabPane, "CommandLine", socketnubmer);
				if (tab == null) {
					try {
						Tab newTab = createTab(username + " Remote DOS", "CommandLine" + socketnubmer);
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("FXML/DosWindow.fxml"));
						DosCtrl ctrl = new DosCtrl(Integer.parseInt(socketnubmer));
						loader.setController(ctrl);
						Parent parent = loader.load();
						newTab.setClosable(true);
						newTab.setUserData(ctrl);
						newTab.setContent(parent);
						final Waiter wait=new Waiter();
						Platform.runLater(() -> {
							tabPane.getTabs().add(newTab);
							tabPane.getSelectionModel().select(newTab);
							wait.setState(false);
						});
						while(wait.getState()){
							Thread.sleep(10);
						}
					} catch (IOException e) {

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				} else
					tabPane.getSelectionModel().select(tab);

			}
		}

	}

	@FXML
	public void viewer() {
		synchronized (lock) {
			TreeItem<ConnectionItem> SlectedItem = mainList.getSelectionModel().getSelectedItem();
			if (SlectedItem != null) {

				String socketnubmer = SlectedItem.getValue().getSock();
				String username = SlectedItem.getValue().getUser();
				Tab tab = getTab(tabPane, "Desktop", socketnubmer);

				if (tab == null) {
					server.send(Integer.parseInt(socketnubmer), "Screen" + server.SPL + socketnubmer);
				} else
					tabPane.getSelectionModel().select(tab);

			}
		}
	}

	private void initViewer(int sn,boolean force) {
		synchronized (lock) {
			final boolean forc=force;
			final int skn=sn;
			TreeItem<ConnectionItem> SlectedItem = find_user(root, String.valueOf(sn));
			if (SlectedItem != null) {

				String socketnubmer = SlectedItem.getValue().getSock();
				String username = SlectedItem.getValue().getUser();
				Tab tab = getTab(tabPane, "Desktop", socketnubmer);

				if (tab == null) {
					try {
						Tab newTab = createTab(username + " Remote desktop", "Desktop" + socketnubmer);
						FXMLLoader loader = new FXMLLoader();
						loader.setLocation(getClass().getResource("FXML/DesktopViewer.fxml"));
						final RemoteDesktopCtrl ctrl = new RemoteDesktopCtrl(Integer.parseInt(socketnubmer));

						loader.setController(ctrl);
						Parent parent = loader.load();
						newTab.setClosable(true);
						newTab.setUserData(ctrl);
						newTab.setContent(parent);
						
						newTab.setOnCloseRequest(new EventHandler<Event>() {

							@Override
							public void handle(Event event) {
								System.out.println("hel");
								ctrl.Stop();
							}
						});
						Platform.runLater(() -> {
							tabPane.getTabs().add(newTab);
							tabPane.getSelectionModel().select(newTab);
							if(forc)
								server.send(Integer.parseInt(socketnubmer), "FstartDeskt" + server.SPL + socketnubmer);
							else
								server.send(Integer.parseInt(socketnubmer), "startDeskt" + server.SPL + socketnubmer);
						});
					
					} catch (IOException e) {

						e.printStackTrace();
					}  
				} else
					tabPane.getSelectionModel().select(tab);

			}
		}
	}

	@FXML
	void exitButton_action(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	void portButton_action(ActionEvent event) {
		setPort();

	}

	private void setPort() {

		boolean notset = true;
		Integer p = null;
		while (notset) {
			TextInputDialog dialog = new TextInputDialog("port");
			dialog.setTitle("Set port");
			dialog.setHeaderText("please enter a port to listen on it");
			dialog.setContentText("port:");
			// Traditional way to get the response value.
			Optional<String> result = dialog.showAndWait();
			try {
				p = Integer.parseInt(result.get());
				notset = false;
			} catch (java.lang.NumberFormatException e) {
				e.printStackTrace();
			}

		}
		port = p;
		save_settings();
	}

	public void save_settings() {
		File file = new File(APP_PATH + "\\server config");
		FileOutputStream writer;
		String settings = startup + "\n" + port;
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
	}

	private void startlistning() {
		if (startButton.getText().equals("Start")) {
			if (server != null)
				server.dispose();
			if (port != null) {
				server = new Server(port, this);
				// startButton.setText("Stop");
			}

		} else {
			server.dispose();

			// startButton.setText("Start");

		}
	}

	@FXML
	private MenuItem startButton;

	@FXML
	void startButton_action(ActionEvent event) {

		startlistning();
	}

	@FXML // this method is used to refrech TreeTableView cause of a drawing bug
			// when collapsing
	void release() {

		mainList.refresh();
	}

	@Override
	public void setStatus(boolean state) {
		if (state)
			Platform.runLater(() -> {
				startButton.setText("Stop");
				status_label.setText("Status: running on port " + port);

			});
		else
			Platform.runLater(() -> {
				startButton.setText("Start");
				status_label.setText("Status: stopped");
			});
	}

}