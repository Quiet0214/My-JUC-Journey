import com.oracle.jrockit.jfr.Producer;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/*
 * 设计模式：保护性暂停
 * 一个线程在等待某个条件满足时会进入暂停状态
 * 直到条件满足后才会继续执行
 *
 * */
public class GuardedSuspension {

    public static void main(String[] args) {
        MailBox mailBox = new MailBox();
        /*用户线程*/
        new Thread(mailBox::get,"User").start();
        /*快递员线程*/
        new Thread(()->{mailBox.put("你好呀！");},"Mailman").start();

    }
}
/*
 * 邮箱类，提供get获取邮件方法和put放入邮件方法
 * */
@Slf4j
class MailBox{
    private String message;

    public void get(){
        synchronized (this){
            while(message==null){
                log.debug("邮箱为空，等待邮件......");
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.debug("获取邮件：{}",message);
            message=null;
            notify();
        }
    }
    public void put(String message){
        synchronized (this){
            while(this.message!=null){
                log.debug("邮箱已满，等待取走......");
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.debug("放入邮件：{}",message);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            this.message=message;
            notify();
        }
    }
}




/*第一版：过于冗余*/
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class GuardedSuspension {
//
//    public static void main(String[] args) {
//        MailBox mailBox = new MailBox();
//        Producer producer = new Producer(mailBox);
//        User user = new User(mailBox);
//        ExecutorService executor = Executors.newFixedThreadPool(6); // 创建固定大小的线程池
//        executor.submit(user::getMail);
//        executor.submit(producer::produce);
//
//    }
//}
//
//
//class MailBox{
//    private Object message;
//    private boolean isEmpty;
//    public MailBox(){
//        isEmpty = true;
//    }
//    public void setMessage(Object message){
//        if(isEmpty){
//            this.message=message;
//        }
//        isEmpty=false;
//    }
//
//    public boolean isEmpty(){
//        return this.isEmpty;
//    }
//
//
//    public Object getMessage(){
//        if(!isEmpty){
//            return message;
//        }
//        isEmpty=true;
//        return null;
//    }
//}
//
//@Slf4j
//class Producer{
//    private MailBox mailBox;
//    public Producer(MailBox mailBox){
//        this.mailBox = mailBox;
//    }
//
//    public void produce(){
//        synchronized (mailBox){
//            while(!mailBox.isEmpty()){
//                log.debug("邮箱有邮件，等待用户取走......");
//                try {
//                    mailBox.wait();
//
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            String mail = "你好！";
//            log.debug("邮箱为空，正在发送邮件：{}",mail);
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            mailBox.setMessage(mail);
//            mailBox.notify();
//        }
//    }
//
//}
//@Slf4j
//class User{
//    private MailBox mailBox;
//    public User(MailBox mailBox){
//        this.mailBox=mailBox;
//    }
//
//    public void getMail(){
//        synchronized (mailBox){
//            while(mailBox.isEmpty()){
//                log.debug("邮箱为空，等待新邮件......");
//                try {
//
//                    mailBox.wait();
//
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            log.debug("取走邮件");
//            Object message = mailBox.getMessage();
//            log.debug("邮件内容为：{}",message);
//            mailBox.notify();
//        }
//    }
//
//}