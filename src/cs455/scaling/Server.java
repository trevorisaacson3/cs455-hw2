package cs455.scaling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

	public static void main(String[] args) throws IOException {
	
		if (args.length == 4){
			AutomaticExit ae = new AutomaticExit(3);
			ae.start();
			// First argument is portnum
			int portnum = Integer.parseInt(args[0]);
			// Second argument is thread-pool-size
			int threadPoolSize = Integer.parseInt(args[1]);
			// Third argument is batch-size
			int batchSize = Integer.parseInt(args[2]);
			// Fourth argument is batch-time		
			int batchTime = Integer.parseInt(args[3]);
			System.out.println("Starting server w/ portnum: " + portnum + ", threadPoolSize: " + threadPoolSize + ", batchSize: " + batchSize + ", batchTime: " + batchTime + " seconds.");
			ThreadPoolManager tpm = new ThreadPoolManager(portnum, threadPoolSize, batchSize, batchTime);
			tpm.checkForNewKeys();



		}

		else{
			System.out.println("No program arguments specified, please specify: (1) portnum, (2) threadPoolSize, (3) batchSize, (4) batchTime (in seconds).");
		}
	}

}
