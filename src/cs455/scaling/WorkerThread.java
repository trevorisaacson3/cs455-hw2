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
    static Selector selector;
    public int workerID = -1;

    public WorkerThread(int workerID, Selector selector){
        this.workerID = workerID;
        this.selector = selector;
    };

    public boolean isAvailable(){
        return this.isAvailable;
    }

    public void setWorkingStatus(){
        isAvailable = false;
    }

    private synchronized void waitForNewTask() {
            this.isAvailable = true; // Worker is available and waiting
            boolean gotNextKey = false;
            while (gotNextKey == false){
                try{
                    Thread.sleep(3000);
                    // System.out.println("\t\t %%% Worker # " +workerID + " is waiting for a key to begin working, is it here yet? ->" + (nextKey == null));
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
                
                if (this.nextKey != null){
                    // System.out.println("\t\t $$$ WorkerThread # " + workerID + " received key, it is now working.");
                    gotNextKey = true;
                }
            }
    }

    public void setNextKey(SelectionKey nextKey){
        this.nextKey = nextKey;
    }

    private void completeNextTask(){

        try{
            // This block of code actually breaks the program, the selector in KeySelector.java apparently has to do registering or else the selector refuses to read beyond the first key it receives.
	        // if (nextKey.isAcceptable()) {
            //     KeySelector ks = (KeySelector) nextKey.attachment();
            //     ServerSocketChannel ss = ks.getServerSocketChannel();
            //     ks.register();
            // }
    
            if (nextKey.isReadable()) {
                this.readAndRespond(nextKey, workerID);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

	public static void readAndRespond(SelectionKey key, int workerID) throws IOException {
			ByteBuffer readBuffer = ByteBuffer.allocate(8 * Constants.KB);
			SocketChannel client = (SocketChannel) key.channel();
			int bytesRead = client.read(readBuffer);

			if (bytesRead == -1){
				client.close();
				System.out.println("Client has disconnected **FROM WORKERTHREAD #" + workerID);
			}
			else {
				byte[] receivedByteArray = readBuffer.array();
				HashMessage receivedHashMessage = new HashMessage(receivedByteArray);
				String hashedMessageString = receivedHashMessage.getHashedString();
				System.out.println("Message being sent **FROM WORKERTHREAD # " + workerID + " to client : " + hashedMessageString);
				readBuffer.clear();
				ByteBuffer writeBuffer = ByteBuffer.allocate(hashedMessageString.getBytes().length);
				writeBuffer = ByteBuffer.wrap(hashedMessageString.getBytes());
				client.write(writeBuffer);
				writeBuffer.clear();
			}
		}



    @Override
    public void run() {

        while (true) {
            // Wait for new task
            waitForNewTask();
            // Do task
            completeNextTask();
            // Set task as complete by marking this key as null; 
            this.nextKey = null;
        }
    }
}
