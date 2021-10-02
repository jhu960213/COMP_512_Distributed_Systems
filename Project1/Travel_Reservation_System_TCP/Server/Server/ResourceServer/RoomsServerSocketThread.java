package Server.ResourceServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class RoomsServerSocketThread extends Thread
{
    Socket socket;
    RoomsServerSocketThread(Socket socket)
    {
        this.socket=socket;
    }

    public void run()
    {
        try
        {
            BufferedReader inFromClient= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
            String message = null;

            while ((message = inFromClient.readLine())!=null && message != "Quit")
            {
                System.out.println("message:"+message);
                String result="Working!";


                String[] params =  message.split(",");
                int res = 0;
                outToClient.println("hello client from server THREAD, your result is: " + res );
            }
            socket.close();
        }
        catch (IOException e)
        {

        }
    }
}
