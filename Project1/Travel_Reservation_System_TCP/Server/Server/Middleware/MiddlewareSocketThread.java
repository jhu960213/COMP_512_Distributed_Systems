package Server.Middleware;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class MiddlewareSocketThread extends Thread
{
    Socket socket;
    MiddlewareSocketThread (Socket socket)
    {
        this.socket=socket;
    }

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

            while ((message = inFromClient.readLine())!=null && message != "Quit")
            {
                System.out.println("message:"+message);
                String result="Working!";


                String[] params =  message.split(",");
                int res=0;
                outToClient.println("hello client from server THREAD, your result is: " + res );
            }
            socket.close();
        }
        catch (IOException e)
        {

        }
  }

  public void command(String serverName, int serverPort, String readerInput) throws IOException {
      // String serverName=args[0];
    Socket Comsocket= null; // establish a socket with a server using the given port#
    try {
      Comsocket = new Socket(serverName, serverPort);
    } catch (IOException e) {
      e.printStackTrace();
    }

    PrintWriter outToServer= null; // open an output stream to the server...
    try {
      outToServer = new PrintWriter(socket.getOutputStream(),true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream())); // open an input stream from the server...

      BufferedReader bufferedReader =new java.io.BufferedReader(new InputStreamReader(System.in)); //to read user's input

      String res = null;
      while(true) // works forever
      {
          // String readerInput=bufferedReader.readLine(); // read user's input
          // if(readerInput.equals("Quit"))
          // break;
          if (res != null)
          {
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
