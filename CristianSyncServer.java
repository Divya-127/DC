import java.io.*;
import java.net.*;
import java.util.Date;

public class CristianSyncServer {

    public static void main(String []args) throws IOException{
        ServerSocket s = new ServerSocket(50555);
        int server_timeout = 100000;
        s.setSoTimeout(server_timeout);
        displayBanner();
        System.out.println("Server is running...");

        try{
            while(true){
                Socket  s1 = s.accept();
                System.out.println("Connection Received : " + s1);
                DataOutputStream dos = new DataOutputStream(s1.getOutputStream());

                Date dt = new Date();
                System.out.println("Sent to client :  " + dt);
                dos.writeLong(dt.getTime());

                System.out.println("Connection Terminated : " + s1);
                s1.close();
                dos.close();
            }
        }catch(SocketTimeoutException e){
            System.out.println("Terminating server due to timeout...");
            System.out.println("Server is terminated!");
            s.close();
        }
    }

    public static void displayBanner(){
        System.out.println("Clock Synchronization Server v1.0.1--- initializing...");
    }

}