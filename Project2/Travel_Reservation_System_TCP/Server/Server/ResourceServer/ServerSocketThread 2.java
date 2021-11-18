package Server.ResourceServer;

import Server.Interface.IResourceManager;
import Server.Exception.InvalidTransactionException;
import Server.Exception.TransactionAbortedException;

import java.io.*;
import java.net.Socket;
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
                if (methodName.equals("Quit")) {
                    break;
                }
                Object[] argList = (Object[]) inFromClient.readObject();
                try {
                    if (argList.length > 0 && !methodName.equals("commit") && !methodName.equals("abort"))
                        resourceManager.checkTransaction((int)argList[0], methodName);
                    long startTime = System.currentTimeMillis();
                    Class[] argTypes = new Class[argList.length];
                    for (int i = 0; i < argList.length; i++) {
                        Class cls = argList[i].getClass();
                        if (cls == Integer.class) cls = int.class;
                        else if (cls == Boolean.class) cls = boolean.class;
                        argTypes[i] = cls;
                    }
                    Method method = resourceManager.getClass().getMethod(methodName, argTypes);
                    Object returnObj = method.invoke(this.resourceManager, argList);
                    outToClient.writeObject(returnObj);
                    long executeTime = System.currentTimeMillis() - startTime;
                    if (argList.length > 0) {
                        resourceManager.addExecuteTime((int)argList[0], executeTime);
                        if (methodName.equals("commit")) resourceManager.commitRecord((int)argList[0], false);
                        if (methodName.equals("abort")) resourceManager.commitRecord((int)argList[0], true);
                    }
                    if (methodName.equals("shutdown") && (returnObj instanceof Boolean) && (Boolean)returnObj)
                        System.exit(1);
                } catch (InvocationTargetException e) {
                    try {
                        outToClient.writeObject(e.getCause());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                } catch (InvalidTransactionException | TransactionAbortedException | IllegalAccessException e) {
                    try {
                        outToClient.writeObject(e);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    try {
                        outToClient.writeObject(e);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
            this.socket.close();
        } catch (IOException | ClassNotFoundException e) {
            if (e instanceof EOFException) {
                try {
                    this.socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else e.printStackTrace();
        }

    }

}
