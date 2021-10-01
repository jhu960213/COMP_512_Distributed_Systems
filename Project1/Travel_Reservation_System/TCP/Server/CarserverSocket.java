package Server;//import TCP.Server.ResourceManager;
import jdk.management.resource.ResourceContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class CarserverSocket
{

  public static void main(String args[])
  {
    // protected String m_name = "";
    // protected RMHashMap m_data = new RMHashMap();
    ResourceManager rm =new ResourceManager("resource"); //create RM for all the threads, multithreading safety features already implemented.
    CarserverSocket server= new CarserverSocket();
    try
    {
			//comment this line and uncomment the next one to run in multiple threads.
      // server.runServer();
      server.runServerThread(rm);
    }
    catch (IOException e)
    { }
  }



  public void runServerThread(ResourceManager rm) throws IOException
  {
    ServerSocket serverSocket = new ServerSocket(2004);
    System.out.println("Server ready...");
    while (true)
    {
      Socket Carsocket=serverSocket.accept();
      new CarserverSocketThread(Carsocket, rm).start();
    }
  }
}
