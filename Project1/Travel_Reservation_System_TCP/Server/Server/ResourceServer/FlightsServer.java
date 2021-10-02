package Server.ResourceServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class FlightsServer
{

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

        }
    }


    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(2004);
        System.out.println("Server ready...");
        while (true)
        {
            Socket socket = serverSocket.accept();
            new FlightsServerSocketThread(socket).start();
        }
    }
}
