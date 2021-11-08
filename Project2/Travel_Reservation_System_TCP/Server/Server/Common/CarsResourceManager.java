package Server.Common;

import Server.LockManager.DeadlockException;

import java.rmi.RemoteException;

public class CarsResourceManager extends ResourceManager {

    public CarsResourceManager(String name) {
        super(name);
    }

    // Create a new car location or add cars to an existing location
    // NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int xid, String location, int count, int price) throws DeadlockException
    {
        Trace.info("RM::addCars(" + xid + ", " + location + ", " + count + ", $" + price + ") called");
        Car curObj = (Car)readData(xid, Car.getKey(location));
        if (curObj == null)
        {
            // Car location doesn't exist yet, add it
            Car newObj = new Car(location, count, price);
            writeData(xid, newObj.getKey(), newObj);
            Trace.info("RM::addCars(" + xid + ") created new location " + location + ", count=" + count + ", price=$" + price);
        }
        else
        {
            // Add count to existing car location and update price if greater than zero
            curObj.setCount(curObj.getCount() + count);
            if (price > 0)
            {
                curObj.setPrice(price);
            }
            writeData(xid, curObj.getKey(), curObj);
            Trace.info("RM::addCars(" + xid + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price);
        }
        return true;
    }

    // Delete cars at a location
    public boolean deleteCars(int xid, String location) throws DeadlockException
    {
        return deleteItem(xid, Car.getKey(location));
    }

    // Returns the number of cars available at a location
    public int queryCars(int xid, String location) throws DeadlockException
    {
        return queryNum(xid, Car.getKey(location));
    }

    // Returns price of cars at this location
    public int queryCarsPrice(int xid, String location) throws DeadlockException
    {
        return queryPrice(xid, Car.getKey(location));
    }

    // Adds car reservation to this customer
    public int reserveCarItem(int xid, int customerID, String location) throws DeadlockException {
        Trace.info("RM::reserveCarItem(" + xid + ", " + customerID + ", " + location + ") called");
        return reserveItem(xid, customerID, Car.getKey(location), location);
    }

    public String queryReservableCars(int xid) {
        Trace.info("RM::queryReservableCars(" + xid + ") called");
        String response = this.m_data.values().size() > 0 ? "Cars:\n" : "";
        for (RMItem item:this.m_data.values())
        {
            Car car = (Car) item;
            response += "Location:"+car.getLocation()+" Counts:"+car.getCount()+" Price:"+car.getPrice()+"\n";
        }
        return response;
    }
}
