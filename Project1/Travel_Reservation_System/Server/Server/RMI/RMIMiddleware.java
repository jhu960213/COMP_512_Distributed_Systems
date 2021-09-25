package Server.RMI;

import Server.Interface.IResourceManager;
import Server.Middleware.Middleware;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import static java.lang.Runtime.*;

public class RMIMiddleware extends Middleware {

    // for connection to middleware
    private static String rmiMiddlewareServerName;
    private static String rmiMiddlewareServerPrefix;
    private static int rmiMiddlewareRegistryPortNum;
    private static int rmiMiddlewareExportPortNum;

    // for connection to resource managers: A = carManager, B = roomManager, C = flightManager
    private static String resourceAServerName;
    private static String resourceAHostName;

    private static String resourceBServerName;
    private static String resourceBHostName;

    private static String resourceCServerName;
    private static String resourceCHostName;

    private static int resourceAPortNum;
    private static int resourceBPortNum;
    private static int resourceCPortNum;

    public RMIMiddleware(String name)
    {
        super(name);
    }

    // start the rmi registry for the middleware server and export remote middleware object reference to clients
    public static void main(String args[])
    {
        //System.setProperty("java.rmi.server.hostname", "localhost");
        if (args.length == 13)
        {
            // commandline args in the format of "serverName, serverPrefix, serverRegistryPortNum, serverExportPortNum"
            rmiMiddlewareServerName = args[0];
            rmiMiddlewareServerPrefix = args[1];
            rmiMiddlewareRegistryPortNum = Integer.parseInt(args[2]);
            rmiMiddlewareExportPortNum = Integer.parseInt(args[3]);

            // commandline args in the format of serverAName, serverAPort, serverBName, serverBPort, serverCName, serverCPort
            resourceAServerName = args[4];
            resourceAHostName = args[5];
            resourceAPortNum = Integer.parseInt(args[6]);

            resourceBServerName = args[7];
            resourceBHostName = args[8];
            resourceBPortNum = Integer.parseInt(args[9]);

            resourceCServerName = args [10];
            resourceCHostName = args[11];
            resourceCPortNum = Integer.parseInt(args[12]);


            // create RMI server entry for middleware
            try
            {
                // create RMI middleware server object
                RMIMiddleware rmiMiddlewareServer = new RMIMiddleware(rmiMiddlewareServerName);

                // connect to resource A
                rmiMiddlewareServer.connectToResourceServer(resourceAHostName, resourceAServerName, resourceAPortNum, rmiMiddlewareServerPrefix + resourceAServerName);
                Thread.sleep(1000); // miliseconds?

                // connect to resource B
                rmiMiddlewareServer.connectToResourceServer(resourceBHostName, resourceBServerName, resourceBPortNum, rmiMiddlewareServerPrefix + resourceBServerName);
                Thread.sleep(1000);

                // connect to resource C
                rmiMiddlewareServer.connectToResourceServer(resourceCHostName, resourceCServerName, resourceCPortNum, rmiMiddlewareServerPrefix + resourceCServerName);


                // dynamically generated the stub (client proxy)
                IResourceManager resourceManager =
                        (IResourceManager) UnicastRemoteObject.exportObject(rmiMiddlewareServer, rmiMiddlewareExportPortNum);

                // Bind the remote object's stub in the rmi middleware server registry
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
                rmiMiddlewareRegistry.rebind(rmiMiddlewareServerPrefix + rmiMiddlewareServerName, resourceManager);


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
                System.out.println("'" + rmiMiddlewareServerName + "' middleware server ready and bound to '"
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
}
