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
import java.lang.Thread;



public class ThreadPoolManager{

	private int totalMessagesSent = 0;
	private int totalMessagesReceived = 0;

	public int getTotalSent(){
		return totalMessagesSent;
	}

	public int getTotalMessagesReceived(){
		return totalMessagesReceived;
	}

	public int getNumNodesConnected(){
		//TODO Implement datastructure for holding nodes and grab the size of the datastructure here.
		return 0;
	}

	public double getMeanClientThroughput(){
		//TODO Calculate mean client throughput and return here
		return 0.0;
	}

	public double getStdDevThroughput(){
		//TODO Calculate std dev of client throughput and return here
		return 0.0;
	}

	// Number of threads to initialize and keep alive for the duration of the program
	private final int numThreads;

	// TODO: Data structure for containing all the threads here
	// TODO: Data structure for queue of pending tasks here

	public ThreadPoolManager(int numThreads){
		this.numThreads = numThreads;
		PrintStatsThread pst = new PrintStatsThread(this);
		pst.start();


		// Create {numThreads} number of threads here and add them to the thread pool, initializing all them in the idle state.
		// for (int i = 0; i < numThreads; i++){
		// 	intializeNewThread();
		// }
	}


//	public void assignTask(/* params? here */){ //Purpose: Assign task to a thread
//		return;
//	}

	//@Override
	public void run() {
		try {
			Selector selector = Selector.open();
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress( 5001));
			serverSocket.configureBlocking(false);

			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {
				System.out.println("Listening for new connections or messages");

				selector.select();

				System.out.println("\tActivity on selector!");

				Set<SelectionKey> selectedKeys = selector.selectedKeys();

				Iterator<SelectionKey> iter = selectedKeys.iterator();
				while (iter.hasNext()) {

					SelectionKey key = iter.next();
					
					if (key.isValid() == false) {
						continue;
					}

					// Open new connection on serverSocket
					if (key.isAcceptable()) {
						register(selector, serverSocket);
					}

					// Read data from previous connection
					if (key.isReadable()) {
						readAndRespond(key);
					}

					// Remove from the set when done
					iter.remove();
				}
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

		private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
			SocketChannel client = serverSocket.accept();

			client.configureBlocking(false);
			client.register(selector, SelectionKey.OP_READ);
			System.out.println("\t\tNew client registered.");
		}

		private static void readAndRespond(SelectionKey key) throws IOException {
			ByteBuffer buffer = ByteBuffer.allocate(256);
			SocketChannel client = (SocketChannel) key.channel();

			int bytesRead = client.read(buffer);

			if (bytesRead == -1){
				client.close();
				System.out.println("Client has disconnected");
			}
			else {
				System.out.println("\t\tReceived: " + new String(buffer.array()));
				System.out.println("\t\tRaw message: " + new String(buffer.array()) + ".getBytes() = " + new String(buffer.array()).getBytes());
				//System.out.println("\t\tReceived (as byte[]) : " + buffer.array());
	
				
				byte[] receivedMessage = buffer.array();
				String receivedString = new String (buffer.array());
				HashMessage convertToHash = new HashMessage(receivedString);
				byte[] hashedMessage = convertToHash.getHashedString().getBytes();
				System.out.println("Going to write back a hashed message of: " + new String(hashedMessage));

				//This allows the buffer to now write instead of read
				//buffer.clear();
				buffer.flip();
				client.write(buffer);
				buffer.clear();
			}
		}



	}
