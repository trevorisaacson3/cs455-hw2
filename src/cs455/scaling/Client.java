package cs455.scaling;
import java.io.IOException;
import java.net.ConnectException;
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
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.time.Instant;
import java.lang.Thread;
import java.time.LocalDateTime;
import java.math.BigInteger;

public class Client {
	
	private AtomicInteger totalSentCount = new AtomicInteger(0);
	private AtomicInteger totalReceivedCount= new AtomicInteger(0);

	private static SocketChannel client;
	private static ByteBuffer writeBuffer;
	private static ByteBuffer readBuffer;

	public String serverHost = "";
	public int serverPort = -1;
	int messageRate = -1;

	public LinkedList<String> unverifiedHashes = new LinkedList<String>();
	
	public Client (){};
	
	public Client (String serverHost, int serverPort, int messageRate){
		AutomaticExit ae = new AutomaticExit(9999);
		ae.start();

		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		initialize();
	}


	private void tryConnection(){
		while (true){
			try {
				System.out.println("Trying to connect to " + serverHost + ":" + serverPort);
				client = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));	
				writeBuffer = ByteBuffer.allocate(8*Constants.KB);
				if (client.isConnected()){
					return;
				}
			}		
			catch (IOException e) {		
				// e.printStackTrace();
				System.err.println("Connection to server timed out, retrying...");
			}
		}
	}

	public void initialize(){

		tryConnection();
		System.out.println("Connected to the server.");
				
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

	public synchronized LinkedList<String> getUnverifiedHashes(){
			return unverifiedHashes;
	}

	public synchronized void addToUnverifiedHashes(String newHash){
			unverifiedHashes.add(newHash);
	}

	public void incrementTotalSent(){
		totalSentCount.incrementAndGet();
	}

	public void incrementTotalReceived(){
		totalReceivedCount.incrementAndGet();
	}

	public int getTotalSent(){
		return totalSentCount.get();
	}

	public void resetTotalSent(){
		totalSentCount.set(0);
	}

	public void resetTotalReceived(){
		totalReceivedCount.set(0);
	}

	public int getTotalReceived(){
		return totalReceivedCount.get();
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
