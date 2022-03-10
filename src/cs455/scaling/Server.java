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

public class Server {

	public static void main(String[] args) throws IOException {
	
		if (args.length == 4){
			// First argument is portnum
			int portnum = Integer.parseInt(args[0]);
			// Second argument is thread-pool-size
			int threadPoolSize = Integer.parseInt(args[1]);
			// Third argument is batch-size
			int batchSize = Integer.parseInt(args[2]);
			// Fourth argument is batch-time		
			int batchTime = Integer.parseInt(args[3]);
			System.out.println("Starting server w/ portnum: " + portnum + ", threadPoolSize: " + threadPoolSize + ", batchSize: " + batchSize + ", batchTime: " + batchTime + " seconds.");
			ThreadPoolManager tpm = new ThreadPoolManager(portnum, threadPoolSize, batchSize, batchTime);
			tpm.checkForNewKeys();


		}

		else{
			System.out.println("No program arguments specified, please specify: (1) portnum, (2) threadPoolSize, (3) batchSize, (4) batchTime (in seconds).");
		}
		// Potential Thread Pool Manager

	//**************** BEGIN PROVIDED CODE ***************

//		Selector selector = Selector.open();
//		ServerSocketChannel serverSocket = ServerSocketChannel.open();
//		serverSocket.bind(new InetSocketAddress("localhost", 5001));
//		serverSocket.configureBlocking(false);
//
//		serverSocket.register(selector, SelectionKey.OP_ACCEPT);
//
//		while (true){
//			System.out.println("Listening for new connections or messages");
//
//			selector.select();
//
//			System.out.println("\tActivity on selector!");
//
//			Set<SelectionKey> selectedKeys = selector.selectedKeys();
//
//			Iterator<SelectionKey> iter = selectedKeys.iterator();
//			while (iter.hasNext()) {
//
//				SelectionKey key = iter.next();
//				// Optionally remove this, continue statement are fairly useless
//				if (key.isValid() == false){
//					continue;
//				}
//
//				// Open new connection on serverSocket
//				if (key.isAcceptable()) {
//					register(selector, serverSocket);
//				}
//
//				// Read data from previous connection
//				if (key.isReadable()){
//					readAndRespond(key);
//				}
//
//				// Remove from the set when done
//				iter.remove();
//			}
//		}

	}

//	private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
//		SocketChannel client = serverSocket.accept();
//
//		client.configureBlocking(false);
//		client.register(selector, SelectionKey.OP_READ);
//		System.out.println("\t\tNew client registered.");
//	}
//
//	private static void readAndRespond(SelectionKey key) throws IOException {
//		ByteBuffer buffer = ByteBuffer.allocate(256);
//		SocketChannel client = (SocketChannel) key.channel();
//
//		int bytesRead = client.read(buffer);
//
//		if (bytesRead == -1){
//			client.close();
//			System.out.println("Client has disconnected");
//		}
//		else {
//			System.out.println("\t\tReceived: " + new String(buffer.array()));
//
//			//This allows the buffer to now write instead of read
//			buffer.flip();
//			client.write(buffer);
//			buffer.clear();
//		}
//	}
	//**************** END PROVIDED CODE ***************
}
