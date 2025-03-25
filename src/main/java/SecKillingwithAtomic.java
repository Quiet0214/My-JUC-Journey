import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;


/*
* 使用Atomic类来模拟电商的秒杀，解决超卖问题。
* */
public class SecKillingwithAtomic {
    public static void main(String[] args) {
        Inventory inventory = new Inventory(new AtomicInteger(10000),new AtomicStampedReference<>("充足",0));
        Users users=new Users(inventory);
        users.start();
        System.out.println(inventory.inventory.get().status);
        System.out.println(inventory.inventory.get().stock);
    }
}


class Users{
    Inventory inventory;
    List<Thread> users = new ArrayList<>();
    public Users(Inventory inventory){
        this.inventory=inventory;
        for(int i=0;i<10000;i++){
            users.add(new Thread(()->{
                inventory.consume();
            },"用户"+i));
        }
    }
    public void start(){
        users.forEach(thread ->{
            thread.start();
        });
        users.forEach(thread ->{
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}


/*
* 存在一个潜在的问题，当我使用updateStatus方法时，如果我获取当前的库存数量，就在我准备更新状态之前
* 这个时候另外一个线程增加了或者减少了很多的库存（倘若他在更改之后不即时更新状态），那么我的更新操作就会是错误的更新。
* 解决方法就是将库存数量和库存状态放到一个对象中，然后使用一个原子类来同时操作这两。
*
* */

/*
@Slf4j
class Inventory{
    AtomicInteger quantity;
    AtomicStampedReference<String> status;
    public Inventory(AtomicInteger quantity, AtomicStampedReference<String> status){
        this.quantity=quantity;
        this.status=status;
    }

    */
/*
    * 下面这段代码会有线程安全问题。
    * *//*

    */
/*public void consume(){
        if(!"售空".equals(status.getReference())){
            quantity.decrementAndGet();
        }

    }*//*

    public void consume(){
        while(true){
            int currentQuantity = quantity.get();
            String currentStatus = status.getReference();
            if("售空".equals(currentStatus) || currentQuantity==0){
                log.debug("库存已售空！");
                return;
            }

            if(quantity.compareAndSet(currentQuantity,currentQuantity-1)){
                log.debug("{}消耗了一张票，还剩{}张票",Thread.currentThread().getName(),quantity.get());
                updateStatus();
                return;
            }
        }

    }
    public void updateStatus(){
        while(true){
            int currentQuantity = quantity.get();
            String currentStatus = status.getReference();
            int stamp = status.getStamp();

            String nextStatus;

            if(currentQuantity==0){
                nextStatus="售空";
            }else if(currentQuantity<5){
                nextStatus="紧张";
            }else{
                nextStatus="充足";
            }
            if(status.compareAndSet(currentStatus,nextStatus,stamp,stamp+1)){
                return;
            }
        }
    }
}
*/


/*
* 使用一个原子类来同时操作库存数量和库存状态。
* */

@Slf4j
class Inventory{
    AtomicInteger quantity;
    AtomicStampedReference<String> status;
    AtomicReference<StockStatus> inventory;
    public Inventory(AtomicInteger quantity, AtomicStampedReference<String> status){
        this.quantity=quantity;
        this.status=status;
        this.inventory= new AtomicReference<>(new StockStatus(quantity.get(),status.getReference()));
    }
    class StockStatus{
        final int stock;
        final String status;
        public StockStatus(int stock,String status){
            this.stock = stock;
            this.status = status;
        }
    }


    /*
     * 下面这段代码会有线程安全问题。
     * */
    /*public void consume(){
        if(!"售空".equals(status.getReference())){
            quantity.decrementAndGet();
        }

    }*/
    public void consume(){
        while(true){
            StockStatus current = inventory.get();
            int currentStock = current.stock;
            String currentStatus = current.status;
            if("售空".equals(currentStatus) || currentStock==0){
                log.debug("库存已售空！");
                return;
            }
            int nextStock = currentStock-1;
            String nextStatus;
            if(nextStock==0){
                nextStatus="售空";
            }else if(nextStock<=5){
                nextStatus="紧张";
            }else{
                nextStatus="充足";
            }

            if(inventory.compareAndSet(current,new StockStatus(nextStock,nextStatus))){
                return;
            }
        }

    }

}