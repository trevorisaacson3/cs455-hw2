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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.sql.Timestamp;
import java.util.Date;
import java.time.Instant;
import java.lang.Thread;
import java.time.LocalDateTime;
import java.math.BigInteger;


public class ClientReceiverThread extends Thread{

	private static ByteBuffer readBuffer = ByteBuffer.allocate(Constants.KB * 8);
    private static Client client;
	private static SocketChannel clientChannel;

    public ClientReceiverThread (SocketChannel clientChannel, Client client){
        this.clientChannel = clientChannel;
        this.client = client;
    }



    @Override
    public void run(){
        while(true){
            try{
            	clientChannel.read(readBuffer);
				byte[] response = readBuffer.array();
				String responseString = new String(response);
				int iteratorCount = 0;
				int numMessagesInBuffer = (int) ((double) responseString.length() / 39.00);
				// System.out.println("num messages in buffer is about: " + numMessagesInBuffer);
				int messageLength = -1;
				try {
					messageLength = Integer.parseInt(responseString.substring(iteratorCount,2));
				}
				catch (NumberFormatException e){
					continue; // If it cannot read the first two numbers of the next part of the buffer, do not parse and verify the rest of hte buffer 
				}
				for (int i = 0; i < numMessagesInBuffer; i+=messageLength){

				messageLength = Integer.parseInt(responseString.substring(iteratorCount,2));
				String messageString = responseString.substring(iteratorCount+2,2+messageLength); // Trim excess padded zeros off of string
				boolean verified = false;
                LinkedList<String> unverifiedHashes = client.getUnverifiedHashes();

				String partOfRS = responseString.substring(2,12);

				if (unverifiedHashes.contains(messageString)) {
					verified = true;
					unverifiedHashes.remove(messageString);
					client.incrementTotalReceived();
				}
				else {
					// System.out.println("\tReceived an unverified string!");
					// System.out.println("\tUnverified string: " + messageString + " length: " + messageString.length());
					// System.out.println("\tSize of list of hashes: " + client.getUnverifiedHashes().size());
					// for (String hashString: client.getUnverifiedHashes()){
						// String partOfNH = hashString.substring(0,10);
						// if (partOfRS == partOfNH){
							// System.out.println("Found a match, size in response: " + responseString.length() + ", size in hash list: "+ hashString.length());
						// }
					// }

				}
			}
				readBuffer.clear();
            }
            catch (IOException e){
				System.out.println("Server has finished sending messages.");
				System.exit(0);
            }
        }
    }
    
}
