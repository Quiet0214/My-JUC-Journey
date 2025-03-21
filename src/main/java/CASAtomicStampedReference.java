import java.util.concurrent.atomic.AtomicStampedReference;

import static java.lang.Thread.sleep;

/*
* 简单的AtomicReference可能还会存在“ABA”问题，在某些情况下，ABA问题是不可被容忍的。
* “ABA问题”：主线程要将A改成C，线程1将A改成B，线程2将B改成A。在主线程执行更改操作之前，线程1和线程2执行了一遍导致又变成了A，但是已经被其他线程修改过了。
* 使用AtomicStampedReference就可以解决这个问题，AtomicStampedReference自带Stamp属性，也可以认为是版本号属性，只有当前要修改的操作的版本号和一开始获取值的时候的版本号一致，才执行，否则不执行。
* */
public class CASAtomicStampedReference {
    static AtomicStampedReference<String> ref = new AtomicStampedReference<>("A",0);
    public static void main(String[] args) throws InterruptedException {
        String prev = ref.getReference();
        int stamp = ref.getStamp();
        other();
        sleep(1000);
        System.out.println(ref.compareAndSet(prev,"C",stamp,stamp+1));
    }

    public static void other() throws InterruptedException {
        new Thread(()->{
            while(true){
                String prev = ref.getReference();
                int stamp = ref.getStamp();
                if(ref.compareAndSet(prev,"B",stamp,stamp+1)){
                    break;
                }
            }
        }).start();
        sleep(1000);
        new Thread(()->{
            while(true){
                String prev = ref.getReference();
                int stamp = ref.getStamp();
                if(ref.compareAndSet(prev,"A",stamp,stamp+1)){
                    break;
                }
            }
        }).start();
    }
}

