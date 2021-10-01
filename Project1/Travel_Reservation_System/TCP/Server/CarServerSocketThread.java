package Server;
import Server.ResourceManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;


public class CarServerSocketThread extends Thread
{
  Socket socket;
  ResourceManager rm;
  CarServerSocketThread (Socket socket, ResourceManager rm)
  { this.socket=socket;
    this.rm = rm;
  }

  public void run()
  {
    try
    {
      BufferedReader inFromClient= new BufferedReader(new InputStreamReader(socket.getInputStream()));
      PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
      String message = null;
      while ((message = inFromClient.readLine())!=null)
      {
        System.out.println("message:"+message);
        String result="Working!";


        String[] params =  message.split(",");
        // int x= Integer.parseInt(params[1]);
        // int y= Integer.parseInt(params[2]);

        int res_int = -1;
        String res_str = null;
        boolean res_bool = false;

        // System.out.println(params[0] +"--"+params[1]+"--"+params[2]);
        /**
         * Add car at a location.
         *
         * This should look a lot like addFlight, only keyed on a string location
         * instead of a flight number.
         *
         * @return Success
         */

        if (params[0].equals("addCars")){
          System.out.println("Carserver Recieved AddCars Command");
          res_bool = rm.addCars(Integer.parseInt(params[1]), params[2], Integer.parseInt(params[3]), Integer.parseInt(params[4]));
        }
        // (int id, String location, int numCars, int price)
        else if (params[0].equals("deleteCars")){
          res_bool = rm.deleteCars(Integer.parseInt(params[1]), params[2]);
        }
        /**
         * Add customer.
         *
         * @return Unique customer identifier
         */
        else if (params[0].equals("newCustomer")){
          ;
        }
        // (int id)


        /**
         * Add customer with id.
         *
         * @return Success
         */
        else if (params[0].equals("newCustomer")){
          ;
        }
        // (int id, int cid)


        /**
         * Delete a customer and associated reservations.
         *
         * @return Success
         */
        else if (params[0].equals("deleteCustomer")){
          ;
        }
        // (int id, int customerID)

        /**
         * Query the customer reservations.
         *
         * @return A formatted bill for the customer
         */
        else if (params[0].equals("queryCustomerInfo")){
          ;
        }
        // (int id, int customerID)


        /**
         * Query the status of a car location.
         *
         * @return Price of car
         */
        else if (params[0].equals("queryCarsPrice")){
          res_int = rm.queryCarsPrice(Integer.parseInt(params[1]), params[2]);
        }
        // (int id, String location)

        /**
         * Reserve a car at this location.
         *
         * @return Success
         */
        else if (params[0].equals("reserveCar")){
          ;
        }
        // (int id, int customerID, String location)


        /**
         * Reserve a bundle for the trip.
         *
         * @return Success
         */
        else if (params[0].equals("bundle")){
          ;
        }
        // (int id, int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room)


        /**
         * Convenience for probing the resource manager.
         *
         * @return Name
         */
         if (res_int!=-1) {
           outToClient.println("hello client from server THREAD, your result is: " + res_int);
         }
         else if (res_str!=null){
           outToClient.println("hello client from server THREAD, your result is: " + res_str );
         } else{
           outToClient.println("hello client from server THREAD, your result is: " + res_bool );
         }

      }
      socket.close();
    }
    catch (IOException e) {}
  }

}
