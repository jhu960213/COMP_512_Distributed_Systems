package clnt;

import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import task.DistTask;
import task.MCPi;

import java.io.*;

// You may have to add other interfaces such as for threading, etc., as needed.
public class DistClient implements Watcher, AsyncCallback.StatCallback, AsyncCallback.DataCallback
{
	private ZooKeeper zk;
	private String zkServer, taskNodeName;
//	private String pinfo;
	private DistTask dTask;

	DistClient(String zkhost, DistTask dt)
	{
		this.zkServer = zkhost;
		this.dTask = dt;
		System.out.println("DISTAPP: ZK Connection information: " + this.zkServer);
//		this.pinfo = ManagementFactory.getRuntimeMXBean().getName();
//		System.out.println("DISTAPP: Client information: " + this.pinfo);
	}

	public ZooKeeper getZk() {
		return zk;
	}

	public void setZk(ZooKeeper zk) {
		this.zk = zk;
	}

	public String getZkServer() {
		return zkServer;
	}

	public void setZkServer(String zkServer) {
		this.zkServer = zkServer;
	}

	public String getTaskNodeName() {
		return taskNodeName;
	}

	public void setTaskNodeName(String taskNodeName) {
		this.taskNodeName = taskNodeName;
	}

//	public String getPinfo() {
//		return pinfo;
//	}
//
//	public void setPinfo(String pinfo) {
//		this.pinfo = pinfo;
//	}

	public DistTask getdTask() {
		return dTask;
	}

	public void setdTask(DistTask dTask) {
		this.dTask = dTask;
	}

	public void startClient() {
		try {
			//connect to ZK, adds a watcher object to detect any changes to its connection with the ensemble
			this.zk = new ZooKeeper(this.zkServer, 1000, this);

			// Serialize our Task object to a byte array!
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this.dTask);
			oos.flush();
			byte [] dTaskSerial = bos.toByteArray();

			// Create a sequential znode with the Task object as its data.
			// TODO replace XX with your group number.
			this.taskNodeName = this.zk.create("/dist04/tasks/task-", dTaskSerial, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			//taskNodeName = zk.create("/distXX/tasks/task-", pinfo.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
			System.out.println("DISTAPP - TaskNode - " + this.taskNodeName);

			//Place watch for the result znode which will be created under our task znode.
			this.zk.exists(this.taskNodeName + "/result", this, this, null);
		}
		catch (Exception e) {
			System.out.println("DISTCLIENT::startClient()::exception: " + e.getMessage() + "\n");
		}
	}

	// Implementing the Watcher interface
	public void process(WatchedEvent e)
	{
		//Get event notifications.

		//!! IMPORTANT !!
		// Do not perform any time consuming/waiting steps here
		//	including in other functions called from here.
		// 	Your will be essentially holding up ZK client library 
		//	thread and you will not get other notifications.
		//	Instead include another thread in your program logic that
		//   does the time consuming "work" and notify that thread from here.

		System.out.println("DISTAPP : Event received : " + e);

		// The result znode was created.
		if(e.getType() == Watcher.Event.EventType.NodeCreated && e.getPath().equals(taskNodeName+"/result"))
		{
			System.out.println("DISTAPP : Node created : " + e.getPath());
			//Ask for data in the result znode (asynchronously). We do not have to watch this znode anymore.
			zk.getData(this.taskNodeName+"/result", null, this, null);
		}
	}

	// Implementing the AsyncCallback.StatCallback interface. This will be invoked by the zk.exists
	public void processResult(int rc, String path, Object ctx, Stat stat)
	{

		//!! IMPORTANT !!
		// Do not perform any time consuming/waiting steps here
		//	including in other functions called from here.
		// 	Your will be essentially holding up ZK client library 
		//	thread and you will not get other notifications.
		//	Instead include another thread in your program logic that
		//   does the time consuming "work" and notify that thread from here.

		System.out.println("DISTAPP - processResult - StatCallback: " + rc + "-" + path + "-" + ctx + "-" + stat);
		switch(Code.get(rc))
		{
			case OK:
				//The result znode is ready.
				System.out.println("DISTAPP - processResult - StatCallback: OK");
				//Ask for data in the result znode (asynchronously). We do not have to watch this znode anymore.
				zk.getData(this.taskNodeName+"/result", null, this, null);
				break;

			case NONODE:
				//The result znode was not ready, we will just make sure to reinstall the watcher.
				// Ideally we should come here only once!, if at all. That will be the time we called
				//  exists on the result znode immediately after creating the task znode.
				System.out.println("DISTAPP - processResult - StatCallback: " + Code.get(rc));
				zk.exists(this.taskNodeName+"/result", this, null, null);
				break;

			default:
				System.out.println("DISTAPP - processResult - StatCallback: " + Code.get(rc));
				break;
		}
	}

	// Implementing the AsyncCallback.DataCallback. This will be invoked as a result of zk.getData on the result node.
	public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat)
	{

		//!! IMPORTANT !!
		// Do not perform any time consuming/waiting steps here
		//	including in other functions called from here.
		// 	Your will be essentially holding up ZK client library 
		//	thread and you will not get other notifications.
		//	Instead include another thread in your program logic that
		//   does the time consuming "work" and notify that thread from here.

		//System.out.println("DISTAPP : processResult : DataCallback : " + rc + ":" + path + ":" + ctx + ":" + new String(data==null?"null".getBytes():data) + ":" + stat);
		System.out.println("DISTAPP - processResult - DataCallback: " + rc + "-" + path + "-" + ctx + "-" + stat);
		try
		{
			//Deserialize the "data" back into a task object (which will now also contain the results) and update our task object reference.
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInput in = new ObjectInputStream(bis);
			this.dTask = (DistTask) in.readObject();
		}
		catch(Exception e)
		{
			// Some error happened, we should set the task object reference to null to avoid confusion.
			System.out.println(e);
			this.dTask = null;
		}

		// Cleanup, we do not need our task and result nodes anymore.
		this.zk.delete(this.taskNodeName + "/result", -1, null, null);
		this.zk.delete(this.taskNodeName, -1, null, null);

		// Free the main thread to go ahead and terminate.
		synchronized(this) {
			this.notify();
		}
	}

	// Called after the computation is done at worker and result is send back here
	//   Get back the Task Object now, which should have our results.
	public DistTask getDistTask()
	{
		return this.dTask;
	}

	public static void main(String args[]) throws Exception
	{
		// You can accept the number of samples to be used for computing Pi from the command argument.
		long n = Long.parseLong(args[0]); // Example, pass 400000000

		// Create a distributed task object for Monte Carlo computation of pi.
		MCPi mcpi = new MCPi(n);

		// Read the ZooKeeper ensemble information from the environment variable.
		// Also pass the task object to be send to the distributed platform.
		DistClient dt = new DistClient(System.getenv("ZKSERVER"), mcpi);

		// Initiate the zk related workflow.
		dt.startClient();

		//DEBUG ONLY - the compute function should be called by the worker.
		//mcpi.compute();
		//System.out.println(mcpi.getPi());

		//We will wait till we get the results and are notified about it.
		synchronized(dt)
		{
			try { dt.wait(); }
			catch(InterruptedException ie){}
		}

		// get back our task object
		mcpi = (MCPi)dt.getDistTask();
		// And display the results.
		System.out.println(mcpi.getPi());

	}
}
