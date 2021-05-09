package edu.rit.cs.mining;


import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;




/**
 * This class is used to define the Blockchain data structure and store all the relavent information of a Blockchain.
 *
 */
public class Blockchain implements Cloneable, Serializable {

    public Block firstBlock;

    public Block currentHeadBlock; // This is the block that miners mine on.
    public String intialTarget = Config.INITIAL_TARGET; // as in coinbase codebase
    public String currentTarget;
    public int nIntervel = Config.nTargetTimespan / Config.nTargetSpacing; // number of blocks before target changes 10 blocks.

    public final Object sync = new Object();

    public Blockchain() {// miner id = 0 is the first miner for the first block
        firstBlock = new Block(null, System.currentTimeMillis(), null, "FirstBlock", 0,
                intialTarget, 1);
        currentHeadBlock = firstBlock;
        currentTarget = intialTarget;

    }

    public Block getFirstBlock() {
        return firstBlock;
    }

    public void setFirstBlock(Block firstBlock) {
        this.firstBlock = firstBlock;
    }

    public Block getCurrentHeadBlock() {
        return currentHeadBlock;
    }

    static public Object deepCopy(Object oldObj) throws Exception {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos =
                    new ByteArrayOutputStream(); // A
            oos = new ObjectOutputStream(bos); // B
            // serialize and pass the object
            oos.writeObject(oldObj);   // C
            oos.flush();               // D
            ByteArrayInputStream bin =
                    new ByteArrayInputStream(bos.toByteArray()); // E
            ois = new ObjectInputStream(bin);                  // F
            // return the new object
            return ois.readObject(); // G
        } catch (Exception e) {
            System.out.println("Exception in ObjectCloner = " + e);
            throw (e);
        } finally {
            oos.close();
            ois.close();
        }
    }

    /**
     * This method is used to verify if any forking has taken place and if no forking took place append all the blocks received to the blockchain.
     * If forking took place 1 is returned and blockchainUpdate in Miner is invoked to update the Blockchain.
     *
     * @param b: The list of blocks received from other miners upon request to update blockchain from a certain blockheight.
     * @param offset: This determines the height from which the blocks are requested in blockchainUpdate method of Miner class.
     *
     * @return 0 or 1 based on the success.
     *
     */


    public int blockchainMerge(ArrayList<Block> b, int offset) {

        synchronized (sync) {
            if (b.get(0).blockHeight <= this.currentHeadBlock.blockHeight) {
                return 0;
            }

            int c = 0;
            Block temp = this.currentHeadBlock;
            while (temp.prevBlock != null && c < offset) {
                temp = temp.prevBlock;
                c++;
            }
            if (!b.get(b.size() - 1).blockHash.equals(temp.blockHash)) {
                return 1;
            }

            // validation check can be done for every block, but right now assuming all honest miners.

            for (int i = b.size() - 2; i >= 0; i--) {
                b.get(i).prevBlock = temp;
                temp = b.get(i);
                this.currentHeadBlock = temp;
                this.currentTarget = temp.target;
            }
            checkTarget();

            return 0;
        }
    }

    /**
     * This method is used to verify if the received new block height is greater than the current blockchain height.
     * If so the new block will be checked for forking and added to the Blockchain
     *
     * @param b: The newly mined block received from another Miner.
     *
     * @return 0 or 1 based on the success.
     *
     */

    public int addNewBlock(ArrayList<Block> b) {
        synchronized (sync) {
            if (b.get(0).blockHeight <= this.currentHeadBlock.blockHeight) {
                return 0;
            }

            if (b.get(0).prevBlock.blockHash.equals(this.currentHeadBlock.blockHash)) {
                System.out.println("Current new Block height received from another Miner in BLockchain Class: "+b.get(0).blockHeight+" current height of miners" +
                        "Blockhain: "+this.currentHeadBlock.blockHeight);
                b.add(this.currentHeadBlock);
                blockchainMerge(b, 0);
                //checkTarget();
                return 0;
            } else {
                return 1;
            }
        }
    }

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
     * This is a helper method to adjust the target value
     *
     */


    public static String HexValueDivideBy(String hexValue, int val) {
        BigInteger tmp = new BigInteger(hexValue, 16);
        tmp = tmp.divide(BigInteger.valueOf(val));
        String newHex = bytesToHex(tmp.toByteArray());
        while (newHex.length() < hexValue.length()) {
            newHex = '0' + newHex;
        }
        return newHex;
    }

    /**
     * This is a helper method to adjust the target value
     *
     */

    public static String HexValueMultipleBy(String hexValue, int val) {
        BigInteger tmp = new BigInteger(hexValue, 16);
        tmp = tmp.multiply(BigInteger.valueOf(val));
        String newHex = bytesToHex(tmp.toByteArray());
        while (newHex.length() < hexValue.length()) {
            newHex = '0' + newHex;
        }
        return newHex;
    }
    /**
     * This method adjusts the target value depending on the frequency of the Blocks of mined ina 20 mins. The current system is
     * timed to work at 10 blocks for every 20 mins.
     *
     */

    public void checkTarget() {
        // might cause deadlock check
        synchronized (sync) {
        if (this.currentHeadBlock.blockHeight % this.nIntervel == 0) { // Only for every 10 blocks target changes
            System.out.println("Updating target now@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2");
            //System.out.println("Current Target: "+this.currentTarget);
            Block temp = this.currentHeadBlock;
            for (int i = 0; i < nIntervel - 1; i++) {
                temp = temp.prevBlock;
            }
            long actualtimetaken = (this.currentHeadBlock.timestamp - temp.timestamp) / 1000; // time in seconds

                if (actualtimetaken > Config.nTargetTimespan) {
                    System.out.println("Current Target: "+this.currentTarget);
                    this.currentTarget = HexValueDivideBy(HexValueMultipleBy(this.currentTarget, 5), 4);
                    System.out.println("Target Increased.!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! block height: "+this.currentHeadBlock.blockHeight);
                    System.out.println("Target value: "+this.currentTarget);

                } else if(actualtimetaken < Config.nTargetTimespan){
                    System.out.println("Current Target: "+this.currentTarget);
                    this.currentTarget = HexValueDivideBy(this.currentTarget, 4);
                    System.out.println("Target decreased!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!block height: "+this.currentHeadBlock.blockHeight);
                    System.out.println("Target value: "+this.currentTarget);
                }
            }
        }

    }

    public void addCurrentheadBlock(Block b) {
        synchronized (sync){
        b.prevBlock = this.currentHeadBlock;
        this.currentHeadBlock = b;
        checkTarget();
        }
    }

    public void setCurrentHeadBlock(Block currentHeadBlock) {
        this.currentHeadBlock = currentHeadBlock;
        checkTarget();

    }

    public Object clone() throws CloneNotSupportedException {
        Blockchain b = (Blockchain) super.clone();
        b.currentHeadBlock = (Block) this.currentHeadBlock.clone();

        return b;
    }

    public String toString(){
        Block temp = this.currentHeadBlock;
        StringBuilder sb= new StringBuilder("");
        while(temp!=null){
            sb.append(temp.toString()).append("\n").append("-----------------------------------------------------------------------------------");
//            System.out.println(temp);
//            System.out.println("-----------------------------------------------------------------------------------");
            temp = temp.prevBlock;
        }
        return sb.toString();
    }


}
