import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/*
* 通过设计多线程下的数据库连接池，来熟悉享元模式。
* 使用wait - notify 来进行保护性暂停。
* 使用AtomicIntegerArray来对连接池对象进行原子性操作。
* */
public class FlyWeightPattern {
    public static void main(String[] args) {
        Pool connectionPool = new Pool(10);
        List<Thread> list = new ArrayList<>();
        for(int i=0;i<100;i++){
            list.add(new Thread(()->{
                try {
                    Connection connection=connectionPool.getConnection();
                    Thread.sleep(3000);
                    connectionPool.releaseConnection(connection);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        for(int i=0;i<100;i++){
            list.get(i).start();
        }
        list.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

    }
}

@Slf4j
class Pool{
    private Connection[] pool;
    private int totalSize;
    private AtomicIntegerArray used;

    public Pool(int num){
        totalSize = num;
        pool=new Connection[num];
        for(int i=0;i<num;i++){
            pool[i]=new Connection();
        }
        this.used= new AtomicIntegerArray(num);
    }
    public Connection getConnection() throws InterruptedException {
        while(true){

                for(int i=0;i<totalSize;i++){
                    if(used.get(i)==0){
                        if(used.compareAndSet(i,0,1)){
                            log.debug("线程{}获得连接池",Thread.currentThread().getName());
                            return pool[i];
                        }
                    }
                }

            synchronized (this){
                wait();
            }
        }
    }
    public void releaseConnection(Connection connection){
        for(int i=0;i<totalSize;i++){
            if(connection==pool[i]){
                log.debug("线程{}释放连接池",Thread.currentThread().getName());
                used.set(i,0);
                synchronized (this){
                    notifyAll();
                }
                break;
            }
        }
    }
}

class Connection{

}
