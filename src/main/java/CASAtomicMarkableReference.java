import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;


/*
* 多线程环境下的「用户账户安全登录锁」
* 某在线支付系统的用户账户模块需要防止并发登录攻击。当一个用户正在登录时，系统需将其账户标记为“锁定状态”，避免其他线程同时修改账户安全信息。要求使用 AtomicMarkableReference 实现以下功能：
    原子性地标记账户是否被锁定。
    在账户锁定时，拒绝其他线程的登录请求。
    登录完成后解除锁定状态。
* 使用AtomicMarkableReference自带的true和false来完成。
* */
@Slf4j
public class CASAtomicMarkableReference {
    private final static int Thread_NUM = 5;
    private static final Object logLock = new Object();
    public static void main(String[] args) {
        UserAccount userAccount = new UserAccount("你被录用了","123456");
        AccountManager am = new AccountManager(userAccount);
        List<Thread> list = new ArrayList<>();
        for (int i=0;i<Thread_NUM;i++){
            list.add(new Thread(()->{
                try {
                    am.login(userAccount);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        list.forEach(thread ->{
            thread.start();
        });
        list.forEach(thread->{
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
    public static void logMessage(String message) {
        synchronized (logLock) {
            log.debug(message);
        }
    }
}


@Slf4j
class AccountManager{
    UserAccount userAccount;
    AtomicMarkableReference<UserAccount> ref;
    public  AccountManager(UserAccount userAccount){
        this.userAccount=userAccount;
        ref = new AtomicMarkableReference<>(userAccount,false);
    }
    public void login(UserAccount userAccount) throws InterruptedException {
        if(!ref.compareAndSet(userAccount,userAccount,false,true)){
            Thread.sleep(500);
            CASAtomicMarkableReference.logMessage("账户已被锁定，请稍后再试。");
            return;
        }
        try {
            CASAtomicMarkableReference.logMessage(Thread.currentThread().getName() + "正在登录，账户已锁定...");
            Thread.sleep(1000);
            if (!isValidLogin(this.userAccount, userAccount.getUserName(), userAccount.getPassword())) {
                CASAtomicMarkableReference.logMessage("用户名或者密码错误！");
            }
            CASAtomicMarkableReference.logMessage(Thread.currentThread().getName() + "登陆成功，账户正在解除锁定。");
        }finally {
            ref.compareAndSet(userAccount,userAccount,true,false);
        }

    }
    public boolean isValidLogin(UserAccount userAccount,String userName, String password){
        return userName.equals(userAccount.getUserName()) && password.equals(userAccount.getPassword());
    }

}






class UserAccount{
    private String userName;
    private String password;

    public UserAccount() {
    }

    public UserAccount(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     * 获取
     * @return userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 设置
     * @param userName
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * 获取
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "UserAccount{userName = " + userName + ", password = " + password + "}";
    }
}
