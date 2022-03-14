package cs455.scaling;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;


public class ThreadPoolManager extends Thread{

	private int totalMessagesSent = 0;
	private int totalMessagesReceived = 0;


	// Number of threads to initialize and keep alive for the duration of the program
	private final int numThreads;

	// Port Number the Server is listening on
	private final int portnum;

	// Maximum size of the list of pending tasks for workerThreads to handle
	private final int batchSize;
	
	// Maximum time duration that tasks should be sitting in the pendingTasks queue before being assigned to workerThreads 
	private final int batchTime;

	// Data structure for containing all the threads
	private HashSet<WorkerThread> allWorkerThreads = new HashSet<>();

	// Data structure for queue of pending tasks
	protected static LinkedBlockingDeque<SelectionKey> pendingTasks; // This is one of the 7 different types of implementations of the BlockingQueue interface, (this is likely? the one we want)

	// Class for managing time between last batch was dispersed to workerThreads
	private BatchTimer batchTimer;


	//Used for debugging purposes to keep track of the number of tasks generated variables (DELETE WHEN NO LONGER NEEDED)
	int totalNumTasks = 0;

	public ThreadPoolManager(int portnum, int numThreads, int batchSize, int batchTime){
		this.numThreads = numThreads;
		this.portnum = portnum;
		this.batchSize = batchSize;
		this.batchTime = batchTime;

		this.pendingTasks = new LinkedBlockingDeque<>(batchSize);
		batchTimer = new BatchTimer(batchTime);
		batchTimer.start();
		PrintStatsThread pst = new PrintStatsThread(this);
		pst.start();
		
		// This creates and initializes all the workerThreads 
		for (int i = 0; i < numThreads; i++){
			WorkerThread nextWorker = new WorkerThread(i, this);
			nextWorker.start();
		 	allWorkerThreads.add(nextWorker);
		}

		// This is the selector that scans for new keys, registers new nodes, and creates new jobs.
		KeySelector ks = new KeySelector(this, portnum);
		ks.start();
	}

	public synchronized LinkedBlockingDeque<SelectionKey> getTaskList(){
		return pendingTasks;
	} 

	public void addTask(SelectionKey key){
		boolean keyGotInserted = false;
		while (keyGotInserted == false){
			if (pendingTasks.offerLast(key) == true){
				// System.out.println("Added new key");
				keyGotInserted = true;
			}
		}
	}

	public synchronized int getTotalSent(){
		return totalMessagesSent;
	}

	public synchronized int getTotalMessagesReceived(){
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

	public synchronized void incrementTotalReceived(){
		++totalMessagesReceived;
	}
	
	public synchronized void incrementTotalSent(){
		++totalMessagesSent;
	}

	public void checkForNewKeys(){


		while (true){
			boolean batchReady = batchTimer.getBatchReadyStatus();
			if (this.pendingTasks.size() == batchSize || batchReady == true){
				while (this.pendingTasks.size() != 0){ // Start assigning keys to threads until it's empty
					for(SelectionKey key : pendingTasks){
						// System.out.println("There are this many keys waiting to be taken from the queue: " + pendingTasks.size());
						WorkerThread nextWorker = getNextAvailableWorker(); // Get the next available worker, this is a blocking call that waits until one is available
						if (nextWorker != null){
							++totalNumTasks;
							nextWorker.setNextKey(key);
							nextWorker.setWorkingStatus();
							nextWorker.notifyWorker(totalNumTasks);
							pendingTasks.remove(key);
						}
					}
				}
			}
		}
	}

	private WorkerThread getNextAvailableWorker(){
		while (true){
			for (WorkerThread wt : allWorkerThreads){
				if (wt.isAvailable()){
					return wt;
				}
			}	
		}
	}

}
