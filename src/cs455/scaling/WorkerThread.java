package cs455.scaling;

public class WorkerThread extends Thread{

    private boolean isAvailable = false;

    public WorkerThread(){};

    public boolean isAvailable(){
        return this.isAvailable;
    }

    private synchronized void waitForNewTask() {
        try{
            this.isAvailable = true; // Worker is available and waiting
            wait(); //TODO: Call notify() on this object to make it start executing
            this.isAvailable = false; //Worker is no longer available and no longer waiting
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    private void completeNextTask(){
        //TODO: Add functionality to make worker complete next task, do this by grabbing the next task from the blockingQueue in ThreadPoolManager
    }


    @Override
    public void run() {

        while (true) {
            // Wait for new task
            waitForNewTask();
            // Do task
            completeNextTask();
            // Repeat
        }
//        super.run();
    }
}
