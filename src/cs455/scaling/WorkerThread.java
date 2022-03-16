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
            this.keyNumber = keyNumber;
            notify();
    }

    private synchronized void waitForNewTask() {
        setAvailability(true);    
        this.isAvailable = true; // Worker is available and waiting
                try{
                    wait();
                    setAvailability(false);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
    }

    public void setNextKey(SelectionKey nextKey){
        this.nextKey = nextKey;
    }

    private void completeNextTask(){
        try{
            if (nextKey != null && nextKey.isValid()){
                if (nextKey.isAcceptable()) {
                    KeySelector ks = (KeySelector) nextKey.attachment();
                    final ServerSocketChannel ssc = ks.serverSocketChannel;
                    boolean registerSuccess = registerKey(nextKey, ssc);
                    if (registerSuccess) {
                        ks.incrementNumRegisteredKeys();
                    }
                }
        
                else if (nextKey.isReadable() | nextKey.isWritable()) {
                    this.readAndRespond(nextKey, workerID);
                } 
            }
            else{
                return;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


	public boolean registerKey(SelectionKey key, ServerSocketChannel ssc) throws IOException {
        SocketChannel client = null;
        int whileCounter = 0;
        client = ssc.accept();
        if (client == null){
            return false;
        }
		client.configureBlocking(false);
        Selector keySelector = key.selector();
		client.register(keySelector, SelectionKey.OP_READ);
        key.selector().wakeup();
		tpm.incrementNodesConnected();
        return true;
	}


	public void readAndRespond(SelectionKey key, int workerID) throws IOException {
			ByteBuffer readBuffer = ByteBuffer.allocate(Constants.KB * 8);
			SocketChannel client = (SocketChannel) key.channel();
			int bytesRead = client.read(readBuffer);
            while(bytesRead != 0){
			    if (bytesRead == -1){
                    tpm.decrementNodesConnected();
				    client.close();
                    return;
			    }
			    else {            
				    byte[] receivedByteArray = readBuffer.array();
				    HashMessage receivedHashMessage = new HashMessage(receivedByteArray);
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
                    byte[] messageBytes = hashedMessageString.getBytes();
    				ByteBuffer writeBuffer = ByteBuffer.allocate(8 * Constants.KB);
                    String responseLength = messageBytes.length + "";
                    byte lChar_one = (byte) responseLength.charAt(0);
                    byte lChar_two = (byte) responseLength.charAt(1);
                    byte [] lengthArray = {lChar_one,lChar_two};
                    byte[] responseBytes =new byte[2 + messageBytes.length];
                    System.arraycopy(lengthArray, 0, responseBytes, 0, 2);
                    System.arraycopy(messageBytes, 0, responseBytes, 2, messageBytes.length);

    				writeBuffer = ByteBuffer.wrap(responseBytes);
                    int sentSize = writeBuffer.array().length;
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
