import com.uftorrent.app.CommonVars;

public class UFTorrent {
    public static CommonVars commonVars = new CommonVars();
    public static void main(String[] args) {
        System.out.println("Here's our env variables!");
        commonVars.print();
    }
}