package Client;

import java.io.*;
import java.util.*;
import java.lang.Math;

public class TestClient extends Client {

    private enum TypeOfBooking{
        FLIGHTS,
        CARS,
        ROOMS
    }
    private ClientTransactionUtil clientLogger;

    public static void main(String args[]) throws IOException
    {
        loadArgs(args);
        TestClient client = new TestClient();
        client.start();
    }

    // executes a specific command
    public boolean execute(Command cmd, Vector<String> arguments) throws Throwable {

        switch (cmd)
        {
            case Help:
            {
                if (arguments.size() == 1) {
                    System.out.println(Command.description());
                } else if (arguments.size() == 2) {
                    Command l_cmd = Command.fromString((String)arguments.elementAt(1));
                    System.out.println(l_cmd.toString());
                } else {
                    System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mImproper use of help command. Location \"help\" or \"help,<CommandName>\"");
                }
                break;
            }
            case TransactionAddAndQueryFlight: {
                checkArgumentsCount(4, arguments.size());
                int flightNum = toInt(arguments.elementAt(1));
                int flightSeats = toInt(arguments.elementAt(2));
                int flightPrice = toInt(arguments.elementAt(3));
                transactionAddAndQueryFlight(flightNum, flightSeats, flightPrice, true);
                break;
            }
            case TransactionAddAndQueryCars: {
                checkArgumentsCount(4, arguments.size());
                String location = arguments.elementAt(1);
                int number = toInt(arguments.elementAt(2));
                int price = toInt(arguments.elementAt(3));
                transactionAddAndQueryCars(location, number, price, true);
                break;
            }
            case TransactionAddAndQueryRooms: {
                checkArgumentsCount(4, arguments.size());
                String location = arguments.elementAt(1);
                int number = toInt(arguments.elementAt(2));
                int price = toInt(arguments.elementAt(3));
                transactionAddAndQueryRooms(location, number, price, true);
                break;
            }
            case TransactionReserveAll: {
                checkArgumentsCount(4, arguments.size());
                int customerId = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));
                String location = arguments.elementAt(3);
                transactionReserveAll(customerId, flightNum, location, true);
                break;
            }
            case TestTransactions: {
                checkArgumentsCount(7, arguments.size());
                int transactionType = toInt(arguments.elementAt(1));
                int numberOfTransaction = toInt(arguments.elementAt(2));
                int throughput = toInt(arguments.elementAt(3));
                int itemDataSize = toInt(arguments.elementAt(4));
                int customerDataSize = toInt(arguments.elementAt(5));
                String clientName = arguments.elementAt(6);
                test(transactionType, numberOfTransaction, throughput, itemDataSize, customerDataSize, true, clientName);
                break;
            }
            case ExecuteTestSuite: {
                checkArgumentsCount(8, arguments.size());
                Random rand = new Random(4);

                String simType = arguments.elementAt(1);
                int numTransactions = toInt(arguments.elementAt(2));
                boolean debug = toBoolean(arguments.elementAt(3));
                int throughPut = toInt(arguments.elementAt(4));
                String transLength = arguments.elementAt(5);
                String transType = arguments.elementAt(6);
                String clientName = arguments.elementAt(7);

                double perTransaction = (double)1/((double)throughPut);
                long startTime;
                long endTime;

                this.clientLogger = new ClientTransactionUtil(clientName);
                System.out.println("Executing test suite - |simType=" + simType +
                        " |# of Transactions=" + numTransactions
                        + " |Debug=" + debug
                        + " |Throughput=" + throughPut + " trans/s"
                        + " |TransLength=" + transLength
                        + " |TransType=" + transType);

                for (int tx = 0; tx < numTransactions; tx++)
                {
                    // starting a new transaction
                    startTime = System.currentTimeMillis();
                    Integer txid = (Integer) callServer("start", new Object[]{});
                    if (debug)
                        System.out.println("\nRunning transaction ID: " + txid + "...");
                    clientLogger.recordStart(txid, startTime);

                    // generate parametrized transaction - structure is fixed but parameters vary with the tx var
                    Object[] commandAndArgs;
                    if (transLength.equals("short")) {
                        commandAndArgs = generateShortTransaction(txid, simType, transType, tx);
                    } else if (transLength.equals("med")) {
                        commandAndArgs = generateMedTransaction(txid, simType, transType, tx);
                    } else {
                        commandAndArgs = generateLongTransaction(txid, simType, transType, tx);
                    }

                    // executing the operations of a transaction
                    Set<Integer> keys = ((HashMap<Integer, String>)commandAndArgs[0]).keySet();
                    for (Integer k: keys)
                    {
                        String command = ((HashMap<Integer, String>)commandAndArgs[0]).get(k);
                        Object[] args = ((HashMap<Integer, Object[]>)commandAndArgs[1]).get(k);
                        if (debug)
                            System.out.println("*** executing client operation: " + command + "... ***");
                        Object response = callServer(command, args);
                        if (debug)
                            System.out.println("*** server response to client operation: ***\n" + response.toString());
                    }

                    // commit transaction
                    boolean commitResponse = (boolean)callServer("commit", new Object[]{txid});
                    if (debug) {
                        if (commitResponse)
                            System.out.println("*** transactions ID: " + txid + " committed successfully.");
                        else {
                            System.out.println("*** transactions ID: " + txid + " failed to commit.");
                        }
                    }

                    // sleeping to adjust for correct throughput
                    endTime = System.currentTimeMillis();
                    clientLogger.recordEnd(txid, endTime);
                    //write a line
                    long duration = endTime - startTime;
                    long waitTime = (long)(perTransaction * Math.pow(10, 3)) - duration;
                    if (waitTime <= 0)
                        System.out.println("*** client should not wait since real transaction duration: " +
                                duration +
                                "(ms) >=" + " theoretical transaction duration: " +
                                perTransaction * Math.pow(10, 3) + "(ms) defined by client's desired throughput ***");
                    else {
                        // check that this is actually correct
                        int tmp = rand.nextInt(2);
                        if (tmp == 0)
                            waitTime += rand.nextInt((int) (0.1*waitTime)); // adding randomness
                        else
                            waitTime -= rand.nextInt((int) (0.1*waitTime)); 
                        Thread.sleep(waitTime);
                    }
                }
                clientLogger.dumpRecords();
                clientLogger.close();
                break;
            }
            case Shutdown: {
                checkArgumentsCount(1, arguments.size());
                System.out.println("Shutting down the system...");
                if ((Boolean) callServer("shutdown", new Object[]{})) {
                    System.out.println("System shut down completed.");
                    return false;
                } else {
                    System.out.println("System shut down failed.");
                }
                break;
            }
            case Quit: {
                checkArgumentsCount(1, arguments.size());
                callServer("Quit", null);
                System.out.println("Quitting client");
                return false;
            }
            default: {
                System.err.println((char)27 + "[31;1mThis client is only for performance testing." + (char)27 + "[0m");
            }
        }
        return true;
    }

    // 3 operations each for "short" transaction, when simType = homo -> transType = car or flight or room
    // when simType = hetero -> transType = don't care
    private Object[] generateShortTransaction(Integer txid, String simtype, String transType, int id) {

        HashMap<Integer, String> commands = new HashMap<Integer, String>();
        HashMap<Integer, Object[]> commandArgs = new HashMap<Integer, Object[]>();

        commands.put(0, "newCustomer");
        commandArgs.put(0, new Object[]{txid, id + 1000});

        switch (simtype) {
           case "homo": {
               switch (transType) {
                   case "car": {
                       commands.put(1, "addCars");
                       commandArgs.put(1, new Object[]{txid, "montreal" + id,  100 + id, 200 + id});

                       commands.put(2, "queryCars");
                       commandArgs.put(2, new Object[]{txid, "montreal" + id});
                       break;
                   }
                   case "room": {
                       commands.put(1, "addRooms");
                       commandArgs.put(1, new Object[]{txid, "medellin" + id, 100 + id, 2000 + id});

                       commands.put(2, "queryRooms");
                       commandArgs.put(2, new Object[]{txid, "medellin" + id});
                       break;
                   }
                   case "flight": {
                       commands.put(1, "addFlight");
                       commandArgs.put(1, new Object[]{txid, 900 + id, 100 + id, 50 + id});

                       commands.put(2, "queryFlight");
                       commandArgs.put(2, new Object[]{txid, 900 + id});
                       break;
                   }
               }
               break;
           }
           case "hetero": {

               commands.put(1, "addCars");
               commandArgs.put(1, new Object[]{txid, "montreal" + id,  100 + id, 200 + id});

               commands.put(2, "addFlight");
               commandArgs.put(2, new Object[]{txid, 900 + id, 100 + id, 50 + id});
               break;
           }
       }
       
       return new Object[]{commands, commandArgs};
    }

    // 6 operations for each "med" transaction
    private Object[] generateMedTransaction(Integer txid, String simtype, String transType, int id) {

        HashMap<Integer, String> commands = new HashMap<Integer, String>();
        HashMap<Integer, Object[]> commandArgs = new HashMap<Integer, Object[]>();

        commands.put(0, "newCustomer");
        commandArgs.put(0, new Object[]{txid, 1000 + id});

        switch (simtype) {
            case "homo": {

                switch (transType) {
                    case "car": {
                        commands.put(1, "addCars");
                        commandArgs.put(1, new Object[]{txid, "montreal" + id,  100 + id, 200 + id});

                        commands.put(2, "queryCars");
                        commandArgs.put(2, new Object[]{txid, "montreal" + id});

                        commands.put(3, "queryCarsPrice");
                        commandArgs.put(3, new Object[]{txid, "montreal" + id});

                        commands.put(4, "reserveCar");
                        commandArgs.put(4, new Object[]{txid, 1000 + id, "montreal" + id});

                        commands.put(5, "deleteCars");
                        commandArgs.put(5, new Object[]{txid, "montreal" + id});
                        break;
                    }
                    case "room": {
                        commands.put(1, "addRooms");
                        commandArgs.put(1, new Object[]{txid, "medellin" + id, 100 + id, 2000 + id});

                        commands.put(2, "queryRooms");
                        commandArgs.put(2, new Object[]{txid, "medellin" + id});

                        commands.put(3, "queryRoomsPrice");
                        commandArgs.put(3, new Object[]{txid, "medellin" + id});

                        commands.put(4, "reserveRoom");
                        commandArgs.put(4, new Object[]{txid, 1000 + id, "medellin" + id});

                        commands.put(5, "deleteRooms");
                        commandArgs.put(5, new Object[]{txid, "medellin" + id});
                        break;
                    }
                    case "flight": {
                        commands.put(1, "addFlight");
                        commandArgs.put(1, new Object[]{txid, 900 + id, 100 + id, 50 + id});

                        commands.put(2, "queryFlight");
                        commandArgs.put(2, new Object[]{txid, 900 + id});

                        commands.put(3, "queryFlightPrice");
                        commandArgs.put(3, new Object[]{txid, 900 + id});

                        commands.put(4, "reserveFlight");
                        commandArgs.put(4, new Object[]{txid, 1000 + id, 900 + id});

                        commands.put(5, "deleteFlight");
                        commandArgs.put(5, new Object[]{txid, 900 + id});
                        break;
                    }
                }
                break;
            }
            case "hetero": {

                commands.put(1, "addCars");
                commandArgs.put(1, new Object[]{txid, "montreal" + id, 100 + id, 200 + id});

                commands.put(2, "addFlight");
                commandArgs.put(2, new Object[]{txid, 900 + id, 100 + id, 50 + id});

                commands.put(3, "queryFlight");
                commandArgs.put(3, new Object[]{txid, 900 + id});

                commands.put(4, "addRooms");
                commandArgs.put(4, new Object[]{txid, "medellin" + id, 100 + id, 2000 + id});

                commands.put(5, "deleteRooms");
                commandArgs.put(5, new Object[]{txid, "medellin" + id});
                break;
            }
        }

        return new Object[]{commands, commandArgs};
    }

    // 12 operations for each "long" transaction
    private Object[] generateLongTransaction(Integer txid, String simtype, String transType, int id) {

        HashMap<Integer, String> commands = new HashMap<Integer, String>();
        HashMap<Integer, Object[]> commandArgs = new HashMap<Integer, Object[]>();

        commands.put(0, "newCustomer");
        commandArgs.put(0, new Object[]{txid, 1000 + id});

        switch (simtype) {
            case "homo": {

                switch (transType) {
                    case "car": {
                        commands.put(1, "addCars");
                        commandArgs.put(1, new Object[]{txid, "montreal" + id,  100 + id, 200 + id});

                        commands.put(2, "queryCars");
                        commandArgs.put(2, new Object[]{txid, "montreal" + id});

                        commands.put(3, "queryCarsPrice");
                        commandArgs.put(3, new Object[]{txid, "montreal" + id});

                        commands.put(4, "reserveCar");
                        commandArgs.put(4, new Object[]{txid, 1000 + id, "montreal" + id});

                        commands.put(5, "deleteCars");
                        commandArgs.put(5, new Object[]{txid, "montreal" + id});

                        commands.put(6, "addCars");
                        commandArgs.put(6, new Object[]{txid, "montreal" + id,  100 + id, 200 + id});

                        commands.put(7, "queryCars");
                        commandArgs.put(7, new Object[]{txid, "montreal" + id});

                        commands.put(8, "queryCarsPrice");
                        commandArgs.put(8, new Object[]{txid, "montreal" + id});

                        commands.put(9, "reserveCar");
                        commandArgs.put(9, new Object[]{txid, 1000 + id, "montreal" + id});

                        commands.put(10, "deleteCars");
                        commandArgs.put(10, new Object[]{txid, "montreal" + id});

                        commands.put(11, "deleteCustomer");
                        commandArgs.put(11, new Object[]{txid, 1000 + id});
                        break;
                    }
                    case "room": {
                        commands.put(1, "addRooms");
                        commandArgs.put(1, new Object[]{txid, "medellin" + id, 100 + id, 2000 + id});

                        commands.put(2, "queryRooms");
                        commandArgs.put(2, new Object[]{txid, "medellin" + id});

                        commands.put(3, "queryRoomsPrice");
                        commandArgs.put(3, new Object[]{txid, "medellin" + id});

                        commands.put(4, "reserveRoom");
                        commandArgs.put(4, new Object[]{txid, 1000 + id, "medellin" + id});

                        commands.put(5, "deleteRooms");
                        commandArgs.put(5, new Object[]{txid, "medellin" + id});

                        commands.put(6, "addRooms");
                        commandArgs.put(6, new Object[]{txid, "medellin" + id, 100 + id, 2000 + id});

                        commands.put(7, "queryRooms");
                        commandArgs.put(7, new Object[]{txid, "medellin" + id});

                        commands.put(8, "queryRoomsPrice");
                        commandArgs.put(8, new Object[]{txid, "medellin" + id});

                        commands.put(9, "reserveRoom");
                        commandArgs.put(9, new Object[]{txid, 1000 + id, "medellin" + id});

                        commands.put(10, "deleteRooms");
                        commandArgs.put(10, new Object[]{txid, "medellin" + id});

                        commands.put(11, "deleteCustomer");
                        commandArgs.put(11, new Object[]{txid, 1000 + id});
                        break;
                    }
                    case "flight": {
                        commands.put(1, "addFlight");
                        commandArgs.put(1, new Object[]{txid, 900 + id, 100 + id, 50 + id});

                        commands.put(2, "queryFlight");
                        commandArgs.put(2, new Object[]{txid, 900 + id});

                        commands.put(3, "queryFlightPrice");
                        commandArgs.put(3, new Object[]{txid, 900 + id});

                        commands.put(4, "reserveFlight");
                        commandArgs.put(4, new Object[]{txid, 1000 + id, 900 + id});

                        commands.put(5, "deleteFlight");
                        commandArgs.put(5, new Object[]{txid, 900 + id});

                        commands.put(6, "addFlight");
                        commandArgs.put(6, new Object[]{txid, 900 + id, 100 + id, 50 + id});

                        commands.put(7, "queryFlight");
                        commandArgs.put(7, new Object[]{txid, 900 + id});

                        commands.put(8, "queryFlightPrice");
                        commandArgs.put(8, new Object[]{txid, 900 + id});

                        commands.put(9, "reserveFlight");
                        commandArgs.put(9, new Object[]{txid, 1000 + id, 900 + id});

                        commands.put(10, "deleteFlight");
                        commandArgs.put(10, new Object[]{txid, 900 + id});

                        commands.put(11, "deleteCustomer");
                        commandArgs.put(11, new Object[]{txid, 1000 + id});
                        break;
                    }
                }
                break;
            }
            case "hetero": {

                commands.put(1, "addCars");
                commandArgs.put(1, new Object[]{txid, "montreal" + id, 100 + id, 200 + id});

                commands.put(2, "addFlight");
                commandArgs.put(2, new Object[]{txid, 900 + id, 100 + id, 50 + id});

                commands.put(3, "queryFlight");
                commandArgs.put(3, new Object[]{txid, 900 + id});

                commands.put(4, "addRooms");
                commandArgs.put(4, new Object[]{txid, "medellin" + id, 100 + id, 2000 + id});

                commands.put(5, "reserveFlight");
                commandArgs.put(5, new Object[]{txid, 1000 + id, 900 + id});

                commands.put(6, "reserveRoom");
                commandArgs.put(6, new Object[]{txid, 1000 + id, "medellin" + id});

                commands.put(7, "queryCars");
                commandArgs.put(7, new Object[]{txid, "montreal" + id});

                commands.put(8, "queryRooms");
                commandArgs.put(8, new Object[]{txid, "medellin" + id});

                commands.put(9, "reserveCar");
                commandArgs.put(9, new Object[]{txid, 1000 + id, "montreal" + id});

                commands.put(10, "deleteRooms");
                commandArgs.put(10, new Object[]{txid, "medellin" + id});

                commands.put(11, "deleteCustomer");
                commandArgs.put(11, new Object[]{txid, 1000 + id});
                break;
            }
        }

        return new Object[]{commands, commandArgs};
    }


    public void test(int transactionType, int numberOfTransactions, int throughput, int itemDataSize, int customerDataSize, boolean random, String clientName) throws Throwable {

        clientLogger = new ClientTransactionUtil(clientName);
        double perTransaction = (1.0/(double)throughput);
        Random rand = new Random(4);
        for (int i=0; i<numberOfTransactions; i++) {
            long startTime = System.currentTimeMillis();
            int xid = 0;
            switch (transactionType) {
                case 0: {
                    xid = transactionAddAndQueryFlight(i, 10000, 10000, false);
                    clientLogger.recordStart(xid, startTime);
                    break;
                }
                case 1: {
                    xid = transactionAddAndQueryCars("location" + i, 10000, 10000, false);
                    clientLogger.recordStart(xid, startTime);
                    break;
                }
                case 2: {
                    xid = transactionAddAndQueryRooms("location" + i, 10000, 10000, false);
                    clientLogger.recordStart(xid, startTime);
                    break;
                }
                case 3: {
                    int number = i % itemDataSize;
                    if (random) {
                        number = rand.nextInt(itemDataSize);
                    }
                    xid = transactionReserveAll(i, number, "location" + number, false);
                    clientLogger.recordStart(xid, startTime);
                    break;
                }
            }

            // end time
            long endTime = System.currentTimeMillis();
            clientLogger.recordEnd(xid, endTime);
            long duration = endTime - startTime;
            long waitTime = (long)(perTransaction * 1000) - duration;
            if (waitTime <= 10) {
                System.out.println("*** client real transaction time: " + duration + " (ms) " + "| theoretical transaction time: " + perTransaction * 1000 + " (ms)");
            } else {
                // check that this is actually correct
                int tmp = rand.nextInt(2);
                if (tmp == 0)
                    waitTime += rand.nextInt((int) (0.1*waitTime)); // adding randomness
                else
                    waitTime -= rand.nextInt((int) (0.1*waitTime));
                Thread.sleep(waitTime);
            }
        }
        // save file
        clientLogger.dumpRecords();
        clientLogger.close();
    }
}
