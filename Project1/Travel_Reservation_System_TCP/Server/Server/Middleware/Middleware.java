package Server.Middleware;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Middleware
{
    private static int middlewareRegistryPortNum = 5004;

    private static String flightsResourceServerHost = "localhost";
    private static int flightsResourceServerPort = 2004;

    private static String carsResourceServerHost = "localhost";
    private static int carsResourceServerPort = 3004;

    private static String roomsResourceServerHost = "localhost";
    private static int roomsResourceServerPort = 4004;

    public static void main(String args[])
    {

        Middleware server= new Middleware();
        try
        {
            server.runServerThread();
        } catch (IOException e) {

        }
    }

    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(middlewareRegistryPortNum);
        System.out.println("Server ready...");
        while (true)
        {
            Socket socket=serverSocket.accept();
            new MiddlewareSocketThread(socket).start();
        }
    }
}
