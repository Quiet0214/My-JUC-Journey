import lombok.experimental.Helper;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/*
* 题目：三个线程依次交替打印
* 要求：线程1输出a5次，线程2输出b5次，线程3输出c5次，最后输出结果是abcabcabcabcabc
* 使用 ReentrantLock + Condition。
* */



/*
* 优化实现
* */
public class MultiThreadedAlternateExecution {
    private final static ReentrantLock lock = new ReentrantLock();
    private final static Condition conditionA = lock.newCondition();
    private final static Condition conditionB = lock.newCondition();
    private final static Condition conditionC = lock.newCondition();
    private static int state = 0;
    private static int loopTime = 5;

    public static void main(String[] args) {
        new Thread(()->{
            print("a",0,conditionA,conditionB);
        },"t1").start();

        new Thread(()->{
            print("b",1,conditionB,conditionC);
        },"t2").start();

        new Thread(()->{
            print("c",2,conditionC,conditionA);
        },"t3").start();

    }


    public static void print(String str,int targetState,Condition current,Condition next){

        for(int i=0;i<loopTime;i++){
            lock.lock();
            try {
                while(state%3 != targetState){
                    current.await();
                }
                System.out.print(str);
                state++;
                next.signal();
            }catch(Exception e){
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }

    }
}














/*
* 初步实现：
* */
/*public class MultiThreadedAlternateExecution {
    public static void main(String[] args) {
        Helper helper =new Helper();
        new Thread(()->{
            helper.print('a','b');
        },"t1").start();

        new Thread(()->{
            helper.print('b','c');
        },"t2").start();

        new Thread(()->{
            helper.print('c','a');
        },"t3").start();
    }
}

class Helper{
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition a = lock.newCondition();
    private final Condition b = lock.newCondition();
    private final Condition c = lock.newCondition();
    private final int loopTime = 5;

    private char flag = 'a';

    public void print(char flag,char next){
        for(int i=0;i<loopTime;i++){
            try{
                lock.lock();
                if(flag!=this.flag){
                    if(flag=='a'){
                        try {
                            a.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }else if (flag =='b'){
                        try {
                            b.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        try {
                            c.await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                System.out.print(flag);
                this.flag=next;
                if(next=='a'){
                    a.signal();
                }else if(next =='b'){
                    b.signal();
                }else{
                    c.signal();
                }
            }finally {
                lock.unlock();
            }
        }
    }
}*/
