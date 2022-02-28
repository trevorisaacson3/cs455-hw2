# cs455 - Distributed Systems - Homework 2
## Scalable Server Design: Using Thread Pools & Micro Batching to Manage and Load Active Network Connections

> As part of this assignment you will be developing a server to handle network traffic by designing and building your own thread pool. This thread pool will have a configurable number of threads that will be used to perform tasks relating to network communications. Specifically, you will use this thread pool to manage all tasks relating to network communications. This includes:    
> (1) Managing incoming network connections
> (2) Receiving data over these network connections
> (3) Organizing data into batches to improve performance
> (4) Sending data over any of these links

> Unlike the previous assignment where we had a receiver thread associated with each socket, we will be managing a collection of connections using a fixed thread pool. A typical set up for this assignment involves a server with a thread pool size of 10 and 100 active clients that send data over their connections. Please note that you must use Java NIO for this assignment.


## Class Descriptions

### Server



### Clients




