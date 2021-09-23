package Server.Middleware;

import Server.Common.*;
import Server.Interface.IResourceManager;
import org.graalvm.util.EconomicMap;

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
    protected RMHashMap m_data = new RMHashMap();

    public Middleware(String name) {
        try {
            this.middlewareName = name;
            resources = new HashMap<String, IResourceManager>();
        } catch (Exception e) {
            System.out.println("\n*** Middleware error: " + e.getMessage() + " ***\n");
        }
    }

  // Reads a data item
  protected RMItem readData(int xid, String key)
  {
    synchronized(m_data) {
      RMItem item = m_data.get(key);
      if (item != null) {
        return (RMItem)item.clone();
      }
      return null;
    }
  }

  // Writes a data item
  protected void writeData(int xid, String key, RMItem value)
  {
    synchronized(m_data) {
      m_data.put(key, value);
    }
  }

  // Remove the item out of storage
  protected void removeData(int xid, String key)
  {
    synchronized(m_data) {
      m_data.remove(key);
    }
  }

  // Deletes the encar item
  protected boolean deleteItem(int xid, String key)
  {
    Trace.info("RM::deleteItem(" + xid + ", " + key + ") called");
    ReservableItem curObj = (ReservableItem)readData(xid, key);
    // Check if there is such an item in the storage
    if (curObj == null)
    {
      Trace.warn("RM::deleteItem(" + xid + ", " + key + ") failed--item doesn't exist");
      return false;
    }
    else
    {
      if (curObj.getReserved() == 0)
      {
        removeData(xid, curObj.getKey());
        Trace.info("RM::deleteItem(" + xid + ", " + key + ") item deleted");
        return true;
      }
      else
      {
        Trace.info("RM::deleteItem(" + xid + ", " + key + ") item can't be deleted because some customers have reserved it");
        return false;
      }
    }
  }

  // Query the number of available seats/rooms/cars
  protected int queryNum(int xid, String key)
  {
    Trace.info("RM::queryNum(" + xid + ", " + key + ") called");
    ReservableItem curObj = (ReservableItem)readData(xid, key);
    int value = 0;
    if (curObj != null)
    {
      value = curObj.getCount();
    }
    Trace.info("RM::queryNum(" + xid + ", " + key + ") returns count=" + value);
    return value;
  }

  // Query the price of an item
  protected int queryPrice(int xid, String key)
  {
    Trace.info("RM::queryPrice(" + xid + ", " + key + ") called");
    ReservableItem curObj = (ReservableItem)readData(xid, key);
    int value = 0;
    if (curObj != null)
    {
      value = curObj.getPrice();
    }
    Trace.info("RM::queryPrice(" + xid + ", " + key + ") returns cost=$" + value);
    return value;
  }

  // Reserve an item
  protected boolean reserveItem(int xid, int customerID, String key, String location)
  {
    Trace.info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );
    // Read customer object if it exists (and read lock it)
    Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
    if (customer == null)
    {
      Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ")  failed--customer doesn't exist");
      return false;
    }

    // Check if the item is available
    ReservableItem item = (ReservableItem)readData(xid, key);
    if (item == null)
    {
      Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
      return false;
    }
    else if (item.getCount() == 0)
    {
      Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--No more items");
      return false;
    }
    else
    {
      customer.reserve(key, location, item.getPrice());
      writeData(xid, customer.getKey(), customer);

      // Decrease the number of available items in the storage
      item.setCount(item.getCount() - 1);
      item.setReserved(item.getReserved() + 1);
      writeData(xid, item.getKey(), item);

      Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
      return true;
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
      Trace.info("RM::newCustomer(" + xid + ") called");
      // Generate a globally unique ID for the new customer
      int cid = Integer.parseInt(String.valueOf(xid) +
        String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
        String.valueOf(Math.round(Math.random() * 100 + 1)));
      Customer customer = new Customer(cid);
      writeData(xid, customer.getKey(), customer);
      Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
      //we call the second newcustomer function, in order to ensure the same CustomerID among all servers.
      //we don't use the same newcustomer(xid) function in the other servers, in order to maintain synchronization.
      this.getResources().get("roomServer").newCustomer(xid, cid);
      this.getResources().get("carServer").newCustomer(xid, cid);
      this.getResources().get("flightServer").newCustomer(xid, cid);
      return cid;
    }

    // TODO: need to ask prof how we want to add new customers to each resource servers
    public boolean newCustomer(int xid, int customerID) throws RemoteException
    {
      Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") called");
      Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
      if (customer == null)
      {
        this.getResources().get("roomServer").newCustomer(xid, customerID);
        this.getResources().get("carServer").newCustomer(xid, customerID);
        this.getResources().get("flightServer").newCustomer(xid, customerID);
        //write to server, obv if customer is not in Middleware, it is not anywhere else.
        customer = new Customer(customerID);
        writeData(xid, customer.getKey(), customer);
        Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") created a new customer");
        return true;
      }
      else
      {
        Trace.info("INFO: RM::newCustomer(" + xid + ", " + customerID + ") failed--customer already exists");
        return false;
      }
    }

    // TODO: need to ask prof how to delete customers from each resource servers
    public boolean deleteCustomer(int xid, int customerID) throws RemoteException
    {
      Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");
      Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
      if (customer == null)
      {
        Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
        return false;
      }
      else
      {
        this.getResources().get("roomServer").deleteCustomer(xid, customerID);
        this.getResources().get("carServer").deleteCustomer(xid, customerID);
        this.getResources().get("flightServer").deleteCustomer(xid, customerID);
        //Call other functions at the server only if customer exists.
        // Increase the reserved numbers of all reservable items which the customer reserved.
        RMHashMap reservations = customer.getReservations();
        for (String reservedKey : reservations.keySet())
        {
          ReservedItem reserveditem = customer.getReservedItem(reservedKey);
          Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times");
          ReservableItem item  = (ReservableItem)readData(xid, reserveditem.getKey());
          Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reserveditem.getKey() + " which is reserved " +  item.getReserved() +  " times and is still available " + item.getCount() + " times");
          item.setReserved(item.getReserved() - reserveditem.getCount());
          item.setCount(item.getCount() + reserveditem.getCount());
          writeData(xid, item.getKey(), item);
        }

        // Remove the customer from the storage
        removeData(xid, customer.getKey());
        Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
        return true;
      }
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
