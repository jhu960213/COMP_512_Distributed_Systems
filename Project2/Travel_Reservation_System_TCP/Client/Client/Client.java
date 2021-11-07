package Client;

import Server.Exception.InvalidTransactionException;
import Server.Exception.TransactionAbortedException;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {

    private static String s_serverHost = "localhost";
    private static int s_serverPort = 5004;
    private ObjectOutputStream outToServer;
    private ObjectInputStream inFromServer;

    public static void loadArgs(String args[])
    {
        if (args.length > 0)
        {
            s_serverHost = args[0];
        }
        if (args.length > 1)
        {
            s_serverPort = Integer.parseInt(args[1]);
        }
        if (args.length > 2)
        {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
            System.exit(1);
        }
    }

    public static void main(String args[]) throws IOException
    {
        loadArgs(args);
        Client client = new Client();
        client.start();
    }

    public void start() throws IOException {

        Socket socket = new Socket(s_serverHost, s_serverPort); // establish a socket with a server using the given port#
        outToServer = new ObjectOutputStream(socket.getOutputStream());
        inFromServer = new ObjectInputStream(socket.getInputStream());
        // Prepare for reading commands
        System.out.println("Please use command: \"help\" for a list of supported commands");

        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        while (true)
        {
            // Read the next command
            String command = "";
            Vector<String> arguments = new Vector<String>();
            try {
                System.out.print((char)27 + "[32;1m\n>] " + (char)27 + "[0m");
                command = stdin.readLine().trim();
            }
            catch (IOException io) {
                System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0m" + io.getLocalizedMessage());
                io.printStackTrace();
                System.exit(1);
            }

            try {
                arguments = parse(command);
                Command cmd = Command.fromString((String)arguments.elementAt(0));
                if (!execute(cmd, arguments)) break;
            }
            catch (Throwable e) {
                if (e instanceof IllegalArgumentException) System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
                else if (e instanceof InvalidTransactionException) System.err.println((char)27 + "[31;1mInvalid transactionID (xid): " + (char)27 + "[0m" + e.getLocalizedMessage());
                else if (e instanceof TransactionAbortedException) System.err.println((char)27 + "[31;1mTransaction aborted (xid): " + (char)27 + "[0m" + e.getLocalizedMessage());
                else {
                    System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
                    e.printStackTrace();
                }
            }
        }
        socket.close();
    }

    public Object callServer(String methodName, Object[] argList) throws Throwable {
        outToServer.writeObject(methodName);
        if (argList == null) return null;
        outToServer.writeObject(argList);
        Object response = inFromServer.readObject();
        if (response instanceof Throwable) throw (Throwable)response;
        return response;
    }

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
            case AddFlight: {
                checkArgumentsCount(5, arguments.size());

                System.out.println("Adding a new flight [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));
                System.out.println("-Flight Seats: " + arguments.elementAt(3));
                System.out.println("-Flight Price: " + arguments.elementAt(4));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));
                int flightSeats = toInt(arguments.elementAt(3));
                int flightPrice = toInt(arguments.elementAt(4));
                Boolean res = (Boolean) callServer("addFlight", new Object[]{id, flightNum, flightSeats, flightPrice});
                if (res) {
                    System.out.println("Flight added");
                } else {
                    System.out.println("Flight could not be added");
                }
                break;
            }
            case AddCars: {
                checkArgumentsCount(5, arguments.size());

                System.out.println("Adding new cars [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));
                System.out.println("-Number of Cars: " + arguments.elementAt(3));
                System.out.println("-Car Price: " + arguments.elementAt(4));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                int numCars = toInt(arguments.elementAt(3));
                int price = toInt(arguments.elementAt(4));

                Boolean res = (Boolean) callServer("addCars", new Object[]{id, location, numCars, price});
                if (res) {
                    System.out.println("Cars added");
                } else {
                    System.out.println("Cars could not be added");
                }
                break;
            }
            case AddRooms: {
                checkArgumentsCount(5, arguments.size());

                System.out.println("Adding new rooms [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Room Location: " + arguments.elementAt(2));
                System.out.println("-Number of Rooms: " + arguments.elementAt(3));
                System.out.println("-Room Price: " + arguments.elementAt(4));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                int numRooms = toInt(arguments.elementAt(3));
                int price = toInt(arguments.elementAt(4));

                Boolean res = (Boolean) callServer("addRooms", new Object[]{id, location, numRooms, price});
                if (res) {
                    System.out.println("Rooms added");
                } else {
                    System.out.println("Rooms could not be added");
                }
                break;
            }
            case AddCustomer: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");

                int id = toInt(arguments.elementAt(1));

                Integer customer = (Integer) callServer("newCustomer", new Object[]{id});
                System.out.println("Add customer ID: " + customer);
                break;
            }
            case AddCustomerID: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));

                Boolean res = (Boolean) callServer("newCustomer", new Object[]{id, customerID});
                if (res) {
                    System.out.println("Add customer ID: " + customerID);
                } else {
                    System.out.println("Customer could not be added");
                }
                break;
            }
            case DeleteFlight: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting a flight [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));

                Boolean res = (Boolean) callServer("deleteFlight", new Object[]{id, flightNum});
                if (res) {
                    System.out.println("Flight Deleted");
                } else {
                    System.out.println("Flight could not be deleted");
                }
                break;
            }
            case DeleteCars: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting all cars at a particular location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                if ((Boolean) callServer("deleteCars", new Object[]{id, location})) {
                    System.out.println("Cars Deleted");
                } else {
                    System.out.println("Cars could not be deleted");
                }
                break;
            }
            case DeleteRooms: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting all rooms at a particular location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                if ((Boolean) callServer("deleteRooms", new Object[]{id, location})) {
                    System.out.println("Rooms Deleted");
                } else {
                    System.out.println("Rooms could not be deleted");
                }
                break;
            }
            case DeleteCustomer: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting a customer from the database [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));

                if ((Boolean) callServer("deleteCustomer", new Object[]{id, customerID})) {
                    System.out.println("Customer Deleted");
                } else {
                    System.out.println("Customer could not be deleted");
                }
                break;
            }
            case QueryFlight: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying a flight [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));

                Integer seats = (Integer) callServer("queryFlight", new Object[]{id, flightNum});
                System.out.println("Number of seats available: " + seats);
                break;
            }
            case QueryCars: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying cars location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                Integer numCars = (Integer) callServer("queryCars", new Object[]{id, location});
                System.out.println("Number of cars at this location: " + numCars);
                break;
            }
            case QueryRooms: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying rooms location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Room Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                Integer numRoom = (Integer) callServer("queryRooms", new Object[]{id, location});
                System.out.println("Number of rooms at this location: " + numRoom);
                break;
            }
            case QueryCustomer: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying customer information [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));

                String bill = (String) callServer("queryCustomerInfo", new Object[]{id, customerID});
                System.out.print(bill);
                break;
            }
            case QueryFlightPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying a flight price [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));

                Integer price = (Integer) callServer("queryFlightPrice", new Object[]{id, flightNum});
                System.out.println("Price of a seat: " + price);
                break;
            }
            case QueryCarsPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying cars price [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                Integer price = (Integer) callServer("queryCarsPrice", new Object[]{id, location});
                System.out.println("Price of cars at this location: " + price);
                break;
            }
            case QueryRoomsPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying rooms price [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Room Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                Integer price = (Integer) callServer("queryRoomsPrice", new Object[]{id, location});
                System.out.println("Price of rooms at this location: " + price);
                break;
            }
            case ReserveFlight: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Reserving seat in a flight [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));
                System.out.println("-Flight Number: " + arguments.elementAt(3));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                int flightNum = toInt(arguments.elementAt(3));

                if ((Boolean) callServer("reserveFlight", new Object[]{id, customerID, flightNum})) {
                    System.out.println("Flight Reserved");
                } else {
                    System.out.println("Flight could not be reserved");
                }
                break;
            }
            case ReserveCar: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Reserving a car at a location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));
                System.out.println("-Car Location: " + arguments.elementAt(3));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                String location = arguments.elementAt(3);

                if ((Boolean) callServer("reserveCar", new Object[]{id, customerID, location})) {
                    System.out.println("Car Reserved");
                } else {
                    System.out.println("Car could not be reserved");
                }
                break;
            }
            case ReserveRoom: {
                checkArgumentsCount(4, arguments.size());

                System.out.println("Reserving a room at a location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));
                System.out.println("-Room Location: " + arguments.elementAt(3));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                String location = arguments.elementAt(3);

                if ((Boolean) callServer("reserveRoom", new Object[]{id, customerID, location})) {
                    System.out.println("Room Reserved");
                } else {
                    System.out.println("Room could not be reserved");
                }
                break;
            }
            case Bundle: {
                if (arguments.size() < 7) {
                    System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mBundle command expects at least 7 arguments. Location \"help\" or \"help,<CommandName>\"");
                    break;
                }

                System.out.println("Reserving an bundle [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));
                for (int i = 0; i < arguments.size() - 6; ++i)
                {
                    System.out.println("-Flight Number: " + arguments.elementAt(3+i));
                }
                System.out.println("-Location for Car/Room: " + arguments.elementAt(arguments.size()-3));
                System.out.println("-Book Car: " + arguments.elementAt(arguments.size()-2));
                System.out.println("-Book Room: " + arguments.elementAt(arguments.size()-1));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));
                Vector<String> flightNumbers = new Vector<>();
                for (int i = 0; i < arguments.size() - 6; ++i) flightNumbers.add(arguments.elementAt(3+i));
                String location = arguments.elementAt(arguments.size()-3);
                boolean car = toBoolean(arguments.elementAt(arguments.size()-2));
                boolean room = toBoolean(arguments.elementAt(arguments.size()-1));

                if ((Boolean) callServer("bundle", new Object[]{id, customerID, flightNumbers, location, car, room})) {
                    System.out.println("Bundle Reserved");
                } else {
                    System.out.println("Bundle could not be reserved");
                }
                break;
            }
            case QueryReservableItems: {
                checkArgumentsCount(5, arguments.size());
                int id = toInt(arguments.elementAt(1));
                boolean flights = toBoolean(arguments.elementAt(2));
                boolean cars = toBoolean(arguments.elementAt(3));
                boolean rooms = toBoolean(arguments.elementAt(4));

                String string = (String) callServer("queryReservableItems", new Object[]{id, flights, cars, rooms});
                System.out.println("The list of reservable items:\n" + string);
                break;
            }
            case QueryFlightReservers: {
                checkArgumentsCount(2, arguments.size());
                System.out.println("Querying all customers that have reserved flights...");
                int id = toInt(arguments.elementAt(1));

                String string = (String) callServer("queryFlightReservers", new Object[]{id});
                System.out.println("FLIGHT ANALYTICS:\n" + string);

                break;
            }
            case QueryCarReservers: {
                checkArgumentsCount(2, arguments.size());
                System.out.println("Querying all customers that have reserved cars...");
                int id = toInt(arguments.elementAt(1));

                String string = (String) callServer("queryCarReservers", new Object[]{id});
                System.out.println("CAR ANALYTICS:\n" + string);
                break;
            }
            case QueryRoomReservers: {
                checkArgumentsCount(2, arguments.size());
                System.out.println("Querying all customers that have reserved rooms...");
                int id = toInt(arguments.elementAt(1));

                String string = (String) callServer("queryRoomReservers", new Object[]{id});
                System.out.println("ROOM ANALYTICS:\n" + string);
                break;
            }
            case Start: {
                checkArgumentsCount(1, arguments.size());
                System.out.println("Starting a transaction...");
                Integer xid = (Integer) callServer("start", new Object[]{});
                System.out.println("Transaction with id: " + xid + " has been started.");
                break;
            }
            case Commit: {
                checkArgumentsCount(2, arguments.size());
                System.out.println("Committing a transaction [xid=" + arguments.elementAt(1) + "]...");
                int id = toInt(arguments.elementAt(1));
                if ((Boolean) callServer("commit", new Object[]{id})) {
                    System.out.println("Transaction " + id + " committed.");
                } else {
                    System.out.println("Transaction " + id + " failed to be committed.");
                }
                break;
            }
            case Abort: {
                checkArgumentsCount(2, arguments.size());
                System.out.println("Aborting a transaction [xid=" + arguments.elementAt(1) + "]...");
                int id = toInt(arguments.elementAt(1));
                callServer("abort", new Object[]{id});
                System.out.println("Transaction " + id + " aborted.");
                break;
            }
            case TransactionAddAndQueryFlight: {
                checkArgumentsCount(4, arguments.size());
                int flightNum = toInt(arguments.elementAt(1));
                int flightSeats = toInt(arguments.elementAt(2));
                int flightPrice = toInt(arguments.elementAt(3));
                transactionAddAndQueryFlight(flightNum, flightSeats, flightPrice);
                break;
            }
            case TransactionAddAndQueryCars: {
                checkArgumentsCount(4, arguments.size());
                String location = arguments.elementAt(1);
                int number = toInt(arguments.elementAt(2));
                int price = toInt(arguments.elementAt(3));
                transactionAddAndQueryCars(location, number, price);
                break;
            }
            case TransactionAddAndQueryRooms: {
                checkArgumentsCount(4, arguments.size());
                String location = arguments.elementAt(1);
                int number = toInt(arguments.elementAt(2));
                int price = toInt(arguments.elementAt(3));
                transactionAddAndQueryRooms(location, number, price);
                break;
            }
            case TransactionReserveAll: {
                checkArgumentsCount(3, arguments.size());
                int flightNum = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);
                transactionReserveAll(flightNum, location);
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
                System.err.println((char)27 + "[31;1mThis command is not available on this client." + (char)27 + "[0m");
            }
        }
        return true;
    }

    //Transactions
    public int transactionAddAndQueryFlight(int flightNum, int flightSeats, int flightPrice) throws Throwable {
        Integer xid = (Integer) callServer("start", new Object[]{});
        System.out.println("Transaction with id: " + xid + " has been started.");
        Boolean res = (Boolean) callServer("addFlight", new Object[]{xid, flightNum, flightSeats, flightPrice});
        System.out.println(res ? "Flight added" : "Flight could not be added");
        Integer seats = (Integer) callServer("queryFlight", new Object[]{xid, flightNum});
        System.out.println("Number of seats available: " + seats);
        Integer price = (Integer) callServer("queryFlightPrice", new Object[]{xid, flightNum});
        System.out.println("Price of a seat: " + price);
        res = (Boolean) callServer("commit", new Object[]{xid});
        System.out.println(res ? ("Transaction " + xid + " committed.") : ("Transaction " + xid + " failed to be committed."));
        return xid;
    }

    public int transactionAddAndQueryCars(String location, int number, int price) throws Throwable {
        Integer xid = (Integer) callServer("start", new Object[]{});
        System.out.println("Transaction with id: " + xid + " has been started.");
        Boolean res = (Boolean) callServer("addCars", new Object[]{xid, location, number, price});
        System.out.println(res ? "Cars added" : "Cars could not be added");
        Integer cars = (Integer) callServer("queryCars", new Object[]{xid, location});
        System.out.println("Number of cars available: " + cars);
        Integer p = (Integer) callServer("queryCarsPrice", new Object[]{xid, location});
        System.out.println("Price of car: " + p);
        res = (Boolean) callServer("commit", new Object[]{xid});
        System.out.println(res ? ("Transaction " + xid + " committed.") : ("Transaction " + xid + " failed to be committed."));
        return xid;
    }
    public int transactionAddAndQueryRooms(String location, int number, int price) throws Throwable {
        Integer xid = (Integer) callServer("start", new Object[]{});
        System.out.println("Transaction with id: " + xid + " has been started.");
        Boolean res = (Boolean) callServer("addRooms", new Object[]{xid, location, number, price});
        System.out.println(res ? "Cars added" : "Cars could not be added");
        Integer rooms = (Integer) callServer("queryRooms", new Object[]{xid, location});
        System.out.println("Number of rooms available: " + rooms);
        Integer p = (Integer) callServer("queryRoomsPrice", new Object[]{xid, location});
        System.out.println("Price of room: " + p);
        res = (Boolean) callServer("commit", new Object[]{xid});
        System.out.println(res ? ("Transaction " + xid + " committed.") : ("Transaction " + xid + " failed to be committed."));
        return xid;
    }

    public int transactionReserveAll(int flightNum, String location) throws Throwable {
        Integer xid = (Integer) callServer("start", new Object[]{});
        System.out.println("Transaction with id: " + xid + " has been started.");
        Integer customerID = (Integer) callServer("newCustomer", new Object[]{xid});
        System.out.println("Add customer ID: " + customerID);
        Boolean res = (Boolean) callServer("reserveFlight", new Object[]{xid, customerID, flightNum});
        System.out.println(res ? ("Flight Reserved for " + customerID) : ("Flight could not be reserved for " + customerID));
        res = (Boolean) callServer("reserveCar", new Object[]{xid, customerID, location});
        System.out.println(res ? ("Car Reserved for " + customerID) : ("Car could not be reserved for " + customerID));
        res = (Boolean) callServer("reserveRoom", new Object[]{xid, customerID, location});
        System.out.println(res ? ("Room Reserved for " + customerID) : ("Room could not be reserved for " + customerID));
        res = (Boolean) callServer("commit", new Object[]{xid});
        System.out.println(res ? ("Transaction " + xid + " committed.") : ("Transaction " + xid + " failed to be committed."));
        return xid;
    }

    public static Vector<String> parse(String command)
    {
        Vector<String> arguments = new Vector<String>();
        StringTokenizer tokenizer = new StringTokenizer(command,",");
        String argument = "";
        while (tokenizer.hasMoreTokens())
        {
            argument = tokenizer.nextToken();
            argument = argument.trim();
            arguments.add(argument);
        }
        return arguments;
    }

    public static void checkArgumentsCount(Integer expected, Integer actual) throws IllegalArgumentException
    {
        if (expected != actual)
        {
            throw new IllegalArgumentException("Invalid number of arguments. Expected " + (expected - 1) + ", received " + (actual - 1) + ". Location \"help,<CommandName>\" to check usage of this command");
        }
    }

    public static int toInt(String string) throws NumberFormatException
    {
        return (Integer.valueOf(string)).intValue();
    }

    public static boolean toBoolean(String string)
    {
        return (Boolean.valueOf(string)).booleanValue();
    }
}
