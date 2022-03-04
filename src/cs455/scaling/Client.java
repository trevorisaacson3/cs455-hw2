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
import java.util.LinkedList;
import java.util.Set;
import java.sql.Timestamp;
import java.util.Date;
import java.time.Instant;
import java.lang.Thread;
import java.time.LocalDateTime;

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
			buffer = ByteBuffer.allocate(256);
		}

		catch (IOException e) {
			e.printStackTrace();
		}

		buffer = ByteBuffer.wrap("Please send this back to me".getBytes());
		String response = null;

		try{
			client.write(buffer);
			buffer.clear();
			client.read(buffer);
			response = new String(buffer.array()).trim();
			System.out.println("Server responded with: " + response);
			buffer.clear();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		PrintStatsThread pst = new PrintStatsThread(this);
		pst.start();

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
			System.out.println("serverHost: " + serverHost + ", serverPort: " + serverPort + ", messageRate: once every " + messageRate + " seconds.");
			Client clientObj = new Client(serverHost, serverPort, messageRate);
		}

		else {
			System.out.println("Please provide three arguments: serverHost, serverPort, and messageRate (once every X seconds)");
		}
		return;
	}
}
