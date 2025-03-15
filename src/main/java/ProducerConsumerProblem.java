import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 生产者-消费者问题
 * 题目描述
 *     在一个仓库（共享缓冲区）中，生产者负责生产产品，消费者负责从仓库取出产品。需要保证：
 *         1.生产者在仓库满时必须等待，直到有空间可用。
 *         2.消费者在仓库空时必须等待，直到有产品可用。
 *         3.生产者和消费者可以并发运行，但不能导致数据不一致问题（例如，取走不存在的产品或存入超出容量的产品）。
 * 要求
 *     1.设定仓库的最大容量为 10。
 *     2.生产者生产产品的时间随机（比如 500-1000ms）。
 *     3.消费者消费产品的时间随机（比如 800-1200ms）。
 *     4.运行过程中输出日志，能够清晰地看到生产、消费、等待的情况。
 */
@Slf4j
public class ProducerConsumerProblem {

    // 队列容量，根据实际需求调整
    private static final int QUEUE_CAPACITY = 10;
    // 生产者和消费者线程数量配置
    private static final int PRODUCER_COUNT = 2;
    private static final int CONSUMER_COUNT = 3;
    // 生产和消费操作的模拟耗时（单位：毫秒）
    private static final int PRODUCE_DURATION = 1000;
    private static final int CONSUME_DURATION = 1200;

    public static void main(String[] args) {
        BlockingQueue<Goods> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        // 启动生产者线程
        for (int i = 0; i < PRODUCER_COUNT; i++) {
            startProducerThread(queue, "生产者" + i);
        }

        // 启动消费者线程
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            startConsumerThread(queue, "消费者" + i);
        }
    }

    /**
     * 创建并启动生产者线程
     */
    private static void startProducerThread(BlockingQueue<Goods> queue, String threadName) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    String goodsName = String.format("%s-商品%d", threadName, i);
                    Goods goods = new Goods(goodsName);

                    log.debug("[{}] 开始生产: {}...", threadName, goodsName);
                    Thread.sleep(PRODUCE_DURATION); // 模拟生产耗时

                    queue.put(goods); // 阻塞直到队列有空位
                    log.debug("[{}] 成功生产: {}", threadName, goodsName);
                }
            } catch (InterruptedException e) {
                log.error("[{}] 生产被中断", threadName, e);
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }, threadName).start();
    }

    /**
     * 创建并启动消费者线程
     */
    private static void startConsumerThread(BlockingQueue<Goods> queue, String threadName) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    log.debug("[{}] 尝试获取商品...", threadName);
                    Thread.sleep(CONSUME_DURATION); // 模拟消费前的处理耗时

                    Goods goods = queue.take(); // 阻塞直到队列有数据
                    log.debug("[{}] 成功消费: {}", threadName, goods.getName());
                }
            } catch (InterruptedException e) {
                log.error("[{}] 消费被中断", threadName, e);
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }, threadName).start();
    }
}

/**
 * 商品实体类
 */
class Goods {
    private String name;

    public Goods(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Goods{name='" + name + "'}";
    }
}