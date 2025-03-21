/*
* JDK官方提供了3个原子数组，都提供了原子更新数组元素的能力。
* 分别是：
*    AtomicIntegerArray：原子更新整型数组里的元素

     AtomicLongArray：原子更新长整型数组里的元素。

     AtomicReferenceArray：原子更新引用类型数组里的元素。
* */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CASAtomicArray {
    public static void main(String[] args) {

        demo(
                ()->new int[10],
                (array) -> array.length,
                (array,index) -> array[index]++,
                (array) -> System.out.println(Arrays.toString(array))
        );


        demo(
                ()->new AtomicIntegerArray(10),
                (array) -> array.length(),
                (array,index) -> array.getAndIncrement(index),
                (array) -> System.out.println(array)
        );

    }


    public static<T> void demo(
            Supplier<T> arraySupplier,
            Function<T,Integer> lengthFun,
            BiConsumer<T,Integer> putConsumer,
            Consumer<T> printConsumer
    ){
        T t = arraySupplier.get();
        int length = lengthFun.apply(t);
        List<Thread> list = new ArrayList<>();
        for(int i=0;i<length;i++){
            list.add(new Thread(()->{
                for(int j=0;j<10000;j++){
                    putConsumer.accept(t,j%length);
                }
            }));
        }
        list.forEach((thread) -> {thread.start();});
        list.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        printConsumer.accept(t);
    }
}

