package Server.Middleware;

import Server.Common.*;
import Server.Interface.IResourceManager;
import java.rmi.RemoteException;
import java.util.*;
import java.util.Calendar;
import java.util.Vector;

public class Middleware implements IResourceManager {

    protected IResourceManager m_flightsResourceManager;
    protected IResourceManager m_carsResourceManager;
    protected IResourceManager m_roomsResourceManager;
    protected RMHashMap customersList;
    protected String middlewareName;

    public Middleware(String name) {
        try {
            this.middlewareName = name;
            this.m_flightsResourceManager = null;
            this.m_carsResourceManager = null;
            this.m_roomsResourceManager = null;
            this.customersList = new RMHashMap();
        } catch (Exception e) {
            System.out.println("\n*** Middleware error: " + e.getMessage() + " ***\n");
        }
    }

    public RMHashMap getCustomersList() {
        return customersList;
    }

    public void setCustomersList(RMHashMap customersList) {
        this.customersList = customersList;
    }

    // Reads a data item
    protected RMItem readData(int xid, String key)
    {
        synchronized(this.customersList) {
            RMItem item = this.customersList.get(key);
            if (item != null) {
                return (RMItem)item.clone();
            }
            return null;
        }
    }

    // Writes a data item
    protected void writeData(int xid, String key, RMItem value)
    {
        synchronized(this.customersList) {
            this.customersList.put(key, value);
        }
    }

    // Remove the item out of storage
    protected void removeData(int xid, String key)
    {
        synchronized(this.customersList) {
            this.customersList.remove(key);
        }
    }

