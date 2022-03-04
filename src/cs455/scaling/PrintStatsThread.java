package cs455.scaling;

import java.lang.Thread;
import java.time.LocalDateTime;
import java.util.Date;

public class PrintStatsThread extends Thread{

    Client client;
    ThreadPoolManager tpm;
    private enum type {TPM, CLIENT};
    private type nodeType;

    public PrintStatsThread(Client client){
        this.client = client;
        this.nodeType = type.CLIENT;
    }

    public PrintStatsThread(ThreadPoolManager tpm){
        this.tpm = tpm;
        this.nodeType = type.TPM;
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
//                int totalSent = client.getTotalSent();
                    System.out.println("[" + currentTime + "] Server Throughput: " + tpm.getTotalSent() + " messages/s, Active Client Connections: " + tpm.getNumNodesConnected() + ", Mean Per-client Throughput: " + tpm.getMeanClientThroughput() + " messages/s, Std. Dev. Of Per-client Throughput: " + tpm.getStdDevThroughput() + " messages/s");
                    Thread.sleep(20000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
