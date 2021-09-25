package Server.Middleware;

import Server.Interface.IResourceManager;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Vector;


public class Middleware implements IResourceManager {

    protected String middlewareName;
    private HashMap<String, IResourceManager> resources;

    public Middleware(String name) {
        try {
            this.middlewareName = name;
            resources = new HashMap<String, IResourceManager>();
        } catch (Exception e) {
            System.out.println("\n*** Middleware error: " + e.getMessage() + " ***\n");
        }
    }

    public HashMap<String, IResourceManager> getResources() {
        return resources;
    }

    public void setResources(HashMap<String, IResourceManager> resources) {
        this.resources = resources;
    }

    public void connectToResourceServer(String serverHost, String serverName, int serverPortNum, String serverProxyName) throws RemoteException
    {
        try {
            while(true) {
                try {
                    Registry registry = LocateRegistry.getRegistry(serverHost, serverPortNum);
                    IResourceManager m_resourceManager = (IResourceManager)registry.lookup(serverProxyName);
                    System.out.println("\nMiddleware connected to '" + serverHost + "' server " +
                            "[" + serverHost + ":" + serverPortNum + "/" + serverProxyName + "]");
                    this.getResources().put(serverName, m_resourceManager);
                    System.out.println("Hashed the " + serverName + "'s proxy server object into storage!\n");
                    break;
                }
                catch (Exception e) {
                    System.out.println("\nRMI middleware failed to connect with server: " + serverName + ":" + serverPortNum + ":\n" + e.getLocalizedMessage() + " waiting to connect again....\n");
                }
                Thread.sleep(500);
            }
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mRMI Middleware server exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }


    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("flightServer").addFlight(xid, flightNum, flightSeats, flightPrice);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("carServer").addCars(xid, location, count, price);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("roomServer").addRooms(xid, location, count, price);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("flightServer").deleteFlight(xid, flightNum);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Delete cars at a location
    public boolean deleteCars(int xid, String location) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("carServer").deleteCars(xid, location);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("roomServer").deleteRooms(xid, location);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum) throws RemoteException
    {
        int response = 0;
        try {
            response = this.getResources().get("flightServer").queryFlight(xid, flightNum);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Returns the number of cars available at a location
    public int queryCars(int xid, String location) throws RemoteException
    {
        int response = 0;
        try {
            response = this.getResources().get("carServer").queryCars(xid, location);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location) throws RemoteException
    {
        int response = 0;
        try {
            response = this.getResources().get("roomServer").queryRooms(xid, location);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) throws RemoteException
    {
        int response = 0;
        try {
            response = this.getResources().get("flightServer").queryFlightPrice(xid, flightNum);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location) throws RemoteException
    {
        int response = 0;
        try {
            response = this.getResources().get("carServer").queryCarsPrice(xid, location);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location) throws RemoteException
    {
        int response = 0;
        try {
            response = this.getResources().get("roomServer"). queryRoomsPrice(xid, location);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    public String queryCustomerInfo(int xid, int customerID) throws RemoteException
    {
        String roomBill = "";
        String carBill = "";
        String flightBill = "";
        try {
            roomBill = this.getResources().get("roomServer").queryCustomerInfo(xid, customerID);
            carBill = this.getResources().get("carServer").queryCustomerInfo(xid, customerID);
            flightBill = this.getResources().get("flightServer").queryCustomerInfo(xid, customerID);
        }
        catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return new String("Total Bill = Room Bill: " + roomBill + " Car Bill: " + carBill + " Flight Bill: " + flightBill);
    }

    // TODO: need to ask prof how we want to add new customers to each resource servers
    public int newCustomer(int xid) throws RemoteException
    {
        return 0;
    }

    // TODO: need to ask prof how we want to add new customers to each resource servers
    public boolean newCustomer(int xid, int customerID) throws RemoteException
    {
        return false;
    }

    // TODO: need to ask prof how to delete customers from each resource servers
    public boolean deleteCustomer(int xid, int customerID) throws RemoteException
    {
        return false;
    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("flightServer").reserveFlight(xid, customerID, flightNum);
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("carServer").reserveCar(xid, customerID, location);
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
    {
        boolean response = false;
        try {
            response = this.getResources().get("roomServer").reserveRoom(xid, customerID, location);
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Reserve bundle
    // TODO: need to add functionality to this method and ask prof how we want to do this
    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
    {
        return false;
    }

    public String getName() throws RemoteException
    {
        String carManagerName = "";
        String roomManagerName = "";
        String flightManagerName = "";
        try {
            carManagerName = this.getResources().get("carServer").getName();
            roomManagerName = this.getResources().get("roomServer").getName();
            flightManagerName = this.getResources().get("flightServer").getName();
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return new String("Resource Managers: " + carManagerName + " | " + roomManagerName + " | " + flightManagerName);
    }
}
