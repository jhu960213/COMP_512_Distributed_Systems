package Server.Middleware;

import Server.Common.*;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Vector;
import static Server.Common.Trace.info;


public class Middleware implements IResourceManager {

    protected IResourceManager m_flightsResourceManager = null;
    protected IResourceManager m_carsResourceManager = null;
    protected IResourceManager m_roomsResourceManager = null;

    protected String middlewareName;
    protected RMHashMap middlewareData = new RMHashMap();

    public Middleware(String name) {
        try {
            this.middlewareName = name;
        } catch (Exception e) {
            System.out.println("\n*** Middleware error: " + e.getMessage() + " ***\n");
        }
    }

    // Reads a data item
    protected RMItem readData(int xid, String key)
    {
        synchronized(this.middlewareData) {
            RMItem item = this.middlewareData.get(key);
            if (item != null) {
                return (RMItem)item.clone();
            }
            return null;
        }
    }

    // Writes a data item
    protected void writeData(int xid, String key, RMItem value)
    {
        synchronized(this.middlewareData) {
            this.middlewareData.put(key, value);
        }
    }

    // Remove the item out of storage
    protected void removeData(int xid, String key)
    {
        synchronized(this.middlewareData) {
            this.middlewareData.remove(key);
        }
    }

    // Deletes the encar item
    protected boolean deleteItem(int xid, String key)
    {
        info("RM::deleteItem(" + xid + ", " + key + ") called");
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
                info("RM::deleteItem(" + xid + ", " + key + ") item deleted");
                return true;
            }
            else
            {
                info("RM::deleteItem(" + xid + ", " + key + ") item can't be deleted because some customers have reserved it");
                return false;
            }
        }
    }

    // Query the number of available seats/rooms/cars
    protected int queryNum(int xid, String key)
    {
        info("RM::queryNum(" + xid + ", " + key + ") called");
        ReservableItem curObj = (ReservableItem)readData(xid, key);
        int value = 0;
        if (curObj != null)
        {
            value = curObj.getCount();
        }
        info("RM::queryNum(" + xid + ", " + key + ") returns count=" + value);
        return value;
    }

    // Query the price of an item
    protected int queryPrice(int xid, String key)
    {
        info("RM::queryPrice(" + xid + ", " + key + ") called");
        ReservableItem curObj = (ReservableItem)readData(xid, key);
        int value = 0;
        if (curObj != null)
        {
            value = curObj.getPrice();
        }
        info("RM::queryPrice(" + xid + ", " + key + ") returns cost=$" + value);
        return value;
    }

    // Reserve an item
    protected boolean reserveItem(int xid, int customerID, String key, String location)
    {
        info("RM::reserveItem(" + xid + ", customer=" + customerID + ", " + key + ", " + location + ") called" );
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

            info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
            return true;
        }
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
    {
      boolean response = false;
      try {
        response = m_flightsResourceManager.addFlight(xid, flightNum, flightSeats, flightPrice);
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
      response = m_carsResourceManager.addCars(xid, location, count, price);
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
      response = m_roomsResourceManager.addRooms(xid, location, count, price);
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
      response = m_flightsResourceManager.deleteFlight(xid, flightNum);
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
      response = m_carsResourceManager.deleteCars(xid, location);
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
      response = m_roomsResourceManager.deleteRooms(xid, location);
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
      response = m_flightsResourceManager.queryFlight(xid, flightNum);
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
      response = m_carsResourceManager.queryCars(xid, location);
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
      response = m_roomsResourceManager.queryRooms(xid, location);
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
      response = m_flightsResourceManager.queryFlightPrice(xid, flightNum);
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
      response = m_carsResourceManager.queryCarsPrice(xid, location);
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
      response = m_roomsResourceManager. queryRoomsPrice(xid, location);
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
      roomBill = m_roomsResourceManager.queryCustomerInfo(xid, customerID);
      carBill = m_carsResourceManager.queryCustomerInfo(xid, customerID);
      flightBill = m_flightsResourceManager.queryCustomerInfo(xid, customerID);
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
    m_roomsResourceManager.newCustomer(xid, cid);
    m_carsResourceManager.newCustomer(xid, cid);
    m_flightsResourceManager.newCustomer(xid, cid);
    return cid;
  }

  // TODO: need to ask prof how we want to add new customers to each resource servers
  public boolean newCustomer(int xid, int customerID) throws RemoteException
  {
    Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") called");
    Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
    if (customer == null)
    {
      m_roomsResourceManager.newCustomer(xid, customerID);
      m_carsResourceManager.newCustomer(xid, customerID);
      m_flightsResourceManager.newCustomer(xid, customerID);
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
      m_roomsResourceManager.deleteCustomer(xid, customerID);
      m_carsResourceManager.deleteCustomer(xid, customerID);
      m_flightsResourceManager.deleteCustomer(xid, customerID);
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
      response = m_flightsResourceManager.reserveFlight(xid, customerID, flightNum);
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
      response = m_carsResourceManager.reserveCar(xid, customerID, location);
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
      response = m_roomsResourceManager.reserveRoom(xid, customerID, location);
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
        return this.middlewareName;
    }
}
