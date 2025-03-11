import lombok.extern.slf4j.Slf4j;


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
            log.debug("{}正在思考",this.getName());
            synchronized (left){
                synchronized (right){
                    log.debug("{}正在吃饭",this.getName());
                }
            }

        }
    }

}

@Slf4j
class Chopstick{
    private String name;
    public Chopstick(String name){
        this.name = name;
    }
    public String toString(){
        return "筷子"+name;
    }
}
