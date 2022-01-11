package application.Association;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

public class ConnectionItem extends RecursiveTreeObject<ConnectionItem> {
	String Sock, ip,windows,user,group;
	boolean isGroup;
	IsGroup ig;
	public ConnectionItem(String sock, String ip, String windows, String user, String group,boolean isGroup) {

		Sock = sock;
		this.ip = ip;
		this.windows = windows;
		this.user = user;
		this.group = group;
		this.isGroup=isGroup;
		ig=new IsGroup(user, group, isGroup,windows);
	}
	


	public boolean isGroup() {
		return isGroup;
	}

	public void setGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public IsGroup getIg() {
		return ig;
	}

	public void setIg(IsGroup ig) {
		this.ig = ig;
	}

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

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	 public class IsGroup{
			
		 String user,group,windows;
			boolean isGroup;
			public IsGroup(String user, String group, boolean isGroup) {
				super();
				this.user = user;
				this.group = group;
				this.isGroup = isGroup;
			}
			public String getUser() {
				return user;
			}
			public String getWindows() {
				return windows;
			}
			public void setWindows(String windows) {
				this.windows = windows;
			}
			public IsGroup(String user, String group, boolean isGroup, String windows) {
				super();
				this.user = user;
				this.group = group;
				this.windows = windows;
				this.isGroup = isGroup;
			}
			public void setUser(String user) {
				this.user = user;
			}
			public String getGroup() {
				return group;
			}
			public void setGroup(String group) {
				this.group = group;
			}
			public boolean isGroup() {
				return isGroup;
			}
			public void setGroup(boolean isGroup) {
				this.isGroup = isGroup;
			}
			
			
	 }
	


}

