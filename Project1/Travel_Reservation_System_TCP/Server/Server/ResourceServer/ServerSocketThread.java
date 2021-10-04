package Server.ResourceServer;

import Server.Interface.IResourceManager;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.lang.reflect.*;

public class ServerSocketThread extends Thread
{
    Socket socket;
    IResourceManager resourceManager;
    public ServerSocketThread(Socket socket, IResourceManager resourceManager)
    {
        this.socket = socket;
        this.resourceManager = resourceManager;
    }

    public void run()
    {
        try
        {
            ObjectInputStream inFromClient = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outToClient = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                String methodName = (String) inFromClient.readObject();
                if (methodName.equals("Quit")) break;
                Object[] argList = (Object[]) inFromClient.readObject();
                for (Method method : this.resourceManager.getClass().getMethods())
                    if (method.getName().equals(methodName) && method.getParameterCount() == argList.length) {
                        Object returnObj = method.invoke(this.resourceManager, argList);
                        outToClient.writeObject(returnObj);
                        break;
                    }
            }
            this.socket.close();
        }
        catch (IOException | ClassNotFoundException e)
        {

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
