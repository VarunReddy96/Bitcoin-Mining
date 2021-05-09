package edu.rit.cs.mining;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class is used to define the Block data structure and store all the relavent information of a Block.
 *
 */
public class Block implements Cloneable, Serializable {


    public Block prevBlock;
    public long timestamp;

    public Block nextBlock;
    public String minerId;
    public int nonce;
    public String target;
    public int blockHeight;
    public String blockHash;

    public Block(){}

    public Block(Block prev, long time, Block next,String minername,int non,String target, int height){
        this.prevBlock = prev;
        this.timestamp = time;
        this.nextBlock = next;
        this.minerId = minername;
         this.nonce = non;
         this.target = target;
         this.blockHeight = height;
         this.blockHash = calculateBlockhash();
    }

    public Block(Block prev, long time, Block next,String minername,String target, int height){
        this.prevBlock = prev;
        this.timestamp = time;
        this.nextBlock = next;
        this.minerId = minername;
        this.target = target;
        this.blockHeight = height;
        //this.blockHash = calculateBlockhash();
    }

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String SHA256(String inputString) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return bytesToHex(sha256.digest(inputString.getBytes(StandardCharsets.UTF_8)));
        }catch (NoSuchAlgorithmException ex) {
            System.err.println(ex.toString());
            return null;
        }
    }

    /**
     * This method is used to calculate the Block hash which will be unque and will chained to the previous block by the use of
     * previous blockhash.
     *
     */

    public String calculateBlockhash(){
        String blockchashString = (this.prevBlock ==null?"":this.prevBlock.blockHash) + this.timestamp + this.minerId + this.target+this.nonce;
        String currblockhash = SHA256(SHA256(blockchashString));
        return currblockhash;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Block getPrevBlock() {
        return prevBlock;
    }

    public void setPrevBlock(Block prevBlock) {
        this.prevBlock = prevBlock;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Block getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(Block nextBlock) {
        this.nextBlock = nextBlock;
    }

    public String getMinerId() {
        return minerId;
    }

    public void setMinerId(String minerId) {
        this.minerId = minerId;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Object clone() throws CloneNotSupportedException
    {
        Block b = (Block)super.clone();

        if(this.prevBlock == null){
            b.prevBlock = null;

        }else{
        b.prevBlock = (Block)this.prevBlock.clone();
        }
        b.nextBlock = null;
        return b;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("previous Blockhash: ");
        sb.append(prevBlock == null ? null : prevBlock.blockHash).append("\n").append("timestamp: "+timestamp).append("\n").append("Miner UID who mined the block: "+minerId)
        .append("\n").append("Nonce: "+nonce).append("\n").append("Target: "+target).append("\n").append("Blockhash: "+blockHash);

        return sb.toString();

    }
}
