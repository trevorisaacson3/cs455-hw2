package cs455.scaling;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;


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
	// Port Number the Server is listening on
	private final int portnum;

	// TODO: Data structure for containing all the threads here
	Set<WorkerThread> allWorkerThreads;
	// TODO: Data structure for queue of pending tasks here
	BlockingQueue<SelectionKey> pendingTasks = new SynchronousQueue<>(); // This is one of the 7 different types of implementations of the BlockingQueue interface
	public ThreadPoolManager(int portnum, int numThreads){
		this.numThreads = numThreads;
		this.portnum = portnum;
		PrintStatsThread pst = new PrintStatsThread(this);
		pst.start();


		// Create {numThreads} number of threads here and add them to the thread pool, initializing all them in the idle state.
		 for (int i = 0; i < numThreads; i++){
		 	allWorkerThreads.add(new WorkerThread());
		 }
	}


//	public void assignTask(/* params? here */){ //Purpose: Assign task to a thread
//		return;
//	}

	//@Override
	public void run() {
		try {
			Selector selector = Selector.open();
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress(portnum));
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
			ByteBuffer readBuffer = ByteBuffer.allocate(8000);
			SocketChannel client = (SocketChannel) key.channel();

			int bytesRead = client.read(readBuffer);

			if (bytesRead == -1){
				client.close();
				System.out.println("Client has disconnected");
			}
			else {
				byte[] receivedByteArray = readBuffer.array();
				HashMessage receivedHashMessage = new HashMessage(receivedByteArray);
				String hashedMessageString = receivedHashMessage.getHashedString();
				System.out.println("Message being sent to client: " + hashedMessageString);
				readBuffer.clear();
				ByteBuffer writeBuffer = ByteBuffer.allocate(hashedMessageString.getBytes().length);
				writeBuffer = ByteBuffer.wrap(hashedMessageString.getBytes());
				client.write(writeBuffer);
				writeBuffer.clear();
			}
		}



	}
