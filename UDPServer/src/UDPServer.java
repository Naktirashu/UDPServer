import java.io.*;

import java.net.*;
import java.util.Observable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.*;

public class UDPServer extends Observable implements Runnable {
	
	static ServerGui serverGui;
	private BlockingQueue<String> queue = new ArrayBlockingQueue<String>(50);


	public final static int DEFAULT_PORT = 7;

	private final int bufferSize; // in bytes

	private final int port;
	

	//private final Logger logger = Logger.getLogger(UDPServer.class.getCanonicalName());

	private volatile boolean isShutDown = false;


	private String messageReceived;
	
	//maybe use this to have Math.random set this to true when its a failure then check this before sending response
	private boolean failure = false;

	
	public UDPServer (int port) {

		this(port, 1892);

	}

	public UDPServer(int port, int i) {
		this.port = port;
		this.bufferSize = i;
		
	}
	
	public UDPServer() {

		this(DEFAULT_PORT);

	}


	@Override

	public void run() {

		byte[] buffer = new byte[bufferSize];

		try (DatagramSocket socket = new DatagramSocket(port)) {

			socket.setSoTimeout(10000); // check every 10 seconds for shutdown
			
			MessageHandlerThread messageThread = new MessageHandlerThread(this);
			messageThread.start();
			

			while (true) {

				if (isShutDown) return;

				DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

				try {

					socket.receive(incoming);
					
					//FIXME this may not be the best way to do it, but works.
					Double failureGenerator = (Math.random()* 100);
					System.out.println("failureGenerator= " + failureGenerator);
					if(failureGenerator > serverGui.getPacketLossPercentage()){
						failure = false;
					}else{
						failure = true;
					}
					
					
					if(failure == true){
						System.out.println("Failed to receive Packet: Caused by Failure Percentage Setting");
					}else{
						//Prints the message that was sent from client, will need to change for File though
						//FIXME
						String s = new String(incoming.getData(),0,incoming.getLength(), "UTF-8");
						//System.out.println("From Client:" + s);
						
						try {
							//Put the new message on the blockingQueue
							queue.put(s);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						this.respond(socket, incoming);
						
						Thread.yield();
					}
					
					

				} catch (SocketTimeoutException ex) {

					if (isShutDown) return;

				} catch (IOException ex) {

					//logger.log(Level.WARNING, ex.getMessage(), ex);

				}

			} // end while

		} catch (SocketException ex) {

			//logger.log(Level.SEVERE, "Could not bind to port: " + port, ex);

		}

	}

	public void shutDown() {

		this.isShutDown = true;

	}

	public void respond(DatagramSocket socket, DatagramPacket packet) throws IOException {

		DatagramPacket outgoing = new DatagramPacket(packet.getData(),

			packet.getLength(), packet.getAddress(), packet.getPort());

		socket.send(outgoing);

	}
	
	public static void main(String[] args) {

		UDPServer server = new UDPServer();
		
		serverGui = new ServerGui();
		
		//severGui observes the server
		server.addObserver(serverGui);
		
		//Display GUI
		serverGui.setVisible(true);

		Thread t = new Thread(server);

		//Start the server Thread
		t.start();

	}

	public void setMessageReceived(String messageReceived) {
		this.messageReceived = messageReceived;
		
		//We got new message, tell the Observers
		setChanged();
		notifyObservers(messageReceived);
		
	}

	public BlockingQueue<String> getQueue() {
		return queue;
	}

	public void setQueue(BlockingQueue<String> queue) {
		this.queue = queue;
	}
	
	public boolean isFailure() {
		return failure ;
	}

	public void setFailure(boolean failure) {
		this.failure = failure;
	}


}
