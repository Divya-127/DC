import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ServerImpl extends UnicastRemoteObject implements ServerInterface {

    class User{
        String userID = " ";
        int borrowCount = 0;
    }
    class Item{
        String ID;
        String name;
        int num;
    }

    private String Campus = "";

    ArrayList<User> userClients = new ArrayList<>();
    HashMap<String, Item> items = new HashMap<>();

    protected ServerImpl() throws RemoteException {
        super();
    }

    @Override
    public RemoteRef getRef(){
        return super.getRef();
    }

    public String getFormatDate(){
        Date date = new Date();
        long times = date.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }

    public void StartServer(String campus) {
        Campus = campus;
        System.out.println(Campus);
        
        Item i1 = new Item();
        i1.name = "Kafka on the Shore";
        i1.num = 2;
        i1.ID = Campus+"1111";
        Item i2 = new Item();
        i2.name = "Norwegian Wood";
        i2.num = 1;
        i2.ID = Campus+"2222";
        Item i3 = new Item();
        i3.name = "Dance Dance Dance";
        i3.num = 5;
        i3.ID = Campus+"3333";
        items.put(i1.ID,i1);
        items.put(i2.ID,i2);
    }

    @Override
    public boolean userLogin(String studentID) throws RemoteException {
        Boolean exist = false;

        for(int i = 0; i < userClients.size(); i ++){
            if(userClients.get(i).userID.equals(studentID)){
                exist = true;
                break;
            }
        }
        if(!exist){
            User newStudent = new User();
            newStudent.userID = studentID;
            newStudent.borrowCount = 0;
            userClients.add(newStudent);
        }
        System.out.println("UserClient " + studentID + " log in successfully");    
        return true;
    }

    @Override
    public String findItem(String userID, String itemName) throws RemoteException {
        String result = "";
        result = findItemLocal(itemName);
        //System.out.println("IN ServerImpl.findItem(): " + result);
        String command = "findItem(" + itemName + ")";
        if(!result.isEmpty()) {
            String log =" User [" + userID + "] found all item named ["+itemName +"] success . Items: "+result;
            System.out.println(log);
        }else{
            String log1 =" User [" + userID + "] found all item named ["+itemName +"] failed. ";
            System.out.println(log1);
        }
        return result;
    }

    public String findItemLocal(String itemName){
        String result = "";
        int availableNum = 0;
        String itemID = "";
        synchronized(this) {
            for(HashMap.Entry<String,Item> entry : items.entrySet()){
                if(entry.getValue().name.equals(itemName)){
                    availableNum += entry.getValue().num;
                    itemID = entry.getKey();
                    result = itemID + " " + Integer.toString(availableNum);
                }
            }
        }
        return result;
    }
}
