import java.rmi.*;

public interface ServerInterface extends Remote{
    boolean userLogin(String studentID) throws RemoteException;
    String findItem (String userID, String itemName) throws RemoteException;
    String waitInQueue(String campusName, String userID, String itemID) throws RemoteException;
    String borrowItem (String campusName, String userID, String itemID, int numberOfDays) throws RemoteException;
}