package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

import org.json.JSONObject;


public class Client {
    private static String s_serverHost = "localhost";
    private static int s_serverPort = 5004;

    public static void main(String args[]) throws IOException
    {
        if (args.length > 0)
        {
            s_serverHost = args[0];
        }
        if (args.length > 1)
        {
            s_serverPort = Integer.parseInt(args[1]);
        }
        if (args.length > 3)
        {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
            System.exit(1);
        }

        Client client = new Client();
        client.start();
    }

    public void start() throws IOException {

        Socket socket= new Socket(s_serverHost, s_serverPort); // establish a socket with a server using the given port#
        PrintWriter outToServer= new PrintWriter(socket.getOutputStream(),true); // open an output stream to the server...
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); // open an input stream from the server...
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in)); //to read user's input

        // Prepare for reading commands
        System.out.println();
        System.out.println("Location \"help\" for list of supported commands");

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

                JSONObject jsonObject = generateJSON(cmd, arguments);
                if (jsonObject.has("method"))
                {
                    outToServer.println(jsonObject.toString());
                } else if (jsonObject.has("Quit"))
                {
                    outToServer.println("Quit");
                    break;
                }
            }
            catch (IllegalArgumentException e) {
                System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0m" + e.getLocalizedMessage());
            }
            catch (Exception e) {
                System.err.println((char)27 + "[31;1mCommand exception: " + (char)27 + "[0mUncaught exception");
                e.printStackTrace();
            }

             // send the user's input via the output stream to the server
            String res = inFromServer.readLine(); // receive the server's result via the input stream from the server
            System.out.println("result: " + res); // print the server result to the user

        }
        socket.close();
    }

    public JSONObject generateJSON(Command cmd, Vector<String> arguments) throws NumberFormatException
    {
        JSONObject jsonObject = new JSONObject();
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

                jsonObject.put("method", "addFlight");
                jsonObject.put("args", Arrays.asList(new Object[]{id, flightNum, flightSeats, flightPrice}));

//                if (m_resourceManager.addFlight(id, flightNum, flightSeats, flightPrice)) {
//                    System.out.println("Flight added");
//                } else {
//                    System.out.println("Flight could not be added");
//                }
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

                jsonObject.put("method", "addCars");
                jsonObject.put("args", Arrays.asList(new Object[]{id, location, numCars, price}));
//                if (m_resourceManager.addCars(id, location, numCars, price)) {
//                    System.out.println("Cars added");
//                } else {
//                    System.out.println("Cars could not be added");
//                }
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

                jsonObject.put("method", "addRooms");
                jsonObject.put("args", Arrays.asList(new Object[]{id, location, numRooms, price}));
//                if (m_resourceManager.addRooms(id, location, numRooms, price)) {
//                    System.out.println("Rooms added");
//                } else {
//                    System.out.println("Rooms could not be added");
//                }
                break;
            }
            case AddCustomer: {
                checkArgumentsCount(2, arguments.size());

                System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");

                int id = toInt(arguments.elementAt(1));

                jsonObject.put("method", "newCustomer");
                jsonObject.put("args", Arrays.asList(new Object[]{id}));
//                int customer = m_resourceManager.newCustomer(id);
//                System.out.println("Add customer ID: " + customer);
                break;
            }
            case AddCustomerID: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Adding a new customer [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));

                jsonObject.put("method", "newCustomer");
                jsonObject.put("args", Arrays.asList(new Object[]{id, customerID}));
//                if (m_resourceManager.newCustomer(id, customerID)) {
//                    System.out.println("Add customer ID: " + customerID);
//                } else {
//                    System.out.println("Customer could not be added");
//                }
                break;
            }
            case DeleteFlight: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting a flight [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));

                jsonObject.put("method", "deleteFlight");
                jsonObject.put("args", Arrays.asList(new Object[]{id, flightNum}));
