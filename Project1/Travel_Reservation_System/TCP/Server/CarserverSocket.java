import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class serverSocket
{

  public static void main(String args[])
  {
    // protected String m_name = "";
    // protected RMHashMap m_data = new RMHashMap();
    ResourceManager rm=new ResourceManager(); //create RM for all the threads, multithreading safety features already implemented.
    serverSocket server= new serverSocket();
    try
    {
			//comment this line and uncomment the next one to run in multiple threads.
      // server.runServer();
      server.runServerThread();
    }
    catch (IOException e)
    { }
  }



  public void runServerThread() throws IOException
  {
    ServerSocket serverSocket = new ServerSocket(2004);
    System.out.println("Server ready...");
    while (true)
    {
      Socket Carsocket=serverSocket.accept();
      new serverSocketThread(Carsocket, rm).start();
    }
  }
}
