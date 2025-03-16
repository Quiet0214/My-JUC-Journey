import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public class DiningPhilosophersProblem {
    public static void main(String[] args) {
        Chopstick[] chopsticks = new Chopstick[5];
        for(int i=0;i<5;i++){
            chopsticks[i]=new Chopstick("筷子"+i);
        }
        Philosopher[] philosophers = new Philosopher[5];
        for(int i=0;i<5;i++){
            philosophers[i]=new Philosopher("哲学家"+i,chopsticks[i],chopsticks[(i+1)%5]);
        }
        for(int i=0;i<5;i++){
            philosophers[i].start();
        }
    }
}


@Slf4j
class Philosopher extends Thread{
    Chopstick left;
    Chopstick right;
    public Philosopher(String name,Chopstick left,Chopstick right){
        super(name);
        this.left =left;
        this.right=right;
    }
    @Override
    public void run(){
        while(true){

            log.debug("{}正在思考......",Thread.currentThread().getName());
            boolean leftLock = false;
            boolean rightLock = false;
            try {
                leftLock = left.lock.tryLock(1,TimeUnit.SECONDS);
                if(leftLock){
                    rightLock = right.lock.tryLock(1,TimeUnit.SECONDS);
                    if(rightLock){
                        log.debug("{}正在吃饭......",Thread.currentThread().getName());
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally{
                if(leftLock){
                    left.lock.unlock();
                }
                if(rightLock){
                    right.lock.unlock();
                }
            }
        }
    }

}

@Slf4j
class Chopstick{
    private final String name;
    public final ReentrantLock lock = new ReentrantLock();
    public Chopstick(String name){
        this.name = name;
    }
    public String toString(){
        return "筷子"+name;
    }
}