//                if (m_resourceManager.deleteFlight(id, flightNum)) {
//                    System.out.println("Flight Deleted");
//                } else {
//                    System.out.println("Flight could not be deleted");
//                }
                break;
            }
            case DeleteCars: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting all cars at a particular location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                jsonObject.put("method", "deleteCars");
                jsonObject.put("args", Arrays.asList(new Object[]{id, location}));
//                if (m_resourceManager.deleteCars(id, location)) {
//                    System.out.println("Cars Deleted");
//                } else {
//                    System.out.println("Cars could not be deleted");
//                }
                break;
            }
            case DeleteRooms: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting all rooms at a particular location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                jsonObject.put("method", "deleteRooms");
                jsonObject.put("args", Arrays.asList(new Object[]{id, location}));
//                if (m_resourceManager.deleteRooms(id, location)) {
//                    System.out.println("Rooms Deleted");
//                } else {
//                    System.out.println("Rooms could not be deleted");
//                }
                break;
            }
            case DeleteCustomer: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Deleting a customer from the database [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));

                jsonObject.put("method", "deleteCustomer");
                jsonObject.put("args", Arrays.asList(new Object[]{id, customerID}));
//                if (m_resourceManager.deleteCustomer(id, customerID)) {
//                    System.out.println("Customer Deleted");
//                } else {
//                    System.out.println("Customer could not be deleted");
//                }
                break;
            }
            case QueryFlight: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying a flight [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));

                jsonObject.put("method", "queryFlight");
                jsonObject.put("args", Arrays.asList(new Object[]{id, flightNum}));
//                int seats = m_resourceManager.queryFlight(id, flightNum);
//                System.out.println("Number of seats available: " + seats);
                break;
            }
            case QueryCars: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying cars location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                jsonObject.put("method", "queryCars");
                jsonObject.put("args", Arrays.asList(new Object[]{id, location}));
