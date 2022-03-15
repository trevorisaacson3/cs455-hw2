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
				System.out.println("Waiting for a received message");
            	clientChannel.read(readBuffer);
				System.out.println("\t\t** Received a message back!");
				client.incrementTotalReceived();
				byte[] response = readBuffer.array();
				String responseString = new String(response);
				responseString = responseString.substring(0,40); // Trim excess padded zeros off of string
				boolean verified = false;
                LinkedList<String> unverifiedHashes = client.getUnverifiedHashes();
				if (unverifiedHashes.contains(responseString)) {
					verified = true;
					unverifiedHashes.remove(responseString);
				}
				System.out.println("Response from server: " + responseString.substring(0,5));
				System.out.println("Response valid?: " + verified);
				// System.out.println("Length expected: " + hashedMessageString.length() + " actual: " + responseString.length());

				readBuffer.clear();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    
}
