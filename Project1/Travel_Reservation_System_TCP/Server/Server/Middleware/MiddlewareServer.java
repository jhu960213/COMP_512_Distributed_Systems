package Server.Middleware;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MiddlewareServer
{
    public static void main(String args[])
    {

        MiddlewareServer server= new MiddlewareServer();
        try
        {
			//comment this line and uncomment the next one to run in multiple threads.
            // server.runServer();
            server.runServerThread();
        } catch (IOException e) {

        }
    }

    public void runServerThread() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket(9004);
        System.out.println("Server ready...");
        while (true)
        {
            Socket socket=serverSocket.accept();
            new MiddlewareSocketThread(socket).start();
        }
    }
}
