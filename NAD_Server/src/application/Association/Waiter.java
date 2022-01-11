package application.Association;

public class Waiter {
	
	private boolean wait=true;
	private Object lock=new Object();
	public boolean getState(){
		synchronized (lock) {
			return wait;
		}
	}
	public void setState(boolean state){
		synchronized (lock) {
			wait=state;
		}
	}
}
