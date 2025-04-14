import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

/*
* 使用CountDownLatch模拟实现LOL游戏的加载页面
* */
public class CountDownLatchToLOL {
    public static void main(String[] args) {
        ExecutorService service = Executors.newFixedThreadPool(10);
        String[] players = new String[10];
        Random r = new Random();
        CountDownLatch countDownLatch=new CountDownLatch(10);
        for(int j=0;j<10;j++){
            int k=j;
            service.submit(()->{
                for(int i=0;i<=100;i++){
                    try {
                        Thread.sleep(r.nextInt(100));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    players[k]=i+"%";
                    System.out.print("\r"+Arrays.toString(players));
                }
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println();
        System.out.println("加载完成，游戏开始！");
        service.shutdown();
    }
}
