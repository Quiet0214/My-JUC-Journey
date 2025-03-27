package MyThreadPool;

import javafx.concurrent.Worker;
import jdk.nashorn.internal.ir.Block;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;



/*
* 简单的线程池，实现了主线程向任务队列放任务时的原子性。
* 线程池中的线程从任务队列中取任务进行执行时的原子性。
* 线程池中一共有2个核心线程,他们在线程池被创建的时候就被启动并且阻塞地等待任务队列中的任务，如果没有任务就会一直等待。
* 主要就是两类线程，一类是main也就是用户线程，它会给线程池不停地提交任务，线程池中的任务队列的最大容量是5.还有一类就是线程池中的工作线程，他们主要就是来干活的，在线程池中不停地等待着用户线程分配任务，拿到任务后就开始干活，拿不到任务的线程就会被阻塞，不会浪费CPU资源，但是会占用内存资源。
* 为线程池设计了两个关闭的方法：1）shutdown。会等待当前队列
* */
@Slf4j
public class MyThreadPool {
    public static void main(String[] args) throws InterruptedException {
        // 创建线程池（核心线程2个，队列容量5，拒绝策略：带超时等待）
        CustomThreadPool customThreadPool = new CustomThreadPool(
                2,
                5,
                (queue, task) -> {
                    log.warn("队列已满，尝试延迟放入...任务 {}", task);
                    if (!queue.offer(task, 500, TimeUnit.MILLISECONDS)) {
                        log.error("队列已满，任务最终被拒绝 {}", task);
                    }
                }
        );

        // 提交20个任务
        for (int i = 0; i < 20; i++) {
            int j = i;
            Runnable runnable = () -> {
                try {
                    log.info("▶▶ 执行任务开始 {}", j);
                    Thread.sleep(2000);  // 模拟任务耗时
                    log.info("▌▌ 执行任务结束 {}", j);
                } catch (InterruptedException e) {
                    log.warn("任务被中断 {}", j);
                }
            };
            customThreadPool.execute(runnable);
            Thread.sleep(50);  // 控制任务提交速度
        }

        // 等待3秒后关闭
        Thread.sleep(3000);
        log.warn("======================== 执行 shutdownNow ========================");
        customThreadPool.shutdownNow();
    }
}
/*
* 拒绝策略的函数式接口，表示线程池中的任务队列在满了的时候执行哪种拒绝策略。（死等，一直不拒绝？等待一段时间后拒绝？立即拒绝？）
* */
@FunctionalInterface
interface RejectPolicy<T>{
    void reject(TaskQueue<T> queue,T task);
}

/*
* 线程池类，其中包括核心线程数量（这里简化了，同时兼具最大线程数的意义）、任务队列、拒绝策略、当前状态。
* */
@Slf4j
class CustomThreadPool{
    private int coreThreadSize;
    // 是否执行了shutdown
    private volatile boolean isShutDown;
    // 是否执行了shutdownNow
    private volatile boolean isStop;
    private TaskQueue<Runnable> taskQueue;
    private RejectPolicy<Runnable> rejectPolicy;
    public HashSet<Worker> workerSet;
    public static int index=1;


    public CustomThreadPool(int coreThreadSize,int taskQueueLength, RejectPolicy<Runnable> rejectPolicy){
        this.coreThreadSize=coreThreadSize;
        this.taskQueue=new TaskQueue<Runnable>(taskQueueLength);
        workerSet=new HashSet<>();
        isShutDown=false;
        isStop=false;
        this.rejectPolicy=rejectPolicy;
        for(int i=0;i<coreThreadSize;i++){
            Worker worker = new Worker();
            workerSet.add(worker);
            worker.start();
        }
    }
    public void shutdown(){
        isShutDown=true;
        for (Worker worker:workerSet){
            if(worker.getIdle()){
                worker.interrupt();
            }

        }
    }
    public void shutdownNow(){
        isStop=true;
        taskQueue.clear();
        for(Worker worker:workerSet){
            worker.interrupt();
        }
    }
    class Worker extends Thread {
        private volatile boolean idle = false;

        public boolean getIdle(){
            return idle;
        }
        public Worker() {
            setName("Worker-" + index++);
        }

