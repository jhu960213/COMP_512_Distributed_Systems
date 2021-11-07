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

            case ExecuteTestSuite: {
                checkArgumentsCount(7, arguments.size());
                Random rand = new Random(4);

                String transactionSimType = arguments.elementAt(1);
                int numOperations = toInt(arguments.elementAt(2));
                int numTransactions = toInt(arguments.elementAt(3));
                boolean debug = toBoolean(arguments.elementAt(4));
                int throughPut = toInt(arguments.elementAt(5));
                String transactionType = arguments.elementAt(6);

                long perTransaction = (long)1/((long)throughPut);
                long startTime;
                long endTime;

                System.out.println("Executing test suite - |TranSimType=" + transactionSimType +
                        " |# of Transactions=" + numTransactions
                        + " |# of Operations/Trans=" + numOperations
                        + " |Debug=" + debug
                        + " |Throughput=" + throughPut + " trans/s"
                        + " |TransactionType=" + transactionType);

                for (int tx = 0; tx < numTransactions; tx++)
                {
                    // starting a new transaction
                    startTime = System.nanoTime();
                    Integer txid = (Integer) callServer("start", new Object[]{});
                    if (debug)
                        System.out.println("\nRunning transaction ID: " + txid + "...");

                    // choosing between hetero or homo transaction simulation
                    Object[] commandAndArgs;
                    if (transactionSimType.equals("hetero")) {
                         commandAndArgs = generateAllCommands(txid);
                    } else {
                        TypeOfBooking simType;
                        if (transactionType.equals("flight"))
                            simType = TypeOfBooking.FLIGHTS;
                        else if (transactionType.equals("cars"))
                            simType = TypeOfBooking.CARS;
                        else
                            simType = TypeOfBooking.ROOMS;
                        commandAndArgs = generateOneTypeOfCommands(txid, simType);
                    }

                    // submitting all operations within a transaction
                    for(int op = 0; op < numOperations; op++)
                    {
                        String command = ((HashMap<Integer, String>)commandAndArgs[0]).get(op);
                        Object[] args = ((HashMap<Integer, Object[]>)commandAndArgs[1]).get(op);
                        if (debug)
                            System.out.println("*** executing client operation " + op + " " + command + "... ***");
                        Object response = callServer(command, args);
                        if (debug)
                            System.out.println("*** server response to client operation: ***\n" + response.toString());
                    }

                    // commit transaction
                    boolean commitResponse = (boolean)callServer("commit", new Object[]{txid});
                    if (debug) {
                        if (commitResponse)
                            System.out.println("*** transactions ID: " + txid + "committed successfully.");
                        else {
                            System.out.println("*** transactions ID: " + txid + "failed to commit.");
                        }
                    }

                    // sleeping to adjust for correct throughput
                    endTime = System.nanoTime();
                    long duration = endTime - startTime;
                    long waitTime = perTransaction - (long)(duration * Math.pow(10,-6));

                    if (rand.nextInt(2) == 0)
                        waitTime += rand.nextInt((int) (0.1*waitTime)); // adding randomness
                    else
                        waitTime -= rand.nextInt((int) (0.1*waitTime)); // adding randomness

                    if (waitTime <= 0)
                        System.out.println("*** client should not wait since real transaction duration: " +
                                (long)(duration * Math.pow(10,-6))  +
                                ">=" + " theoretical transaction duration: " +
                                perTransaction + " defined by client's desired throughput ***");
                    else {

                        Thread.sleep(waitTime);
                    }
                }
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

    private Object[] generateOneTypeOfCommands(Integer txid, TypeOfBooking book) {

        HashMap<Integer, String> commands = new HashMap<Integer, String>();
        HashMap<Integer, Object[]> commandArgs = new HashMap<Integer, Object[]>();

        // doesn't matter what type of transaction we are running we always need to add customer first before reserving stuff
        commands.put(0, "newCustomer");
        commandArgs.put(0, new Object[]{txid, 1000});

        switch (book){
            case FLIGHTS:
                commands.put(1, "addFlight");
                commandArgs.put(1, new Object[]{txid, 911, 100, 50});

                commands.put(2, "queryFlight");
                commandArgs.put(2, new Object[]{txid, 911});

                commands.put(3, "queryFlightPrice");
                commandArgs.put(3, new Object[]{txid, 911});

                commands.put(4, "reserveFlight");
                commandArgs.put(4, new Object[]{txid, 1000, 911});

                commands.put(5, "deleteFlight");
                commandArgs.put(5, new Object[]{txid, 911});
                break;
            case CARS:
                commands.put(1, "addCars");
                commandArgs.put(1, new Object[]{txid, "chicago", 100, 150});

                commands.put(2, "queryCars");
                commandArgs.put(2, new Object[]{txid, "chicago"});

                commands.put(3, "queryCarsPrice");
                commandArgs.put(3, new Object[]{txid, "chicago"});

                commands.put(4, "deleteCar");
                commandArgs.put(4, new Object[]{txid, 1000, "chicago"});

                commands.put(5, "deleteCars");
                commandArgs.put(5, new Object[]{txid, "chicago"});
                break;
            case ROOMS:
                commands.put(1, "addRooms");
                commandArgs.put(1, new Object[]{txid, "medellin", 100, 2000});

                commands.put(2, "queryRooms");
                commandArgs.put(2, new Object[]{txid, "medellin"});

                commands.put(3, "queryRoomsPrice");
                commandArgs.put(3, new Object[]{txid, "medellin"});

                commands.put(4, "deleteRoom");
                commandArgs.put(4, new Object[]{txid, 1000, "medellin"});

                commands.put(5, "deleteRooms");
                commandArgs.put(5, new Object[]{txid, "medellin"});
                break;
        }

        return new Object[]{commands, commandArgs};
    }

    private Object[] generateAllCommands(Integer txid) {

        // default values - flightNum = 911, Location = montreal, CustomerID = 1000

        HashMap<Integer, String> commands = new HashMap<Integer, String>();
        HashMap<Integer, Object[]> commandArgs = new HashMap<Integer, Object[]>();

        commands.put(0, "addFlight");
        commandArgs.put(0, new Object[]{txid, 911, 100, 50});

        commands.put(1, "addCars");
        commandArgs.put(1, new Object[]{txid, "montreal", 100, 150});

        commands.put(2, "addRooms");
        commandArgs.put(2, new Object[]{txid, "toronto", 100, 2000});

        commands.put(3, "newCustomer");
        commandArgs.put(3, new Object[]{txid});

        commands.put(4, "newCustomer");
        commandArgs.put(4, new Object[]{txid, 1000});

        commands.put(5, "queryFlight");
        commandArgs.put(5, new Object[]{txid, 911});

        commands.put(6, "queryCars");
        commandArgs.put(6, new Object[]{txid, "montreal"});

        commands.put(7, "queryRooms");
        commandArgs.put(7, new Object[]{txid, "toronto"});

        commands.put(8, "queryCustomerInfo");
        commandArgs.put(8, new Object[]{txid, 1000});

        commands.put(9, "queryFlightPrice");
        commandArgs.put(9, new Object[]{txid, 911});

        commands.put(10, "queryCarsPrice");
        commandArgs.put(10, new Object[]{txid, "montreal"});

        commands.put(11, "queryRoomsPrice");
        commandArgs.put(11, new Object[]{txid, "toronto"});

        commands.put(12, "reserveFlight");
        commandArgs.put(12, new Object[]{txid, 1000, 911});

        commands.put(13, "reserveCar");
        commandArgs.put(13, new Object[]{txid, 1000, "montreal"});

        commands.put(14, "reserveRoom");
        commandArgs.put(14, new Object[]{txid, 1000, "toronto"});

        commands.put(15, "deleteFlight");
        commandArgs.put(15, new Object[]{txid, 911});

        commands.put(16, "deleteCars");
        commandArgs.put(16, new Object[]{txid, "montreal"});

        commands.put(17, "deleteRooms");
        commandArgs.put(17, new Object[]{txid, "toronto"});

        commands.put(18, "deleteCustomer");
        commandArgs.put(18, new Object[]{txid, 1000});

        commands.put(19, "bundle");
        Vector<String> flights = new Vector<>();
        flights.add("911");
        commandArgs.put(19, new Object[]{txid, 1000, flights, "montreal", true, true});

        return new Object[]{commands, commandArgs};
    }
}
