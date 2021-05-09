package edu.rit.cs.mining.miner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import com.thetransactioncompany.jsonrpc2.server.*;
import edu.rit.cs.mining.Block;
import edu.rit.cs.mining.IpNode;

import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This Class is used to handle the Json requests and depending on the requests type various inner
 * classes are invoked and appropriate responses are sent back as required.
 *
 */

public class MinerJsonRpcRequestsHandler {


    /**
     * This Class is used to handle the Update_Blockchain request from other miners in the network.
     *
     */
    public static class UpdateChain implements RequestHandler {

        public Miner miner;
        public Socket socket;

        public UpdateChain(Miner re, Socket s) {
            this.miner = re;
            this.socket = s;
        }

        public String[] handledRequests() {

            return new String[]{"Update_Blockchain"};
        }
        /**
         * This is the method used to process the requests and send the responses back.
         *
         * @param req: The request received from other miner
         *
         */

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

            if (req.getMethod().equals("Update_Blockchain")) {

                Map<String, Object> myParams = req.getNamedParams();
                Gson gson = new Gson();
                //String mac_id = gson.fromJson(myParams.get("Hello").toString(), String.class);
                int bheight = gson.fromJson(myParams.get("blockheight").toString(), Integer.class);
                System.out.println("Received blockheight from other miner is: "+bheight + " CUrrent miner chain height: "
                +this.miner.blockchain.currentHeadBlock.blockHeight);
                Block resblock = null;
                ArrayList<Block> newblocks = new ArrayList<>();
                try {
                    if (bheight < this.miner.blockchain.currentHeadBlock.blockHeight) {

                        Block temp = this.miner.blockchain.currentHeadBlock;
                        //Block res = null;

                        while(temp!=null && temp.blockHeight >= bheight){
                            //res = temp;
                            newblocks.add(temp);
                            temp = temp.prevBlock;
                        }
//                        Type responsetype = new TypeToken<ArrayList<Block>>() {}.getType();
//                        String testblocks = gson.toJson(newblocks,responsetype);
                        System.out.println("Sending Response back to Miner from blockheight in Update BlockchainHandler: "
                                +this.miner.blockchain.currentHeadBlock.blockHeight);
                        //return new JSONRPC2Response(testblocks, req.getID());

                    } else {
                        newblocks.add(this.miner.blockchain.firstBlock);
//                        Type responsetype = new TypeToken<ArrayList<Block>>() {}.getType();
//                        String testblocks = gson.toJson(newblocks,responsetype);
                        System.out.println("Returning first created block because received height is less in Update BlockchainHandler:!!!!");
                        //return new JSONRPC2Response(testblocks, req.getID());
                    }
                    Type responsetype = new TypeToken<ArrayList<Block>>() {}.getType();
                    String testblocks = gson.toJson(newblocks,responsetype);
                    return new JSONRPC2Response(testblocks, req.getID());
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }
    }

    /**
     * This Class is used to handle the New_Block request from other miners in the network.
     *
     */

    public static class AddNewblock implements RequestHandler {

        public Miner miner;
        public Socket socket;

        public AddNewblock(Miner re, Socket s) {
            this.miner = re;
            this.socket = s;
        }

        public String[] handledRequests() {

            return new String[]{"New_Block"};
        }

        /**
         * This is the method used to process the requests and send the responses back.
         *
         * @param req: The request received from other miner
         *
         */


        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

            if (req.getMethod().equals("New_Block")) {
                Map<String, Object> myParams = req.getNamedParams();
                Gson gson = new Gson();
                //String mac_id = gson.fromJson(myParams.get("Hello").toString(), String.class);
                Type responsetype = new TypeToken<ArrayList<Block>>() {
                }.getType();
                ArrayList<Block> newblocks = gson.fromJson(myParams.get("Block").toString(), responsetype);
                synchronized (this.miner.o){
                int addblk = this.miner.blockchain.addNewBlock(newblocks);
                if(addblk==1){
                    this.miner.updateBlockchain(1);
                }

                    //this.miner.isMiningLatestBlock = true;

                    this.miner.isNewBlockFound = true; //finding a new block.
                    System.out.println("Updated isMining New Block Boolean");
                }
                Object input = "Sucessfully updated";
                return new JSONRPC2Response(input, req.getID());


            }
            return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }
    }

    /**
     * This Class is used to handle the Update_MinerList request from the Registry which sends every 5 secs.
     *
     */

    public static class UpdateMinersList implements NotificationHandler {

        public Miner miner;
        public Socket socket;

        public UpdateMinersList(Miner re, Socket s) {
            this.miner = re;
            this.socket = s;
        }

        public String[] handledNotifications() {

            return new String[]{"Update_MinerList"};
        }

        /**
         * This is the method used to process the requests and send the responses back.
         *
         * @param notification: The request received from other miner
         *
         */

        public void process(JSONRPC2Notification notification, MessageContext ctx) {

            if (notification.getMethod().equals("Update_MinerList")) {
                System.out.println("Received updated miners list");
//                System.out.println("Uodating miners list");
                Map<String, Object> myParams = notification.getNamedParams();
                Gson gson = new Gson();
                //String mac_id = gson.fromJson(myParams.get("Hello").toString(), String.class);
                Type responsetype = new TypeToken<ConcurrentHashMap<String, IpNode>>() {
                }.getType();
                //System.out.println("Cretaed the reposne type for threceived list");
                this.miner.minersList = gson.fromJson(myParams.get("minerslist").toString(), responsetype);

                //this.miner.minersList = (ConcurrentHashMap<String, IpNode>) myParams.get("minerslist");

                System.out.println("Updated Miners list: "+this.miner.minersList);
                String input = "Sucessfully updated";
                //return new JSONRPC2Response("Sucessfully updated", req.getID()); // not required can optimize by not sending anything back


            }
            //return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
        }
    }

    }
