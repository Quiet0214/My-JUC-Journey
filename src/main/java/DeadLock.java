import lombok.extern.slf4j.Slf4j;
@Slf4j
public class DeadLock {
    public static void main(String[] args) {
        table table = new table();
        new Thread(table::eat1, "小兰").start();
        new Thread(table::eat2, "小明").start();
    }

}
@Slf4j
class table{
    private final Object spoon = new Object();
    private final Object fork = new Object();

    public void eat1(){
        synchronized (this.fork){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug(Thread.currentThread().getName()+"先拿了fork,等待拿spoon");
            synchronized (this.spoon){
                log.debug(Thread.currentThread().getName()+"再拿了spoon,正在吃饭");
            }
        }
    }

    public void eat2(){
        synchronized(this.spoon){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug(Thread.currentThread().getName()+"先拿了spoon,等待拿fork");
            synchronized (this.fork){
                log.debug(Thread.currentThread().getName()+"再拿了fork,正在吃饭");
            }
        }
    }
}

