package Server.RMI;

import Server.Common.FlightsResourceManager;
import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import static java.lang.Runtime.getRuntime;

public class RMIFlightsResourceManager extends FlightsResourceManager {

    private static String rmiFlightsResourceManagerServerName;
    private static String rmiFlightsResourceManagerServerPrefix;
    private static int rmiFlightsResourceManagerRegistryPortNum;
    private static int rmiFlightsResourceManagerExportPortNum;

    public RMIFlightsResourceManager(String name)
    {
        super(name);
    }

    // start the rmi registry for the flights resource manager server and export remote flights resource manager object reference to clients
    public static void main(String args[])
    {
        //System.setProperty("java.rmi.server.hostname", "localhost");
        if (args.length == 4)
        {
            // scan commandline args in the format of "serverName, serverPrefix, serverRegistryPortNum, serverExportPortNum"
            rmiFlightsResourceManagerServerName = args[0];
            rmiFlightsResourceManagerServerPrefix = args[1];
            rmiFlightsResourceManagerRegistryPortNum = Integer.parseInt(args[2]);
            rmiFlightsResourceManagerExportPortNum = Integer.parseInt(args[3]);

            // create RMI server entry for flightsResourceManager
            try
            {
                // create RMI flightsResourceManager server object
                RMIFlightsResourceManager rmiFlightsResourceManagerServer = new RMIFlightsResourceManager(rmiFlightsResourceManagerServerName);

                // dynamically generated the stub (client proxy)
                IResourceManager resourceManager =
                        (IResourceManager) UnicastRemoteObject.exportObject(rmiFlightsResourceManagerServer, rmiFlightsResourceManagerExportPortNum);

                // Bind the remote object's stub in the rmi flightsResourceManager server registry
                Registry tmpRegistry;
                try
                {
                    tmpRegistry = LocateRegistry.createRegistry(rmiFlightsResourceManagerRegistryPortNum);
                }
                catch (RemoteException e)
                {
                    System.out.println("\n*** RMI flights resource manager error: Failed to export and " +
                            "create registry instance on localhost," +
                            " an existing registry may have already been " +
                            "exported and created on port:" + rmiFlightsResourceManagerRegistryPortNum + "! ***\n");
                    tmpRegistry = LocateRegistry.getRegistry(rmiFlightsResourceManagerRegistryPortNum);
                }
                final Registry rmiFlightsResourceManagerRegistry = tmpRegistry;
                rmiFlightsResourceManagerRegistry.rebind(rmiFlightsResourceManagerServerPrefix + rmiFlightsResourceManagerServerName, resourceManager);

                // unbinding registry when rmi flightsResourceManager server shuts down
                getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        rmiFlightsResourceManagerRegistry.unbind(rmiFlightsResourceManagerServerPrefix + rmiFlightsResourceManagerServerName);
                        System.out.println("'" + rmiFlightsResourceManagerServerName + "' resource manager unbound");
                    } catch (Exception e) {
                        System.err.println((char) 27 + "[31;1mRMI flights resource manager server exception: " + (char) 27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }));
                System.out.println("'" + rmiFlightsResourceManagerServerName + "' resource manager server ready and bound to '"
                        + rmiFlightsResourceManagerServerPrefix + rmiFlightsResourceManagerServerName + "'");

            }
            catch (Exception e)
            {
                System.err.println((char)27 + "[31;1mRMI flights resource manager server exception: " + (char)27 + "[0mUncaught exception");
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
            System.out.println("\n*** RMI flights resource manager error: Wrong number of arguments," +
                    "please input the correct server name, server prefix, and port # " +
                    "to start the RMI flights resource manager server! ***\n");
            System.exit(1);
        }
    }


}
