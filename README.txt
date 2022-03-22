# cs455 - Distributed Systems - Homework 2

# Team 4
## Team Members (3)
Alec Moran 
Griffin Zavadil 
Trevor Isaacson 

## Building and Running the program

To build the project, run the following command in the terminal from this project's main directory (the directory containing this README): 
```
    $ gradle build           
```

The client nodes can be started using the following command in the terminal:
```
    $ java-cp ./build/libs/HW2-1.0.jar cs455.scaling.Client {server-hostname} {server-port} {message-rate (seconds)}
```

The server node can be started using the following command in the terminal:
```
    $ java-cp ./build/libs/HW2-1.0.jar cs455.scaling.Server {server-port} {threadPool-size} {batch-size} {batch-time (seconds)}
```

## Files included in submission
- build.gradle
- README.txt
- src/cs455/scaling/AutomaticExit.java
- src/cs455/scaling/BatchTimer.java
- src/cs455/scaling/Client.java
- src/cs455/scaling/ClientReceiverThread.java
- src/cs455/scaling/Constants.java
- src/cs455/scaling/HashMessage.java
- src/cs455/scaling/KeySelector.java
- src/cs455/scaling/PrintStatsThread.java
- src/cs455/scaling/Server.java
- src/cs455/scaling/ThreadPoolManager.java
- src/cs455/scaling/WorkerThread.java


## Class Descriptions

### AutomaticExit.java
This class is used to automatically shut down the application, either the client or the server node, after a specified amount of time to prevent unhandled program instances from running forever on state capital machines. For debugging purposes this was set to reasonably short periods of time but for the final submission of this assignment the maximum run-time period for the program has been extended to 9999 minutes which is around 6.93 days.

### BatchTimer.java
This class is used to keep track of the batchTime and to periodically release the current batch if the time since it has last released has expired (using the batch-time specified in the program arguments).

### Client.java
This class is the primary class for client nodes and contains the necessary main method needed to initialize a client node in it's entirety. It is responsible for creating other necessary objects and it is also responsible for regularly sending messages to the server.

### ClientReceiverThread.java
This class is instantiated by the Client class for receiving and validating incoming messages from the server. This is a separate class and thread from the main Client class to allow for the sending and receiving of messages to be done simultaneously and independent of one another to keep the client throughput more efficient.

### Constants.java
This class is used for keeping track of constants used across the program, the only constant we ended up needing to use consistently was the conversion of bytes to KB (1024 bytes = 1 KB).

### HashMessage.java
This class is used for everything hash-related. It's primary use is to generate a new hashed message using the SHA1 algorithm, but it is also used convert the hashed String back into a byte array or to print out a hashed or unhashed byte array[] in a readable string format to compare for debugging purposes.

### KeySelector.java
This class is used to store the Selector that receives keys with activity on them, which then adds them to the batch of tasks to complete.

### PrintStatsThread.java
This class is used by both the client and the server to periodically print out their respective necessary data to the console.

### Server.java
This class is the primary class for the server node and contains the necessary main method needed to initialize the server node in it's entirety. It's purpose is to create and run the thread pool manager which then goes on to create every other necessary object instance needed for the server.

### ThreadPoolManager.java
This class is instantiated by Server.java and is the primary class controlling the server workflow. It maintains a KeySelector class (a class for the selector), as well as a thread pool of WorkerThread classes that are used to handle tasks. This class also manages the batch and keeps track of the total connected clients and message throughputs.

### WorkerThread.java
This class usually has 1-100 instantiations created by the ThreadPoolManager and it's primary purpose is to read tasks in the form of SelectionKeys and to either register that key or to read and respond to it through it's attached SocketChannel. WorkerThreads will run in an infinite loop of waiting for tasks, completing said task, then entering the waiting state again in a blocking call that can only be notified and released when the ThreadPoolManager has designated a new task (aka a new SelectionKey to it) and called .notify() on the WorkerThread to allow it to begin processing the next task.
