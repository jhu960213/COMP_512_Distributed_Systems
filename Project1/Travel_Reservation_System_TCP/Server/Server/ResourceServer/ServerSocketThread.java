package Server.ResourceServer;

import Server.Common.Customer;
import Server.Interface.IResourceManager;
import org.json.JSONArray;
import org.json.JSONObject;

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
                for (Object obj : jsonArray) {
                    if (obj.getClass().equals(JSONArray.class)) {
                        Vector<Object> arr = new Vector<>();
                        for (Object obj2 : (JSONArray)obj) arr.add(obj2);
                        argList.add(arr);
                    } else if (obj.getClass().equals(JSONObject.class)) {
                        argList.add(new Customer((JSONObject) obj));
                    }
                    else argList.add(obj);
                }

                Object returnObj = null;
                for (Method method : this.resourceManager.getClass().getMethods())
                    if (method.getName().equals(methodName) && method.getParameterCount() == argList.size())
                    {
                        returnObj = method.invoke(this.resourceManager, argList.toArray());
                        break;
                    }

                System.out.println("Result = " + returnObj);
                outToClient.println(returnObj);
                outToClient.println("end");
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
