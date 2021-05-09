package edu.rit.cs.mining;

public class Config {
    public static int LOGINID_SIZE = 8; // the size UID of the miners.
    public static String INITIAL_TARGET = "000000ffff000000000000000000000000000000000000000000000000";
    public static int nTargetTimespan = 20*60; // 20 mins to change the difficulty
    public static int nTargetSpacing = 2*60;// 2 mins to mine a block
    public static long ECHO_TIMEOUT = 30000; // 30 secs. // If a miner doesnt ping in 30 secs it is assumed miner left the network
    public static long LoginResponseTimeout = 15000; // 15 secs for a miner to connect to the registry.
    public static int REGISTRY_PORT = 10001; // This is the port on which Registry JsonRpcRequestsListener listens
    public static int MINER_PORT_REQUESTS = 11001; // This is the port in miner on which AllJsonRequestHandlerListener listens
    public static int MINER_PORT_NOTIFICATIONS = 9001; // This is the port in miner on which JsonNotificationListener listens
    public static int MERGEBLOCKCHAIN_OFFSET = 3; // If the currenthead of blockchain is not equal to the sent ArrayList<Block>
    // last element then query other Miners for updated blkchain by setting a offset
    public static int MINER_Heartbeat_LISTNER_PORT = 6001;
    public static long MINER_Heartbeat_INTERVEL = 5000; // 5 secs
    public static int JSONRPC2Session_TIMEOUT = 500;//0.5 secs

    public static long MINER_LIST_UPDATE_TIMEOUT = 5000; // 5 secs.
    // dispatcher request id's for JSONRPC
    public static int Heartbeat_REQUESTID = 102;
    public static int Update_Blockchain_REQUESTID = 10000;
    public static int gettestresult_REQUESTID = 100;
    public static int New_Block_REQUESTID = 1001;
    public static int Update_MinerList_REQUESTID = 9070;

    public static int Login_REQUESTID = 103;

}
