package edu.rit.cs.mining.registry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.NotificationHandler;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import edu.rit.cs.mining.Block;
import edu.rit.cs.mining.IpNode;
import edu.rit.cs.mining.LoginResponse;
import edu.rit.cs.mining.Config;
import org.apache.commons.lang3.RandomStringUtils;

import java.lang.reflect.Type;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * This Class is used to handle the Json requests and depending on the requests type various inner
 * classes are invoked and appropriate responses are sent back as required.
 *
 */

// This is a JSONRequesthandler.

public class JsonRpcRequestsHandler {

    /**
     * This Class is used to handle the Login request from other miners in the network.
     *
     */

    public static class Login implements RequestHandler {

        public Registry reg;
        public Socket socket;

        public Login(Registry re, Socket s) {
            this.reg = re;
            this.socket = s;
        }

        public String[] handledRequests() {

            return new String[]{"Login"};
        }


        public String generateId() {

            String shortId = RandomStringUtils.random(Config.LOGINID_SIZE, "0123456789abcdef"); // hex value of size 8
            while (this.reg.miners.containsKey(shortId)) {
                shortId = RandomStringUtils.random(Config.LOGINID_SIZE, "0123456789abcdef");
            }
            return shortId;
        }

        /**
         * This is the method used to process the requests and send the responses back.
         *
         * @param req: The request received from other miner
         *
         */

        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

            if (req.getMethod().equals("Login")) {
                Map<String, Object> myParams = req.getNamedParams();
                Gson gson = new Gson();

                InetAddress mineripaddress = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress();
                String remoteHostName = mineripaddress.getHostName();
                IpNode minerip = new IpNode(mineripaddress, System.currentTimeMillis());
                boolean isidavailable = gson.fromJson(myParams.get("isIdavailable").toString(), boolean.class);
                LoginResponse lr;
                String minerid;
                if (!isidavailable) {
                    //MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                    System.out.println("Generating login id");
                    minerid = remoteHostName + "-"+generateId();

                } else {
                    minerid = gson.fromJson(myParams.get("minerid").toString(), String.class);

                }
                synchronized (this.reg.o) {
                    this.reg.minercount++; // dont overflow use timestamp to generate hash
                    // Id generated based on timestamps
                    this.reg.timechecker.put(this.reg.minercount, System.currentTimeMillis());
                }
                System.out.println("minerid: "+minerid + "minerip node time: "+minerip.getTime() + "minerip node ipadd: "
                +minerip.getIp());
                this.reg.miners.put(minerid, minerip);
                lr = new LoginResponse(this.reg.blkchain, minerid, this.reg.miners);

                String loginjson = gson.toJson(lr);
                return new JSONRPC2Response(loginjson, req.getID());

            } else {

                // Method name not supported

                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }

    /**
     * This Class is used to handle the Heartbeat request from other miners in the network.
     *
     */


    public static class HeartbeatAnalyzer implements NotificationHandler {

        public Registry reg;
        public Socket socket;

        public HeartbeatAnalyzer(Registry re, Socket s) {
            this.reg = re;
            this.socket = s;
        }

        public String[] handledNotifications() {

            return new String[]{"heartbeat"};
        }

        /**
         * This is the method used to process the requests and send the responses back.
         *
         * @param notification: The notification received from the miner.
         *
         */


        public void process(JSONRPC2Notification notification, MessageContext ctx) {

            if (notification.getMethod().equals("heartbeat")) {
                Map<String, Object> myParams = notification.getNamedParams();
                Gson gson = new Gson();
                //String mac_id = gson.fromJson(myParams.get("Hello").toString(), String.class);
                String idminer = gson.fromJson(myParams.get("minerid").toString(), String.class);

                if (this.reg.miners.containsKey(idminer)) {
                    System.out.println("Received heartbeat from "+this.reg.miners.get(idminer).ip);
                    this.reg.miners.get(idminer).setTime(System.currentTimeMillis());
                    this.reg.miners.put(idminer, this.reg.miners.get(idminer));
                } else {
                    System.out.println("Getting weird pings in timeecho");
                    //new JSONRPC2Response("Getting weird pings in timeecho",notification.getID());
                }


                //return new JSONRPC2Response("Echo received", req.getID());

            }
        }
    }

    /**
     * This Class is used for internal testing and can be ignored by the students.
     *
     */



    public static class Testing implements RequestHandler {

        public Registry reg;
        public Socket socket;

        public Testing(Registry re, Socket s) {
            this.reg = re;
            this.socket = s;
        }

        public String[] handledRequests() {

            return new String[]{"gettestresult"};
        }


        public JSONRPC2Response process(JSONRPC2Request req, MessageContext ctx) {

            if (req.getMethod().equals("gettestresult")) {
                Map<String, Object> myParams = req.getNamedParams();
                Gson gson = new Gson();
                //String mac_id = gson.fromJson(myParams.get("Hello").toString(), String.class);

                Block b1 = new Block(this.reg.blkchain.firstBlock,System.currentTimeMillis(),null,"YoloVarun"
                ,5,"Targetyolo",2);
                Block b2 = new Block(b1,System.currentTimeMillis(),null,"YoloBitcoin"
                        ,5,"yoloTarget2",3);
                ArrayList<Block> ans = new ArrayList<>();
                ans.add(b2);
                ans.add(b1);
                ans.add(this.reg.blkchain.firstBlock);
                Type responsetype = new TypeToken<ArrayList<Block>>() {}.getType();
                String testblocks = gson.toJson(ans,responsetype);
                return new JSONRPC2Response(testblocks, req.getID());

            } else {

                // Method name not supported

                return new JSONRPC2Response(JSONRPC2Error.METHOD_NOT_FOUND, req.getID());
            }
        }
    }
}
