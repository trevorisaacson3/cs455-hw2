package cs455.scaling;
import java.util.concurrent.SynchronousQueue;


public class AutomaticExit extends Thread{

    int numMinutes = 5;

    public AutomaticExit(){};

    public AutomaticExit(int numMinutes){
        this.numMinutes = numMinutes;
    }

    @Override
    public void run(){
        try{
            Thread.sleep(1000 * 60 * numMinutes);
        }
        catch(InterruptedException e) {
        }
        System.exit(0);
    }
}