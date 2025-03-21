import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;



/*
* AtomicInteger 是对整型的原子保护，如果要对引用类型进行原子保护，就要使用下面这个AtomicReference。
* */
public class CASAtomicReference {

    public static void main(String[] args) {
        DecimalAccountCAS cas = new DecimalAccountCAS(new AtomicReference<>(new BigDecimal("10000")));
        List<Thread> list = new ArrayList<>();
        for(int i=0;i<1000;i++){
            list.add(new Thread(()->{
                cas.WithDraw(BigDecimal.TEN);
            }));
        }
        list.forEach(i ->{
            i.start();
        });
        list.forEach(i->{
            try {
                i.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(cas.getBalance());
    }
}


class DecimalAccountCAS implements DecimalAccount{
    private AtomicReference<BigDecimal> balance;


    public DecimalAccountCAS(AtomicReference<BigDecimal> balance) {
        this.balance = balance;
    }

    @Override
    public BigDecimal getBalance() {
        return balance.get();
    }

    @Override
    public void WithDraw(BigDecimal bigDecimal) {
        while(true){
            BigDecimal prev = balance.get();
            BigDecimal next = prev.subtract(bigDecimal);
            if(balance.compareAndSet(prev,next)){
                break;
            }
        }
    }
}


interface DecimalAccount{

    public BigDecimal getBalance();

    public void WithDraw(BigDecimal bigDecimal);
}
