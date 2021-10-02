package Server.Middleware;

import Server.Common.MethodCall;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.reflect.*;

public class MiddlewareSocketThread extends Thread
{
    Socket socket;
    MiddlewareSocketThread (Socket socket)
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
                System.out.println("received message:"+message);

                JSONObject jsonObject = new JSONObject(message);
                JSONArray jsonArray = jsonObject.getJSONArray("args");

                String methodName = jsonObject.getString("method");
                List<Object> argList = new ArrayList<>();
                for (Object obj : jsonArray) argList.add(obj);

                Object returnObj = null;
                for (Method method : this.getClass().getMethods())
                    if (method.getName().equals(methodName))
                    {
                        Class<?>[] paramTypes = method.getParameterTypes();
                        returnObj = method.invoke(this, argList.toArray());
                        break;
                    }
                System.out.println("Result = " + returnObj);
                outToClient.println(returnObj);
            }
            socket.close();
        }
        catch (IOException e)
        {

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public boolean addFlight(int xid, int flightNum, int flightSeats, int flightPrice)
    {
        boolean response = false;
        System.out.println("addFlighCalled");
        return response;
    }
}
