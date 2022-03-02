package cs455.scaling.client;
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

public class Client {
	
	private static SocketChannel client;
	private static ByteBuffer buffer;

	public static void main(String[] args) throws IOException {
	
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

	}
}
