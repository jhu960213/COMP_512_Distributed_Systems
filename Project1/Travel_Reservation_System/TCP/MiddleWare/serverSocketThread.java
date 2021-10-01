import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class serverSocketThread extends Thread
{
  Socket socket;
  serverSocketThread (Socket socket)
  { this.socket=socket; }

  public void run()
  {
    try
    {
      BufferedReader inFromClient= new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
      String message = null;

      String FlightServer = "FlightServer";
      String CarServer = "CarServer";
      String RoomServer = "RoomServer";
      int FlightPort = 3004;
      int CarPort = 2004;
      int RoomPort = 1004;

      while (((message = inFromClient.readLine())!=null) && (message != "Quit"))
      {
        System.out.println("message:"+message);
        String result="Working!";

//        simpleMath sm=new simpleMath();
        String[] params =  message.split(",");
        // int x= Integer.parseInt(params[1]);
        // int y= Integer.parseInt(params[2]);
        int res=0;
        System.out.println(params[0] +"--"+params[1]+"--"+params[2]);
        if (params[0].equals("addFlight")){
          command(FlightServer, FlightPort, message);
        }
        // (int id, int flightNum, int flightSeats, int flightPrice)

        /**
         * Add car at a location.
         *
         * This should look a lot like addFlight, only keyed on a string location
         * instead of a flight number.
         *
         * @return Success
         */
        else if (params[0].equals("addCars")){
          command(CarServer, CarPort, message);
        }
        // (int id, String location, int numCars, int price)
        /**
         * Add room at a location.
         *
         * This should look a lot like addFlight, only keyed on a string location
         * instead of a flight number.
         *
         * @return Success
         */
        else if (params[0].equals("addRooms")){
          command(RoomServer, RoomPort, message);
        }
        // (int id, String location, int numRooms, int price)

        /**
         * Add customer.
         *
         * @return Unique customer identifier
         */
        else if (params[0].equals("newCustomer")){

        }
        // (int id)


        /**
         * Add customer with id.
         *
         * @return Success
         */
        else if (params[0].equals("newCustomer")){

        }
        // (int id, int cid)

        /**
         * Delete the flight.
         *
         * deleteFlight implies whole deletion of the flight. If there is a
         * reservation on the flight, then the flight cannot be deleted
         *
         * @return Success
         */
        else if (params[0].equals("deleteFlight")){
          command(FlightServer, FlightPort, message);
        }
        // (int id, int flightNum)


        /**
         * Delete all cars at a location.
         *
         * It may not succeed if there are reservations for this location
         *
         * @return Success
         */
        else if (params[0].equals("deleteCars")){
          command(CarServer, CarPort, message);

        }
        // (int id, String location)


        /**
         * Delete all rooms at a location.
         *
         * It may not succeed if there are reservations for this location.
         *
         * @return Success
         */
        else if (params[0].equals("deleteRooms")){
          command(RoomServer, RoomPort, message);
        }
        // (int id, String location)


        /**
         * Delete a customer and associated reservations.
         *
         * @return Success
         */
        else if (params[0].equals("deleteCustomer")){}
        // (int id, int customerID)


        /**
         * Query the status of a flight.
         *
         * @return Number of empty seats
         */
        else if (params[0].equals("queryFlight")){
          command(FlightServer, FlightPort, message);
        }
        // (int id, int flightNumber)


        /**
         * Query the status of a car location.
         *
         * @return Number of available cars at this location
         */
        else if (params[0].equals("queryCars")){
          command(CarServer, CarPort, message);
        }
        // (int id, String location)


        /**
         * Query the status of a room location.
         *
         * @return Number of available rooms at this location
         */
        else if (params[0].equals("queryRooms")){
          command(RoomServer, RoomPort, message);
        }
        // (int id, String location)


        /**
         * Query the customer reservations.
         *
         * @return A formatted bill for the customer
         */
        else if(params[0].equals("queryCustomerInfo")){}
        // (int id, int customerID)


        /**
         * Query the status of a flight.
         *
         * @return Price of a seat in this flight
         */
        else if (params[0].equals("queryFlightPrice")){
          command(FlightServer, FlightPort, message);
        }
        // (int id, int flightNumber)


        /**
         * Query the status of a car location.
         *
         * @return Price of car
         */
        else if (params[0].equals("queryCarsPrice")){
          command(CarServer, CarPort, message);
        }
        // (int id, String location)


        /**
         * Query the status of a room location.
         *
         * @return Price of a room
         */
        else if (params[0].equals("queryRoomsPrice")){
          command(RoomServer, RoomPort, message);
        }
        // (int id, String location)


        /**
         * Reserve a seat on this flight.
         *
         * @return Success
         */
        else if (params[0].equals("reserveFlight")){
          command(FlightServer, FlightPort, message);
        }
        // (int id, int customerID, int flightNumber)


        /**
         * Reserve a car at this location.
         *
         * @return Success
         */
        else if (params[0].equals("reserveCar")){
          command(CarServer, CarPort, message);
        }
        // (int id, int customerID, String location)


        /**
         * Reserve a room at this location.
         *
         * @return Success
         */
        else if (params[0].equals("reserveRoom")){
          command(RoomServer, RoomPort, message);
        }
        // (int id, int customerID, String location)


        /**
         * Reserve a bundle for the trip.
         *
         * @return Success
         */
        else if (params[0].equals("bundle")){}
        // (int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room)


        /**
         * Convenience for probing the resource manager.
         *
         * @return Name
         */

         outToClient.println("hello client from server THREAD, your result is: " + res );
      }
      socket.close();
    }
    catch (IOException e) {}
  }
  public void command(String serverName, int serverPort, String readerInput){
    // String serverName=args[0];

    Socket Comsocket= new Socket(serverName, serverPort); // establish a socket with a server using the given port#

    PrintWriter outToServer= new PrintWriter(Comsocket.getOutputStream(),true); // open an output stream to the server...
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(Comsocket.getInputStream())); // open an input stream from the server...

    BufferedReader bufferedReader =new java.io.BufferedReader(new InputStreamReader(System.in)); //to read user's input

    String res = null;
    while(true) // works forever
    {
      // String readerInput=bufferedReader.readLine(); // read user's input
      // if(readerInput.equals("Quit"))
        // break;
        if (res != null){
          break;
        }
        //we break upon receiving a response. Unsure if this is the best course of action
        //MARK: Check best course of action.

      outToServer.println(readerInput); // send the user's input via the output stream to the server
      res=inFromServer.readLine(); // receive the server's result via the input stream from the server
      System.out.println("result: "+res); // print the server result to the user
    }

    Comsocket.close();
    //we close connection every time
  }

}