//                int numCars = m_resourceManager.queryCars(id, location);
//                System.out.println("Number of cars at this location: " + numCars);
                break;
            }
            case QueryRooms: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying rooms location [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Room Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                jsonObject.put("method", "queryRooms");
                jsonObject.put("args", Arrays.asList(new Object[]{id, location}));
//                int numRoom = m_resourceManager.queryRooms(id, location);
//                System.out.println("Number of rooms at this location: " + numRoom);
                break;
            }
            case QueryCustomer: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying customer information [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Customer ID: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int customerID = toInt(arguments.elementAt(2));

                jsonObject.put("method", "queryCustomerInfo");
                jsonObject.put("args", Arrays.asList(new Object[]{id, customerID}));
//                String bill = m_resourceManager.queryCustomerInfo(id, customerID);
//                System.out.print(bill);
                break;
            }
            case QueryFlightPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying a flight price [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Flight Number: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                int flightNum = toInt(arguments.elementAt(2));

                jsonObject.put("method", "queryFlightPrice");
                jsonObject.put("args", Arrays.asList(new Object[]{id, flightNum}));
//                int price = m_resourceManager.queryFlightPrice(id, flightNum);
//                System.out.println("Price of a seat: " + price);
                break;
            }
            case QueryCarsPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying cars price [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Car Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                jsonObject.put("method", "queryCarsPrice");
                jsonObject.put("args", Arrays.asList(new Object[]{id, location}));
//                int price = m_resourceManager.queryCarsPrice(id, location);
//                System.out.println("Price of cars at this location: " + price);
                break;
            }
            case QueryRoomsPrice: {
                checkArgumentsCount(3, arguments.size());

                System.out.println("Querying rooms price [xid=" + arguments.elementAt(1) + "]");
                System.out.println("-Room Location: " + arguments.elementAt(2));

                int id = toInt(arguments.elementAt(1));
                String location = arguments.elementAt(2);

                jsonObject.put("method", "queryRoomsPrice");
                jsonObject.put("args", Arrays.asList(new Object[]{id, location}));
//                int price = m_resourceManager.queryRoomsPrice(id, location);
//                System.out.println("Price of rooms at this location: " + price);
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

                jsonObject.put("method", "reserveFlight");
                jsonObject.put("args", Arrays.asList(new Object[]{id, customerID, flightNum}));
//                if (m_resourceManager.reserveFlight(id, customerID, flightNum)) {
//                    System.out.println("Flight Reserved");
//                } else {
//                    System.out.println("Flight could not be reserved");
//                }
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

                jsonObject.put("method", "reserveCar");
                jsonObject.put("args", Arrays.asList(new Object[]{id, customerID, location}));
//                if (m_resourceManager.reserveCar(id, customerID, location)) {
//                    System.out.println("Car Reserved");
//                } else {
//                    System.out.println("Car could not be reserved");
//                }
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

                jsonObject.put("method", "reserveRoom");
                jsonObject.put("args", Arrays.asList(new Object[]{id, customerID, location}));
//                if (m_resourceManager.reserveRoom(id, customerID, location)) {
//                    System.out.println("Room Reserved");
//                } else {
//                    System.out.println("Room could not be reserved");
//                }
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
                Vector<String> flightNumbers = new Vector<String>();
                for (int i = 0; i < arguments.size() - 6; ++i)
                {
                    flightNumbers.addElement(arguments.elementAt(3+i));
                }
                String location = arguments.elementAt(arguments.size()-3);
                boolean car = toBoolean(arguments.elementAt(arguments.size()-2));
                boolean room = toBoolean(arguments.elementAt(arguments.size()-1));

                jsonObject.put("method", "bundle");
                jsonObject.put("args", Arrays.asList(new Object[]{id, customerID, flightNumbers, location, car, room}));
//                if (m_resourceManager.bundle(id, customerID, flightNumbers, location, car, room)) {
//                    System.out.println("Bundle Reserved");
//                } else {
//                    System.out.println("Bundle could not be reserved");
//                }
                break;
            }
            case QueryReservableItems: {
                checkArgumentsCount(5, arguments.size());
                int id = toInt(arguments.elementAt(1));
                boolean flights = toBoolean(arguments.elementAt(2));
                boolean cars = toBoolean(arguments.elementAt(3));
                boolean rooms = toBoolean(arguments.elementAt(4));

                jsonObject.put("method", "queryReservableItems");
                jsonObject.put("args", Arrays.asList(new Object[]{id, flights, cars, rooms}));
//                String string = m_resourceManager.queryReservableItems(id, flights, cars, rooms);
//                System.out.println("The list of reservable items:\n" + string);

                break;
            }
            case QueryFlightReservers: {
                checkArgumentsCount(2, arguments.size());
                System.out.println("Querying all customers that have reserved flights...");
                int id = toInt(arguments.elementAt(1));

                jsonObject.put("method", "queryFlightReservers");
                jsonObject.put("args", Arrays.asList(new Object[]{id}));
//                String string = m_resourceManager.queryFlightReservers(id);
//                System.out.println("FLIGHT ANALYTICS:\n" + string);

                break;
            }
            case QueryCarReservers: {
                checkArgumentsCount(2, arguments.size());
                System.out.println("Querying all customers that have reserved cars...");
                int id = toInt(arguments.elementAt(1));

                jsonObject.put("method", "queryCarReservers");
                jsonObject.put("args", Arrays.asList(new Object[]{id}));
//                String string = m_resourceManager.queryCarReservers(id);
//                System.out.println("CAR ANALYTICS:\n" + string);

                break;
            }
            case QueryRoomReservers: {
                checkArgumentsCount(2, arguments.size());
                System.out.println("Querying all customers that have reserved rooms...");
                int id = toInt(arguments.elementAt(1));

                jsonObject.put("method", "queryRoomReservers");
                jsonObject.put("args", Arrays.asList(new Object[]{id}));
//                String string = m_resourceManager.queryRoomReservers(id);
//                System.out.println("ROOM ANALYTICS:\n" + string);

                break;
            }
            case Quit:
                checkArgumentsCount(1, arguments.size());

                System.out.println("Quitting client");
                System.exit(0);
        }
        return jsonObject;
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

    public static boolean toBoolean(String string)// throws Exception
    {
        return (Boolean.valueOf(string)).booleanValue();
    }
}
