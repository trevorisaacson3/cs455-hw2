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

public class WorkerThread extends Thread{

    private boolean isAvailable = false;
    private static ThreadPoolManager tpm;
    private SelectionKey nextKey = null;
    public int workerID = -1;
    private int keyNumber = -1;

    public WorkerThread(int workerID, ThreadPoolManager tpm){
        this.workerID = workerID;
        this.tpm = tpm;
    };

    public synchronized boolean isAvailable(){
        return this.isAvailable;
    }

    public synchronized void setAvailability(boolean inputAvailability){
        isAvailable = inputAvailability;
    }

    public synchronized void notifyWorker(int keyNumber){
            // System.out.println("Worker # " + workerID + " is working on key #" + keyNumber);
            this.keyNumber = keyNumber;
            notify();
    }

    private synchronized void waitForNewTask() {
        setAvailability(true);    
        this.isAvailable = true; // Worker is available and waiting
                try{
                    wait();
                    setAvailability(false);
                    // System.out.println("Worker # " + workerID + " is now proceeding to work");
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
    }

    public void setNextKey(SelectionKey nextKey){
        this.nextKey = nextKey;
    }

    private void completeNextTask(){
        // System.out.println("Worker # " + workerID + "\t Starting Task");
        try{
	        if (nextKey.isAcceptable()) {
                // System.out.println("Worker # " + workerID + "\t Found register key");
                KeySelector ks = (KeySelector) nextKey.attachment();
                final ServerSocketChannel ssc = ks.serverSocketChannel;
                boolean registerSuccess = registerKey(nextKey, ssc);
                if (registerSuccess) {
                    ks.incrementNumRegisteredKeys();
                }
                // ks.register();
            }
    
            else if (nextKey.isReadable() | nextKey.isWritable()) {
                // System.out.println("Worker # " + workerID + "\t Found read/writable key");
                this.readAndRespond(nextKey, workerID);
            }
            else {
                // System.out.println("Worker # " + workerID + "\t FOUND UNKNOWN TYPE OF KEY");
                // System.out.println("\t###############################################");


            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        // System.out.println("Worker # " + workerID + "\t Finished task.");
    }


	public boolean registerKey(SelectionKey key, ServerSocketChannel ssc) throws IOException {
		// System.out.println("Trying to register a key");
        SocketChannel client = null;
        int whileCounter = 0;
        client = ssc.accept();
        if (client == null){
        System.out.println("Worker # " + workerID + " is waiting on key #" + keyNumber);
        System.out.println("Worker # " + workerID + " is waiting on key #" + keyNumber);
        System.out.println("Worker # " + workerID + " is waiting on key #" + keyNumber);
        System.out.println("Worker # " + workerID + " is waiting on key #" + keyNumber);
            return false;
        }
        // while (client == null){
		// 	client = ssc.accept();
        //     if (whileCounter == 40){
        //         return;
        //     }
        //     try{
        //         Thread.sleep(100);
        //         System.out.println("Worker # " + workerID + " is waiting on key #" + keyNumber);
        //         ++whileCounter;
        //     }
        //     catch(InterruptedException e){

        //     }
			// System.out.println("\t\tclient waiting to register");
            // System.exit(1)
		// }
		// SocketChannel client = ssc.accept();
		client.configureBlocking(false);
        Selector keySelector = key.selector();
		client.register(keySelector, SelectionKey.OP_READ);
		tpm.incrementNodesConnected();
        return true;
		// System.out.println("\t\tNew client registered using WT selector");
	}


	public void readAndRespond(SelectionKey key, int workerID) throws IOException {
			ByteBuffer readBuffer = ByteBuffer.allocate(Constants.KB * 8);
			SocketChannel client = (SocketChannel) key.channel();
			int bytesRead = client.read(readBuffer);
            while(bytesRead != 0){
			    if (bytesRead == -1){
                    tpm.decrementNodesConnected();
				    client.close();
				    // System.out.println("Client has disconnected **FROM WORKERTHREAD #" + workerID);
			    }
			    else {            
				    byte[] receivedByteArray = readBuffer.array();
				    HashMessage receivedHashMessage = new HashMessage(receivedByteArray);
				    // System.out.println("WorkerThread # " + workerID  + "\treceived: " + receivedHashMessage.bytesToString(receivedByteArray).substring(0,5) + " size: " + receivedByteArray.length + " <- (8192) (bytes)?");
                    boolean allZeros = true;
                    for (byte b : receivedByteArray){ //Check to make sure buffer is not just zeros
                        if (b != 0){
                            allZeros = false;
                        }
                    }
                    if (allZeros){
                        return; //Ignore hashing and responding to an empty byte stream
                    }

                    tpm.incrementTotalReceived();
    				String hashedMessageString = receivedHashMessage.getHashedString();
    				readBuffer.clear();
    				ByteBuffer writeBuffer = ByteBuffer.allocate(Constants.KB * 8);
    				writeBuffer = ByteBuffer.wrap(hashedMessageString.getBytes());
                    int sentSize = writeBuffer.array().length;
    				// System.out.println("WorkerThread # " + workerID  + "\tsent back: " + hashedMessageString.substring(0,5) + " size: " + (sentSize) + " <- (40) (chars)?");
    				client.write(writeBuffer);
    				writeBuffer.clear();
                    tpm.incrementTotalSent();
                    client.register(key.selector(), SelectionKey.OP_READ);
			        bytesRead = client.read(readBuffer);
    		    }
    		}
    }


    @Override
    public void run() {

        while (true) {
            // Wait for new task
            waitForNewTask();
            // Do task
            completeNextTask();
            // Set key as null to prevent accidentally working on the same key twice; 
            this.nextKey = null;
            this.keyNumber = -1;
        }
    }
}
