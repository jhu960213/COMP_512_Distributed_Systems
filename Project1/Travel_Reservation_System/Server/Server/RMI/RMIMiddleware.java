package Server.RMI;

import Server.Interface.IResourceManager;
import Server.Middleware.Middleware;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import static java.lang.Runtime.*;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

public class RMIMiddleware extends Middleware {

    private static String rmiMiddlewareServerName = "Middleware";
    private static String rmiMiddlewareServerPrefix = "group_04_";;
    private static int rmiMiddlewareRegistryPortNum = 5004;

    private static String flightsResourceServerHost = "localhost";
    private static int flightsResourceServerPort = 2004;
    private static String flightsResourceServerName = "FlightsServer";

    private static String carsResourceServerHost = "localhost";
    private static int carsResourceServerPort = 3004;
    private static String carsResourceServerName = "CarsServer";

    private static String roomsResourceServerHost = "localhost";
    private static int roomsResourceServerPort = 4004;
    private static String roomsResourceServerName = "RoomsServer";

    private static String s_rmiPrefix = "group_04_";

    public RMIMiddleware(String name) {
        super(name);
    }

    // start the rmi registry for the middleware server and export remote middleware object reference to clients
    public static void main(String args[])
    {
        if (args.length > 3)
        {
            flightsResourceServerHost = args[0];
            carsResourceServerHost = args[1];
            roomsResourceServerHost = args[2];
            rmiMiddlewareServerName = args[3];
            if (args.length > 4) flightsResourceServerPort = Integer.parseInt(args[4]);
            if (args.length > 5) carsResourceServerPort = Integer.parseInt(args[5]);
            if (args.length > 6) roomsResourceServerPort = Integer.parseInt(args[6]);
            if (args.length > 7) rmiMiddlewareRegistryPortNum = Integer.parseInt(args[7]);

            try {
                // create RMI middleware server object
                RMIMiddleware rmiMiddlewareServer = new RMIMiddleware(rmiMiddlewareServerName);

                rmiMiddlewareServer.m_flightsResourceManager = rmiMiddlewareServer.connectServer(flightsResourceServerHost, flightsResourceServerPort, flightsResourceServerName);
                rmiMiddlewareServer.m_carsResourceManager = rmiMiddlewareServer.connectServer(carsResourceServerHost, carsResourceServerPort, carsResourceServerName);
                rmiMiddlewareServer.m_roomsResourceManager = rmiMiddlewareServer.connectServer(roomsResourceServerHost, roomsResourceServerPort, roomsResourceServerName);

                // dynamically generated the stub (client proxy)
                IResourceManager resourceManager =
                        (IResourceManager) UnicastRemoteObject.exportObject(rmiMiddlewareServer, rmiMiddlewareRegistryPortNum);

                // Bind the remte object's stub in the rmi middleware server registry
                Registry tmpRegistry;
                try
                {
                    tmpRegistry = LocateRegistry.createRegistry(rmiMiddlewareRegistryPortNum);
                }
                catch (Exception e)
                {
                    System.out.println("\n*** RMI middleware error: Failed to export and " +
                            "create registry instance on localhost," +
                            " an existing registry may have already been " +
                            "exported and created on port:" + rmiMiddlewareRegistryPortNum + "! ***\n");
                    tmpRegistry = LocateRegistry.getRegistry(rmiMiddlewareRegistryPortNum);
                }
                final Registry rmiMiddlewareRegistry = tmpRegistry;
                rmiMiddlewareRegistry.bind(rmiMiddlewareServerPrefix + rmiMiddlewareServerName, resourceManager);

                // unbinding registry when rmi middleware server shuts down
                getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        rmiMiddlewareRegistry.unbind(rmiMiddlewareServerPrefix + rmiMiddlewareServerName);
                        System.out.println("'" + rmiMiddlewareServerName + "' resource manager unbound");
                    } catch (Exception e) {
                        System.err.println((char) 27 + "[31;1mRMI middleware server exception: " + (char) 27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }));
                System.out.println("'" + rmiMiddlewareServerName + "' resource manager server ready and bound to '"
                        + rmiMiddlewareServerPrefix + rmiMiddlewareServerName + "'");

            }
            catch (Exception e)
            {
                System.err.println((char)27 + "[31;1mRMI middleware server exception: " + (char)27 + "[0mUncaught exception");
                e.printStackTrace();
                System.exit(1);
            }

            // create and install a security manager
            if (System.getSecurityManager() == null)
            {
                System.setSecurityManager(new SecurityManager());
            }
        }

        else
        {
            System.out.println("\n*** RMI middleware error: Wrong number of arguments," +
                    "please input the correct server name, server prefix, and port # " +
                    "to start the RMI middlware server! ***\n");
            System.exit(1);
        }
    }

    public IResourceManager connectServer(String server, int port, String name)
    {
        try {
            boolean first = true;
            while (true) {
                try {
                    Registry registry = LocateRegistry.getRegistry(server, port);
                    IResourceManager m_resourceManager = (IResourceManager)registry.lookup(s_rmiPrefix + name);
                    System.out.println("Connected to '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
                    return m_resourceManager;
                }
                catch (NotBoundException|RemoteException e) {
                    if (first) {
                        System.out.println("Waiting for '" + name + "' server [" + server + ":" + port + "/" + s_rmiPrefix + name + "]");
                        first = false;
                    }
                }
                Thread.sleep(500);
            }
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
