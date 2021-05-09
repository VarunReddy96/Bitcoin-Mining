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
import java.util.concurrent.Callable;

// name: GetUpdatedblockchain

/**
 * This Class is used to send requests for blocks to update blockchain from certain offset and receive the specified blocks from all the miners in the network.
 *
 */


public class BlockchainUpdate implements Callable<ArrayList<Block>> {

    public URL neighborurl;
    public Miner miner;
    public int offset;
    public BlockchainUpdate(URL u, Miner m, int os){
        this.neighborurl = u;
        this.miner = m;
        this.offset = os;
    }

    /**
     * This method is used to send requests for other miners in the network for the specified blocks.
     *
     */


    public ArrayList<Block> call(){
        JSONRPC2Session mySession = new JSONRPC2Session(this.neighborurl);
//        mySession.getOptions().setConnectTimeout(Config.JSONRPC2Session_TIMEOUT);
//        mySession.getOptions().setReadTimeout(Config.JSONRPC2Session_TIMEOUT);
        System.out.println("Neighbor URL for blkchain update: "+this.neighborurl);
        String method = "Update_Blockchain";
        int requestID = Config.Update_Blockchain_REQUESTID;
        Map<String, Object> myParams = new HashMap<>();
        myParams.put("blockheight", Math.max(this.miner.blockchain.currentHeadBlock.blockHeight -offset,1));
        //System.out.println(myParams + "The value if myparams in pubsubagent advertise");
        JSONRPC2Request request = new JSONRPC2Request(method, requestID);
        request.setNamedParams(myParams);
        JSONRPC2Response response = null;

        try {
            response = mySession.send(request);

        } catch (JSONRPC2SessionException e) {

            System.out.println("Error here at sending request to Miner in Blockchain Update!! at request bind");
            //e.printStackTrace();
            //System.err.println(e.getMessage());
            return null;
            //System.exit(-10);
            // handle exception...
        }

        // Print response result / error
        if (response.indicatesSuccess()) {
            Gson gson = new Gson();
            Type responsetype = new TypeToken<ArrayList<Block>>() {
            }.getType();
            ArrayList<Block> newblocks = gson.fromJson(response.getResult().toString(), responsetype);
            //this.miner.blockchain.mergeBlockchain(newblocks); // check this line!!!!!!!!!!!!!!!!!!
            return newblocks;
        } else {
            System.out.println("Error here!! at response! but in else statement");
            System.out.println(response.getError().getMessage());
            System.out.println("Error in receiving blockchain Update");
            System.exit(-1);

        }
        return null;
    }
}
