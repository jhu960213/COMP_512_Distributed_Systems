package Server.Common;

import Server.Interface.IResourceManager;
import java.rmi.RemoteException;
import java.util.*;


public class FlightsResourceManager implements IResourceManager {

    private String name;
    private RMHashMap data;

    public FlightsResourceManager(String name) {
        try {
            this.name = name;
            this.data = new RMHashMap();
        } catch (Exception e) {
            System.out.println("\n*** Flight resource manager error: " + e.getMessage() + " ***\n");
        }
    }

    // getters and setters
    public String getName() throws RemoteException
    {
        return name;
    }

    public void setName(String name) throws RemoteException
    {
        this.name = name;
    }

    public RMHashMap getData() throws RemoteException
    {
        return data;
    }

    public void setData(RMHashMap data) throws RemoteException
    {
        this.data = data;
    }

    // Reads a data item
    protected RMItem readData(int xid, String key)
    {
        synchronized(this.data) {
            RMItem item = this.data.get(key);
            if (item != null) {
                return (RMItem)item.clone();
            }
            return null;
        }
    }

    // Writes a data item
    protected void writeData(int xid, String key, RMItem value)
    {
        synchronized(this.data) {
            this.data.put(key, value);
        }
    }

    // Remove the item out of storage
    protected void removeData(int xid, String key)
    {
        synchronized(this.data) {
            this.data.remove(key);
        }
    }

    // Deletes the encar item
    protected synchronized boolean deleteItem(int xid, String key)
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
    protected synchronized int reserveItem(int xid, int customerID, String key, String location)
    {
        // Check if the item is available
        ReservableItem item = (ReservableItem)readData(xid, key);
        if (item == null)
        {
            Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--item doesn't exist");
            return -1;
        }
        else if (item.getCount() == 0)
        {
            Trace.warn("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") failed--No more items");
            return -1;
        }
        else
        {
            // Decrease the number of available items in the storage
            item.setCount(item.getCount() - 1);
            item.setReserved(item.getReserved() + 1);
            writeData(xid, item.getKey(), item);

            Trace.info("RM::reserveItem(" + xid + ", " + customerID + ", " + key + ", " + location + ") succeeded");
            return item.getPrice();
        }
    }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public synchronized boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws RemoteException
    {
        Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
        Flight curObj = (Flight)readData(xid, Flight.getKey(flightNum));
        if (curObj == null)
        {
            // Doesn't exist yet, add it
            Flight newObj = new Flight(flightNum, flightSeats, flightPrice);
            writeData(xid, newObj.getKey(), newObj);
            Trace.info("RM::addFlight(" + xid + ") created new flight " + flightNum + ", seats=" + flightSeats + ", price=$" + flightPrice);
        }
        else
        {
            // Add seats to existing flight and update the price if greater than zero
            curObj.setCount(curObj.getCount() + flightSeats);
            if (flightPrice > 0)
            {
                curObj.setPrice(flightPrice);
            }
            writeData(xid, curObj.getKey(), curObj);
            Trace.info("RM::addFlight(" + xid + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice);
        }
        return true;
    }

    public boolean addCars(int xid, String location, int count, int price) throws RemoteException
    {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    public boolean addRooms(int xid, String location, int count, int price) throws RemoteException
    {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum) throws RemoteException
    {
        return deleteItem(xid, Flight.getKey(flightNum));
    }

    // Delete cars at a location
    public boolean deleteCars(int xid, String location) throws RemoteException
    {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    // Delete rooms at a location
    public boolean deleteRooms(int xid, String location) throws RemoteException
    {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum) throws RemoteException
    {
        return queryNum(xid, Flight.getKey(flightNum));
    }

    // Returns the number of cars available at a location
    public int queryCars(int xid, String location) throws RemoteException
    {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    // Returns the amount of rooms available at a location
    public int queryRooms(int xid, String location) throws RemoteException
    {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) throws RemoteException
    {
        return queryPrice(xid, Flight.getKey(flightNum));
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location) throws RemoteException
    {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    // Returns room price at this location
    public int queryRoomsPrice(int xid, String location) throws RemoteException
    {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    public String queryCustomerInfo(int xid, int customerID) throws RemoteException
    {
        throw new RemoteException("\n*** Querying customers is handled in the middleware! ***\n");
    }

    public int newCustomer(int xid) throws RemoteException
    {
        throw new RemoteException("\n*** Adding new customer is handled in the middleware! ***\n");
    }

    public boolean newCustomer(int xid, int customerID) throws RemoteException
    {
        throw new RemoteException("\n*** Adding new customer is handled in the middleware! ***\n");
    }

    public boolean deleteCustomer(int xid, int customerID) throws RemoteException
    {
        throw new RemoteException("\n*** Deleting new customer is handled in the middleware! ***\n");
    }

    public boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
    {
        throw new RemoteException("\n*** Reserving Flight is handled in the middleware! ***\n");
    }

    public boolean reserveCar(int xid, int customerID, String location) throws RemoteException
    {
        throw new RemoteException("\n*** Reserving Car is handled in the middleware! ***\n");
    }

    public boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
    {
        throw new RemoteException("\n*** Reserving Room is handled in the middleware! ***\n");
    }

    // Adds flight reservation to this customer
    public int reserveFlightItem(int xid, int customerID, int flightNumber) throws RemoteException {
        return reserveItem(xid, customerID, Flight.getKey(flightNumber), String.valueOf(flightNumber));
    }

    // Adds car reservation to this customer
    public int reserveCarItem(int xid, int customerID, String location) throws RemoteException {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    // Adds room reservation to this customer
    public int reserveRoomItem(int xid, int customerID, String location) throws RemoteException {
        throw new RemoteException("\n*** Calling a wrong server! ***\n");
    }

    // Reserve bundle
    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
    {
        return false;
    }
    public Map<String, Integer> reserveFlightItemBundle(int xid, int customerID, Vector<String> flightNumbers) throws RemoteException {
        Map<String, Integer> prices = new HashMap<>();
        for (String flightNum:flightNumbers)
        {
            int price = reserveFlightItem(xid, customerID, Integer.parseInt(flightNum));
            if (price>0) prices.put(flightNum, price);
        }
        return prices;
    }
}
