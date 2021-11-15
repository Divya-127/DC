import java.rmi.*;

public interface ServerInterface extends Remote{
    boolean userLogin(String studentID) throws RemoteException;
    String findItem (String userID, String itemName) throws RemoteException;
}