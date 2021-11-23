import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Main {
    static Writer writers[]=new Writer[10];
    int k = 3;
    public static void main(String[] args) {
        try {
            FileOutputStream fw = new FileOutputStream("file.txt",true);
            Monitor m = new Monitor(fw);
            for (int i = 0; i < writers.length; i++) {
                if(i%2==0)
                    writers[i] = new Writer(m, "U\n");
                else
                    writers[i] = new Writer(m, "E\n");
                writers[i].start();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
    }
}
