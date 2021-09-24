package Server.RMI;

import Server.Common.CarsResourceManager;
import Server.Interface.IResourceManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import static java.lang.Runtime.getRuntime;

public class RMICarsResourceManager extends CarsResourceManager {

    private static String rmiCarsResourceManagerServerName;
    private static String rmiCarsResourceManagerServerPrefix;
    private static int rmiCarsResourceManagerRegistryPortNum;
    private static int rmiCarsResourceManagerExportPortNum;

    public RMICarsResourceManager(String name)
    {
        super(name);
    }

    // start the rmi registry for the car resource manager server and export remote car resource manager object reference to clients
    public static void main(String args[])
    {
        System.setProperty("java.rmi.server.hostname", "localhost");
        if (args.length == 4)
        {
            // scan commandline args in the format of "serverName, serverPrefix, serverRegistryPortNum, serverExportPortNum"
            rmiCarsResourceManagerServerName = args[0];
            rmiCarsResourceManagerServerPrefix = args[1];
            rmiCarsResourceManagerRegistryPortNum = Integer.parseInt(args[2]);
            rmiCarsResourceManagerExportPortNum = Integer.parseInt(args[3]);

            // create RMI server entry for carsResourceManager
            try
            {
                // create RMI carsResourceManager server object
                RMICarsResourceManager rmiCarsResourceManagerServer = new RMICarsResourceManager(rmiCarsResourceManagerServerName);

                // dynamically generated the stub (client proxy)
                IResourceManager resourceManager =
                        (IResourceManager) UnicastRemoteObject.exportObject(rmiCarsResourceManagerServer, rmiCarsResourceManagerExportPortNum);

                // Bind the remote object's stub in the rmi carsResourceManager server registry
                Registry tmpRegistry;
                try
                {
                    tmpRegistry = LocateRegistry.createRegistry(rmiCarsResourceManagerRegistryPortNum);
                }
                catch (Exception e)
                {
                    System.out.println("\n*** RMI car resource manager error: Failed to export and " +
                            "create registry instance on localhost," +
                            " an existing registry may have already been " +
                            "exported and created on port:" + rmiCarsResourceManagerRegistryPortNum + "! ***\n");
                    tmpRegistry = LocateRegistry.getRegistry(rmiCarsResourceManagerRegistryPortNum);
                }
                final Registry rmiCarResourceManagerRegistry = tmpRegistry;
                rmiCarResourceManagerRegistry.rebind(rmiCarsResourceManagerServerPrefix + rmiCarsResourceManagerServerName, resourceManager);

                // unbinding registry when rmi carsResourceManager server shuts down
                getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        rmiCarResourceManagerRegistry.unbind(rmiCarsResourceManagerServerPrefix + rmiCarsResourceManagerServerName);
                        System.out.println("'" + rmiCarsResourceManagerServerName + "' resource manager unbound");
                    } catch (Exception e) {
                        System.err.println((char) 27 + "[31;1mRMI car resource manager server exception: " + (char) 27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }));
                System.out.println("'" + rmiCarsResourceManagerServerName + "' car manager server ready and bound to '"
                        + rmiCarsResourceManagerServerPrefix + rmiCarsResourceManagerServerName + "'");

            }
            catch (Exception e)
            {
                System.err.println((char)27 + "[31;1mRMI car resource manager server exception: " + (char)27 + "[0mUncaught exception");
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
            System.out.println("\n*** RMI car resource manager error: Wrong number of arguments," +
                    "please input the correct server name, server prefix, and port # " +
                    "to start the RMI car resource manager server! ***\n");
            System.exit(1);
        }
    }


}
