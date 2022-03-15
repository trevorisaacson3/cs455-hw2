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

import cs455.scaling.Server;

public class WorkerThread extends Thread{

    private boolean isAvailable = false;
    private static ThreadPoolManager tpm;
    private SelectionKey nextKey = null;
    public int workerID = -1;

    public WorkerThread(int workerID, ThreadPoolManager tpm){
        this.workerID = workerID;
        this.tpm = tpm;
    };

    public boolean isAvailable(){
        return this.isAvailable;
    }

    public synchronized void notifyWorker(int keyNumber){
            System.out.println("Worker # " + workerID + " is working on key #" + keyNumber);
            notify();
    }

    private synchronized void waitForNewTask() {
            this.isAvailable = true; // Worker is available and waiting
                try{
                    wait();
                    this.isAvailable = false;
                    System.out.println("Worker # " + workerID + " is now proceeding to work");
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
    }

    public void setNextKey(SelectionKey nextKey){
        this.nextKey = nextKey;
    }

    private void completeNextTask(){
        System.out.println("Worker # " + workerID + "\t Starting Task");
        try{
	        if (nextKey.isAcceptable()) {
                System.out.println("Worker # " + workerID + "\t Found register key");
                KeySelector ks = (KeySelector) nextKey.attachment();
                final ServerSocketChannel ssc = (ServerSocketChannel) ks.serverSocket;
                registerKey(nextKey, ssc);
            }
    
            else if (nextKey.isReadable() | nextKey.isWritable()) {
                System.out.println("Worker # " + workerID + "\t Found read/writable key");
                tpm.incrementTotalReceived();
                this.readAndRespond(nextKey, workerID);
                tpm.incrementTotalSent();
            }
            else {
                System.out.println("Worker # " + workerID + "\t FOUND UNKNOWN TYPE OF KEY");
                System.out.println("\t###############################################");


            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Worker # " + workerID + "\t Finished task.");
    }


	public static void registerKey(SelectionKey key, ServerSocketChannel ssc) throws IOException {
		System.out.println("Trying to register a key");
		SocketChannel client = ssc.accept();
		client.configureBlocking(false);
        Selector keySelector = key.selector();
		client.register(keySelector, SelectionKey.OP_READ);
		tpm.incrementNodesConnected();
		System.out.println("\t\tNew client registered using WT selector");
	}


	public void readAndRespond(SelectionKey key, int workerID) throws IOException {
			ByteBuffer readBuffer = ByteBuffer.allocate(Constants.KB * 8);
			SocketChannel client = (SocketChannel) key.channel();
			int bytesRead = client.read(readBuffer);

			if (bytesRead == -1){
                tpm.decrementNodesConnected();
				client.close();
				System.out.println("Client has disconnected **FROM WORKERTHREAD #" + workerID);
			}
			else {
				byte[] receivedByteArray = readBuffer.array();
				HashMessage receivedHashMessage = new HashMessage(receivedByteArray);
				System.out.println("WorkerThread # " + workerID  + "\treceived: " + receivedHashMessage.bytesToString(receivedByteArray).substring(0,5) + " size: " + receivedByteArray.length + " <- (8192) (bytes)?");
                boolean allZeros = true;
                for (byte b : receivedByteArray){ //Check to make sure buffer is not just zeros
                    if (b != 0){
                        allZeros = false;
                    }
                }
                if (allZeros){
                    return; //Ignore hashing and responding to an empty byte stream
                }

				String hashedMessageString = receivedHashMessage.getHashedString();
				readBuffer.clear();
				ByteBuffer writeBuffer = ByteBuffer.allocate(Constants.KB * 8);
				writeBuffer = ByteBuffer.wrap(hashedMessageString.getBytes());
                int sentSize = writeBuffer.array().length;
				System.out.println("WorkerThread # " + workerID  + "\tsent back: " + hashedMessageString.substring(0,5) + " size: " + (sentSize) + " <- (40) (chars)?");
				client.write(writeBuffer);
				writeBuffer.clear();
                client.register(key.selector(), SelectionKey.OP_READ);
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
        }
    }
}
