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
import java.util.Set;
import java.sql.Timestamp;
import java.util.Date;
import java.time.Instant;
import java.lang.Thread;

public class Client {
	
	private int totalSentCount = 0;
	private int totalReceivedCount= 0;

	private static SocketChannel client;
	private static ByteBuffer buffer;

	String serverHost;
	int serverPort = -1;
	int messageRate = -1;
	
	public Client (String serverHost, int serverPort, int messageRate){

		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
	}

	public static void main(String[] args) throws IOException {

		Client clientObj;
		if (args.length == 3){
			// First argument is server-host
			String serverHost = args[0];
			// Second argument is server-port
			int serverPort = Integer.parseInt(args[1]);
			// Third argument is message-rate
			int messageRate = Integer.parseInt(args[2]);
			System.out.println("serverHost: " + serverHost + ", serverPort: " + serverPort + ", messageRate: once every " + messageRate + " seconds.");
			clientObj = new Client(serverHost, serverPort, messageRate);
		}
		try {
			client = SocketChannel.open(new InetSocketAddress("localhost", 5001));
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
		try{
	
			Date date = new Date();
			while(true){
				long currentTime = date.getTime();
				System.out.println("[" + currentTime + "] Total Sent Count: " + clientObj.totalSentCount + ", Total Received Count: " + clientObj.totalReceivedCount);
				Thread.sleep(20000);
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}
