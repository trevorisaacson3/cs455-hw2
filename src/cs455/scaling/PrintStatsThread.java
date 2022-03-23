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
import java.util.LinkedList;
import java.util.Vector;

public class PrintStatsThread extends Thread{

    Client client;
    ThreadPoolManager tpm;
    private enum type {TPM, CLIENT};
    private type nodeType;
    private Instant startTime;
    private LinkedList<Double> throughPutAverages = new LinkedList<Double>();


    public PrintStatsThread(Client client){
        this.client = client;
        this.nodeType = type.CLIENT;
    }

    public PrintStatsThread(ThreadPoolManager tpm){
        this.tpm = tpm;
        this.nodeType = type.TPM;
        this.startTime = Instant.now();
    }

    private double[] ObjectArrayToDoubleArray(Object[] inputArray){
        double[] outputArray = new double[inputArray.length];
        for (int i = 0; i < inputArray.length; i++){
            int nextValue = (int) inputArray[i];
            outputArray[i] = Double.valueOf(nextValue);
        }
        return outputArray;
    }

    private double getStandardDev(double[] inputArray){
        double arraySum = 0.0, stdDev = 0.0;

        for(double nextNum : inputArray) {
            arraySum += (nextNum / 20.0); //Divide by 20 since we're taking the average over the past 20 seconds  
        }
       
        double arrayLength = inputArray.length;
        double arrayMean = arraySum/arrayLength;

        for(double nextNum: inputArray) {
            stdDev += Math.pow((nextNum / 20.0) - arrayMean, 2); //Divide by 20 since we're taking the average over the past 20 seconds   
        }
        return Math.sqrt(stdDev/arrayLength);
    }

    @Override
    public void run() {

        if (nodeType == type.CLIENT) {
            try {
                while (true) {
                    Thread.sleep(20000);
                    LocalDateTime currentTime = LocalDateTime.now();
                    System.out.println("[" + currentTime + "] Total Sent Count: " + client.getTotalSent() + ", Total Received Count: " + client.getTotalReceived());
                    client.resetTotalReceived();
                    client.resetTotalSent();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (nodeType == type.TPM) {
            try {
                while (true) {
                    this.startTime = Instant.now();
                    Thread.sleep(20000);
                    LocalDateTime currentTime = LocalDateTime.now();
                    Instant endTime = Instant.now();
                    double[] tpaArray = ObjectArrayToDoubleArray(tpm.getRecentThroughputs());
                    long preciseDuration = (Duration.between(startTime, endTime).toMillis()) / 1000; // converting per millisecond unit rate to per second rate (1000ms = 1s) 
                    preciseDuration = preciseDuration == 0 ? 1 : preciseDuration; // Prevent divide by zero error if the program just started and the timer hasn't started yet
                    double averageSent = (double)(tpm.getTotalSent()) / (double) preciseDuration;
                    long numNodesConnected = (long) tpm.getNumNodesConnected() == 0 ? 1 : (long) tpm.getNumNodesConnected(); // Prevent divide by zero error if the program just started and no nodes have connected yet
                    double meanPerClientTP = averageSent / (double) numNodesConnected;
                    double stdDev = 0.0;
                    stdDev = getStandardDev(tpaArray);
                    DecimalFormat df = new DecimalFormat ("#.###");
                    String stdDev_string, meanPerClientTP_string;
                    stdDev_string = df.format(stdDev);
                    meanPerClientTP_string = df.format(meanPerClientTP);
                    System.out.println("[" + currentTime + "] Server Throughput: " + averageSent + " messages/s, Active Client Connections: " + tpm.getNumNodesConnected() + ", Mean Per-client Throughput: " + meanPerClientTP_string + " messages/s, Std. Dev. Of Per-client Throughput: " + stdDev_string + " messages/s");
                    this.tpm.resetTotalSent();
                    this.tpm.clearRecentThroughputs();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
