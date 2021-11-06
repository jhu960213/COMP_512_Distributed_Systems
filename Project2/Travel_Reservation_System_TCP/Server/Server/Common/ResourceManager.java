// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.Common;

import Server.Interface.IResourceManager;
import Server.LockManager.DeadlockException;
import Server.LockManager.LockManager;
import Server.LockManager.TransactionLockObject;

import java.util.Map;
import java.util.Vector;

import static Server.Common.Trace.info;

public class ResourceManager implements IResourceManager
{
	protected String m_name = "";
	protected RMHashMap m_data = new RMHashMap();
	private LockManager lockManager = new LockManager();
	private TransactionDataManager transactionDataManager = new TransactionDataManager();

	public ResourceManager(String p_name)
	{
		m_name = p_name;
	}

	// Reads a data item
	protected RMItem readData(int xid, String key) throws DeadlockException
	{
		boolean lockGranted = lockManager.Lock(xid, key, TransactionLockObject.LockType.LOCK_READ);
		if (lockGranted) {
			synchronized(m_data) {
				RMItem item = m_data.get(key);
				if (item != null) {
					return (RMItem)item.clone();
				}
			}
		}
		return null;
	}

	// Writes a data item
	protected void writeData(int xid, String key, RMItem value) throws DeadlockException
	{
		boolean lockGranted = lockManager.Lock(xid, key, TransactionLockObject.LockType.LOCK_WRITE);
		if (lockGranted) {
			synchronized(m_data) {
				transactionDataManager.addUndoInfo(xid, key, m_data.get(key));
				m_data.put(key, value);
			}
		}
	}

	// Remove the item out of storage
	protected void removeData(int xid, String key) throws DeadlockException
	{
		boolean lockGranted = lockManager.Lock(xid, key, TransactionLockObject.LockType.LOCK_WRITE);
		if (lockGranted) {
			synchronized(m_data) {
				transactionDataManager.addUndoInfo(xid, key, m_data.get(key));
				m_data.remove(key);
			}
		}
	}

	// Writes a data item
	protected void undoWriteData(int xid, String key, RMItem value)
	{
		try {
			boolean lockGranted = lockManager.Lock(xid, key, TransactionLockObject.LockType.LOCK_WRITE);
			if (lockGranted) {
				synchronized(m_data) {
					if (value == null) m_data.remove(key);
					else m_data.put(key, value);
				}
			}
		} catch (Exception e) {

		}
	}

	// Deletes the encar item
	protected boolean deleteItem(int xid, String key) throws DeadlockException
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
	protected int queryNum(int xid, String key) throws DeadlockException
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
	protected int queryPrice(int xid, String key) throws DeadlockException
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
	protected int reserveItem(int xid, int customerID, String key, String location) throws DeadlockException
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
	public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) throws DeadlockException {
		return false;
	}

	// Create a new car location or add cars to an existing location
	// NOTE: if price <= 0 and the location already exists, it maintains its current price
	public boolean addCars(int xid, String location, int count, int price) throws DeadlockException
	{
		return false;
	}

	// Create a new room location or add rooms to an existing location
	// NOTE: if price <= 0 and the room location already exists, it maintains its current price
	public boolean addRooms(int xid, String location, int count, int price) throws DeadlockException
	{
		return false;
	}

	// Deletes flight
	public boolean deleteFlight(int xid, int flightNum) throws DeadlockException {
		return false;
	}

	// Delete cars at a location
	public boolean deleteCars(int xid, String location) throws DeadlockException
	{
		return false;
	}

	// Delete rooms at a location
	public boolean deleteRooms(int xid, String location) throws DeadlockException
	{
		return false;
	}

	// Returns the number of empty seats in this flight
	public int queryFlight(int xid, int flightNum) throws DeadlockException {
		return 0;
	}

	// Returns the number of cars available at a location
	public int queryCars(int xid, String location) throws DeadlockException
	{
		return 0;
	}

	// Returns the amount of rooms available at a location
	public int queryRooms(int xid, String location) throws DeadlockException
	{
		return 0;
	}

	// Returns price of a seat in this flight
	public int queryFlightPrice(int xid, int flightNum) throws DeadlockException
	{
		return 0;
	}

	// Returns price of cars at this location
	public int queryCarsPrice(int xid, String location) throws DeadlockException
	{
		return 0;
	}

	// Returns room price at this location
	public int queryRoomsPrice(int xid, String location) throws DeadlockException
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
	public boolean reserveFlight(int xid, int customerID, int flightNum) throws DeadlockException
	{
		return false;
	}

	// Adds car reservation to this customer
	public boolean reserveCar(int xid, int customerID, String location) throws DeadlockException
	{
		return false;
	}

	// Adds room reservation to this customer
	public boolean reserveRoom(int xid, int customerID, String location) throws DeadlockException
	{
		return false;
	}

	// Adds flight reservation to this customer
	public int reserveFlightItem(int xid, int customerID, int flightNumber) throws DeadlockException
	{
		return 0;
	}

	// Adds car reservation to this customer
	public int reserveCarItem(int xid, int customerID, String location) throws DeadlockException
	{
		return 0;
	}

	// Adds room reservation to this customer
	public int reserveRoomItem(int xid, int customerID, String location) throws DeadlockException
	{
		return 0;
	}

	// Reserve bundle 
	public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room)
	{
		return false;
	}

	public Map<String, Integer> reserveFlightItemBundle(int id, int customerID, Vector<String> flightNumbers) throws DeadlockException
	{
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

	public void cancelReservations(int xid, Customer customer, int customerID) throws DeadlockException
	{
		// loop through all the reservations the customer currently has and cancel them
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
	public int start() {
		return 0;
	}

	public boolean commit(int xid) {
		Trace.info("RM::commit(" + xid + ") called");
		transactionDataManager.cleanTransaction(xid);
		return lockManager.UnlockAll(xid);
	}

	public void abort(int xid) {
		Trace.info("RM::abort(" + xid + ") called");
		RMHashMap undo = transactionDataManager.undoTransactionDataList(xid);
		if (undo!=null && undo.size() > 0)
			for (Map.Entry<String, RMItem> entry: undo.entrySet())
				undoWriteData(xid, entry.getKey(), entry.getValue());
		lockManager.UnlockAll(xid);
	}

	public boolean shutdown() {
		return false;
	}
	public void checkTransaction(int transactionId, String methodName) {
	}

	public String getName()
	{
		return m_name;
	}
}
 
