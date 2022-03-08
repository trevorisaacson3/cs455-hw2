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
	private static ByteBuffer buffer;

	public String serverHost = "";
	public int serverPort = -1;
	int messageRate = -1;

	LinkedList<String> allHashes = new LinkedList<String>();
	
	public Client (){};
	
	public Client (String serverHost, int serverPort, int messageRate){

		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageRate = messageRate;

		try {
			System.out.println("Trying to connect to " + serverHost + ":" + serverPort);
			client = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));
			buffer = ByteBuffer.allocate(8);
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		PrintStatsThread pst = new PrintStatsThread(this);
		pst.start();

		while (true) {
		
			HashMessage nextMessage = new HashMessage();
			byte[] unhashedMessageBytes = nextMessage.getByteArray();
			String hashedMessageString = nextMessage.getHashedString();
			System.out.println("Hashed String of bytes being sent to server: " + hashedMessageString);
		
			buffer = ByteBuffer.wrap(unhashedMessageBytes);

			try{
				client.write(buffer);
				incrementTotalSent();
				buffer.clear();
				client.read(buffer);
				incrementTotalReceived();
				byte[] response = buffer.array();
				String responseString = new String(response).substring(0, 40);
				boolean valid = false;
				if (responseString.equals(hashedMessageString)) {
					valid = true;
				}
				System.out.println("Response from server: " + responseString);
				System.out.println("Response is valid: " + valid);
				buffer.clear();
				Thread.sleep(1000 / messageRate);
			}
			catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
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
