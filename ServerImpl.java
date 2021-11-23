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
    HashMap<String, ArrayList<String> > waitList = new HashMap<>();
    HashMap<String, ArrayList<String> > borrowedItems = new HashMap<>();

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

    @Override
    public String borrowItem (String campusName, String userID, String itemID, int numberOfDays) {
        String result = "";
        String command = "borrowItem(" + userID + "," + itemID + "," + numberOfDays + ")";

        for(int i = 0; i < userClients.size();i ++) {
            if (userClients.get(i).userID.equals(userID)) {
                try {
                    if (campusName.equals(Campus)) {
                        result = borrowLocal(userID, itemID);
                    } else if (campusName.equals("CON")) {
                        int serverport = 2234;
                        result = UDPRequest.UDPborrowItem(command, serverport);
                    } else if (campusName.equals("MCG")) {
                        int serverport = 2235;
                        result = UDPRequest.UDPborrowItem(command, serverport);
                    } else if (campusName.equals("MON")) {
                        int serverport = 2236;
                        result = UDPRequest.UDPborrowItem(command, serverport);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(!campusName.equals(Campus)) {
                    if (result.isEmpty()) {
                        String log = " Server borrow item ["+itemID+"] for user ["+userID+"] from server ["+campusName+"] failed";
                        System.out.println(log);
                        try {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String log2 = " Server borrow item ["+itemID+"] for user ["+userID+"] from server ["+campusName+"] success";
                        System.out.println(log2);
                        try {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
        return result;
    }

    @Override
    public String waitInQueue(String campusName, String userID, String itemID) {
        String result = " ";
        String command = "waitInQueue(" + userID + "," + itemID + ")";
        for (User temp : userClients) {
            if (temp.userID.equals(userID)) {
                try {
                    if (campusName.equals(Campus)) {
                        result = waitInLocal(userID, itemID);
                    } else if (campusName.equals("CON")) {
                        int serverport = 2234;
                        result = UDPRequest.UDPwaitInQueue(command, serverport);
                    } else if (campusName.equals("MCG")) {
                        int serverport = 2235;
                        result = UDPRequest.UDPwaitInQueue(command, serverport);
                    } else if (campusName.equals("MON")) {
                        int serverport = 2236;
                        result = UDPRequest.UDPwaitInQueue(command, serverport);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public String borrowLocal(String userID, String itemID){
        String result = "";
        String failReason = "";
        int flag = 0;
        String userCampus = userID.substring(0,3);
        synchronized (this){
            if(!userCampus.equals(Campus)){
                for(HashMap.Entry<String, ArrayList<String>> entry : borrowedItems.entrySet()){
                    if(entry.getValue().contains(userID)){
                        flag = 1;
                        failReason = "User can only borrow 1 item from other libraries";
                    }
                }
            }
            if(flag == 0) {
                if (items.get(itemID).num > 0) {
                    if (!borrowedItems.containsKey(itemID)) {
                        ArrayList<String> newBorrowedUser = new ArrayList<>();
                        newBorrowedUser.add(userID);
                        borrowedItems.put(itemID, newBorrowedUser);
                        items.get(itemID).num--;
                    } else {
                        if (!borrowedItems.get(itemID).contains(userID)) {
                            borrowedItems.get(itemID).add(userID);
                            items.get(itemID).num--;
                        }
                    }
                    result = itemID;
                }else{
                    failReason = "No item left";
                }
            }
            if (result.isEmpty()) {
                String log = " User [" + userID + "] borrow item ["+itemID+"] failed: ";
                System.out.println(log);
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String log2 = " User [" + userID + "] borrow item ["+itemID+"] success.";
                System.out.println(log2);
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public String waitInLocal(String userID, String itemID){
        String result = " ";
        synchronized (this) {

            if(!waitList.containsKey(itemID)){
                ArrayList<String> users = new ArrayList<>();
                users.add(userID);
                waitList.put(itemID,users);
                result = String.valueOf(waitList.get(itemID).indexOf(userID)+1);
            }else{
                if(!waitList.get(itemID).contains(userID)){
                    waitList.get(itemID).add(userID);
                    result = String.valueOf(waitList.get(itemID).indexOf(userID)+1);
                }
            }

            if (result.equals(" ")) {
                String log = " Server add user [" + userID + "] in wait queue of item ["+itemID+ "] failed.";
                System.out.println(log);
                try {
                   } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                String log1 = " Server add user [" + userID + "] in wait queue of " +
                        "item ["+itemID+ "] at position [" +result+"] success.";
                System.out.println(log1);
                try {
                   } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }
}
