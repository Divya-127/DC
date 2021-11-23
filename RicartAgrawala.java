import java.util.*;
import java.io.*;

public class RicartAgrawala {

    public static List<Process> processes, cs_list;
    public static Process current; 
    public static List<String> books = new ArrayList<>();
    static String[] bks = new String[100];

    public static class Process {
        int id;
        int timestamp;
        Process(int id, int timestamp) {
            this.id = id;
            this.timestamp = timestamp;
        }
    }

	public static void main(String args[]) {
		Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        for (int i = 0; i < 10; i++) {
            books.add("");
        }

		System.out.println("Enter the number of process:");
		int n = Integer.parseInt(sc.nextLine());

        processes = new ArrayList<>();
        for(int i=0;i<n;i++) {
            processes.add(new Process(i, rand.nextInt(10000)));
            System.out.println("Process " + processes.get(i).id + " Timestamp " + processes.get(i).timestamp);
            if(i%3==0)
                bks[i] = "Physics";
            else if (i%3==1) {
                bks[i] = "Chemistry";
            } else{
                bks[i] = "Biology";
            }
        }

		System.out.println("\nEnter the id of process who want to enter CS:");
        String s = sc.nextLine();

        cs_list = new ArrayList<>();
        for(String str: s.split(" ")) {
            cs_list.add(processes.get(Integer.parseInt(str)));
        }

        System.out.println("");

        for(Process p: new ArrayList<>(cs_list)) {
            Thread t1 = new Thread(new Runnable() {
                public void run()
                {
                    System.out.println(p.id+" Started Executing ");
                    enter_cs(p);
                }});  
            t1.start();
        }
        sc.close();
    }
    
    public static void enter_cs(Process process) {
        List<String> replies = new ArrayList<>();
        books.add(process.id, bks[process.id]);
        for(Process p: processes) 
            if (p.id != process.id)
                request(p, process, replies);
        while(replies.size()!=processes.size()-1);

        current = process;
        System.out.println(process.id + " Process entered CS");
        System.out.println(process.id+ " Process completed its work ");
        System.out.println(process.id+ " added " +bks[process.id]);
        System.out.println(process.id + " Process left CS\n");

        release(process);
    }

    public static void request(Process p, Process process, List<String> replies) {
        while (current==p);
        while (cs_list.contains(p)) {
            if (p.timestamp > process.timestamp) {
                replies.add("REPLY");
                return;
            }
        }
        replies.add("REPLY");
    }

    public static void release(Process process) {
        current = null;
        cs_list.remove(process);
    }
}