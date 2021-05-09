package edu.rit.cs.mining.miner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import edu.rit.cs.mining.Block;
import edu.rit.cs.mining.Config;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * This Class is used to send the newly mined block to other miners in the network.
 *
 */
public class PublishBlock extends Thread {

    public Miner miner;

    public PublishBlock(Miner m){
        this.miner = m;
    }


    public void run(){
        try {
            for (String s : this.miner.minersList.keySet()) {
                if(!s.equals(this.miner.minerId)) {
                    System.out.println("Sending new block to : "+"http:/" + this.miner.minersList.get(s).getIp() + ":" + Config.MINER_PORT_REQUESTS);
                    //"http:/" + minersList.get(s).getIp()+ ":"
                    //URL neighbor = new URL("http://" + this.miner.minersList.get(s).getIp().getHostName() + ":" + Config.MINER_PORT_REQUESTS);
                    URL neighbor = new URL("http:/" + this.miner.minersList.get(s).getIp()+ ":"+ Config.MINER_PORT_REQUESTS);
                    JSONRPC2Session mySession = new JSONRPC2Session(neighbor);
                    mySession.getOptions().setConnectTimeout(Config.JSONRPC2Session_TIMEOUT);
                    mySession.getOptions().setReadTimeout(Config.JSONRPC2Session_TIMEOUT);

                    String method = "New_Block";
                    Gson gson = new Gson();
                    int requestID = Config.New_Block_REQUESTID;
                    Map<String, Object> myParams = new HashMap<>();
                    ArrayList<Block> newblocks = new ArrayList<>();
                    newblocks.add(this.miner.blockchain.currentHeadBlock);

                    Type responsetype = new TypeToken<ArrayList<Block>>() {}.getType();
                    String testblocks = gson.toJson(newblocks,responsetype);
                    myParams.put("Block", testblocks);
                    //System.out.println(myParams + "The value if myparams in pubsubaget advertise");
                    JSONRPC2Request request = new JSONRPC2Request(method, requestID);
                    request.setNamedParams(myParams);
                    JSONRPC2Response response = null;
                    System.out.println("Sending new block to the above url miner............................................");
                    int attemptsCounter = 0;
                    //while(attemptsCounter < 15){
                    try {
                        response = mySession.send(request);
                        System.out.println("Sent new block");


                    } catch (JSONRPC2SessionException e) {

                        System.out.println("Error here at Publish Blocks!! at request bind");
                        continue;
                        // handle exception...
                    }


                    // Print response result / error
                    if (response.indicatesSuccess()) {
                        System.out.println("Sent the new Block succesfully");
                    } else {
                        System.out.println("Error here!! at response! but in else statement");
                        System.out.println(response.getError().getMessage());

                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
