package Server.ResourceServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class FlightsServerSocketThread extends Thread
{
  Socket socket;
  FlightsServerSocketThread (Socket socket)
  { this.socket=socket; }

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

        int res=0;

         outToClient.println("hello client from server THREAD, your result is: " + res );
      }
      socket.close();
    }
    catch (IOException e) {

    }
  }

}
