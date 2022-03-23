package cs455.scaling;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Queue;

public class WorkerThread extends Thread{

    private boolean isAvailable = false;
    private static ThreadPoolManager tpm;
    private Task nextTask = null;
    public int workerID = -1;

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

    public synchronized void notifyWorker(){
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

    public void setNextTask(Task nextTask){
        this.nextTask = nextTask;
    }

    private void completeNextTask(){
        nextTask.completeTask();
    }

    @Override
    public void run() {

        while (true) {
            // Wait for new task
            waitForNewTask();
            // Do task
            completeNextTask();
            // Set key as null to prevent accidentally working on the same key twice; 
            this.nextTask = null;
        }
    }
}
