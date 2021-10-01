import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class serverSocket
{

  public static void main(String args[])
  {

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
    ServerSocket serverSocket = new ServerSocket(9004);
    System.out.println("Server ready...");
    while (true)
    {
      Socket socket=serverSocket.accept();
      new serverSocketThread(socket).start();
    }
  }
}