    public String getName() throws RemoteException
    {
        return this.middlewareName;
    }

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
        Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ") called");
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            Trace.warn("RM::queryCustomerInfo(" + xid + ", " + customerID + ") failed--customer doesn't exist");
            // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
            return "No such customer";
        }
        else
        {
            Trace.info("RM::queryCustomerInfo(" + xid + ", " + customerID + ")");
            System.out.println(customer.getBill());
            return customer.getBill();
        }
    }

    public synchronized int newCustomer(int xid) throws RemoteException
    {
        int cid = 0; // 0 will be the default value returned if it failed to add customers at middleware
        try {
            Trace.info("RM::newCustomer(" + xid + ") called");
            cid = Integer.parseInt(String.valueOf(xid) +
                    String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                    String.valueOf(Math.round(Math.random() * 100 + 1)));
            Customer customer = new Customer(cid);
            writeData(xid, customer.getKey(), customer);
            Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
            return cid;
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return cid;
    }

    public synchronized boolean newCustomer(int xid, int customerID) throws RemoteException
    {
        try {
            Trace.info("RM::newCustomer(" + xid + ", " + customerID + ") called");
            Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
            if (customer == null)
            {
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
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return false;
    }

    public void cancelReservations(Object customer, int xid, int customerID) throws RemoteException {
        throw new RemoteException("\n*** Canceling a customer's reservations should be handled by the appropriate resource manager! ***\n");
    }

    public synchronized boolean deleteCustomer(int xid, int customerID) throws RemoteException
    {
        try {
            Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") called");
            Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
            if (customer == null)
            {
                Trace.warn("RM::deleteCustomer(" + xid + ", " + customerID + ") failed--customer doesn't exist");
                return false;
            }
            else
            {
                // canceling the reservations in all 3 resource servers
                this.m_carsResourceManager.cancelReservations(customer, xid, customerID);
                this.m_flightsResourceManager.cancelReservations(customer, xid, customerID);
                this.m_roomsResourceManager.cancelReservations(customer, xid, customerID);
                // Remove the customer from the storage
                removeData(xid, customer.getKey());
                Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
                return true;
            }
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return false;
    }

    // Adds flight reservation to this customer
    public synchronized boolean reserveFlight(int xid, int customerID, int flightNum) throws RemoteException
    {
        Boolean response = false;
        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            return false;
        }

        try {
            int price = m_flightsResourceManager.reserveFlightItem(xid, customerID, flightNum);
            if (price > -1)
            {
                customer.reserve(Flight.getKey(flightNum), String.valueOf(flightNum), price);
                writeData(xid, customer.getKey(), customer);
                response = true;
            }
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Adds car reservation to this customer
    public synchronized boolean reserveCar(int xid, int customerID, String location) throws RemoteException
    {
        Boolean response = false;
        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            return false;
        }
        try {
            int price = m_carsResourceManager.reserveCarItem(xid, customerID, location);
            if (price > -1)
            {
                customer.reserve(Car.getKey(location), location, price);
                writeData(xid, customer.getKey(), customer);
                response = true;
            }
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    // Adds room reservation to this customer
    public synchronized boolean reserveRoom(int xid, int customerID, String location) throws RemoteException
    {
        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            return false;
        }
        Boolean response = false;
        try {
            int price = m_roomsResourceManager.reserveRoomItem(xid, customerID, location);
            if (price > -1)
            {
                customer.reserve(Room.getKey(location), location, price);
                writeData(xid, customer.getKey(), customer);
                response = true;
            }
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    public int reserveFlightItem(int id, int customerID, int flightNumber) throws RemoteException {
        throw new RemoteException("\n*** Reserving Flight Item is handled in the specific ResourceManager! ***\n");
    }

    public int reserveCarItem(int id, int customerID, String location) throws RemoteException {
        throw new RemoteException("\n*** Reserving Car Item is handled in the specific ResourceManager! ***\n");
    }

    public int reserveRoomItem(int id, int customerID, String location) throws RemoteException {
        throw new RemoteException("\n*** Reserving Room Item is handled in the specific ResourceManager! ***\n");
    }

    public Map<String, Integer> reserveFlightItemBundle(int id, int customerID, Vector<String> flightNumbers) throws RemoteException {
        throw new RemoteException("\n*** Reserving Flight Item Bundle is handled in the specific ResourceManager! ***\n");
    }

    // Reserve bundle
    public synchronized boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException
    {
        Trace.info("RM::bundle(" + xid + ", " + customerId + ", " + flightNumbers + ", " + location + ", " + car + ", " + room + ") called");
        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer)readData(xid, Customer.getKey(customerId));
        if (customer == null)
        {
            return false;
        }
        Boolean response = false;
        try {
            if (flightNumbers.size() > 0)
            {
                Map<String, Integer> prices = m_flightsResourceManager.reserveFlightItemBundle(xid, customerId, flightNumbers);
                if (prices.size() > 0) {
                    for (String flightNum : prices.keySet())
                        customer.reserve(Flight.getKey(Integer.parseInt(flightNum)), flightNum, prices.get(flightNum));
                    response = true;
                }
            }
            if (car)
            {
                int price = m_carsResourceManager.reserveCarItem(xid, customerId, location);
                if (price > -1)
                {
                    customer.reserve(Car.getKey(location), location, price);
                    response = true;
                }
            }
            if (room)
            {
                int price = m_roomsResourceManager.reserveRoomItem(xid, customerId, location);
                if (price > -1)
                {
                    customer.reserve(Room.getKey(location), location, price);
                    response = true;
                }
            }
            writeData(xid, customer.getKey(), customer);
            Trace.info("RM::bundle(" + xid + ", " + customerId + ", " + flightNumbers + ", " + location + ", " + car + ", " + room + ") succeeded");
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    public String queryReservableFlights(int xid) throws RemoteException {
        throw new RemoteException("\n*** queryReservableFlights is handled in the specific ResourceManager! ***\n");
    }

    public String queryReservableCars(int xid) throws RemoteException {
        throw new RemoteException("\n*** queryReservableCars is handled in the specific ResourceManager! ***\n");
    }

    public String queryReservableRooms(int xid) throws RemoteException {
        throw new RemoteException("\n*** queryReservableRooms is handled in the specific ResourceManager! ***\n");
    }

    public String queryReservableItems(int xid, boolean flights, boolean cars, boolean rooms) throws RemoteException
    {
        Trace.info("RM::queryReservableItems(" + xid + ", " + flights + ", " + cars + ", " + rooms + ") called");
        String response = "";
        try {
            if (flights)
            {
                response += "Flights:\n" + m_flightsResourceManager.queryReservableFlights(xid);
            }
            if (cars)
            {
                response += "Cars:\n" + m_carsResourceManager.queryReservableCars(xid);
            }
            if (rooms)
            {
                response += "Rooms:\n" + m_roomsResourceManager.queryReservableRooms(xid);
            }
            Trace.info("RM::queryReservableItems(" + xid + ", " + flights + ", " + cars + ", " + rooms + ") succeed");
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    public String queryFlightReservers(int xid) throws RemoteException
    {
        String response = "";
        Collection<RMItem> customersList = this.getCustomersList().values();
        for(RMItem c: customersList) {
            Customer currentCustomer = (Customer)c;
            RMHashMap reservations = currentCustomer.getReservations();
            for(String reservedKey : reservations.keySet()) {
                ReservedItem reservedItem = currentCustomer.getReservedItem(reservedKey);
                if 


            }


        }


    }
}
