/*
* 懒汉式线程不安全
*   每个线程在获取单例的实例对象的时候都会判断出当前instance是空的，所以每个线程都会创建一个新的实例，这就违背了单例模式。
* */


/*
* 如何解决？
* 修改getInstance（）方法如下：
* public static Singleton getInstance() {
        synchronized (Singleton.class) {
            if (instance == null) {
                instance = new Singleton();
            }
        }
        return instance;
    }
   这样虽然是线程安全地，但是这样会产生性能问题，每个获取实例的线程都会被卡住，频繁地上锁解锁。
* 于是有人提出了下面地想法：双重检查锁定
* public static Singleton getInstance() {
        if (instance == null) { // 第一次检查
            synchronized (Singleton.class) {
                if (instance == null) { // 第二次检查
                    instance = new Singleton(); // 非原子操作
                }
            }
        }
        return instance;
    }
* 就是在上锁之前先进行一次判断，这样如果实例已经存在，后续的所有线程就不用频繁地上锁解锁了，性能大大提升，但是又会出现线程不安全问题。
* 原因是，对于这一行代码：instance = new Singleton(); // 非原子操作，cpu会将它拆分成多条指令，其中有两条指令
* 1.使用Singleton的构造方法创建instance实例。
* 2.将创建好的内存地址赋值给instance。
* 可能会在指令优化重排序后，先将创建好的内存地址赋值给instance，再使用Singleton的构造方法创建instance实例。
* 如果在cpu在执行了重排序后的第一个指令（先将创建好的内存地址赋值给instance），这时另外一个线程来了判断instance就不是null，然后就会把instance返回。
* 但是此时instance实例还没被创建好，如果该线程在获取到没创建好的实例进行一系列操作的话，所以就会引发一系列错误。
* 所以这又是线程不安全的了。
*
* 最后是在instance变量前面添加一个volatile关键字来保证有序性，即它的指令不会再被重排序了。这样就是高性能的线程安全的代码了。
* */

public class ThreadSingleton {
    public static void main(String[] args) {
        Runnable task = () -> System.out.println(Singleton.getInstance().hashCode());
        for (int i = 0; i < 10; i++) new Thread(task).start();
    }
}


class Singleton {
    private static Singleton instance;
    private Singleton() {
        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}

