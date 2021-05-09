package edu.rit.cs.mining.registry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import edu.rit.cs.mining.Config;
import edu.rit.cs.mining.IpNode;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// UpdateMinerListNotifier

/**
 * This class is used to send the minerslist every 5 secs to all the miners in the network.
 *
 */
public class MinerListUpdates extends Thread{

    public Registry reg;

    public MinerListUpdates(Registry re) {
        this.reg = re;
    }

    public void run() {
        while(true) {
            try {
                System.out.println("Sending Updated Miners List");
                for (String s : this.reg.miners.keySet()) {

                    System.out.println("Sending to URL: "+("http:/" + this.reg.miners.get(s).getIp()+ ":" + Config.MINER_PORT_NOTIFICATIONS));
                    //URL neighbor = new URL("http://" + this.reg.miners.get(s).getIp().getHostName() + ":" + Config.MINER_PORT_NOTIFICATIONS);
                    URL neighbor = new URL("http:/" + this.reg.miners.get(s).getIp()+ ":" + Config.MINER_PORT_NOTIFICATIONS);
                    JSONRPC2Session mySession = new JSONRPC2Session(neighbor);
                    mySession.getOptions().setConnectTimeout(150);
                    mySession.getOptions().setReadTimeout(150);

                    String method = "Update_MinerList";
                    int requestID = Config.Update_MinerList_REQUESTID;
                    Map<String, Object> myParams = new HashMap<>();
                    Gson gson = new Gson();
                    Type responsetype = new TypeToken<ConcurrentHashMap<String, IpNode>>() {
                    }.getType();
                    String minerListInJsonString = gson.toJson(this.reg.miners,responsetype);
                    myParams.put("minerslist", minerListInJsonString);
                    //System.out.println(myParams + "The value if myparams in pubsubaget advertise");
                    System.out.println("Update MinerList request sent to the miner");
                    JSONRPC2Notification request = new JSONRPC2Notification(method,myParams);
                    //request.setNamedParams(myParams);
//                    JSONRPC2Response response = null;
                    try {
                        mySession.send(request);
                        //response = mySession.send(request);

                    } catch (JSONRPC2SessionException e) {


                        //e.printStackTrace();
                        //System.err.println(e.getMessage());
                        //System.out.println("Error here at sending Updated Miners List!! at request bind");
                        //System.out.println("Timelimit exceeded");
                        //Thread.sleep(Config.MINER_LIST_UPDATE_TIMEOUT);
                        continue;
                        //System.exit(-8);
                        // handle exception...
                    }

                    //System.out.println("Response received from the miner.........");

                    // Print response result / error
//                    if (response.indicatesSuccess()) {
//                        System.out.println("Minerslist sucessfully sent");
//                    } else {
//
//                        System.out.println(response.getError().getMessage());
//                        System.out.println("Minerslist not sucessfulyy sent in MinerUpdates!!!!!");
//                        System.exit(-9);
//
//                    }

                }
                System.out.println("Miner Updates going to sleep");
                Thread.sleep(Config.MINER_LIST_UPDATE_TIMEOUT); // Send updated minerlist every 5 secs to miners
            } catch (Exception e) {
                System.out.println("Exception happened at MinerListUpdates");
                e.printStackTrace();
            }
        }


    }
}
