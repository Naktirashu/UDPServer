import java.io.*;

import java.net.*;

import java.util.logging.*;

public class UDPServer implements Runnable {

	public final static int DEFAULT_PORT = 7;

	private final int bufferSize; // in bytes

	private final int port;

	//private final Logger logger = Logger.getLogger(UDPServer.class.getCanonicalName());

	private volatile boolean isShutDown = false;

	
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

			while (true) {

				if (isShutDown) return;

				DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

				try {

					socket.receive(incoming);
					
					//Prints the message that was sent from client, will need to change for File though
					//FIXME
					String s = new String(incoming.getData(),0,incoming.getLength(), "UTF-8");
					System.out.println("From Client:" + s);

					this.respond(socket, incoming);

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

		Thread t = new Thread(server);

		t.start();

	}

}