        @Override
        public void run() {
            log.info("{} 线程启动 ▶▶▶", getName());
            idle=true;
            while (true) {
                if (isStop) {
                    log.warn("{} 收到立即停止指令", getName());
                    break;
                }
                if (isShutDown && taskQueue.getSize() == 0) {
                    log.warn("{} 队列已空，正常停止", getName());
                    break;
                }
                try {
                    Runnable task = taskQueue.get();
                    log.info("{} 取得任务 {}", getName(), task);
                    idle=false;
                    task.run();
                    log.info("{} 完成任务 {}", getName(), task);
                    idle=true;
                } catch (InterruptedException e) {
                    log.warn("{} 被中断", getName());
                    break;
                }
            }
            log.info("{} 线程结束 ▋▋▋", getName());
        }
    }



    public void execute(Runnable task) {
        if(task ==null) throw new RuntimeException("执行的任务为空！");
        if(isShutDown || isStop){
            log.debug("提交任务失败，线程池已被销毁。");
            return;
        }
        taskQueue.tryput(rejectPolicy,task);
    }


    public <T> Future<T> submit(Callable<T> task) {
        if(task==null) throw new RuntimeException("任务不能为空！");
        if(isShutDown || isStop){
            log.debug("提交任务失败，线程池已被销毁。");
            return null;
        }
        FutureTask<T> futureTask = new FutureTask<>(task);
        execute(futureTask);
        return futureTask;
    }


    public Future<?> submit(Runnable task) {
        if(task==null) throw new RuntimeException("任务不能为空！");
        if(isShutDown || isStop){
            log.debug("提交任务失败，线程池已被销毁。");
            return null;
        }
        FutureTask<?> futureTask = new FutureTask<>(task,null);
        execute(futureTask);
        return null;
    }
}


/*
* 自己实现的线程池中的任务队列（具有阻塞功能）。
* */
@Slf4j
class TaskQueue<T>{
    private Deque<T> queue = new ArrayDeque<>();
    private ReentrantLock lock = new ReentrantLock();
    private int length;


    public int getSize(){
        lock.lock();
        try {
            return queue.size();
        }finally {
            lock.unlock();
        }

    }
    public void clear(){
        lock.lock();
        try {
            queue.clear();
        }finally {
            lock.unlock();
        }
    }
    /*
    * 生产者的条件变量
    * */
    private Condition fullWaitSet = lock.newCondition();
    /*
    * 消费者的条件变量
    * */
    private Condition emptyWaitSet = lock.newCondition();

    public TaskQueue(int length){
        this.length=length;
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == 0) {
                log.debug("队列为空，等待任务...");
                emptyWaitSet.await();
            }
            T result = queue.removeFirst();
            log.debug("取出任务，剩余任务数: {}", queue.size());
            fullWaitSet.signal();
            return result;
        } finally {
            lock.unlock();
        }
    }


    /*
    * 非阻塞式 任务入队。
    * 超时失败。
    * True ： 入队成功
    * False ： 入队失败
    * */
    public boolean offer(T task, long timeout, TimeUnit unit){
        long nanos = unit.toNanos(timeout);
        lock.lock();
        try{
            while(queue.size()==length){
                if(nanos<=0){
                    log.debug("时间过长，抛弃该任务！");
                    return false;
                }
                try {
                    nanos = fullWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.addLast(task);
            emptyWaitSet.signal();
        }finally {
            lock.unlock();
        }
        return true;
    }



    /*
    * 阻塞式 任务入队
    *
    * */
    public void put(T task){
        lock.lock();
        try{
            while(queue.size()==length){
                try {
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            queue.addLast(task);
            emptyWaitSet.signal();
        }finally {
            lock.unlock();
        }
    }

    public void tryput(RejectPolicy<T> rejectPolicy, T task) {
        lock.lock();
        try {
            if (queue.size() == length) {
                log.warn("触发拒绝策略（当前队列大小 {}）", length);
                rejectPolicy.reject(this, task);
            } else {
                queue.addLast(task);
                log.debug("加入队列，当前队列大小: {}", queue.size());
                emptyWaitSet.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}


