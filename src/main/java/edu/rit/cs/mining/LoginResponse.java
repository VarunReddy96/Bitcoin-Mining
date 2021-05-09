package edu.rit.cs.mining;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to represent the message sent for the login request from the Miner.
 * This contains the Blockchain, minerslist in the network, and the miners UID.
 *
 */

public class LoginResponse implements Serializable {

    public Blockchain blkChain;
    public String minersId;
    public ConcurrentHashMap<String, IpNode> minerList;

    public LoginResponse(Blockchain b, String id, ConcurrentHashMap<String, IpNode> list){
        this.blkChain = b;
        this.minerList = list;
        this.minersId = id;
    }

    public Blockchain getBlkChain() {
        return blkChain;
    }

    public void setBlkChain(Blockchain blkChain) {
        this.blkChain = blkChain;
    }

    public String getMinersId() {
        return minersId;
    }

    public void setMinersId(String minersId) {
        this.minersId = minersId;
    }

    public ConcurrentHashMap<String, IpNode> getMinerList() {
        return minerList;
    }

    public void setMinerList(ConcurrentHashMap<String, IpNode> minerList) {
        this.minerList = minerList;
    }
}
