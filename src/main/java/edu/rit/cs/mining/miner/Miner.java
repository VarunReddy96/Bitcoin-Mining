package edu.rit.cs.mining.miner;

import com.google.gson.Gson;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
import edu.rit.cs.mining.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This Class implements the various functionalities of the Miner.
 */


public class Miner {
    public URL serverURL = null;
    public String serverIPaddress;
    public String minerId;
    public InetAddress ip;
    public ConcurrentHashMap<String, IpNode> minersList;
    public Blockchain blockchain;
    public Block currentBlock;
    public boolean check;
    public boolean isMiningLatestBlock;
    public boolean isNewBlockFound;
    public final Object o = new Object();
    public boolean isMining = true;


    public URL getServerURL() {
        return serverURL;
    }

    public void setServerURL(URL serverURL) {
        this.serverURL = serverURL;
    }

    public String getMinerId() {
        return minerId;
    }

    public void setMinerId(String minerId) {
        this.minerId = minerId;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public boolean isMiningLatestBlock() {
        return isMiningLatestBlock;
    }

    public void setMiningLatestBlock(boolean miningLatestBlock) {
        isMiningLatestBlock = miningLatestBlock;
    }

    /**
     * This constructor is used to initalize the Miner by first requesting a login UID from the Registry and then updating its
     * Blockchain by requesting other miners.
     *
     * @param args: Contains the Registry URL.
     */


    public Miner(String[] args) {
        try {

            serverURL = new URL("http://" + args[0] + ":" + Config.REGISTRY_PORT);
            serverIPaddress = args[0];
            ip = InetAddress.getLocalHost();
            System.out.println("ip: "+ip.getHostAddress());
            //System.out.println("Checking network Interfaces");
            //ipResolution();
            String ipaddress = ("" + ip).split("/")[1];
            //System.out.println("Split ip string is: "+ipaddress);
            ip = InetAddress.getByName(ipaddress);
            //ip = getFirstNonLoopbackAddress(true,false);
            LoginResponse lr = null;
            long timeout = System.currentTimeMillis() + Config.LoginResponseTimeout; // timeout for login registry
            while (lr == null && System.currentTimeMillis() <= timeout) {
                lr = login();
            }
            new MinerJsonRpcRequestsListener(this).start();
            new MinerJsonRpcNotificationListener(this).start();

            if (lr != null) {
                this.minerId = lr.minersId;
                this.minersList = lr.minerList;
                this.blockchain = lr.blkChain;
                new Heartbeat(serverURL, this).start();

                System.out.println("Updating Blockchain---------------------");
                updateBlockchain(0);
                System.out.println("Updating Blockchain Done---------------------");

                isMiningLatestBlock = true;
                isNewBlockFound = false;
                System.out.println("mineris: " + this.minerId + " minerlist: " + this.minersList + " Success");
                //isMining = true;
                new MiningStart(this).start();
                //gettestresults();
                //sendHello();
            } else {
                System.out.println("LoginResponse from te registry is null!! Please retry");
                System.exit(1);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to get the initial information like Blockchain, Miner UID, minerslist in the network from the Registry
     * as a LoginResponse Class.
     */

    public LoginResponse login() {
        JSONRPC2Session mySession = new JSONRPC2Session(serverURL);

        String method = "Login";
        int requestID = Config.Login_REQUESTID;
        Map<String, Object> myParams = new HashMap<>();
        String mineridvalue = checkLoginFile("login.txt");
        System.out.println("Checking for login file is present: " + mineridvalue);
        if (mineridvalue != null) {
            myParams.put("isIdavailable", true);
            myParams.put("minerid", mineridvalue);
        } else {
            myParams.put("isIdavailable", false);
        }
        //System.out.println(myParams + "The value if myparams in pubsubaget advertise");
        JSONRPC2Request request = new JSONRPC2Request(method, requestID);
        request.setNamedParams(myParams);
        JSONRPC2Response response = null;

        try {
            response = mySession.send(request);

        } catch (JSONRPC2SessionException e) {

            System.out.println("Error here!! at request bind");
            e.printStackTrace();
            System.err.println(e.getMessage());
            // handle exception...
        }
        LoginResponse logres;

        // Print response result / error
        if (response.indicatesSuccess()) {
            Gson gson = new Gson();
            System.out.println("After response from registry : " + response.getResult());
            logres = gson.fromJson(response.getResult().toString(), LoginResponse.class);

            //System.out.println("Just the blokchain: "+res.get("loginresponse"));

            System.out.println("Is loginres null " + (logres == null) + " **************");
            //logres = (LoginResponse) response.getResult();
            if (mineridvalue == null) {
                createLoginFile(logres);
            }
            //System.out.println(response.getResult());
        } else {
            logres = null;
            System.out.println("Error here!! at response! but in else statement");
            System.out.println(response.getError().getMessage());

        }
        return logres;
    }

    /**
     * This method is used to update the Blockchain during the initial connection to the registry and also when forking takes place.
     * If forking happened blockchainMerge method returns 1 showing that blockchain needs to be updated. Offset is further decreased by 3 and
     * blockchain is updated. This process continues until a consensus on the Blockchain is received. Worst case the offset can go upto
     * (currentBlockHeight - 1). the first Block will be the same. Upon receiving all the blockchains from various miners choose the longest list
     * or the maximum blocks sent.
     *
     * @param offset: The offset from the current head of Blockchain.
     */

    public void updateBlockchain(int offset) {
        synchronized (o) {
            isMiningLatestBlock = false;
            ExecutorService executor = Executors.newCachedThreadPool();
            ArrayList<Future<ArrayList<Block>>> blkchains = new ArrayList<>();
            int mergeblk = 1;
            int temp = this.blockchain.currentHeadBlock.blockHeight - offset;
            while (temp < this.blockchain.currentHeadBlock.blockHeight) {
                try {
                    for (String s : minersList.keySet()) {
                        if (!s.equals(this.minerId)) {
                            System.out.println("Sending to miner with URL: "+("http:/" + minersList.get(s).getIp() + ":" + Config.MINER_PORT_REQUESTS
                                    +" Miner id: "+s));
                            //URL neighbor = new URL("http://" + minersList.get(s).getIp().getHostName() + ":" + Config.MINER_PORT_REQUESTS);
                            URL neighbor = new URL("http:/" + minersList.get(s).getIp()+ ":" + Config.MINER_PORT_REQUESTS);
                            blkchains.add(executor.submit(new BlockchainUpdate(neighbor, this, temp)));
                        }
                    }
                    int ans = Integer.MIN_VALUE;
                    ArrayList<Block> maxblkchain = new ArrayList<>();

                    for (int i = 0; i < blkchains.size(); i++) { // Forking all the blkchains to get the maximum length blkchain
                        if (blkchains.get(i).get()!=null && blkchains.get(i).get().size() > ans) {
                            maxblkchain = blkchains.get(i).get();
                            ans = blkchains.get(i).get().size();
                        }
                    }

                    if (maxblkchain.size() > 0) {
                        mergeblk = this.blockchain.blockchainMerge(maxblkchain, temp); // This condition is used to verify if only 1 miner is present in the
                    } else {
                        mergeblk = 0;
                    }
                    if (mergeblk != 1) {
                        break;
                    }
                    temp = temp + Config.MERGEBLOCKCHAIN_OFFSET;


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            isMiningLatestBlock = true;
        }
    }

    public void storeBlockchainInFile() {
        try {
            File currentDirFile = new File(".");
            FileOutputStream fileOut = new FileOutputStream(currentDirFile.getCanonicalPath() + "/Ledger.dat");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            synchronized (o){
            objectOut.writeObject(this.blockchain);
            }
            objectOut.close();
            System.out.println("The Object  was successfully written to a file");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in creating Blockchain File");
        }

    }

    /**
     * This method creates a Login.txt file if unavailable with the Miner.
     *
     * @param lr: The LoginResponse received from the Registry.
     */

    public void createLoginFile(LoginResponse lr) {
        System.out.println("Checking null");
        try {
            File currentDirFile = new File(".");
            String helper = currentDirFile.getAbsolutePath();
            String currentDir = helper.substring(0, helper.length() - currentDirFile.getCanonicalPath().length());//this line may need a try-catch block
            System.out.println("In creating login file canocical filepath: " + currentDirFile.getCanonicalPath());
            System.out.println("In creating login file absolute filepath: " + currentDirFile.getAbsolutePath());
            File myObj = new File(currentDirFile.getCanonicalPath() + "/login.txt");
            myObj.createNewFile();
            FileWriter fw = new FileWriter(myObj);
            //BufferedWriter writer give better performance
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(lr.minersId);
            //Closing BufferedWriter Stream
            bw.close();

        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * This method is used to check if the login.txt file is already available with the miner.
     *
     * @param filename: login.txt file is checked.
     */

    public String checkLoginFile(String filename) {

        System.out.println("Entering checkloginfile");
        try {
            File currentDirFile = new File(".");
            File myObj = new File(currentDirFile.getCanonicalFile() + "/" + filename);

            if (myObj.exists()) {
                System.out.println("File absolute path " + myObj.getAbsolutePath());

                BufferedReader br = new BufferedReader(new FileReader(myObj));
                StringBuilder id = new StringBuilder();
                String st;
                while ((st = br.readLine()) != null) {
                    id.append(st);
                }
                br.close();
                System.out.println("Returning after file found**** " + id.toString());
                return id.toString();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("In checking login.tx!FIle not found returning null");
        return null;
    }

    public static void main(String[] args) {
        Miner miner = new Miner(args);
        //new MiningStart(miner).start();

//        Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
//            miner.isMining = false;
//            miner.storeBlockchainInFile();
//            System.exit(0);
//        }});
        Scanner scan = new Scanner(System.in);
        System.out.println("Press q and enter to exit and store the Blockchain");
        if(scan.hasNext()){
            miner.isMining = false;
            miner.storeBlockchainInFile();
            System.exit(0);
        }
        //new Miner(args).startMining();
        //new Miner(args);
    }
}
