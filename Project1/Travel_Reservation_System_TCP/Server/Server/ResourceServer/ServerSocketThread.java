package Server.ResourceServer;

import Server.Interface.IResourceManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
            BufferedReader inFromClient= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
            String message = null;

            while ((message = inFromClient.readLine())!=null && message!="Quit")
            {
                System.out.println("received message:"+message);

                JSONObject jsonObject = new JSONObject(message);
                JSONArray jsonArray = jsonObject.getJSONArray("args");

                String methodName = jsonObject.getString("method");
                List<Object> argList = new ArrayList<>();
                for (Object obj : jsonArray) argList.add(obj);

                Object returnObj = null;
                for (Method method : this.resourceManager.getClass().getMethods())
                    if (method.getName().equals(methodName))
                    {
                        Class<?>[] paramTypes = method.getParameterTypes();
                        returnObj = method.invoke(this.resourceManager, argList.toArray());
                        break;
                    }

                System.out.println("Result = " + returnObj);
                if (returnObj != null) outToClient.println(returnObj);
                else outToClient.println("No such command");
            }
            this.socket.close();
        }
        catch (IOException e)
        {

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
