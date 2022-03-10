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
	public static Selector selector;
	public static ServerSocketChannel serverSocket;

    public KeySelector(ThreadPoolManager tpm, Selector selector, int portnum){
        this.tpm = tpm;
		this.selector = selector;
		this.portnum = portnum;
    }

	// This was used for registering from the workerThread, but registering from the workerThread doesn't currently work
	// public ServerSocketChannel getServerSocketChannel(){
	// 	return this.serverSocket;
	// }

	@Override
	public void run() {
		try {
			// Selector selector = Selector.open(); // The selector is opened from ThreadPoolManager instead.
			serverSocket = ServerSocketChannel.open();
			serverSocket.bind(new InetSocketAddress(portnum));
			serverSocket.configureBlocking(false);

			serverSocket.register(selector, SelectionKey.OP_ACCEPT);

			int numKeysRead = 0;

			while (true) {
				// System.out.println("Listening for new connections or messages");
				selector.select();
				// System.out.println("\tActivity on selector!");
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();

				while (iter.hasNext()) {
					// System.out.println("Number of keys is " + selectedKeys.size());
					// System.out.println("Number of keys read so far is " + ++numKeysRead);
					SelectionKey key = iter.next();
					
					if (key.isValid() == false) {
						continue;
					}

					// Open new connection on serverSocket
					if (key.isAcceptable()) {
						// Registering is currently handled by the selector itself, not sure if the workerThreads are supposed to handle registering, but workerNodes handling registering does not currently work
						register();
					}

					// Read data from previous connection
					if (key.isReadable()) {
						//Add read-write task to pendingTasks in threadPoolManager so that the threadPools can handle those
						tpm.addTask(key);
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

	public synchronized static void register() throws IOException {
		SocketChannel client = serverSocket.accept();
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);
		System.out.println("\t\tNew client registered using selector");
	}

}
