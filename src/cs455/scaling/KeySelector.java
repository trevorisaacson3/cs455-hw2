// The purpose of this class is to house the selector object itself and to keep reading for new keys to either register into the selector system or add to the queue for other workerThreads to handle
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

public class KeySelector extends Thread{

    private ThreadPoolManager tpm;
    private int portnum = -1;
	public static ServerSocketChannel serverSocket;
	public Selector selector;

    public KeySelector(ThreadPoolManager tpm, int portnum){
        this.tpm = tpm;
		this.portnum = portnum;
    }

	@Override
	public void run() {
		readKeys();
	}

	private synchronized void readKeys(){
		try {

			selector = Selector.open();
			serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress(portnum));
			serverSocket.configureBlocking(false);

			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

			HashSet<SelectionKey> registeredKeys = new HashSet<SelectionKey>();
			while (true) {
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					// System.out.println("KeyOPS is" + key.interestOps());
					
					if (key.isValid() == false) {
						continue;
					}

					else if (key.isAcceptable() && !(registeredKeys.contains(key))) {
						registeredKeys.add(key);
						// System.out.println("NEW REGISTER KEY");
						key.attach(this);
						tpm.addTask(key);
						try{
							wait();
						}
						catch(InterruptedException e){
							e.printStackTrace();
						}
					}

					else if (key.isReadable()) {
						// System.out.println("NEW RESPOND KEY, Iteration #" + numIters);
						//Add read-write task to pendingTasks in threadPoolManager so that the threadPools can handle those
						tpm.addTask(key);
						key.channel().register(selector, SelectionKey.OP_WRITE);
					}

					// Remove from the set when done
					iter.remove();
				}
			}
		}
		catch (IOException e){
			e.printStackTrace();
		}

	}


	public synchronized void register() throws IOException {
		System.out.println("Trying to register a key");
		SocketChannel client = serverSocket.accept();
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);
		this.tpm.incrementNodesConnected();
		System.out.println("\t\tNew client registered using selector");
		notify();
	}

}
