package Server.Middleware;

import Server.Common.*;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Vector;
import static Server.Common.Trace.info;


public class Middleware implements IResourceManager {
//All in one MiddleWare that acts as Client to RM for Flights/Cars/Rooms, and Server to Client.
  Map<String, Map<String, Value>> bookMap = new HashMap<String, HashMap<String, Value>>();
  Map<String, Value> userMap = new HashMap<String, Value>();
  protected String middlewareName;
    protected  Middleware() throws RemoteException{
      super();
    }


    public Middleware(String name) {
        try {
            this.middlewareName = name;
        } catch (Exception e) {
            System.out.println("\n*** Middleware error: " + e.getMessage() + " ***\n");
        }
    }
  public void connectServer(String server, int port, String name, String booking)
  {
    try {
      boolean first = true;
      while (true) {
        try {
          Registry registry = LocateRegistry.getRegistry(server, port);
          m_resourceManager = (IResourceManager)registry.lookup(s_rmiPrefix + name);
          System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
          userMap.put(name, m_resourceManager);
          userName = name;
          bookMap.put(booking, userMap);
          //Use a hashmap of a hashmap to retreive multiple servers, in case we want to scale.
          break;
        }
        catch (NotBoundException|RemoteException e) {
          if (first) {
            System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
            first = false;
          }
        }
        Thread.sleep(500);
      }
    }
    catch (Exception e) {
      System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
      e.printStackTrace();
      System.exit(1);
    }
  }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
    {
      m_resourceManager =
      return m_resourceManager.addFlight(xid, flightNum, flightSeats, flightPrice);
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price) throws RemoteException
    {
      return m_resourceManager.addCars(xid, location, count, price);
    }

    // Create a new room location or add rooms to an existing location
    // NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
    {
      return  m_resourceManager.addRooms(xid, location, count, price);
    }

    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum) throws RemoteException
    {
      return m_resourceManager.deleteFlight(xid, flightNum);
    }

    // Delete cars at a location
    public boolean deleteCars(int xid, String location) throws RemoteException
    {
      return m_resourceManager.deleteCars(xid, location);
    }

    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location) throws RemoteException
    {
      return m_resourceManager.deleteRooms(xid, location);
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum) throws RemoteException
    {
      return m_resourceManager.queryFlight(xid, flightNum);
    }

    // Returns the number of cars available at a location
    public int queryCars(int xid, String location) throws RemoteException
    {
        return m_resourceManager.queryCars(xid, location);
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location) throws RemoteException
    {
        return m_resourceManager.queryRooms(xid, location);
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) throws RemoteException
    {
        return m_resourceManager.queryFlightPrice(xid, flightNum);
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location) throws RemoteException
    {
        return m_resourceManager.queryCarsPrice(xid, location);
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location) throws RemoteException
    {
        return m_resourceManager.queryRoomsPrice(xid, location);
    }

    public String queryCustomerInfo(int xid, int customerID) throws RemoteException
    {
      return m_resourceManager.queryCustomerInfo(xid, customerID);
    }

    public int newCustomer(int xid) throws RemoteException
    {
      m_resourceManager.newCustomer(xid);
        // Generate a globally unique ID for the new customer
    }

    public boolean newCustomer(int xid, int customerID) throws RemoteException
    {
      return m_resourceManager.newCustomer(xid, customerID);
    }

    public boolean deleteCustomer(int xid, int customerID) throws RemoteException
    {
      return m_resourceManager.deleteCustomer(xid, customerID);
    }

    // Adds flight reservation to this customer
    public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
    {
        return (m_resourceManager.reserveFlight(xid, customerID, flightNum));
    }

    // Adds car reservation to this customer
    public boolean reserveCar(int xid, int customerID, String location) throws RemoteException
    {
        return (m_resourceManager.reserveCar(xid, customerID, location));
    }

    // Adds room reservation to this customer
    public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
    {
        return (m_resourceManager.reserveRoom(xid, customerID, location));
    }

    // Reserve bundle
    // TODO: need to add functionality to this method
    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
    {
      return m_resourceManager.bundle(xid, customerID, flightNumbers, location, car, room);
    }

    public String getName() throws RemoteException
    {
        return this.middlewareName;
    }
}
