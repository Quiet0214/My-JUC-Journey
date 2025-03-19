import java.io.*;



/*
* 通过下面的代码我们可以知道transient和static关键字修饰的变量是不可以被序列化的。
* */
public class Serialization {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        File directory = new File("./file");
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("文件夹创建成功");
            } else {
                System.out.println("文件夹创建失败");
                return;
            }
        }
        User cs = new User(1,"慈溪","123456789");
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("./file/cs.obj"));
        os.writeObject(cs);
        ObjectInputStream is = new ObjectInputStream(new FileInputStream("./file/cs.obj"));
        User.country="USA";
        User user = (User) is.readObject();
        System.out.println(user);
    }
}

class User implements Serializable{
    public static String country ="China";
    private int id;
    private String username;
    private transient String password;

    public User() {
    }

    public User( int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }


    /**
     * 获取
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * 设置
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置
     * @param username
     */
    public void setUsername(String username) {
        this.username = username;
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
        return "User{country = " + country + ", id = " + id + ", username = " + username + ", password = " + password + "}";
    }
}
