import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class ThreadPoolManager {

	private final int numThreads;
	// Data structure for containing all the threads here
	// Data structure for queue of pending tasks here

	public ThreadPoolManager(int numThreads){
		this.numThreads = numThreads;


		// Create {numThreads} number of threads here and add them to the thread pool, initializing all them in the idle state.
		// for (int i = 0; i < numThreads; i++){
		// 	intializeNewThread();
		// }
	}


	public void assignTask(/* params? here */){ //Purpose: Assign task to a thread
		return;
	}

	
}
