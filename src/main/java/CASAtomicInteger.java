import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;


/*
* 使用原子整数自带原子性的方法来实现10个线程同时对账户1000元的取款。
* 原子整数内部是用Compare And Set方法来保护原子性的。
* */

public class CASAtomicInteger {
    public static void main(String[] args) {
        AtomicInteger money = new AtomicInteger(1000);
        for(int i=0;i<10;i++){
            new Thread(()->{
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                int k =updateAndGet(money,value -> value-100);
            }).start();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(money.get());
    }

    /*
    * 如此设计updateAndGet的话，这个方法就没有了通用性，只能用来做减少100的操作。
    * */
    public static void updateAndGet(AtomicInteger i){
        while(true){
            int prev = i.get();
            int next = prev-100;
            if(i.compareAndSet(prev,next)){
                break;
            }
        }
    }

    /*
    * 修改之后。
    * 第二个参数是一个函数式接口，这个接口中通常只有一个抽象方法，可以使用lambda表达式作为参数传入。
    * 具有很强的通用性，使用的时候只要把原子整数和编写好的函数式接口传入即可得到想要的结果。
    * */
    public static int updateAndGet(AtomicInteger i, IntUnaryOperator operator){
        while(true){
            int prev = i.get();
            int next = operator.applyAsInt(prev);
            if(i.compareAndSet(prev,next)){
                return next;
            }
        }
    }
}
