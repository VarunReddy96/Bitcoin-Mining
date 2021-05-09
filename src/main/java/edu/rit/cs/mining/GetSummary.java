package edu.rit.cs.mining;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GetSummary {

    public static Object retriveBlockchainInFile() {
        try {

//            Scanner scan = new Scanner(System.in);
//            System.out.println("Enter file path of Ledger.dat file");
//            String filePath = scan.nextLine();
//            File currentDirFile = new File(".");
            FileInputStream fileIn = new FileInputStream("D:\\Courses\\grader3\\Ledger4.dat");
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
        Map<String, Integer> map= new HashMap<>();
        //printing the blockchain
        while (temp2 != null) {
            if(map.containsKey(temp2.minerId)){
                map.put(temp2.minerId, map.get(temp2.minerId) + 1);
            }else{
                map.put(temp2.minerId,1);
            }
//            System.out.println(temp2.toString());
            //System.out.println("block hash: " + temp2.blockHash + " prevhash: " + (temp2.prevBlock == null ? "null" : temp2.prevBlock.blockHash));
            temp2 = temp2.prevBlock;
        }

        System.out.println("The summary of miners who mined the blocks are:");
        for(String s:map.keySet()){
            System.out.println("MinerId: "+s+"---Blocks Mined: "+map.get(s));
        }

        //System.out.println(map);
    }

}
