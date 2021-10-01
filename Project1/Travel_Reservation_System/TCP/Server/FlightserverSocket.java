package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class FlightserverSocket
{

  public static void main(String args[])
  {

    FlightserverSocket server= new FlightserverSocket();
    try
    {
			//comment this line and uncomment the next one to run in multiple threads.
      server.runServerThread();
    }
    catch (IOException e)
    { }
  }


  public void runServerThread() throws IOException
  {
    ServerSocket serverSocket = new ServerSocket(3004);
    System.out.println("Server ready...");
    while (true)
    {
      Socket socket=serverSocket.accept();
      new FlightserverSocketThread(socket).start();
    }
  }
}
