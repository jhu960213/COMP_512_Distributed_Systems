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
        if (args.length > 0)
        {
            s_portNum = Integer.parseInt(args[0]);
        }
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
        System.out.println("RoomsServer ready...");
        while (true)
        {
            Socket socket = serverSocket.accept();
            new ServerSocketThread(socket, roomsResourceManager).start();
        }
    }
}
