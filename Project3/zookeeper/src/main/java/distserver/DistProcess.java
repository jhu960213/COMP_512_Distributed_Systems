package distserver;

import org.apache.zookeeper.AsyncCallback.*;
import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import task.DistTask;
import utils.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;

import static java.lang.Runtime.getRuntime;

public class DistProcess extends Thread
{
	private ZooKeeper zk;
	private String zkServer;
	private String pInfo;
	private boolean isMaster;
	private String workerNodeName;
	HashMap<String, Boolean> tasks;
	HashMap<String, Boolean> workers;
	LinkedList<String> pendingTaskList;
	Logger LOG;

	public DistProcess(String zkhost)
	{
		this.zkServer = zkhost;
		this.isMaster = false;
		this.pInfo = ManagementFactory.getRuntimeMXBean().getName();
		pendingTaskList = new LinkedList<>();
		workers = new HashMap<>();
		tasks = new HashMap<>();
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
		if (!isMaster()) this.zk.delete(this.workerNodeName, -1, null, null);
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
		}
	}

	Watcher connectionWater = new Watcher() {
		public void process(WatchedEvent watchedEvent) {
			LOG.info("DISTAPP - Event received: " + watchedEvent);
		}
	};

	public void startProcess() throws IOException, KeeperException, InterruptedException {
		try {
			//connect to ZK, installs a watcher to detect changes in its connection with ensemble.
			this.zk = new ZooKeeper(this.zkServer, 1000, connectionWater);

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
			runForWorker();
			this.isMaster = false;

			getWorkerTasks();

		}
		System.out.println("DISTAPP - ZK Connection information: " + this.zkServer);
		System.out.println("DISTAPP - Role: " + " I will be functioning as " +(this.isMaster?"master":"worker"));
		System.out.println("DISTAPP - Process information: " + this.pInfo);
		System.out.println("DISTAPP - New " + (this.isMaster?"master":"worker") + " started...");
	}

	// **** DEALING WITH WORKER STATE CHANGES **** //

	// instantiate and installs a new watcher for when the workers change in the zookeeper system on the workers znode
	Watcher WorkersWatcher = new Watcher()
	{
		public void process(WatchedEvent watchedEvent) {
			LOG.info("DISTAPP - Event received: " + watchedEvent);
			getWorkers();
		}
	};

	ChildrenCallback WorkersCallback = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children)
		{
			System.out.println("DISTAPP - forWorkersChangeChildrenCallback called:" + rc + ", " + path + ", " + ctx + ", " + children);
			switch (Code.get(rc)) {
				// in the event of connection loss we need to re-execute getWorkers with a new watcher and call back
				case CONNECTIONLOSS: {
					getWorkers();
					break;
				}
				// in the event that the getWorkers call was successful then print to console the available workers
				case OK: {
					LOG.info("DISTAPP - Current available # of workers: " + children.size());
					System.out.println("DISTAPP - WorkerList: " + children);
					HashMap<String, Boolean> newMap = new HashMap<>();
					for (String worker: children) {
						newMap.put(worker, workers.getOrDefault(worker, true));
						zk.getChildren("/dist04/workers/" + worker, WorkerTaskWatcher, WorkerTaskWatcherCallBack, null);
					}
					workers = newMap;
					assign();
					break;
				} default: {
					LOG.info("Call to zk.getChildren() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}
		}
	};

	public void getWorkers()
	{
		zk.getChildren("/dist04/workers", WorkersWatcher, WorkersCallback, null);
	}

	// **** DEALING WITH WORKERS' JOB STATE CHANGES **** //

	Watcher WorkerTaskWatcher = new Watcher()
	{
		public void process(WatchedEvent e) {
			LOG.info("DISTAPP - Event received: " + e);
			zk.getChildren(e.getPath(), WorkerTaskWatcher, WorkerTaskWatcherCallBack, null);
		}
	};

	ChildrenCallback WorkerTaskWatcherCallBack = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children) {
			System.out.println("DISTAPP - WorkerTaskWatcherCallBack called:" + rc + ", " + path + ", " + ctx + ", " + children);
			if (Code.get(rc) == Code.OK) {
				if (isMaster) {
					if (children.size() == 0) {
						LOG.info("DISTAPP - " + path + " has finished its task.");
						workers.put(path.substring("/dist04/workers/".length()), true);
						assign();
					}
				} else {
					if (children.size() > 0) {
						LOG.info("DISTAPP - " + path + " has been assigned: " + children);
						Thread thread = new Thread(new Runnable() {
							public void run() {
								processTasks(children);
							}
						});
						thread.run();
					}
				}
			}
		}
	};

	public void processTasks(List<String> tasks) {
		try {
			String task = tasks.get(0);
			System.out.println("DISTAPP - processTask called:" + task);

			byte[] data = zk.getData("/dist04/tasks/" + task, false, null);

			// Re-construct our task object.
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInput in = new ObjectInputStream(bis);
			DistTask dt = (DistTask) in.readObject();

			System.out.println("dt = " + dt);
			// Execute the task.
			dt.compute();

			// Serialize our Task object back to a byte array!
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(dt);
			oos.flush();
			byte[] dataSerial = bos.toByteArray();

			// Store it inside the result node.
			this.zk.create("/dist04/tasks/" + (String) task + "/result", dataSerial, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			this.zk.delete(workerNodeName + "/" + task, -1);
		}
		catch(NodeExistsException nee){System.out.println(nee);}
		catch(KeeperException ke){System.out.println(ke);}
		catch(InterruptedException ie){System.out.println(ie);}
		catch(IOException io){System.out.println(io);}
		catch(ClassNotFoundException cne){System.out.println(cne);}
	}

	public void getWorkerTasks() {
		this.zk.getChildren(workerNodeName, WorkerTaskWatcher, WorkerTaskWatcherCallBack, null);
	}


	Watcher TasksWatcher = new Watcher()
	{
		public void process(WatchedEvent watchedEvent) {
			LOG.info("DISTAPP - Event received: " + watchedEvent);
			getTasks();
		}
	};

	ChildrenCallback TasksCallBack = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children)
		{
			System.out.println("DISTAPP - TasksCallBack called:" + rc + ", " + path + ", " + ctx + ", " + children);
			if (Code.get(rc) == Code.OK) {
				System.out.println("DISTAPP - TaskList: " + children);
				HashMap newMap = new HashMap();
				for (String task:children) {
					if (!tasks.containsKey(task)) pendingTaskList.add(task);
					newMap.put(task, tasks.getOrDefault(task, false));
				}
				tasks = newMap;
				assign();
			}
		}
	};

	public void assign() {
		if (workers.size() == 0 || pendingTaskList.size() == 0) return;
		System.out.println("DISTAPP - assign: ");
		System.out.println("~~~~Workers = " + workers);
		System.out.println("~~~~PendingTasks = " + pendingTaskList);
		System.out.println("~~~~tasks = " + tasks);
		for (Map.Entry<String, Boolean> worker:workers.entrySet())
			if (worker.getValue()) {
				if (pendingTaskList.isEmpty()) break;
				String task = pendingTaskList.poll();
				System.out.println("----" + task + " to " + worker.getKey() + "-----");
				this.zk.create("/dist04/workers/" + worker.getKey() + "/" + task, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, new Create2Callback() {
					public void processResult(int i, String s, Object o, String s1, Stat stat) {
					}
				}, null);
				tasks.put(task, true);
				worker.setValue(false);
			}
	}

	// Master fetching tasks under the task znode
	public void getTasks()
	{
		zk.getChildren("/dist04/tasks", TasksWatcher, TasksCallBack, null);
	}

//	Watcher TaskResultWatcher = new Watcher() {
//		public void process(WatchedEvent watchedEvent) {
//			String taskName = watchedEvent.getPath().substring("/dist04/tasks/".length());
//			Worker worker = workers.getOrDefault(assignedTasks.get(taskName), null);
//			if (worker != null) worker.setIdle(true);
//			assign();
//		}
//	};
//
//	ChildrenCallback TaskResultCallBack = new ChildrenCallback() {
//		public void processResult(int rc, String path, Object ctx, List<String> children) {
//
//		}
//	};
//
//	public void getTaskResult(String task)
//	{
//		zk.getChildren("/dist04/tasks/" + task, TaskResultWatcher, TaskResultCallBack, null);
//	}

	// Try to become the master.
	public void runForMaster() throws KeeperException, InterruptedException
	{
		// Try to create an ephemeral node to be the master, put the hostname and pid of this process as the data.
		// This is an example of Synchronous API invocation as the function waits for the execution and no callback is involved..
		this.zk.create("/dist04/master", this.pInfo.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	// Try to become the worker
	public void runForWorker() throws KeeperException, InterruptedException
	{
		workerNodeName = this.zk.create("/dist04/workers/worker-", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
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
