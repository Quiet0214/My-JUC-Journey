import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Supplier;

/*
* 对比使用AtomicLong原子长整型和LongAdder原子自增器进行多线程安全的情况下进行原子自增的速度差异。
* 可以看到使用原子自增器LongAdder明显速度更快。
* 这是由于LongAdder会在有竞争的时候会设置多个累加单元（累加单元的数量不会超过CPU的核心数），每个累加单元各自都在进行自增，最后再将它们累加起来。
*
* */

public class CASLongAdder {
    public static void main(String[] args) {
        demo(
                ()->new AtomicLong(),
                (addr)-> addr.getAndIncrement()
        );

        demo(
                () -> new LongAdder(),
                (addr) ->addr.increment()
        );
    }
    public static <T> void demo(Supplier<T> supplier, Consumer<T> consumer){
        T addr =supplier.get();
        List<Thread> list = new ArrayList<>();
        for(int i=0;i<5;i++){
            list.add(new Thread(()->{
                for(int j=0;j<500000;j++){
                    consumer.accept(addr);
                }

            }));
        }
        Long start = System.currentTimeMillis();
        list.forEach(thread -> thread.start());
        list.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Long end = System.currentTimeMillis();

        System.out.println(addr+"  "+(end-start));

    }
}


