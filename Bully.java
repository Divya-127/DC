import java.io.*;
import java.util.*;
 
class Bully{

    static int n;
    static char process[] = {'A','B','C','D','E'};
    static int priority[] = new int[100];
    static int status[] = new int[100];
    static int elected;
     
    public static void main(String args[])throws IOException
    {
        Random rand = new Random();
        n = 5;
         
        for(int i=0;i<n;i++)
        {
            status[i] = rand.nextInt(2);
            priority[i] = rand.nextInt(100);
            System.out.println("Process Number" + process[i] + " Alive " + status[i] + " Priority " + priority[i]);
        }    
        System.out.println("Alive 0 means the process is dead");
        System.out.println("Process B will intiate the election:");
        startElection(1);
        System.out.println("Final coordinator elected is "+process[elected]);
    }
     
    static void startElection(int p)
    {
        elected = p;
        for(int i=0; i<n; i++)
        {
            if(priority[p]<priority[i])
            {
                System.out.println("Election message sent from " + p + " to " + i);
                if(status[i]==1) startElection(i);
            }
        }
    }
}