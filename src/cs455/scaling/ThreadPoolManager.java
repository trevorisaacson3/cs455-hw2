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
import java.util.HashMap;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolManager extends Thread{

	private AtomicInteger totalMessagesSent = new AtomicInteger(0);
	private AtomicInteger totalMessagesReceived = new AtomicInteger(0);


	// Number of threads to initialize and keep alive for the duration of the program
	private final int numThreads;

	// Number of threads to initialize and keep alive for the duration of the program
	private AtomicInteger totalNumConnections = new AtomicInteger(0);

	// Port Number the Server is listening on
	private final int portnum;

	// Maximum size of the list of pending tasks for workerThreads to handle
	private final int batchSize;
	
	// Maximum time duration that tasks should be sitting in the pendingTasks queue before being assigned to workerThreads 
	private final int batchTime;

	// Data structure for containing all the threads
	private HashSet<WorkerThread> allWorkerThreads = new HashSet<>();

	// Data structure for queue of pending tasks
	private static LinkedBlockingQueue<Task> pendingTasks = new LinkedBlockingQueue<Task>(); // This is one of the 7 different types of implementations of the BlockingQueue interface, (this is likely? the one we want)

	// Class for managing time between last batch was dispersed to workerThreads
	private BatchTimer batchTimer;

	//Used for debugging purposes to keep track of the number of tasks generated variables (DELETE WHEN NO LONGER NEEDED)
	int totalNumTasks = 0;

	private static HashMap<SelectionKey,Integer> recentThroughPuts = new HashMap<>(150);

	public ThreadPoolManager(int portnum, int numThreads, int batchSize, int batchTime){
		this.numThreads = numThreads;
		this.portnum = portnum;
		this.batchSize = batchSize;
		this.batchTime = batchTime;
		pendingTasks = new LinkedBlockingQueue<Task>(batchSize);

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

	public LinkedBlockingQueue<Task> getTaskList(){
		synchronized (this){
			return pendingTasks;
		}
	} 

	public boolean addTask(SelectionKey key, Tasktype tType){
		if (key.interestOps() == SelectionKey.OP_READ){
			key.interestOps(SelectionKey.OP_WRITE); //This prevents us from adding a key to the batch twice in a row before it's been processed by the workerThread and it's interestOps are changed back to OP_READ
		}
		Task nextTask = new Task(tType, key, this);
		synchronized(this){
			if (getPendingTasks().contains(nextTask)){
				return false;
			}
		}
		boolean addSuccessful = false;
		while (addSuccessful == false){
			addSuccessful = pendingTasks.offer(nextTask);
		}
		return true;
	}

	public int getTotalSent(){
		return totalMessagesSent.get();
	}

	public void resetTotalSent(){
		totalMessagesSent.set(0);
	}
	
	public int getTotalMessagesReceived(){
		return totalMessagesReceived.get();
	}

	public void decrementNodesConnected(){
		totalNumConnections.decrementAndGet();
	}	

	public void incrementNodesConnected(){
		totalNumConnections.incrementAndGet();
	}

	public int getNumNodesConnected(){
		return totalNumConnections.get();
	}

	public void incrementTotalReceived(){
		totalMessagesReceived.incrementAndGet();
	}
	
	public synchronized Object[] getRecentThroughputs(){
		Collection <Integer> currentThroughputs = this.recentThroughPuts.values();
		return currentThroughputs.toArray();
	}

	public synchronized void clearRecentThroughputs(){
		recentThroughPuts.clear();
	}

	public synchronized void incrementTotalSent(SelectionKey key){
		int currentReceived = 0;
		try{
			currentReceived = recentThroughPuts.get(key);
		}
		catch (NullPointerException e){
		}
		recentThroughPuts.put(key, currentReceived + 1);
		totalMessagesSent.incrementAndGet();
	}

	public LinkedBlockingQueue<Task> getPendingTasks(){
		synchronized (this){
			return pendingTasks;
		}
	}

	public Task getNextTask(){
		synchronized(this){
			return pendingTasks.poll();
		}
	}

	public void checkForNewKeys(){
		while (true){
			int batchLoad = getPendingTasks().size();
			boolean batchReady = batchTimer.getBatchReadyStatus();
			if ((batchLoad == batchSize || batchReady == true ) && batchLoad != 0){
				Task nextTask = getNextTask();
				WorkerThread nextWorker = getNextAvailableWorker(); // Get the next available worker, this is a blocking call that waits until one is available
				if (nextWorker != null){
					++totalNumTasks;
					nextWorker.setNextTask(nextTask);
					nextWorker.notifyWorker();
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
