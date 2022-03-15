package cs455.scaling;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import cs455.scaling.ClientReceiverThread;

import java.sql.Timestamp;
import java.util.Date;
import java.time.Instant;
import java.lang.Thread;
import java.time.LocalDateTime;
import java.math.BigInteger;

public class Client {
	
	private int totalSentCount = 0;
	private int totalReceivedCount= 0;

	private static SocketChannel client;
	private static ByteBuffer writeBuffer;
	private static ByteBuffer readBuffer;

	public String serverHost = "";
	public int serverPort = -1;
	int messageRate = -1;

	LinkedList<String> unverifiedHashes = new LinkedList<String>();
	
	public Client (){};
	
	public Client (String serverHost, int serverPort, int messageRate){
		AutomaticExit ae = new AutomaticExit(1);
		ae.start();

		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageRate = messageRate;

		try {
			System.out.println("Trying to connect to " + serverHost + ":" + serverPort);
			client = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));
			writeBuffer = ByteBuffer.allocate(8*Constants.KB);
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		PrintStatsThread pst = new PrintStatsThread(this);
		pst.start();

		ClientReceiverThread crt = new ClientReceiverThread(client, this);
		crt.start();

		while (true) {
		
			HashMessage nextMessage = new HashMessage();
			byte[] unhashedMessageBytes = nextMessage.getByteArray();
			String hashedMessageString = nextMessage.getHashedString();
			System.out.println("Sent: " + nextMessage.bytesToString(unhashedMessageBytes).substring(0,5));
			System.out.println("Expecting: " + hashedMessageString.substring(0,5));
		
			writeBuffer = ByteBuffer.wrap(unhashedMessageBytes);
			// System.out.println("Message to send is " + unhashedMessageBytes.length + " bytes long");
			// readBuffer = ByteBuffer.allocate(Constants.KB * 8);

			try{
				client.write(writeBuffer);
				incrementTotalSent();
				addToUnverifiedHashes(hashedMessageString);
				// unverifiedHashes.add(hashedMessageString);
				writeBuffer.clear();
				// client.read(readBuffer);
				// incrementTotalReceived();
				// byte[] response = readBuffer.array();
				// String responseString = new String(response);
				// responseString = responseString.substring(0,40); // Trim excess padded zeros off of string
				// boolean verified = false;
				// if (unverifiedHashes.contains(responseString)) {
					// verified = true;
					// unverifiedHashes.remove(responseString);
				// }
				// System.out.println("Response from server: " + responseString.substring(0,5));
				// System.out.println("Response valid?: " + verified);
				// System.out.println("Length expected: " + hashedMessageString.length() + " actual: " + responseString.length());

				// readBuffer.clear();
				Thread.sleep(1000 / messageRate);
			}
			catch (IOException | InterruptedException e) {
				System.out.println("Disconnected from SocketChannel, did the server close?");
				e.printStackTrace();
			}
		}
	}

	public synchronized LinkedList<String> getUnverifiedHashes(){
		return unverifiedHashes;
	}

	public synchronized void addToUnverifiedHashes(String newHash){
		unverifiedHashes.add(newHash);
	}

	public synchronized void incrementTotalSent(){
		totalSentCount++;
	}

	public synchronized void incrementTotalReceived(){
		totalReceivedCount++;
	}

	public synchronized int getTotalSent(){
		return totalSentCount;
	}

	public synchronized int getTotalReceived(){
		return totalReceivedCount;
	}

	public static void main(String[] args) throws IOException {

		if (args.length == 3){
			// First argument is server-host
			String serverHost = args[0];
			// Second argument is server-port
			int serverPort = Integer.parseInt(args[1]);
			// Third argument is message-rate
			int messageRate = Integer.parseInt(args[2]);
			System.out.println("Starting client w/ serverHost: " + serverHost + ", serverPort: " + serverPort + ", messageRate: " + messageRate + " messages per second.");
			Client clientObj = new Client(serverHost, serverPort, messageRate);
		}

		else {
			System.out.println("No program arguments specified, please specify: (1) serverHost, (2) serverPort, and (3) messageRate (X messages per second)");
		}
		return;
	}
}
