// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Vector;

import static Server.Common.Trace.info;

public class ResourceManager implements IResourceManager
{
	protected String m_name = "";
	protected RMHashMap m_data = new RMHashMap();

	public ResourceManager(String p_name)
	{
		m_name = p_name;
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
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
	{
		return false;
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price) 
	{
		return false;
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int xid, String location, int count, int price) 
	{
		return false;
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum) 
	{
		return false;
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location) 
	{
		return false;
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location)
	{
		return false;
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum) 
	{
		return 0;
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location) 
	{
		return 0;
	}

	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location) 
	{
		return 0;
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum) 
	{
		return 0;
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location) 
	{
		return 0;
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location) 
	{
		return 0;
	}

	public String queryCustomerInfo(int xid, int customerID) 
	{
		return null;
	}

	public int newCustomer(int xid) 
	{
		return 0;
	}

	public boolean newCustomer(int xid, int customerID) 
	{
		return false;
	}

	public boolean deleteCustomer(int xid, int customerID) 
	{
		return false;
	}

	// Adds flight reservation to this customer
	public boolean reserveFlight(int xid, int customerID, int flightNum) 
	{
		return false;
	}

	// Adds car reservation to this customer
	public boolean reserveCar(int xid, int customerID, String location) 
	{
		return false;
	}

	// Adds room reservation to this customer
	public boolean reserveRoom(int xid, int customerID, String location) 
	{
		return false;
	}

	// Adds flight reservation to this customer
	public int reserveFlightItem(int xid, int customerID, int flightNumber)
	{
		return 0;
	}

	// Adds car reservation to this customer
	public int reserveCarItem(int xid, int customerID, String location)
	{
		return 0;
	}

	// Adds room reservation to this customer
	public int reserveRoomItem(int xid, int customerID, String location)
	{
		return 0;
	}

	// Reserve bundle 
	public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) 
	{
		return false;
	}

	public Map<String, Integer> reserveFlightItemBundle(int id, int customerID, Vector<String> flightNumbers)  {
		return null;
	}


	public String queryReservableFlights(int xid)  {
		return null;
	}

	public String queryReservableCars(int xid)  {
		return null;
	}

	public String queryReservableRooms(int xid)  {
		return null;
	}

	public String queryReservableItems(int xid, boolean flights, boolean cars, boolean rooms)  {
		return null;
	}

	public String queryFlightReservers(int xid)  {
		return null;

	}

	public String queryCarReservers(int xid)  {
		return null;

	}

	public String queryRoomReservers(int xid)  {
		return null;

	}

	public synchronized void cancelReservations(Object customerObj, int xid, int customerID)  {

		// loop through all the reservations the customer currently has and cancel them
		Customer customer = (Customer)customerObj;
		RMHashMap reservations = customer.getReservations();
		for (String reservedKey : reservations.keySet())
		{
			ReservedItem reservedItem = customer.getReservedItem(reservedKey);
			Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reservedItem.getKey() + " " +  reservedItem.getCount() +  " times");
			ReservableItem reservableItem = (ReservableItem)readData(xid, reservedItem.getKey());
			if (reservableItem != null ) {
				info("RM::deleteCustomer(" + xid + ", " + customerID + ") has reserved " + reservedItem.getKey() + " which is reserved " + reservableItem.getReserved() + " times and is still available " + reservableItem.getCount() + " times");
				reservableItem.setReserved(reservableItem.getReserved() - reservedItem.getCount());
				reservableItem.setCount(reservableItem.getCount() + reservedItem.getCount());
				writeData(xid, reservableItem.getKey(), reservableItem);
			}
		}
		Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") now has no reservations pertaining to " + this.getName() + "!");
	}

	public String getName() 
	{
		return m_name;
	}
}
 
