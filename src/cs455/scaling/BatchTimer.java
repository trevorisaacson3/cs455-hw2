package cs455.scaling;

public class BatchTimer extends Thread{

    private int batchTime = 1;
    private boolean batchTimerStarted = false;
    private boolean batchReady = false;

    public BatchTimer(int batchTime){
        this.batchTime = batchTime;
    }

    public synchronized boolean getBatchReadyStatus(){
        return batchReady;
    }

    private synchronized void setBatchReady(boolean batchStatus){
        batchReady = batchStatus;
    }

    @Override
    public void run(){
        while (true){
            if (getBatchReadyStatus() == false){
                try{
                    Thread.sleep(1000*batchTime);
                }
                catch(InterruptedException e){
                    e.printStackTrace();
                }
                setBatchReady(true);
            }
        }
    }

}
