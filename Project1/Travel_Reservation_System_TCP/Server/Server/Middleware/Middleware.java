package Server.Middleware;

import Server.Interface.IResourceManager;
import Server.ResourceServer.ServerSocketThread;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Middleware implements IResourceManager {
    enum ResourceServer {
        Flights, Cars, Rooms
    }
    private static int middlewareRegistryPortNum = 5004;

    private static String flightsResourceServerHost = "localhost";
    private static int flightsResourceServerPort = 2005;

    private static String carsResourceServerHost = "localhost";
    private static int carsResourceServerPort = 3004;

    private static String roomsResourceServerHost = "localhost";
    private static int roomsResourceServerPort = 4004;

    public static void main(String args[])
    {

        Middleware server= new Middleware();
        try
        {
            server.runServerThread();
        } catch (IOException e) {

        }
    }

    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(middlewareRegistryPortNum);
        System.out.println("Server ready...");
        while (true)
        {
            Socket socket=serverSocket.accept();
            new ServerSocketThread(socket, this).start();
        }
    }

    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Flights, "addFlight", new Object[]{Integer.valueOf(xid), Integer.valueOf(flightNum), Integer.valueOf(flightSeats), Integer.valueOf(flightPrice)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware addFlight response:" + response);
        return Boolean.parseBoolean(response);
    }

    public boolean addCars(int xid, String location, int numCars, int price) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Cars, "addCars", new Object[]{Integer.valueOf(xid), location, Integer.valueOf(numCars), Integer.valueOf(price)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware addCars response:" + response);
        return Boolean.parseBoolean(response);
    }

    public boolean addRooms(int xid, String location, int numRooms, int price) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Rooms, "addRooms", new Object[]{Integer.valueOf(xid), location, Integer.valueOf(numRooms), Integer.valueOf(price)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware addRooms response:" + response);
        return Boolean.parseBoolean(response);
    }

    public int newCustomer(int xid)
    {
        return 0;
    }

    public boolean newCustomer(int xid, int cid)
    {
        return false;
    }

    public void cancelReservations(Object customer, int xid, int customerID)
    {

    }

    public boolean deleteFlight(int xid, int flightNum) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Flights, "deleteFlight", new Object[]{Integer.valueOf(xid), Integer.valueOf(flightNum)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware deleteFlight response:" + response);
        return Boolean.parseBoolean(response);
    }

    public boolean deleteCars(int xid, String location) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Cars, "deleteCars", new Object[]{Integer.valueOf(xid), location});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware deleteCars response:" + response);
        return Boolean.parseBoolean(response);
    }

    public boolean deleteRooms(int xid, String location) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Rooms, "deleteRooms", new Object[]{Integer.valueOf(xid), location});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware deleteRooms response:" + response);
        return Boolean.parseBoolean(response);
    }

    public boolean deleteCustomer(int xid, int customerID) {
        String response = null;

        return Boolean.parseBoolean(response);
    }

    public int queryFlight(int xid, int flightNumber) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Flights, "queryFlight", new Object[]{Integer.valueOf(xid), Integer.valueOf(flightNumber)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryFlight response:" + response);
        return Integer.parseInt(response);
    }

    public int queryCars(int xid, String location) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Cars, "queryCars", new Object[]{Integer.valueOf(xid), location});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryCars response:" + response);
        return Integer.parseInt(response);
    }

    public int queryRooms(int xid, String location) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Rooms, "queryRooms", new Object[]{Integer.valueOf(xid), location});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryRooms response:" + response);
        return Integer.parseInt(response);
    }

    public String queryCustomerInfo(int xid, int customerID)
    {
        return null;
    }

    public int queryFlightPrice(int xid, int flightNumber) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Flights, "queryFlightPrice", new Object[]{Integer.valueOf(xid), Integer.valueOf(flightNumber)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware addFlight queryFlightPrice:" + response);
        return Integer.parseInt(response);
    }

    public int queryCarsPrice(int xid, String location) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Cars, "queryCarsPrice", new Object[]{Integer.valueOf(xid), location});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryCarsPrice response:" + response);
        return Integer.parseInt(response);
    }

    public int queryRoomsPrice(int xid, String location) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Rooms, "queryRoomsPrice", new Object[]{Integer.valueOf(xid), location});
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryRoomsPrice response:" + response);
        return Integer.parseInt(response);
    }

    public boolean reserveFlight(int xid, int customerID, int flightNumber) {
        String response = null;
        System.out.println("Middleware reserveFlight response:" + response);
        return Boolean.parseBoolean(response);
    }

    public boolean reserveCar(int xid, int customerID, String location) {
        String response = null;
        System.out.println("Middleware reserveCar response:" + response);
        return Boolean.parseBoolean(response);
    }

    public boolean reserveRoom(int xid, int customerID, String location) {
        String response = null;
        System.out.println("Middleware reserveRoom response:" + response);
        return Boolean.parseBoolean(response);
    }

    public int reserveFlightItem(int xid, int customerID, int flightNumber) {
        //Should not enter
        return 0;
    }

    public int reserveCarItem(int xid, int customerID, String location) {
        //Should not enter
        return 0;
    }

    public int reserveRoomItem(int xid, int customerID, String location) {
        //Should not enter
        return 0;
    }

    public boolean bundle(int xid, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room)
    {
        return false;
    }

    public Map<String, Integer> reserveFlightItemBundle(int xid, int customerID, Vector<String> flightNumbers)
    {
        //Should not enter
        return null;
    }


    public String queryReservableFlights(int xid) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Flights, "queryReservableFlights", new Object[]{Integer.valueOf(xid)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String queryReservableCars(int xid) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Cars, "queryReservableCars", new Object[]{Integer.valueOf(xid)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String queryReservableRooms(int xid) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Rooms, "queryReservableRooms", new Object[]{Integer.valueOf(xid)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
    public String queryReservableItems(int xid, boolean flights, boolean cars, boolean rooms) {
        String response = null;
        return response;
    }
    public String queryFlightReservers(int xid) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Flights, "queryFlightReservers", new Object[]{Integer.valueOf(xid)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String queryCarReservers(int xid) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Cars, "queryCarReservers", new Object[]{Integer.valueOf(xid)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String queryRoomReservers(int xid) {
        String response = null;
        try {
            response = callResourceServerMethod(ResourceServer.Rooms, "queryRoomReservers", new Object[]{Integer.valueOf(xid)});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String getName() {
        return "Middleware";
    }

    public String callResourceServerMethod(ResourceServer resourceServer, String methodName, Object[] argList) throws IOException {
        String host = "";
        int port = 0;
        switch (resourceServer)
        {
            case Flights:
                host = flightsResourceServerHost;
                port = flightsResourceServerPort;
                break;
            case Cars:
                host = carsResourceServerHost;
                port = carsResourceServerPort;
                break;
            case Rooms:
                host = roomsResourceServerHost;
                port = roomsResourceServerPort;
                break;
        }
        Socket socket= new Socket(host, port);
        PrintWriter outToServer= new PrintWriter(socket.getOutputStream(),true);
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        JSONObject jsonObject = new JSONObject();
        List<Object> args = new ArrayList<Object>();
        for (Object obj : argList) args.add(obj);
        jsonObject.put("method", methodName);
        jsonObject.put("args", args);
        outToServer.println(jsonObject.toString());
        String response = inFromServer.readLine();
        socket.close();
        return response;
    }
}
