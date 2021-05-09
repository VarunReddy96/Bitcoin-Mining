package edu.rit.cs.mining.miner;

import edu.rit.cs.mining.Block;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MiningStart extends Thread{

    public Miner miner;
    public MiningStart(Miner r){
        miner = r;
    }

    /**
     * This method is used to return a String from a byte array.
     *
     * @param hash: The byte array to be converted to String.
     * @return a String of the byte array hash
     */

    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * This method is used to calculate the SHA256 hash.
     *
     * @param inputString: The String for which hash to be computed.
     * @return String the sha256 hash of the inputString
     */

    public static String SHA256(String inputString) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return bytesToHex(sha256.digest(inputString.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            System.err.println(ex.toString());
            return null;
        }
    }

    /**
     * This method tries to find a nonce such that the blockhash is less than the target.
     *
     * @param blockHash:  The contents of the Block header excluding the nonce in String format.
     * @param targetHash: The target hash
     * @return the nonce which satisfies the POW.
     */

    public int pow(String blockHash, String targetHash) {
        System.out.println("Performing Proof-of-Work...wait...");

        int nonce = 0;
        String tmp_hash = "undefined";
        for (nonce = 0; nonce <= Integer.MAX_VALUE; nonce++) {
            //System.out.println("Testing Nonce: " + nonce);
            if (!this.miner.isMiningLatestBlock || this.miner.isNewBlockFound) {
                System.out.println("Exiting POW because isMiningLatestBlock: " + this.miner.isMiningLatestBlock + " is NEW block found: " + this.miner.isNewBlockFound);
                return -1;
            }
            tmp_hash = SHA256(SHA256(blockHash + String.valueOf(nonce)));
            if (targetHash.compareTo(tmp_hash) > 0)
                break;
        }
        System.out.println("Resulting Hash: " + tmp_hash);
        System.out.println("Nonce:" + nonce);
        return nonce;

    }

    /**
     * This method is used to validate if the mined block is following the protocol and is used to Publish the new block to other miners in the network.
     *
     * @param minedblock: The block which is newly mined by the miner.
     */

    public void addAndPublishBlocks(Block minedblock) {
        String blockhash = minedblock.blockHash;
        if (minedblock.target.compareTo(blockhash) < 0){
            System.out.println("Mined Invalid Block!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }
        this.miner.blockchain.addCurrentheadBlock(minedblock);
        new PublishBlock(this.miner).start();

        //}
    }

    /**
     * This method listens on a predefined port and when receives a request will launch a new thread to analyze the request and
     * starts hearing again.
     *
     */

    public void run(){
        int counter = 0;
        while (counter < 150000) {
            System.out.println("isMiningLatestBlock: " + this.miner.isMiningLatestBlock + " is NewBlockFound: " + this.miner.isNewBlockFound);
            while (this.miner.isMiningLatestBlock && !this.miner.isNewBlockFound) {
                System.out.println("Mining block..........." + (this.miner.blockchain.currentHeadBlock.blockHeight+1));
                Block newminedblock = new Block(this.miner.blockchain.currentHeadBlock, System.currentTimeMillis(), null, this.miner.minerId,
                        this.miner.blockchain.currentTarget, this.miner.blockchain.currentHeadBlock.blockHeight + 1);

                String blockContents = (newminedblock.prevBlock ==null?"":newminedblock.prevBlock.blockHash) + newminedblock.timestamp
                        + this.miner.minerId + newminedblock.target; // Nonce is not included in the block contents. It is yet to be mined.
                int nonce = pow(blockContents, this.miner.blockchain.currentTarget);
                if (nonce == -1) { // if other miner found the block
                    counter++;
                    synchronized (this.miner.o) {
                        this.miner.isNewBlockFound = false;
                    }
                    continue;
                }
                System.out.println("Succesfully mined a block");
                newminedblock.setNonce(nonce);
                newminedblock.blockHash = newminedblock.calculateBlockhash();  // calculating blockhash after the nonce hash been found.
                addAndPublishBlocks(newminedblock);
                counter++;
            }

            //this.miner.isMiningLatestBlock = true;


        }

        //storeBlockchainInFile();

    }
}
