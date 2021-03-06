package Server.Common;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class FlightsResourceManager extends ResourceManager {

    public FlightsResourceManager(String name) {
        super(name);
    }

    // Create a new flight, or add seats to existing flight
    // NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public synchronized boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice) 
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

    // Deletes flight
    public boolean deleteFlight(int xid, int flightNum) 
    {
        return deleteItem(xid, Flight.getKey(flightNum));
    }

    // Returns the number of empty seats in this flight
    public int queryFlight(int xid, int flightNum) 
    {
        return queryNum(xid, Flight.getKey(flightNum));
    }

    // Returns price of a seat in this flight
    public int queryFlightPrice(int xid, int flightNum) 
    {
        return queryPrice(xid, Flight.getKey(flightNum));
    }

    // Adds flight reservation to this customer
    public int reserveFlightItem(int xid, int customerID, int flightNumber)
    {
        Trace.info("RM::reserveFlightItem(" + xid + ", " + customerID + ", " + flightNumber + ") called");
        return reserveItem(xid, customerID, Flight.getKey(flightNumber), String.valueOf(flightNumber));
    }

    public Map<String, Integer> reserveFlightItemBundle(int xid, int customerID, Vector<String> flightNumbers)  {
        Trace.info("RM::reserveFlightItemBundle(" + xid + ", " + customerID + ", " + flightNumbers + ") called");
        Map<String, Integer> prices = new HashMap<>();
        for (String flightNum:flightNumbers)
        {
            int price = reserveFlightItem(xid, customerID, Integer.parseInt(flightNum));
            if (price>0) prices.put(flightNum, price);
        }
        return prices;
    }

    public String queryReservableFlights(int xid)  {
        Trace.info("RM::queryReservableFlights(" + xid + ") called");
        String response = this.m_data.values().size() > 0 ? "Flights:\n" : "";
        for (RMItem item:this.m_data.values())
        {
            Flight flight = (Flight)item;
            response += "FlightNum:"+flight.getFlightNumber()+" Seats:"+flight.getCount()+" Price:"+flight.getPrice()+"\n";
        }
        return response;
    }

}
