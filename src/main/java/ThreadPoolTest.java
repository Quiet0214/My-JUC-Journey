import lombok.extern.slf4j.Slf4j;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;


/*
* 测试使用线程池的方法
* 方法被集成为method n
* 具体方法的使用介绍在方法前面的注释。
* */
@Slf4j
public class ThreadPoolTest {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService pool1=Executors.newFixedThreadPool(2);
        ExecutorService pool2=Executors.newFixedThreadPool(1);
        ScheduledExecutorService pool3 = Executors.newScheduledThreadPool(1);
        method7(pool3);
    }
    /*
    * 定时每周四18:00打印输出一行字。
    * */
    private static void method7(ScheduledExecutorService pool3) {
        long period = 1000*60*60*24*7;
        //获取当前时间
        LocalDateTime now = LocalDateTime.now();


        //获取周四时间
        LocalDateTime time = now.withHour(18).withMinute(0).withSecond(0).withNano(0).with(DayOfWeek.THURSDAY);
        if(time.isBefore(now)){
            time=time.plusWeeks(1);
        }
        long delay = Duration.between(now, time).toMillis();


        pool3.scheduleAtFixedRate(()->{
            log.debug("现在是周四晚上六点。");
        },delay,period,TimeUnit.MILLISECONDS);
    }

    /*
    * 将有同步关系的任务放到不同的线程池中，也就是说将线程池根据处理的任务种类进行分类。
    * 其实前台任务就是CPU密集型任务、后厨任务就是I/O密集型任务。
    * */
    private static void method6(ExecutorService pool1, ExecutorService pool2) {
        pool1.submit(
                ()->{
                    log.debug("前台点餐");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Future<String> submit = pool2.submit(
                            () -> {
                                log.debug("后厨做饭");
                                return "宫保鸡丁";
                            }
                    );
                    try {
                        log.debug("前台出餐:{}",submit.get());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        pool1.submit(
                ()->{
                    log.debug("前台点餐");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Future<String> submit = pool2.submit(
                            () -> {
                                log.debug("后台做饭");
                                return "酸菜鱼";
                            }
                    );
                    try {
                        log.debug("前台出餐:{}",submit.get());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                }
        );
    }

    /*
    * 将两种有同步关系的任务交给同一个线程池来执行
    * 会出现饥饿现象
    * */
    private static void method5(ExecutorService pool) {
        pool.submit(
                ()->{
                    log.debug("前台点餐");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Future<String> submit = pool.submit(
                            () -> {
                                log.debug("后厨做饭");
                                return "宫保鸡丁";
                            }
                    );
                    try {
                        log.debug("前台出餐:{}",submit.get());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        pool.submit(
                ()->{
                    log.debug("前台点餐");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Future<String> submit = pool.submit(
                            () -> {
                                log.debug("后台做饭");
                                return "酸菜鱼";
                            }
                    );
                    try {
                        log.debug("前台出餐:{}",submit.get());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                }
        );
    }

    /*
    * 使用shutdown和shutdownNow方法
    * */
    private static void method4(ExecutorService pool) {
        pool.submit(
                ()->{
                    log.debug("begin");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    log.debug("end");
                }
        );

        pool.submit(
                ()->{
                    log.debug("begin");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    log.debug("end");
                }
        );

        pool.submit(
                ()->{
                    log.debug("begin");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    log.debug("end");
                }
        );
        log.debug("shutdownNow");
        pool.shutdownNow();
    }

    /*
    * 使用invokeAny方法
    * */
    private static void method3(ExecutorService pool) throws InterruptedException, ExecutionException {
        String s = pool.invokeAny(Arrays.asList(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        log.debug("1");
                        return "ok1";
                    }
                },
                new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        log.debug("2");
                        return "ok2";
                    }
                },
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        log.debug("3");
                        return "ok3";
                    }
                }

        ));
        log.debug(s);
    }

    /*
    * 使用invokeAll方法
    * */
    private static void method2(ExecutorService pool) throws InterruptedException, ExecutionException {
        List<Future<String>> list =  pool.invokeAll(Arrays.asList(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        log.debug("1");
                        return "ok1";
                    }
                },
                new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        log.debug("2");
                        return "ok2";
                    }
                },
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        log.debug("3");
                        return "ok3";
                    }
                }

        ));
        for(int i=0;i<list.size();i++){
            log.debug(list.get(i).get());
        }
    }

    /*
    * 使用submit方法
    * */
    private static void method1(ExecutorService pool) throws InterruptedException, ExecutionException {
        Future<String> future = pool.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {

                System.out.println(123);
                Thread.sleep(1000);
                return "ok";
            }
        });
        System.out.println(future.get());
    }
}
