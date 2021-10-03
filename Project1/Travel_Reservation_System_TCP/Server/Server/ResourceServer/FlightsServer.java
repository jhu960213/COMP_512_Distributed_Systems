package Server.ResourceServer;

import Server.Common.FlightsResourceManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class FlightsServer
{
    private static int s_portNum = 2005;
    FlightsResourceManager flightsResourceManager;

    FlightsServer()
    {
        flightsResourceManager = new FlightsResourceManager("FlightsResourceManager");
    }

    public static void main(String args[])
    {
        FlightsServer server= new FlightsServer();
        try
        {
			//comment this line and uncomment the next one to run in multiple threads.
            server.runServerThread();
        }
        catch (IOException e)
        {
            System.out.println("Exception:"+e);
        }
    }


    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(s_portNum);
        System.out.println("Server ready...");
        while (true)
        {
            Socket socket = serverSocket.accept();
            new ServerSocketThread(socket, flightsResourceManager).start();
        }
    }
}
