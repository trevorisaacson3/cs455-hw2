package cs455.scaling;
import java.lang.Object.*;
import java.text.DecimalFormat;
import java.lang.Thread;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Vector;

public class PrintStatsThread extends Thread{

    Client client;
    ThreadPoolManager tpm;
    private enum type {TPM, CLIENT};
    private type nodeType;
    private Instant startTime;
    private Vector<Double> throughPutAverages = new Vector<Double>();


    public PrintStatsThread(Client client){
        this.client = client;
        this.nodeType = type.CLIENT;
    }

    public PrintStatsThread(ThreadPoolManager tpm){
        this.tpm = tpm;
        this.nodeType = type.TPM;
        this.startTime = Instant.now();
    }

    private double getStandardDev(Object[] inputArray){
        double arraySum = 0.0, stdDev = 0.0;

        for(Object nextNum : inputArray) {
            arraySum += (double) nextNum;
        }
       
        double arrayLength = (double) inputArray.length;
        // System.out.println("Array is this long: " + arrayLength);
        double arrayMean = arraySum/arrayLength;
        // System.out.println("Array has mean: " + arrayMean);

        for(Object nextNum: inputArray) {
            stdDev += Math.pow((double)nextNum - arrayMean, 2);
        }
        // System.out.println("stdDev is : " + stdDev);
        // double insideParams = stdDev/arrayLength;
        // System.out.println("stdDev is : " + insideParams);

        return Math.sqrt(stdDev/arrayLength);
    }

    @Override
    public void run() {

        if (nodeType == type.CLIENT) {
            try {
//                Date date = new Date();
                while (true) {
                    //	long currentTime = date.getTime();
                    LocalDateTime currentTime = LocalDateTime.now();
//                int totalSent = client.getTotalSent();
                    System.out.println("[" + currentTime + "] Total Sent Count: " + client.getTotalSent() + ", Total Received Count: " + client.getTotalReceived());
                    Thread.sleep(20000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (nodeType == type.TPM) {
            try {
//                Date date = new Date();
                while (true) {
                    //	long currentTime = date.getTime();
                    LocalDateTime currentTime = LocalDateTime.now();
                    Instant endTime = Instant.now();
                    long preciseDuration = (Duration.between(startTime, endTime).toMillis()) / 1000; // converting per millisecond unit rate to per second rate (1000ms = 1s) 
                    preciseDuration = preciseDuration == 0 ? 1 : preciseDuration; // Prevent divide by zero error if the program just started and the timer hasn't started yet
                    double averageSent = (double)(tpm.getTotalSent()) / (double) preciseDuration;
                    // System.out.println("(long)(tpm.getTotalSent()) " + (long)(tpm.getTotalSent()));
                    long numNodesConnected = (long) tpm.getNumNodesConnected() == 0 ? 1 : (long) tpm.getNumNodesConnected(); // Prevent divide by zero error if the program just started and no nodes have connected yet
                    double meanPerClientTP = averageSent / (double) numNodesConnected;
                    throughPutAverages.add(meanPerClientTP);
                    Object[] tpaArray = throughPutAverages.toArray();
                    // avgStats.accept(meanPerClientTP);
                    double stdDev = getStandardDev(tpaArray);
                    DecimalFormat df = new DecimalFormat ("#.000");
                    df.format(stdDev);
                    df.format(meanPerClientTP);
                    // System.out.println("Duration is " + preciseDuration);
                    System.out.println("[" + currentTime + "] Server Throughput: " + averageSent + " messages/s, Active Client Connections: " + tpm.getNumNodesConnected() + ", Mean Per-client Throughput: " + meanPerClientTP + " messages/s, Std. Dev. Of Per-client Throughput: " + stdDev + " messages/s");
                    Thread.sleep(20000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
