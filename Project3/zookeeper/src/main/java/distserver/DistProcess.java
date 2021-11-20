package distserver;

import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import task.DistTask;
import utils.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.List;

import static java.lang.Runtime.getRuntime;

// TODO
// Replace XX with your group number.
// You may have to add other interfaces such as for threading, etc., as needed.
// This class will contain the logic for both your master process as well as the worker processes.
//  Make sure that the callbacks and watch do not conflict between your master's logic and worker's logic.
//		This is important as both the master and worker may need same kind of callbacks and could result
//			with the same callback functions.
//	For a simple implementation I have written all the code in a single class (including the callbacks).
//		You are free it break it apart into multiple classes, if that is your programming style or helps
//		you manage the code more modularly.
//	REMEMBER !! ZK client library is single thread - Watches & CallBacks should not be used for time consuming tasks.
//		Ideally, Watches & CallBacks should only be used to assign the "work" to a separate thread inside your program.
public class DistProcess extends Thread implements Watcher, AsyncCallback.ChildrenCallback
{
	private ZooKeeper zk;
	private String zkServer;
	private String pInfo;
	private boolean isMaster;
	private Long pid;
	private DistProcessStatus status;
	private Logger LOG;

	public DistProcess(String zkhost)
	{
		this.zkServer = zkhost;
		this.isMaster = false;
		this.pInfo = ManagementFactory.getRuntimeMXBean().getName();
		this.pid = ManagementFactory.getRuntimeMXBean().getPid();
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

	public String getpInfo() {
		return pInfo;
	}

	public void setpInfo(String pInfo) {
		this.pInfo = pInfo;
	}

	public boolean isMaster() {
		return isMaster;
	}

	public void setMaster(boolean master) {
		isMaster = master;
	}

	public void stopProcess() throws InterruptedException {
		this.zk.close();
	}

	@Override
	public void run() {
		try {
			startProcess();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void startProcess() throws IOException, KeeperException, InterruptedException {
		try {
			//connect to ZK, installs a watcher to detect changes in its connection with ensemble.
			this.zk = new ZooKeeper(this.zkServer, 1000, this);

			// See if you can become the master (i.e, no other master exists)
			runForMaster();
			this.isMaster = true;

			// Install monitoring on workers and get changes to the list of available workers
			getWorkers();

			// Install monitoring on any new tasks that will be created.
			getTasks();
		}
		catch(NodeExistsException nee)  {
			System.out.println("DISTAPP - Exception: " + nee.getMessage());
			// Creates worker znode
			runForWorker(this.pid);
			this.isMaster = false;
			// Installs monitoring on new tasks that will be created
			// TODO monitor for worker tasks?
			// TODO: What else will you need if this was a worker process?
		}
		System.out.println("DISTAPP - ZK Connection information: " + this.zkServer);
		System.out.println("DISTAPP - Role: " + " I will be functioning as " +(this.isMaster?"master":"worker"));
		System.out.println("DISTAPP - Process information: " + this.pInfo);
		System.out.println("DISTAPP - New " + (this.isMaster?"master":"worker") + " started...");
	}

	// **** DEALING WITH WORKER STATE CHANGES **** //

	// instantiate and installs a new watcher for when the workers change in the zookeeper system on the workers znode
	Watcher forWorkersChange = new Watcher()
	{
		public void process(WatchedEvent e) {
			if(e.getType() == Event.EventType.NodeChildrenChanged)
			{
				LOG.info("DISTAPP - Event received: " + e);
				assert "/dist04/workers".equals(e.getPath());
				getWorkers();
			}
		}
	};

	ChildrenCallback forWorkersChangeChildrenCallback = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children)
		{
			switch (Code.get(rc)) {
				// in the event of connection loss we need to re-execute getWorkers with a new watcher and call back
				case CONNECTIONLOSS: {
					getWorkers();
					break;
				}
				// in the event that the getWorkers call was successful then print to console the available workers
				case OK: {
					LOG.info("DISTAPP - Current available # of workers: " + children.size());
					System.out.print("DISTAPP - WorkerList: ");
					for (String name: children) {
						System.out.print(name + "|");
					}
					System.out.println("");
					break;
				} default: {
					LOG.info("Call to zk.getChildren() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}
		}
	};

	public void getWorkers()
	{
		zk.getChildren("/dist04/workers", forWorkersChange, forWorkersChangeChildrenCallback, null);
	}


	// Master fetching tasks under the task znode
	public void getTasks()
	{
		zk.getChildren("/dist04/tasks", this, this, null);
	}

	// Try to become the master.
	public void runForMaster() throws KeeperException, InterruptedException
	{
		this.status = DistProcessStatus.MASTER;
		// Try to create an ephemeral node to be the master, put the hostname and pid of this process as the data.
		// This is an example of Synchronous API invocation as the function waits for the execution and no callback is involved..
		this.zk.create("/dist04/master", this.pInfo.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	public void runForWorker(Long pid) throws KeeperException, InterruptedException
	{
		this.status = DistProcessStatus.IDLE;
		// Tries to create an ephemeral znode for a worker and put the hostname and pid of process in it's data field
		this.zk.create("/dist04/workers/worker-" + pid, this.status.toString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	public void process(WatchedEvent e)
	{
		//Get watcher notifications.

		//!! IMPORTANT !!
		// Do not perform any time consuming/waiting steps here
		//	including in other functions called from here.
		// 	You will be essentially holding up ZK client library
		//	thread and you will not get other notifications.
		//	Instead include another thread in your program logic that
		//   does the time consuming "work" and notify that thread from here.

		System.out.println("DISTAPP - Event received: " + e);
		// Master should be notified if any new znodes are added to tasks.
		if(e.getType() == Watcher.Event.EventType.NodeChildrenChanged && e.getPath().equals("/dist04/tasks"))
		{
			// There has been changes to the children of the node.
			// We are going to re-install the Watch as well as request for the list of the children.
			getTasks();
		}
	}

	// Asynchronous callback that is invoked by the zk.getChildren request.
	public void processResult(int rc, String path, Object ctx, List<String> children)
	{
		//!! IMPORTANT !!
		// Do not perform any time consuming/waiting steps here
		//	including in other functions called from here.
		// 	Your will be essentially holding up ZK client library 
		//	thread and you will not get other notifications.
		//	Instead include another thread in your program logic that
		//   does the time consuming "work" and notify that thread from here.

		// This logic is for master !!
		// Every time a new task znode is created by the client, this will be invoked.

		// TODO: Filter out and go over only the newly created task znodes.
		//		Also have a mechanism to assign these tasks to a "Worker" process.
		//		The worker must invoke the "compute" function of the Task send by the client.
		// What to do if you do not have a free worker process?

		System.out.println("DISTAPP - processResult: " + rc + "-" + path + "-" + ctx);
		for(String c: children)
		{
			System.out.println(c);
			try
			{
				//TODO There is quite a bit of worker specific activities here,
				// that should be moved done by a process function as the worker.

				//TODO!! This is not a good approach, you should get the data using an async version of the API.
				byte[] taskSerial = this.zk.getData("/dist04/tasks/"+c, false, null);

				// Re-construct our task object.
				ByteArrayInputStream bis = new ByteArrayInputStream(taskSerial);
				ObjectInput in = new ObjectInputStream(bis);
				DistTask dt = (DistTask) in.readObject();

				//Execute the task.
				//TODO: Again, time consuming stuff. Should be done by some other thread and not inside a callback!
				dt.compute();
				
				// Serialize our Task object back to a byte array!
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos);
				oos.writeObject(dt); oos.flush();
				taskSerial = bos.toByteArray();

				// Store it inside the result node.
				this.zk.create("/dist04/tasks/"+c+"/result", taskSerial, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				//zk.create("/distXX/tasks/"+c+"/result", ("Hello from "+pinfo).getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			catch(NodeExistsException nee){System.out.println(nee);}
			catch(KeeperException ke){System.out.println(ke);}
			catch(InterruptedException ie){System.out.println(ie);}
			catch(IOException io){System.out.println(io);}
			catch(ClassNotFoundException cne){System.out.println(cne);}
		}
	}

	public static void main(String args[]) throws Exception
	{
		// Read the ZooKeeper ensemble information from the environment variable.
		DistProcess distProcess = new DistProcess(System.getenv("ZKSERVER"));
		distProcess.start();

		// Replace this with an approach that will make sure that the process is up and running forever.
		getRuntime().addShutdownHook(new Thread(() -> {
			try {
				distProcess.stopProcess();
				if (distProcess.isMaster())
					System.out.print("DISTAPP - Shutting down master process... \n");
				else
					System.out.println("DISTAPP - Shutting down worker process... \n");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}));
	}
}
