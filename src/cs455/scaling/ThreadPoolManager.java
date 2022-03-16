package cs455.scaling;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class ThreadPoolManager extends Thread{

	private int totalMessagesSent = 0;
	private int totalMessagesReceived = 0;


	// Number of threads to initialize and keep alive for the duration of the program
	private final int numThreads;

	// Number of threads to initialize and keep alive for the duration of the program
	private int totalNumConnections;

	// Port Number the Server is listening on
	private final int portnum;

	// Maximum size of the list of pending tasks for workerThreads to handle
	private final int batchSize;
	
	// Maximum time duration that tasks should be sitting in the pendingTasks queue before being assigned to workerThreads 
	private final int batchTime;

	// Data structure for containing all the threads
	private HashSet<WorkerThread> allWorkerThreads = new HashSet<>();

	// Data structure for queue of pending tasks
	private static LinkedList<SelectionKey> pendingTasks = new LinkedList<SelectionKey>(); // This is one of the 7 different types of implementations of the BlockingQueue interface, (this is likely? the one we want)

	// Class for managing time between last batch was dispersed to workerThreads
	private BatchTimer batchTimer;


	//Used for debugging purposes to keep track of the number of tasks generated variables (DELETE WHEN NO LONGER NEEDED)
	int totalNumTasks = 0;

	public ThreadPoolManager(int portnum, int numThreads, int batchSize, int batchTime){
		this.numThreads = numThreads;
		this.portnum = portnum;
		this.batchSize = batchSize;
		this.batchTime = batchTime;

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

	public LinkedList<SelectionKey> getTaskList(){
		synchronized (this){
			return pendingTasks;
		}
	} 

	public void addTask(SelectionKey key){
			if (!getPendingTasks().contains(key) && getPendingTasks().size() < batchSize){
					addKey(key);
				}
	}

	public boolean addKey(SelectionKey key){
		synchronized (this) {
			return pendingTasks.add(key);
		}
	}

	public synchronized int getTotalSent(){
		return totalMessagesSent;
	}

	public synchronized void resetTotalSent(){
		this.totalMessagesSent= 0;
	}
	
	public synchronized int getTotalMessagesReceived(){
		return totalMessagesReceived;
	}

	public synchronized void decrementNodesConnected(){
		--totalNumConnections;
	}	

	public synchronized void incrementNodesConnected(){
		++totalNumConnections;
	}

	public synchronized int getNumNodesConnected(){
		return totalNumConnections;
	}

	public synchronized void incrementTotalReceived(){
		++totalMessagesReceived;
	}
	
	public synchronized void incrementTotalSent(){
		++totalMessagesSent;
	}

	public LinkedList<SelectionKey> getPendingTasks(){
		synchronized (this){
			return pendingTasks;
		}
	}

	public void clearPendingTasks(){
		synchronized(this){
			pendingTasks.clear();
		}
	}

	private boolean pendingTasksContainsRegistry(){
		synchronized (this){
			if (pendingTasks.size() != 0){
				for (SelectionKey sk: pendingTasks){
					if (sk.isAcceptable()){
						return true;
					}
				}
			}
			return false;
		}
	}

	public void checkForNewKeys(){
		while (true){
			int batchLoad = getPendingTasks().size();
			boolean batchReady = batchTimer.getBatchReadyStatus();
			boolean batchContainsRegistry = pendingTasksContainsRegistry();
			if (batchLoad == batchSize || batchReady == true || pendingTasksContainsRegistry()){
				while (getPendingTasks().size() != 0){ // Start assigning keys to threads until it's empty
					synchronized(this){
						LinkedList<SelectionKey> currentKeys = this.getPendingTasks();
						Iterator<SelectionKey> keyIterator = currentKeys.iterator();
						while (keyIterator.hasNext()){
							SelectionKey key = keyIterator.next();
							WorkerThread nextWorker = getNextAvailableWorker(); // Get the next available worker, this is a blocking call that waits until one is available
							if (nextWorker != null){
								++totalNumTasks;
								nextWorker.setNextKey(key);
								nextWorker.notifyWorker();
							}
						}
						clearPendingTasks();
					}
				}
				batchTimer.setBatchReady(false);
			}
		}
	}

	private WorkerThread getNextAvailableWorker(){
		while (true){
			for (WorkerThread wt : allWorkerThreads){
				if (wt.isAvailable()){
					wt.setAvailability(false);
					return wt;
				}
			}	
		}
	}

}
