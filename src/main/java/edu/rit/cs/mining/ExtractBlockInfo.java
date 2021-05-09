package edu.rit.cs.mining;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;

public class ExtractBlockInfo {

    public static Object retriveBlockchainInFile() {
        try {

            Scanner scan = new Scanner(System.in);
            System.out.println("Enter file path of Ledger.dat file");
            String filePath = scan.nextLine();
            File currentDirFile = new File(".");
            FileInputStream fileIn = new FileInputStream(filePath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);

            Object obj = objectIn.readObject();

            System.out.println("The Object has been read from the file");
            objectIn.close();
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in creating Blockchain File");
            return null;
        }
    }

    public static void main(String[] args) {
        Blockchain b = (Blockchain) retriveBlockchainInFile();

        Block temp2 = b.currentHeadBlock;
        //printing the blockchain
        while (temp2 != null) {
            System.out.println(temp2.toString());
            //System.out.println("block hash: " + temp2.blockHash + " prevhash: " + (temp2.prevBlock == null ? "null" : temp2.prevBlock.blockHash));
            temp2 = temp2.prevBlock;
        }
    }
}
