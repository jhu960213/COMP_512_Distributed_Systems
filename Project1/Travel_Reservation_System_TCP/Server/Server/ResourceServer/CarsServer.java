package Server.ResourceServer;

import Server.Common.CarsResourceManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class CarsServer
{

    private static int s_portNum = 3004;

    CarsResourceManager carsResourceManager;

    CarsServer()
    {
        carsResourceManager = new CarsResourceManager("CarsResourceManager");
    }

    public static void main(String args[])
    {
        CarsServer server= new CarsServer();
        try
        {
            server.runServerThread();
        } catch (IOException e) {

        }
    }

    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(s_portNum);
        System.out.println("CarsServer ready...");
        while (true)
        {
            Socket socket = serverSocket.accept();
            new ServerSocketThread(socket, carsResourceManager).start();
        }
    }
}
