package application.connection;

public interface Handler {
	
	public void Data(byte data[],int clientNumber);
	public void Diconnected(int clientNumber);
	public void Exceptions(String message);
	public void notify(int clinetNumber,String server,String message);

}
