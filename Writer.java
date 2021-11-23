public class Writer extends Thread {
    private Monitor m;
    String s; 
    Writer(Monitor m,String s) { 
        this.m=m; 
        this.s = s;
    }    
    public void run() { 
        try { 
            m.write(s); 
        } 
        catch (Exception e) { 
            e.printStackTrace(); 
        } 
    } 
}
