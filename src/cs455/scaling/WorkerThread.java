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
    private ThreadPoolManager tpm;
    private SelectionKey nextKey = null;
    public int workerID = -1;

    public WorkerThread(int workerID, ThreadPoolManager tpm){
        this.workerID = workerID;
        this.tpm = tpm;
    };

    public boolean isAvailable(){
        return this.isAvailable;
    }

    public void setWorkingStatus(){
        isAvailable = false;
    }

    public synchronized void notifyWorker(int keyNumber){
            System.out.println("Worker # " + workerID + " is working on key #" + keyNumber);
            notify();
    }

    private synchronized void waitForNewTask() {
            this.isAvailable = true; // Worker is available and waiting
                try{
                    wait();
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
                ks.register();
            }
    
            else if (nextKey.isWritable()) {
                System.out.println("Worker # " + workerID + "\t Found writable key");
                tpm.incrementTotalReceived();
                this.readAndRespond(nextKey, workerID);
                tpm.incrementTotalSent();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Worker # " + workerID + "\t Finished task.");
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
				System.out.println("WorkerThread # " + workerID  + "\treceived: " + receivedHashMessage.bytesToString(receivedByteArray).substring(0,5) + " size: " + receivedByteArray.length + "<- (8192)?");

				String hashedMessageString = receivedHashMessage.getHashedString();
				System.out.println("WorkerThread # " + workerID  + "\tsent back: " + hashedMessageString.substring(0,5) + " size: " + hashedMessageString.getBytes().length + " <- (40)?");
				readBuffer.clear();
				ByteBuffer writeBuffer = ByteBuffer.allocate(Constants.KB * 8);
				writeBuffer = ByteBuffer.wrap(hashedMessageString.getBytes());
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
