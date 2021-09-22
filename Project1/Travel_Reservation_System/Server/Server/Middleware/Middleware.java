package Server.Middleware;

import Server.Common.*;
import Server.Interface.IResourceManager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;
import java.util.ArrayList;
import static Server.Common.Trace.info;


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

    public void connectToResourceServer(String serverName, int serverPortNum, String serverProxyName) throws RemoteException, NotBoundException
    {
        try {
            Registry registry = LocateRegistry.getRegistry(serverName, serverPortNum);
            IResourceManager m_resourceManager = (IResourceManager)registry.lookup(serverProxyName);
            System.out.println("\nMiddleware connected to '" + serverName + "' server " +
                    "[" + serverName + ":" + serverPortNum + "/" + serverProxyName + "]");
            this.getResources().put(serverName, m_resourceManager);
        }
        catch (Exception e) {
            System.out.println("\nRMI middleware connection error with other servers: " + e.getMessage() + "\n");
        }
    }


    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
    {
        return this.getResources().get("flightServer").addFlight(xid, flightNum, flightSeats, flightPrice);
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price) throws RemoteException
    {
        return this.getResources().get("carServer").addCars(xid, location, count, price);
    }

    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
    {
        return this.getResources().get("roomServer").addRooms(xid, location, count, price);
    }

    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum) throws RemoteException
    {
        return this.getResources().get("flightServer").deleteFlight(xid, flightNum);
    }

    // Delete cars at a location
    public boolean deleteCars(int xid, String location) throws RemoteException
    {
        return this.getResources().get("carServer").deleteCars(xid, location);
    }

    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location) throws RemoteException
    {
        return this.getResources().get("roomServer").deleteRooms(xid, location);
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum) throws RemoteException
    {
        return this.getResources().get("flightServer").queryFlight(xid, flightNum);
    }

    // Returns the number of cars available at a location
    public int queryCars(int xid, String location) throws RemoteException
    {
        return this.getResources().get("carServer").queryCars(xid, location);
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location) throws RemoteException
    {
        return this.getResources().get("roomServer").queryRooms(xid, location);
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) throws RemoteException
    {
        return this.getResources().get("flightServer").queryFlightPrice(xid, flightNum);
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location) throws RemoteException
    {
        return this.getResources().get("carServer").queryCarsPrice(xid, location);
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location) throws RemoteException
    {
        return this.getResources().get("roomServer"). queryRoomsPrice(xid, location);
    }

    public String queryCustomerInfo(int xid, int customerID) throws RemoteException
    {
        return this.getResources().get()
    }

    public int newCustomer(int xid) throws RemoteException
    {

    }

    public boolean newCustomer(int xid, int customerID) throws RemoteException
    {

    }

    public boolean deleteCustomer(int xid, int customerID) throws RemoteException
    {

    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
    {

    }

    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location) throws RemoteException
    {

    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
    {

    }

    // Reserve bundle
    // TODO: need to add functionality to this method
    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
    {

    }

    public String getName() throws RemoteException
    {

    }
}
