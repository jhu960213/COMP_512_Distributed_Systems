package Server;//import TCP.Server.ResourceManager;
//import jdk.management.resource.ResourceContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import Server.ResourceManager;
import Server.CarServerSocketThread;

public class CarServerSocket
{
  public volatile ResourceManager rm = new ResourceManager("car");

  public static void main(String args[])
  {
    ResourceManager rm = new ResourceManager("res"); //create RM for all the threads, multithreading safety features already implemented.
    CarServerSocket server= new CarServerSocket();
    try
    {
      server.runServerThread();
    }
    catch (IOException e)
    { }
  }



  public void runServerThread() throws IOException
  {
    ServerSocket serverSocket = new ServerSocket(6004);
    System.out.println("CarServer ready...");
    while (true)
    {
      Socket CarSocket=serverSocket.accept();
      new CarServerSocketThread(CarSocket, rm).start();
    }
  }
}
