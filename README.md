# Bitcoin Mining Simulator

This application provides a bare minimum capabilities implemented in the Bitcoin network. This project was designed as a part of 
Curriculum for parallel computing course at Rochester Institute of Technology. I have designed various features which bitcoin network 
uses to operate and communicate but diverge from the Bitcoin protocol in few areas like there are no transactions and crypto currency 
involved. This framework can also be used to test optimized consensus algorithm POW and to simulate mining pool functionality as well.
The in detailed implementation of the application is shown here:
```https://docs.google.com/presentation/d/1ohuium11yMoDeP7hqWzkTnSJl8F5R80pXpV3bGMj05Q/edit#slide=id.p```

## Requirements

Atleast 2 machines to start the Registry(Central Server) and Miner.

## Starting the Bitcoin Mining Simulator

1. Clone the repository.
2. Execute the command `mvn package` which produces jar files in the `target` folder.

## Running the Miner and analysis

### Starting the Registry
In any one of the machines in the current directory execute the command:
```java -cp target/mining_pool-1.0-SNAPSHOT.jar edu.rit.cs.mining.registry.Registry```
### Launch the Miner 

In the other machines execute the following command to start the miners:
```java -cp target/mining_pool-1.0-SNAPSHOT.jar edu.rit.cs.mining.miner.Miner stark.cs.rit.edu```
Note, to quit the mining code, press `q` then press `enter`/`return`.

### Extract Block Info
```java -cp target/mining_pool-1.0-SNAPSHOT.jar edu.rit.cs.mining.ExtractBlockInfo <path>/Ledger.dat```

### Generate Summary
```java -cp target/mining_pool-1.0-SNAPSHOT.jar edu.rit.cs.mining.GenerateSummary <path>/Ledger.dat```
