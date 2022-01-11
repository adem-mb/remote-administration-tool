package application.Association;

public class Nitem {

	String Sock, ip,windows,user;

	public String getSock() {
		return Sock;
	}

	public void setSock(String sock) {
		Sock = sock;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getWindows() {
		return windows;
	}

	public void setWindows(String windows) {
		this.windows = windows;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Nitem(String sock, String ip, String windows, String user) {
		super();
		Sock = sock;
		this.ip = ip;
		this.windows = windows;
		this.user = user;
	}

	


}
