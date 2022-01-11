package application.Association;

import javafx.fxml.FXMLLoader;

public class UserData {
 
	private FXMLLoader loader;
	public UserData(FXMLLoader loader, String type, String sock) {
		super();
		this.loader = loader;
		Type = type;
		this.sock = sock;
	}
	private String Type,sock;
	public FXMLLoader getLoader() {
		return loader;
	}
	public void setLoader(FXMLLoader loader) {
		this.loader = loader;
	}
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	public String getSock() {
		return sock;
	}
	public void setSock(String sock) {
		this.sock = sock;
	}
	
}
