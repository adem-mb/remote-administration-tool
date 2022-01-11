package application.Association;

import java.io.Serializable;

public class DataObject implements Serializable {
	public String command,data1[];
	public byte[] data2=null;
	public DataObject(String command,String data1[],byte[] data2){
		this.command=command;
		this.data1=data1;
		this.data2=data2;
		
	}
}
