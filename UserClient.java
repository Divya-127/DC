import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.rmi.Naming;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;


public class UserClient {

    static FileOutputStream fw;
    static Monitor m;
    static Writer w;
    public static void main(String[] args) {
        try {
            int RMIPort;
            String hostName;
            String portNum = " ";

            fw = new FileOutputStream("file.txt");
            m = new Monitor(fw);
            System.out.println("Enter userID:");
            Scanner Id = new Scanner(System.in);
            String userID = Id.nextLine();
            String campus = userID.substring(0,3);

            w = new Writer(m,userID);
            w.start();

            if(userID.length() != 8) {
                System.out.println("Invalid UserID");
                System.exit(1);
            }
            if(!userID.substring(3,4).equals("U")){
                System.out.println("Invalid UserID");
                System.exit(1);
            }
            
            URL url = new URL("http://localhost:5000/getPortNum/"+campus);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                if(output.contains(":")) {
                    portNum = output.split(":")[1].trim().replace("\"","");
                    break;
                }
            }
            conn.disconnect();

            System.out.println(getFormatDate() + " " + userID + " attempt to connect campus " + campus + " on port number " + portNum);
                        

            InputStreamReader is = new InputStreamReader(System.in);
            br = new BufferedReader(is);

            RMIPort = Integer.parseInt(portNum);
            String registryURL = "rmi://localhost:" + portNum + "/DLMS-" + campus;

            ServerInterface h = (ServerInterface)Naming.lookup(registryURL);
            System.out.println("Lookup completed " );

            if(h.userLogin(userID)){
                System.out.println("Log in successfully");
                w = new Writer(m,"Log in succesful"+userID+"\n");
                w.start();    
                Socket s = new Socket("localhost", 50555);
                DataInputStream dis = new DataInputStream(s.getInputStream());
                SimpleDateFormat time_pattern = new SimpleDateFormat("HH:mm:ss.SSSSSS");
                Date dt = new Date();
                long req_time = dt.getTime();
                long s_time = dis.readLong();
                Date server_time = new Date();
                server_time.setTime(s_time);
                Date actual_time = new Date();
                long res_time = actual_time.getTime();
                System.out.println("Time returned by server : " + time_pattern.format(server_time));
                long process_delay_latency = res_time - req_time;
                System.out.println("Process Delay Latency : " + (double)process_delay_latency/1000.0 + " seconds");
                System.out.println("Actual clock time at client side :" + time_pattern.format(actual_time));
                long client_time = s_time + process_delay_latency/2;
                dt.setTime(client_time);
                System.out.println("Synchronzed process client time : " + time_pattern.format(dt));
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                String sys_time = "" + df.format(dt);
                long error = res_time - client_time;
                System.out.println("Synchronization error : " + (double)error/1000.0 + " seconds");
                dis.close();
                s.close();
            }
            else {
                System.out.println("Log in failed");
                w = new Writer(m,"Log in Failed"+userID+"\n");
                w.start();
            }

            while(true) {
                System.out.println(" ");
                System.out.println("Please Select An Operation: ");
                System.out.println("1: FindItem");
                System.out.println("2: Borrow" + "\n");
                System.out.println("3: Exit"+"\n");
                Scanner sc = new Scanner(System.in);
                int input = sc.nextInt();

                switch (input) {
                    case 1:
                        System.out.println("Enter the ItemName");
                        Scanner input1 = new Scanner(System.in);
                        String itemName = input1.nextLine();

                        String userAction1 = " User ["+ userID + "] find item ["+itemName+"] ---> ";
                        String findResult = h.findItem(userID,itemName);
                        if(findResult.isEmpty()){
                            System.out.println(userAction1+ "Failed ");
                        }
                        else {
                            System.out.println(userAction1+ "Success. All items: " + findResult);
                        }
                        break;        
                    case 2:
                        System.out.println("Enter The ItemID");
                        Scanner inputS = new Scanner(System.in);
                        String itemID = inputS.nextLine();
                        System.out.println("Enter The NumberOfDays");
                        Scanner input2 = new Scanner(System.in);
                        int days = input2.nextInt();
                        String itemCampus = itemID.substring(0,3);
                        String userAction = " User ["+ userID + "] borrow item ["+itemID+"] for ["+days+"] days ---> ";
                        String result = h.borrowItem(itemCampus, userID, itemID, days);
                        if(!result.isEmpty()){
                            System.out.println(userAction+"Success.");
                            w = new Writer(m,userAction+"\n");
                            w.start();
                        }else{
                            System.out.println(userAction+"Failed.");
                            w = new Writer(m,userAction+"Failed\n");
                            w.start();
                            System.out.println("Do You Want To Wait In The Queue?(Y/N)");
                            Scanner input5 = new Scanner(System.in);
                            String response = input5.nextLine();
                            String userAction2 = " User ["+ userID + "] wait in queue for item ["+itemID+"] ---> ";
                            if(response.equalsIgnoreCase("y")){
                                String waitCampus = itemID.substring(0,3);
                                String waitResult = h.waitInQueue(waitCampus, userID, itemID);
                                if(waitResult.equals(" ")){
                                    System.out.println(userAction2+"Failed.");
                                }else{
                                    System.out.println(userAction2+"Success. Position In Queue :"+waitResult);
                                }
                            }
                        }
                        case 3:
                            System.exit(3);
                            break;                    
                        default:
                                break;
                }
            }
        }
        catch (Exception e) {
            System.out.println("Exception in UserClient: " + e);
        }
    }
    
    public static String getFormatDate(){
        Date date = new Date();
        long times = date.getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }
}
