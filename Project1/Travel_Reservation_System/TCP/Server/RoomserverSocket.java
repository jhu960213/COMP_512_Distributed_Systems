package Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class RoomserverSocket
{

  public static void main(String args[])
  {

    RoomserverSocket server= new RoomserverSocket();
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
    ServerSocket serverSocket = new ServerSocket(1004);
    System.out.println("Server ready...");
    while (true)
    {
      Socket socket=serverSocket.accept();
      new RoomserverSocketThread(socket).start();
    }
  }
}