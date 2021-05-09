package edu.rit.cs.mining.miner;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import edu.rit.cs.mining.Config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This Class is used to send heartbeat to the Registry to let the Registry know that the miner is active.
 *
 *
 */


public class Heartbeat extends Thread {

    public Miner miner;
    public URL serverURL;

    public Heartbeat(URL u, Miner m){
        this.miner = m;
        this.serverURL = u;
    }
    // make this udp.

    /**
     * This method sends the heartbeat every 5secs to the Registry.
     *
     */

    public void run(){
        while(true) {

            try{
            URL serverURL = new URL("http://" + this.miner.serverIPaddress + ":" + Config.MINER_Heartbeat_LISTNER_PORT);
            JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
            mySession.getOptions().setConnectTimeout(Config.JSONRPC2Session_TIMEOUT);
            mySession.getOptions().setReadTimeout(Config.JSONRPC2Session_TIMEOUT);
            String method = "heartbeat";
            int requestID = Config.Heartbeat_REQUESTID;
            Map<String, Object> myParams = new HashMap<>();
            myParams.put("minerid", this.miner.minerId);
            //System.out.println(myParams + "The value if myparams in pubsubaget advertise");
            JSONRPC2Notification request = new JSONRPC2Notification(method,myParams);
            //request.setNamedParams(myParams);
//                    JSONRPC2Response response = null;
            try {
                mySession.send(request);
                //response = mySession.send(request);

            }catch (JSONRPC2SessionException e) {

                //System.out.println("Error here at Sending HeartBeat No notifications received!! at request bind");
                //e.printStackTrace();
                //System.err.println(e.getMessage());
                //System.out.println("Error in Heart beat Jsonrpc");
                try{
                    Thread.sleep(Config.MINER_Heartbeat_INTERVEL);
                }catch (Exception e1){
                    System.out.print("Thread sleep error in HeartBeat Class");
                    e1.printStackTrace();
                }
                continue;
                //System.exit(-2);
                // handle exception...
            }


//            // Print response result / error
//            if (response.indicatesSuccess()) {
//                System.out.println(response.getResult());
//            } else {
//                System.out.println("Error here!! ALiveEchoSender JSONRPC");
//                System.out.println(response.getError().getMessage());
//
//            }

            try{
                Thread.sleep(Config.MINER_Heartbeat_INTERVEL);
            }catch (Exception e){
                System.out.print("Thread sleep error in HeartBeat Class");
                e.printStackTrace();
            }
        }catch (Exception e2){
                System.out.println("Exception in creating URl in HearBeat Class");
                System.exit(-11);
            }
        }
    }
}
