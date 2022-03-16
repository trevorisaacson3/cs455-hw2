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

	public volatile LinkedList<String> unverifiedHashes = new LinkedList<String>();
	
	public Client (){};
	
	public Client (String serverHost, int serverPort, int messageRate){
		AutomaticExit ae = new AutomaticExit(9999);
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
		
			writeBuffer = ByteBuffer.wrap(unhashedMessageBytes);
			try{
				addToUnverifiedHashes(hashedMessageString);
				client.write(writeBuffer);
				incrementTotalSent();
				writeBuffer.clear();
				Thread.sleep(1000 / messageRate);
			}
			catch (IOException | InterruptedException | NullPointerException e) {
				System.out.println("Disconnected from SocketChannel, did the server close?");
				System.exit(1);
			}
		}
	}

	public LinkedList<String> getUnverifiedHashes(){
		synchronized(this){
			return unverifiedHashes;
		}
	}

	public void addToUnverifiedHashes(String newHash){
		synchronized (this){
			unverifiedHashes.add(newHash);
		}
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

	public synchronized void resetTotalSent(){
		this.totalSentCount = 0;
	}

	public synchronized void resetTotalReceived(){
		this.totalReceivedCount = 0;
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
