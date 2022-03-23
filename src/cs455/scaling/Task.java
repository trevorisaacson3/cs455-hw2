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

public class Task {
    public Tasktype taskType;
    private SelectionKey nextKey;
    private ThreadPoolManager tpm;

    Task(Tasktype taskType, SelectionKey nextKey, ThreadPoolManager tpm){
        this.taskType = taskType;
        this.nextKey = nextKey;
        this.tpm = tpm;
    }

    public void completeTask(){
        try{
            if (nextKey != null && nextKey.isValid()){
                if (nextKey.isAcceptable()) {
                    if (nextKey.attachment().getClass() != KeySelector.class){
                        return;
                    }
                    KeySelector ks = (KeySelector) nextKey.attachment();
                    final ServerSocketChannel ssc = ks.serverSocketChannel;
                    boolean registerSuccess = registerClient();
                    if (registerSuccess) {
                        ks.incrementNumRegisteredKeys();
                    }
                    return;
                }
        
                else if (nextKey.interestOps() == SelectionKey.OP_WRITE){
                    nextKey.interestOps(SelectionKey.OP_READ);
                    this.readAndRespond();
                    return;
                } 
                else { 
                    nextKey.interestOps(SelectionKey.OP_READ);
                    return;
                }
            }
            else{
                return;
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

        // if (taskType == Tasktype.REGISTER){
        //     registerClient();
        // }
        // if (taskType == Tasktype.READ_AND_RESPOND){
        //     readAndRespond();
        // }
    }


    private boolean registerClient() throws IOException{
        SocketChannel client = null;
        int whileCounter = 0;
        KeySelector ks = (KeySelector) nextKey.attachment();
        ServerSocketChannel ssc = ks.serverSocketChannel; 
        client = ssc.accept();
        if (client == null){
            return false;
        }
		client.configureBlocking(false);
        Selector keySelector = nextKey.selector();
		client.register(keySelector, SelectionKey.OP_READ);
        client.finishConnect();
		tpm.incrementNodesConnected();
        return true;


    }

    private void readAndRespond() throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(Constants.KB * 8);
			SocketChannel client = (SocketChannel) nextKey.channel();
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
    				String hashedMessageString = receivedHashMessage.getHashedString();
                    tpm.incrementTotalReceived();
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
                    tpm.incrementTotalSent(nextKey);
                    client.register(nextKey.selector(), SelectionKey.OP_READ);
			        bytesRead = client.read(readBuffer);
    		    }
    		}
            readBuffer.clear();


    }
}
