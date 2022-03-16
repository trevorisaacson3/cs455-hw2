package cs455.scaling;

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