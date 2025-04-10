import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/*
* 自己模拟实现带缓存功能的数据库查询操作。
* 1.解决了缓存击穿问题
* 2.解决了缓存和数据库不一致问题。
* 3.注意加了锁之后注定要损失一部分性能。
* */
public class MyCache {
    public static void main(String[] args) {
        List<Thread> list = new ArrayList<>();
        CacheSqlOptions sqlOptions = new CacheSqlOptions();
        for(int i =0;i<10;i++){

            list.add(new Thread(()->{
                sqlOptions.query(1);
            }));
        }
        for(int i =0;i<3;i++){
            list.add(new Thread(()->{
                sqlOptions.update(1);
            }));
        }
        for(Thread thread:list){
            thread.start();
        }
        for (Thread thread:list){
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class CacheSqlOptions extends SqlOptions{
    Map<Integer,String> map = new HashMap<>();
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public String query(int id) {
        lock.readLock().lock();
        //1.先查询缓存
        try {
            String value = map.get(id);
            if(value!=null){
                return value;
            }
        } finally {
            lock.readLock().unlock();
        }


        lock.writeLock().lock();
        String value;

        try {
            //2.缓存不存在，再查询数据库
            value = map.get(id);
            if (value == null) {
                value = super.query(id); // 模拟数据库查询
                map.put(id, value);
            }
        } finally {
            lock.writeLock().unlock();
        }
        return value;
    }

    @Override
    public String update(int id) {
        //1.先更新数据库
        String value= super.update(id);
        lock.writeLock().lock();

        try {
            //2.再删除缓存
            map.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
        return value;
    }
}


class SqlOptions{
    public String query(int id){
        String sql = "select * from table where table.id = id";
        System.out.println(Thread.currentThread().getName()+sql);
        return sql;
    }
    public String update(int id)  {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String sql = "update table set original = new where table.id = id";
        System.out.println(Thread.currentThread().getName()+sql);
        return sql;
    }
}
