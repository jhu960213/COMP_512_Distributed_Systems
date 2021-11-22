package Server.RMI;

import Server.Common.RoomsResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static java.lang.Runtime.getRuntime;

public class RMIRoomsResourceManager extends RoomsResourceManager {

    private static String rmiRoomsResourceManagerServerName;
    private static String rmiRoomsResourceManagerServerPrefix;
    private static int rmiRoomsResourceManagerRegistryPortNum;
    private static int rmiRoomsResourceManagerExportPortNum;

    public RMIRoomsResourceManager(String name)
    {
        super(name);
    }

    // start the rmi registry for the room resource manager server and export remote room resource manager object reference to clients
    public static void main(String args[])
    {
        if (args.length == 4)
        {
            // scan commandline args in the format of "serverName, serverPrefix, serverRegistryPortNum, serverExportPortNum"
            rmiRoomsResourceManagerServerName = args[0];
            rmiRoomsResourceManagerServerPrefix = args[1];
            rmiRoomsResourceManagerRegistryPortNum = Integer.parseInt(args[2]);
            rmiRoomsResourceManagerExportPortNum = Integer.parseInt(args[3]);

            // create RMI server entry for roomsResourceManager
            try
            {
                // create RMI roomsResourceManager server object
                RMIRoomsResourceManager rmiRoomsResourceManagerServer = new RMIRoomsResourceManager(rmiRoomsResourceManagerServerName);

                // dynamically generated the stub (client proxy)
                IResourceManager resourceManager =
                        (IResourceManager) UnicastRemoteObject.exportObject(rmiRoomsResourceManagerServer, rmiRoomsResourceManagerExportPortNum);

                // Bind the remote object's stub in the rmi roomsResourceManager server registry
                Registry tmpRegistry;
                try
                {
                    tmpRegistry = LocateRegistry.createRegistry(rmiRoomsResourceManagerRegistryPortNum);
                }
                catch (Exception e)
                {
                    System.out.println("\n*** RMI room resource manager error: Failed to export and " +
                            "create registry instance on localhost," +
                            " an existing registry may have already been " +
                            "exported and created on port:" + rmiRoomsResourceManagerRegistryPortNum + "! ***\n");
                    tmpRegistry = LocateRegistry.getRegistry(rmiRoomsResourceManagerRegistryPortNum);
                }
                final Registry rmiRoomsResourceManagerRegistry = tmpRegistry;
                rmiRoomsResourceManagerRegistry.rebind(rmiRoomsResourceManagerServerPrefix + rmiRoomsResourceManagerServerName, resourceManager);

                // unbinding registry when rmi roomsResourceManager server shuts down
                getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        rmiRoomsResourceManagerRegistry.unbind(rmiRoomsResourceManagerServerPrefix + rmiRoomsResourceManagerServerName);
                        System.out.println("'" + rmiRoomsResourceManagerServerName + "' resource manager unbound");
                    } catch (Exception e) {
                        System.err.println((char) 27 + "[31;1mRMI room resource manager server exception: " + (char) 27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }));
                System.out.println("'" + rmiRoomsResourceManagerServerName + "' resource manager server ready and bound to '"
                        + rmiRoomsResourceManagerServerPrefix + rmiRoomsResourceManagerServerName + "'");

            }
            catch (Exception e)
            {
                System.err.println((char)27 + "[31;1mRMI room resource manager server exception: " + (char)27 + "[0mUncaught exception");
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
            System.out.println("\n*** RMI room resource manager error: Wrong number of arguments," +
                    "please input the correct server name, server prefix, and port # " +
                    "to start the RMI room resource manager server! ***\n");
            System.exit(1);
        }
    }

}
