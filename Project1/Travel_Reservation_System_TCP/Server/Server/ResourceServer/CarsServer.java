package Server.ResourceServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class CarsServer
{

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
        ServerSocket serverSocket = new ServerSocket(2004);
        System.out.println("Server ready...");
        while (true)
        {
            Socket socket = serverSocket.accept();
            new CarsServerSocketThread(socket).start();
        }
    }
}
