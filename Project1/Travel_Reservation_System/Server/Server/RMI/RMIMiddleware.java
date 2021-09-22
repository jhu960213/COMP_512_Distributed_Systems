package Server.RMI;

import Server.Interface.IResourceManager;
import Server.Middleware.Middleware;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import static java.lang.Runtime.*;

public class RMIMiddleware extends Middleware {

    private static String rmiMiddlewareServerName;
    private static String rmiMiddlewareServerPrefix;
    private static int rmiMiddlewareRegistryPortNum;
    private static int rmiMiddlewareExportPortNum;

    public RMIMiddleware(String name) {
        super(name);
    }

    // start the rmi registry for the middleware server and export remote middleware object reference to clients
    public static void main(String args[])
    {
        if (args.length == 4)
        {
            // scan commandline args in the format of "serverName, serverPrefix, serverRegistryPortNum, serverExportPortNum"
            rmiMiddlewareServerName = args[0];
            rmiMiddlewareServerPrefix = args[1];
            rmiMiddlewareRegistryPortNum = Integer.parseInt(args[2]);
            rmiMiddlewareExportPortNum = Integer.parseInt(args[3]);

            // create RMI server entry for middleware
            try
            {
                // create RMI middleware server object
                RMIMiddleware rmiMiddlewareServer = new RMIMiddleware(rmiMiddlewareServerName);

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
}
