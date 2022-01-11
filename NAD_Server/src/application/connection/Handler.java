package application.connection;

public interface Handler {
	
	public void Data(byte data[],int socketNumber);
	public void Diconnected(int socketNumber);
	public void Exceptions(String message);
	public void setStatus(boolean state);
}
