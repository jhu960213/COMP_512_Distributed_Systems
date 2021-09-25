package Client;

import Server.Interface.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;

public class RMIClient extends Client
{
	private static String serverHostName;
	private static String serverName;
	private static int serverHostPortNum;
	private static String rmiServerHostPrefix;

	public RMIClient()
	{
		super();
	}

	public void connectServer()
	{
		connectServer(serverHostName, serverHostPortNum, rmiServerHostPrefix, serverName);
	}

	public void connectServer(String serverHostName, int portNum, String serverPrefix, String serverName)
	{
		try {
			boolean first = true;
			while (true) {
				try {
					Registry registry = LocateRegistry.getRegistry(serverHostName, portNum);
					m_resourceManager = (IResourceManager)registry.lookup(serverPrefix + serverName);
					System.out.println("Connected to '" + serverName + "' server [" + serverName + ":" + portNum + "/" + serverPrefix + serverName + "]");
					break;
				}
				catch (NotBoundException|RemoteException e) {
					if (first) {
						System.out.println("Waiting for '" + serverName + "' server [" + serverName + ":" + portNum + "/" + serverPrefix + serverName + "]");
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
	}

	public static void main(String args[])
	{	
		if (args.length == 4) {
			serverHostName = args[0];
			serverName = args[1];
			serverHostPortNum = Integer.parseInt(args[2]);
			rmiServerHostPrefix = args[3];
		} else {
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
			System.exit(1);
		}

		// Set the security policy
		if (System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		// Get a reference to the RMIRegister
		try {
			RMIClient client = new RMIClient();
			client.connectServer();
			client.start();
		} 
		catch (Exception e) {    
			System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
			e.printStackTrace();
			System.exit(1);
		}
	}
}

