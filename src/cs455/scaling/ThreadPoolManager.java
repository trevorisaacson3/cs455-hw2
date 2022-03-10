package cs455.scaling;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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

	// Copy of the selector used in KeySelector (//TODO: Determine if this is necessary as a class variable)
	public static Selector selector;

	// Data structure for containing all the threads
	private HashSet<WorkerThread> allWorkerThreads = new HashSet<>();

	// Data structure for queue of pending tasks
	protected static LinkedBlockingDeque<SelectionKey> pendingTasks; // This is one of the 7 different types of implementations of the BlockingQueue interface, (this is likely? the one we want)


	public ThreadPoolManager(int portnum, int numThreads, int batchSize, int batchTime){
		this.numThreads = numThreads;
		this.portnum = portnum;
		this.batchSize = batchSize;
		this.batchTime = batchTime;

		this.pendingTasks = new LinkedBlockingDeque<>(batchSize);
		//TODO: Set time limit in between tasks using batchTime (in seconds).

		PrintStatsThread pst = new PrintStatsThread(this);
		pst.start();

		try{
			this.selector = Selector.open();
		}
		catch (IOException e){
			e.printStackTrace();
		}
		
		// This creates and initializes all the workerThreads 
		for (int i = 0; i < numThreads; i++){
			WorkerThread nextWorker = new WorkerThread(i, this.selector);
			nextWorker.start();
		 	allWorkerThreads.add(nextWorker);
		}


		
		KeySelector ks = new KeySelector(this, selector, portnum);
		ks.start();
	}

	public synchronized LinkedBlockingDeque<SelectionKey> getTaskList(){
		return pendingTasks;
	} 

	public void addTask(SelectionKey key){
		boolean keyGotInserted = false;
		while (keyGotInserted == false){
			if (pendingTasks.offerLast(key) == true){
				keyGotInserted = true;
			}
		}
	}

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

	public void checkForNewKeys(){


		while (true){
			// Use this for debugging the pendingTasks queue if keys in it are not being taken by the workerThreads for some reason.
			// try{
			// 	// Thread.sleep(3000);
			// 	System.out.println("Size of pendingTasks is: " + pendingTasks.size() + ", waiting for size to reach: " + batchSize);
			// }
			// catch (InterruptedException e){
			// 	e.printStackTrace();
			// }
			int i = 0;
			if (this.pendingTasks.size() == batchSize){
				while (this.pendingTasks.size() != 0){ // Start assigning keys to threads until it's empty
					for(SelectionKey key : pendingTasks){
						WorkerThread nextWorker = getNextAvailableWorker(); // Get the next available worker, this is a blocking call that waits until one is available
						if (nextWorker != null){
							// System.out.println("Worker #" + nextWorker.workerID + " is available");
							nextWorker.setNextKey(key);
							nextWorker.setWorkingStatus();
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
