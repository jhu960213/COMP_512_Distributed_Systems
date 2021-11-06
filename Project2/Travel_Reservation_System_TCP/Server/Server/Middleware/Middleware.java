package Server.Middleware;

import Server.Common.*;
import Server.Interface.IResourceManager;
import Server.Exception.TransactionAbortedException;
import Server.LockManager.DeadlockException;
import Server.Exception.InvalidTransactionException;
import Server.LockManager.LockManager;
import Server.LockManager.TransactionLockObject;
import Server.ResourceServer.ServerSocketThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Middleware implements IResourceManager {
    public enum ResourceServer {
        Middleware, Flights, Cars, Rooms
    }
    private static int middlewareRegistryPortNum = 5004;

    private static String flightsResourceServerHost = "localhost";
    private static int flightsResourceServerPort = 2004;

    private static String carsResourceServerHost = "localhost";
    private static int carsResourceServerPort = 3004;

    private static String roomsResourceServerHost = "localhost";
    private static int roomsResourceServerPort = 4004;

    protected RMHashMap customersList;
    private TransactionManager transactionManager;
    private LockManager lockManager;
    private TransactionDataManager transactionDataManager;

    public Middleware() {
        try {
            this.customersList = new RMHashMap();
            this.transactionManager = new TransactionManager();
            this.lockManager = new LockManager();
            this.transactionDataManager = new TransactionDataManager();
        } catch (Exception e) {
            System.out.println("\n*** Middleware error: " + e.getMessage() + " ***\n");
        }
    }

    public String getName() {
        return "Middleware";
    }

    public static void main(String args[])
    {
        if (args.length > 0) flightsResourceServerHost = args[0];
        if (args.length > 1) carsResourceServerHost = args[1];
        if (args.length > 2) roomsResourceServerHost = args[2];
        if (args.length > 3) flightsResourceServerPort = Integer.parseInt(args[3]);
        if (args.length > 4) carsResourceServerPort = Integer.parseInt(args[4]);
        if (args.length > 5) roomsResourceServerPort = Integer.parseInt(args[5]);
        if (args.length > 6) middlewareRegistryPortNum = Integer.parseInt(args[6]);

        Middleware server= new Middleware();
        try {
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

    public RMHashMap getCustomersList() {
        return customersList;
    }

    public void setCustomersList(RMHashMap customersList) {
        this.customersList = customersList;
    }

    // Reads a data item
    protected RMItem readData(int xid, String key)
    {
        transactionManager.addDataOperation(xid, ResourceServer.Middleware);
        try {
            Boolean lockGranted = lockManager.Lock(xid, key, TransactionLockObject.LockType.LOCK_READ);
            if (lockGranted) {
                synchronized(this.customersList) {
                    RMItem item = this.customersList.get(key);
                    if (item != null) {
                        return (RMItem)item.clone();
                    }
                }
            }
        } catch (DeadlockException e) {
            passivelyAbort(e.getXId());
            e.printStackTrace();
        }
        return null;
    }

    // Writes a data item
    protected void writeData(int xid, String key, RMItem value)
    {
        transactionManager.addDataOperation(xid, ResourceServer.Middleware);
        try {
            Boolean lockGranted = lockManager.Lock(xid, key, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                synchronized(this.customersList) {
                    transactionDataManager.addUndoInfo(xid, key, customersList.get(key));
                    this.customersList.put(key, value);
                }
            }
        } catch (DeadlockException e) {
            passivelyAbort(e.getXId());
            e.printStackTrace();
        }
    }

    // Remove the item out of storage
    protected void removeData(int xid, String key)
    {
        transactionManager.addDataOperation(xid, ResourceServer.Middleware);
        try {
            Boolean lockGranted = lockManager.Lock(xid, key, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                synchronized(this.customersList) {
                    transactionDataManager.addUndoInfo(xid, key, customersList.get(key));
                    this.customersList.remove(key);
                }
            }
        } catch (DeadlockException e) {
            passivelyAbort(e.getXId());
            e.printStackTrace();
        }
    }
    protected void undoWriteData(int xid, String key, RMItem value)
    {
        try {
            boolean lockGranted = lockManager.Lock(xid, key, TransactionLockObject.LockType.LOCK_WRITE);
            if (lockGranted) {
                synchronized(customersList) {
                    if (value == null) customersList.remove(key);
                    else customersList.put(key, value);
                }
            }
        } catch (Exception e) {

        }
    }

    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) {
        Trace.info("RM::addFlight(" + xid + ", " + flightNum + ", " + flightSeats + ", $" + flightPrice + ") called");
        Boolean response = false;
        try {
            response = (Boolean) callResourceServerMethod(ResourceServer.Flights, "addFlight", new Object[]{Integer.valueOf(xid), Integer.valueOf(flightNum), Integer.valueOf(flightSeats), Integer.valueOf(flightPrice)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware addFlight response:" + response);
        return response;
    }

    public boolean addCars(int xid, String location, int numCars, int price) {
        Boolean response = false;
        try {
            response = (Boolean) callResourceServerMethod(ResourceServer.Cars, "addCars", new Object[]{Integer.valueOf(xid), location, Integer.valueOf(numCars), Integer.valueOf(price)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware addCars response:" + response);
        return response;
    }

    public boolean addRooms(int xid, String location, int numRooms, int price) {
        Boolean response = false;
        try {
            response = (Boolean) callResourceServerMethod(ResourceServer.Rooms, "addRooms", new Object[]{Integer.valueOf(xid), location, Integer.valueOf(numRooms), Integer.valueOf(price)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware addRooms response:" + response);
        return response;
    }

    public int newCustomer(int xid) {
        int cid = 0; // 0 will be the default value returned if it failed to add customers at middleware
        Trace.info("RM::newCustomer(" + xid + ") called");
        cid = Integer.parseInt(String.valueOf(xid) +
                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                String.valueOf(Math.round(Math.random() * 100 + 1)));
        Customer customer = new Customer(cid);
        writeData(xid, customer.getKey(), customer);
        Trace.info("RM::newCustomer(" + cid + ") returns ID=" + cid);
        return cid;
    }

    public boolean newCustomer(int xid, int customerID)
    {
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
    }

    public void cancelReservations(int xid, Customer customer, int customerID) throws DeadlockException {

    }

    public boolean deleteFlight(int xid, int flightNum) {
        Boolean response = false;
        try {
            response = (Boolean) callResourceServerMethod(ResourceServer.Flights, "deleteFlight", new Object[]{Integer.valueOf(xid), Integer.valueOf(flightNum)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware deleteFlight response:" + response);
        return response;
    }

    public boolean deleteCars(int xid, String location) {
        Boolean response = false;
        try {
            response = (Boolean) callResourceServerMethod(ResourceServer.Cars, "deleteCars", new Object[]{Integer.valueOf(xid), location});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware deleteCars response:" + response);
        return response;
    }

    public boolean deleteRooms(int xid, String location) {
        Boolean response = false;
        try {
            response = (Boolean) callResourceServerMethod(ResourceServer.Rooms, "deleteRooms", new Object[]{Integer.valueOf(xid), location});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware deleteRooms response:" + response);
        return response;
    }

    public boolean deleteCustomer(int xid, int customerID) {
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
                callResourceServerMethod(ResourceServer.Flights, "cancelReservations", new Object[]{Integer.valueOf(xid), customer, Integer.valueOf(customerID)});
                callResourceServerMethod(ResourceServer.Cars, "cancelReservations", new Object[]{Integer.valueOf(xid), customer, Integer.valueOf(customerID)});
                callResourceServerMethod(ResourceServer.Rooms, "cancelReservations", new Object[]{Integer.valueOf(xid), customer, Integer.valueOf(customerID)});
                // Remove the customer from the storage
                removeData(xid, customer.getKey());
                Trace.info("RM::deleteCustomer(" + xid + ", " + customerID + ") succeeded");
                return true;
            }
        } catch (Throwable e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return false;
    }

    public int queryFlight(int xid, int flightNumber) {
        int response = 0;
        try {
            response = (Integer) callResourceServerMethod(ResourceServer.Flights, "queryFlight", new Object[]{Integer.valueOf(xid), Integer.valueOf(flightNumber)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryFlight response:" + response);
        return response;
    }

    public int queryCars(int xid, String location) {
        int response = 0;
        try {
            response = (Integer)callResourceServerMethod(ResourceServer.Cars, "queryCars", new Object[]{Integer.valueOf(xid), location});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryCars response:" + response);
        return response;
    }

    public int queryRooms(int xid, String location) {
        int response = 0;
        try {
            response = (Integer)callResourceServerMethod(ResourceServer.Rooms, "queryRooms", new Object[]{Integer.valueOf(xid), location});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryRooms response:" + response);
        return response;
    }

    public String queryCustomerInfo(int xid, int customerID)
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

    public int queryFlightPrice(int xid, int flightNumber) {
        int response = 0;
        try {
            response = (Integer)callResourceServerMethod(ResourceServer.Flights, "queryFlightPrice", new Object[]{Integer.valueOf(xid), Integer.valueOf(flightNumber)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware addFlight queryFlightPrice:" + response);
        return response;
    }

    public int queryCarsPrice(int xid, String location) {
        int response = 0;
        try {
            response = (Integer)callResourceServerMethod(ResourceServer.Cars, "queryCarsPrice", new Object[]{Integer.valueOf(xid), location});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryCarsPrice response:" + response);
        return response;
    }

    public int queryRoomsPrice(int xid, String location) {
        int response = 0;
        try {
            response = (Integer)callResourceServerMethod(ResourceServer.Rooms, "queryRoomsPrice", new Object[]{Integer.valueOf(xid), location});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        System.out.println("Middleware queryRoomsPrice response:" + response);
        return response;
    }

    public boolean reserveFlight(int xid, int customerID, int flightNumber) {
        Trace.info("RM::reserveFlight(" + xid + ", " + customerID + ", " + flightNumber + ") called");
        Boolean response = false;
        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            return false;
        }

        try {
            Integer price = (Integer) callResourceServerMethod(ResourceServer.Flights, "reserveFlightItem", new Object[]{Integer.valueOf(xid), customerID, flightNumber});
            if (price > -1)
            {
                customer.reserve(Flight.getKey(flightNumber), String.valueOf(flightNumber), price);
                writeData(xid, customer.getKey(), customer);
                response = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean reserveCar(int xid, int customerID, String location) {
        Trace.info("RM::reserveCar(" + xid + ", " + customerID + ", " + location + ") called");
        Boolean response = false;
        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            return false;
        }

        try {
            Integer price = (Integer) callResourceServerMethod(ResourceServer.Cars, "reserveCarItem", new Object[]{Integer.valueOf(xid), customerID, location});
            if (price > -1)
            {
                customer.reserve(Car.getKey(location), location, price);
                writeData(xid, customer.getKey(), customer);
                response = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean reserveRoom(int xid, int customerID, String location) {
        Trace.info("RM::reserveRoom(" + xid + ", " + customerID + ", " + location + ") called");
        Boolean response = false;
        // Read customer object if it exists (and read lock it)
        Customer customer = (Customer)readData(xid, Customer.getKey(customerID));
        if (customer == null)
        {
            return false;
        }

        try {
            Integer price = (Integer) callResourceServerMethod(ResourceServer.Rooms, "reserveRoomItem", new Object[]{Integer.valueOf(xid), customerID, location});
            if (price > -1)
            {
                customer.reserve(Room.getKey(location), location, price);
                writeData(xid, customer.getKey(), customer);
                response = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return response;
    }

    public int reserveFlightItem(int xid, int customerID, int flightNumber) throws DeadlockException {
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

    public boolean bundle(int xid, int customerId, Vector<String> flightNumbers, String location, boolean car, boolean room)
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
                Map<String, Integer> prices = (Map<String, Integer>) callResourceServerMethod(ResourceServer.Flights, "reserveFlightItemBundle", new Object[]{Integer.valueOf(xid), customerId, flightNumbers});
                if (prices.size() > 0) {
                    for (String flightNum : prices.keySet())
                        customer.reserve(Flight.getKey(Integer.parseInt(flightNum)), flightNum, prices.get(flightNum));
                    response = true;
                }
            }
            if (car)
            {
                Integer price = (Integer) callResourceServerMethod(ResourceServer.Cars, "reserveCarItem", new Object[]{Integer.valueOf(xid), customerId, location});
                if (price > -1)
                {
                    customer.reserve(Car.getKey(location), location, price);
                    response = true;
                }
            }
            if (room)
            {
                Integer price = (Integer) callResourceServerMethod(ResourceServer.Rooms, "reserveRoomItem", new Object[]{Integer.valueOf(xid), customerId, location});
                if (price > -1)
                {
                    customer.reserve(Room.getKey(location), location, price);
                    response = true;
                }
            }
            writeData(xid, customer.getKey(), customer);
            Trace.info("RM::bundle(" + xid + ", " + customerId + ", " + flightNumbers + ", " + location + ", " + car + ", " + room + ") succeeded");
        } catch (Throwable e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    public Map<String, Integer> reserveFlightItemBundle(int xid, int customerID, Vector<String> flightNumbers)
    {
        //Should not enter
        return null;
    }


    public String queryReservableFlights(int xid) {
        String response = null;
        try {
            response = (String) callResourceServerMethod(ResourceServer.Flights, "queryReservableFlights", new Object[]{Integer.valueOf(xid)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return response;
    }

    public String queryReservableCars(int xid) {
        String response = null;
        try {
            response = (String) callResourceServerMethod(ResourceServer.Cars, "queryReservableCars", new Object[]{Integer.valueOf(xid)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return response;
    }

    public String queryReservableRooms(int xid) {
        String response = null;
        try {
            response = (String) callResourceServerMethod(ResourceServer.Rooms, "queryReservableRooms", new Object[]{Integer.valueOf(xid)});
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return response;
    }
    public String queryReservableItems(int xid, boolean flights, boolean cars, boolean rooms) {
        Trace.info("RM::queryReservableItems(" + xid + ", " + flights + ", " + cars + ", " + rooms + ") called");
        String response = "";
        try {
            if (flights) response += callResourceServerMethod(ResourceServer.Flights, "queryReservableFlights", new Object[]{Integer.valueOf(xid)});
            if (cars) response += callResourceServerMethod(ResourceServer.Cars, "queryReservableCars", new Object[]{Integer.valueOf(xid)});
            if (rooms) response += callResourceServerMethod(ResourceServer.Rooms, "queryReservableRooms", new Object[]{Integer.valueOf(xid)});
            Trace.info("RM::queryReservableItems(" + xid + ", " + flights + ", " + cars + ", " + rooms + ") succeed");
        } catch (Throwable e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }
    public String queryFlightReservers(int xid) {
        String response = "";
        try {
            Trace.info("RM::queryFlightReservers(" + xid + ") called");
            Collection<RMItem> customersList = this.getCustomersList().values();
            for (RMItem c : customersList) {
                Customer currentCustomer = (Customer) c;
                RMHashMap reservations = currentCustomer.getReservations();
                for (String reservedKey : reservations.keySet()) {
                    ReservedItem reservedItem = currentCustomer.getReservedItem(reservedKey);
                    if (reservedItem.getItemType() == ReservedItem.ItemType.Flight) {
                        response += "Customer ID:" + currentCustomer.getKey() + "|reserved:" + reservedItem.getCount()
                                + " seat(s) |flightNum: " + reservedItem.getLocation() + " |at: $" + reservedItem.getPrice() + "|\n";
                    }
                }
            }
            return response;
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    public String queryCarReservers(int xid) {
        String response = "";
        try {
            Trace.info("RM::queryCarReservers(" + xid + ") called");
            Collection<RMItem> customersList = this.getCustomersList().values();
            for (RMItem c : customersList) {
                Customer currentCustomer = (Customer) c;
                RMHashMap reservations = currentCustomer.getReservations();
                for (String reservedKey : reservations.keySet()) {
                    ReservedItem reservedItem = currentCustomer.getReservedItem(reservedKey);
                    if (reservedItem.getItemType() == ReservedItem.ItemType.Car) {
                        response += "Customer ID: " + currentCustomer.getKey() + "|reserved: " + reservedItem.getCount()
                                + "car(s) |location: " + reservedItem.getLocation() + " |at: $" + reservedItem.getPrice() + "|\n";
                    }
                }
            }
            return response;
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    public String queryRoomReservers(int xid) {
        String response = "";
        try {
            Trace.info("RM::queryRoomReservers(" + xid + ") called");
            Collection<RMItem> customersList = this.getCustomersList().values();
            for (RMItem c : customersList) {
                Customer currentCustomer = (Customer) c;
                RMHashMap reservations = currentCustomer.getReservations();
                for (String reservedKey : reservations.keySet()) {
                    ReservedItem reservedItem = currentCustomer.getReservedItem(reservedKey);
                    if (reservedItem.getItemType() == ReservedItem.ItemType.Room) {
                        response += "Customer ID: " + currentCustomer.getKey() + "|reserved: " + reservedItem.getCount()
                                + "room(s) |location: " + reservedItem.getLocation() + " |at: $" + reservedItem.getPrice() + "|\n";
                    }
                }
            }
            return response;
        } catch (Exception e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return response;
    }

    public int start() {
        Trace.info("Middleware::start() called");
        return transactionManager.start();
    }

    public boolean commit(int xid) throws InvalidTransactionException, TransactionAbortedException{
        checkTransaction(xid, "commit");
        try {
            Trace.info("Middleware::commit(" + xid + ") called");
            for (ResourceServer type: transactionManager.dataOperationsOfTransaction(xid))
                if (type == ResourceServer.Middleware) {
                    transactionDataManager.cleanTransaction(xid);
                    if (!lockManager.UnlockAll(xid)) return false;
                } else {
                    if (!(Boolean)callResourceServerMethod(type, "commit", new Object[]{Integer.valueOf(xid)})) return false;
                }
            return transactionManager.commit(xid);
        } catch (Throwable e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
        return false;
    }

    public void abort(int xid) throws InvalidTransactionException {
        try {
            checkTransaction(xid, "abort");
            Trace.info("Middleware::abort(" + xid + ") called");
            for (ResourceServer type: transactionManager.dataOperationsOfTransaction(xid))
                if (type == ResourceServer.Middleware) {
                    RMHashMap undo = transactionDataManager.undoTransactionDataList(xid);
                    if (undo != null && undo.size() > 0)
                        for (Map.Entry<String, RMItem> entry: undo.entrySet())
                            undoWriteData(xid, entry.getKey(), entry.getValue());
                    lockManager.UnlockAll(xid);
                } else {
                    callResourceServerMethod(type, "abort", new Object[]{Integer.valueOf(xid)});
                }
            transactionManager.abort(xid);
        } catch (Throwable e) {
            if (e instanceof InvalidTransactionException) throw (InvalidTransactionException) e;
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
    }

    public void passivelyAbort(int xid) {
        try {
            Trace.info("Middleware::passivelyAbort(" + xid + ") called");
            for (ResourceServer type: transactionManager.dataOperationsOfTransaction(xid))
                if (type == ResourceServer.Middleware) {
                    RMHashMap undo = transactionDataManager.undoTransactionDataList(xid);
                    if (undo != null && undo.size() > 0)
                        for (Map.Entry<String, RMItem> entry: undo.entrySet())
                            undoWriteData(xid, entry.getKey(), entry.getValue());
                    lockManager.UnlockAll(xid);
                } else {
                    callResourceServerMethod(type, "abort", new Object[]{Integer.valueOf(xid)});
                }
            transactionManager.abort(xid);
        } catch (Throwable e) {
            System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
        }
    }

    public boolean shutdown() {
        Trace.info("Middleware::shutdown() called");
        try {
            Boolean res = (Boolean) callResourceServerMethod(ResourceServer.Flights, "shutdown", new Object[]{});
            if (!res) return false;
        } catch (Throwable e) {
        }

        try {
            Boolean res = (Boolean) callResourceServerMethod(ResourceServer.Cars, "shutdown", new Object[]{});
            if (!res) return false;
        } catch (Throwable e) {
        }

        try {
            Boolean res = (Boolean) callResourceServerMethod(ResourceServer.Rooms, "shutdown", new Object[]{});
            if (!res) return false;
        } catch (Throwable e) {
        }
        return true;
    }
    public void checkTransaction(int transactionId, String methodName) throws TransactionAbortedException, InvalidTransactionException {
        Trace.info("Middleware::checkTransaction(" + transactionId + "," + methodName + ") called");
        transactionManager.checkTransaction(transactionId, methodName);
    }

    public Object callResourceServerMethod(ResourceServer resourceServer, String methodName, Object[] argList) throws Throwable {
        String host = "";
        int port = 0;
        switch (resourceServer)
        {
            case Flights: host = flightsResourceServerHost; port = flightsResourceServerPort; break;
            case Cars: host = carsResourceServerHost; port = carsResourceServerPort; break;
            case Rooms: host = roomsResourceServerHost; port = roomsResourceServerPort; break;
        }

        if (argList.length > 0 && !methodName.equals("commit") && !methodName.equals("abort")) {
            transactionManager.addDataOperation((Integer) argList[0], resourceServer);
        }

        Socket socket= new Socket(host, port);
        ObjectOutputStream outToServer = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inFromServer = new ObjectInputStream(socket.getInputStream());

        outToServer.writeObject(methodName);
        outToServer.writeObject(argList);
        Object returnObj =  inFromServer.readObject();

        if (returnObj instanceof Throwable) {
            if (returnObj instanceof DeadlockException) {
                passivelyAbort(((DeadlockException)returnObj).getXId());
            }
            throw (Throwable) returnObj;
        }
        return returnObj;
    }
}
