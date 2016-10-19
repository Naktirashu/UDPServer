import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MessageHandlerThread extends Thread {
	//test comment
	UDPServer udpServer;
	
	private InetAddress server;
	private DatagramSocket socket;
	private int port;
	private volatile boolean stopped = false;
	String theLine = "";
	
	//test for sending strings
	
	
	public MessageHandlerThread(UDPServer udpServer) {
		//unfinished 
		this.udpServer = udpServer;
	}

	public void halt(){
		this.stopped = true;
	}
	
	
	@Override
	public void run() {
		while(true){
			if(stopped) return;
			//String theLine = null;
			try {
				theLine = udpServer.getQueue().take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			udpServer.setMessageReceived(theLine);
			
			Thread.yield();
		}
		
	}

}
