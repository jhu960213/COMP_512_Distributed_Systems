
package Server.RMI;

import Server.Common.CarsResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RMICarsResourceManager extends CarsResourceManager {

    private static String s_serverName = "CarsServer";
    private static int s_portNum = 3004;
    private static String s_rmiPrefix = "group_04_";

    public static void main(String args[])
    {
        if (args.length > 0)
        {
            s_portNum = Integer.parseInt(args[0]);
        }

        // Create the RMI server entry
        try {
            // Create a new Server object
            RMICarsResourceManager server = new RMICarsResourceManager(s_serverName);

            // Dynamically generate the stub (client proxy)
            IResourceManager resourceManager = (IResourceManager)UnicastRemoteObject.exportObject(server, 0);

            // Bind the remote object's stub in the registry
            Registry l_registry;
            try {
                l_registry = LocateRegistry.createRegistry(s_portNum);
            } catch (RemoteException e) {
                l_registry = LocateRegistry.getRegistry(s_portNum);
            }
            final Registry registry = l_registry;
            registry.rebind(s_rmiPrefix + s_serverName, resourceManager);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        registry.unbind(s_rmiPrefix + s_serverName);
                        System.out.println("'" + s_serverName + "' resource manager unbound");
                    }
                    catch(Exception e) {
                        System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("'" + s_serverName + "' resource manager server ready and bound to '" + s_rmiPrefix + s_serverName + "'");
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }

        // Create and install a security manager
        if (System.getSecurityManager() == null)
        {
            System.setSecurityManager(new SecurityManager());
        }
    }

    public RMICarsResourceManager(String name)
    {
        super(name);
    }
}
