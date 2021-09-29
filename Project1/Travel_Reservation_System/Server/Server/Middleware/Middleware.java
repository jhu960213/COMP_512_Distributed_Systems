package Server.Middleware;

import Server.Common.*;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Vector;
import static Server.Common.Trace.info;


public class Middleware implements IResourceManager {

    protected IResourceManager m_flightsResourceManager;
    protected IResourceManager m_carsResourceManager;
    protected IResourceManager m_roomsResourceManager;
    protected String middlewareName;

    public Middleware(String name) {
        try {
            this.middlewareName = name;
            this.m_flightsResourceManager = null;
            this.m_carsResourceManager = null;
            this.m_roomsResourceManager = null;
        } catch (Exception e) {
            System.out.println("\n*** Middleware error: " + e.getMessage() + " ***\n");
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

  public int newCustomer(int xid) throws RemoteException
  {
      int cid = 0; // 0 will be the default value returned if it failed to add customers in all 3 resource servers
      try {
          cid = Integer.parseInt(String.valueOf(xid) +
                  String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                  String.valueOf(Math.round(Math.random() * 100 + 1)));
          boolean tmp1 = m_roomsResourceManager.newCustomer(xid, cid);
          boolean tmp2 = m_carsResourceManager.newCustomer(xid, cid);
          boolean tmp3 = m_flightsResourceManager.newCustomer(xid, cid);
          if (tmp1 && tmp2 && tmp3) {
              Trace.info("RM::newCustomer(Successfully added new customers to all 3 resource servers!)");
          } else {
              Trace.info("RM::newCustomer(At least 1 or more resource servers failed to add a new customer!)");
          }
          return cid;
      } catch (Exception e) {
          System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
      }
      return cid;
  }

  public boolean newCustomer(int xid, int customerID) throws RemoteException
  {
      try {
          boolean tmp1 = this.m_carsResourceManager.newCustomer(xid, customerID);
          boolean tmp2 = this.m_roomsResourceManager.newCustomer(xid, customerID);
          boolean tmp3 = this.m_flightsResourceManager.newCustomer(xid, customerID);
          if (tmp1 && tmp2 && tmp3) {
              Trace.info("RM::newCustomer(Successfully added new customers to all 3 resource servers!)");
              return true;
          } else {
              Trace.info("RM::newCustomer(At least 1 or more resource servers failed to add a new customer!)");
              return false;
          }
      } catch (Exception e) {
          System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
      }
      return false;
  }

  public boolean deleteCustomer(int xid, int customerID) throws RemoteException
  {
      try {
          boolean tmp1 = this.m_carsResourceManager.deleteCustomer(xid, customerID);
          boolean tmp2 = this.m_roomsResourceManager.deleteCustomer(xid, customerID);
          boolean tmp3 = this.m_flightsResourceManager.deleteCustomer(xid, customerID);
          if (tmp1 && tmp2 && tmp3) {
              return true;
          } else {
              return false;
          }
      } catch (Exception e) {
          System.out.println("\nMiddleware server exception: " + e.getMessage() + "\n");
      }
      return false;
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

}
