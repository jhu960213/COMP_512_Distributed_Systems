package Server.ResourceServer;

import Server.Common.CarsResourceManager;
import Server.Common.RoomsResourceManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RoomsServer {
    private static int s_portNum = 4004;
    RoomsResourceManager roomsResourceManager;
    RoomsServer()
    {
        roomsResourceManager = new RoomsResourceManager("RoomsResourceManager");
    }

    public static void main(String args[])
    {

        RoomsServer server= new RoomsServer();
        try
        {
            server.runServerThread();
        }
        catch (IOException e)
        {

        }
    }


    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(s_portNum);
        System.out.println("Server ready...");
        while (true)
        {
            Socket socket = serverSocket.accept();
            new ServerSocketThread(socket, roomsResourceManager).start();
        }
    }
}
