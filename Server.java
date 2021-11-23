import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    
    public static void main(String[] args) {
        
        InputStreamReader is = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(is);
        String portNum = " ", registryURL = " ";
        int udpportnum = 0;
        String libraryName = "";

        try{
            if(args[0].equals("CON")){
                portNum = "1234";
                udpportnum = 2234;
                libraryName = "Concordia";
            }
            else if(args[0].equals("MCG")){
                portNum = "1235";
                udpportnum = 2236;
                libraryName = "McGill Publications";
            }
            else if(args[0].equals("MON")){
                portNum = "1237";
                udpportnum = 2237;
                libraryName = "Montreal";
            }
            else {
                System.out.println("Server startup failed");
                System.exit(0);
            }
            System.out.println("Welcome to " + libraryName + " online library!");
            int RMIPortNum = Integer.parseInt(portNum);
            startRegistry(RMIPortNum);
            ServerImpl obj = new ServerImpl();
            registryURL = "rmi://localhost:" + portNum + "/DLMS-" + args[0];
            String registryURL2 = "rmi://localhost:" + "1236/DLMS-" + args[0];
            Naming.rebind(registryURL, obj);
            Naming.rebind(registryURL2, obj);
            System.out.println ("Server registered.  Registry currently contains:");
            listRegistry(registryURL);
            System.out.println("DLMS Server ready.");

            DatagramSocket serversocket = new DatagramSocket(udpportnum);
            obj.StartServer(args[0]);
        }
        catch (Exception re) {
            System.out.println("Exception in Server.main: " + re);
        }
    }

    private static void startRegistry(int RMIPortNum) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(RMIPortNum);
            registry.list( );
        } catch (RemoteException e) {
            Registry registry = LocateRegistry.createRegistry(RMIPortNum);
            LocateRegistry.createRegistry(1236);
        }
    }

    private static void listRegistry(String registryURL) throws RemoteException, MalformedURLException {

        String [ ] names = Naming.list(registryURL);
        for (int i=0; i < names.length; i++)
            System.out.println(names[i]);
    }
}
