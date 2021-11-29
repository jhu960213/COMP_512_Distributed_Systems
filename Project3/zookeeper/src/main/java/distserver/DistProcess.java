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

	HashMap<String, String> assignedTasks;
	SortedSet<String> pendingTask;

	HashSet<String> idleWorkers;
	HashSet<String> busyWorkers;

	public DistProcess(String zkhost)
	{
		this.zkServer = zkhost;
		this.isMaster = false;
		this.pInfo = ManagementFactory.getRuntimeMXBean().getName();
		pendingTask = new TreeSet<>();
		assignedTasks = new HashMap<>();
		idleWorkers = new HashSet<>();
		busyWorkers = new HashSet<>();
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

	public void stopProcess() throws InterruptedException, KeeperException {
		if (!isMaster()) this.zk.delete(this.workerNodeName, -1);
		this.zk.close();
	}

	public void startProcess() throws IOException, KeeperException, InterruptedException {
		try {
			//connect to ZK, installs a watcher to detect changes in its connection with ensemble.
			this.zk = new ZooKeeper(this.zkServer, 1000, new Watcher() {
				public void process(WatchedEvent watchedEvent) {
					Logger.info("DISTAPP - Event received: " + watchedEvent);
				}
			});
			runForMaster();
			this.isMaster = true;
			getWorkers();
			getTasks();
		}
		catch(NodeExistsException nee)  {
			runForWorker();
			this.isMaster = false;
			getWorkerTasks();
		}
		Logger.info("DISTAPP - ZK Connection information: " + this.zkServer);
		Logger.info("DISTAPP - Role: " + " I will be functioning as " +(this.isMaster?"master":"worker"));
		Logger.info("DISTAPP - Process information: " + this.pInfo);
		Logger.info("DISTAPP - New " + (this.isMaster?"master":"worker") + " started...");
		while (true) {}	//keep main thread alive
	}

	public void runForMaster() throws KeeperException, InterruptedException
	{
		this.zk.create("/dist04/master", this.pInfo.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	public void runForWorker() throws KeeperException, InterruptedException
	{
		workerNodeName = this.zk.create("/dist04/workers/worker-", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
	}

	public void getTasks()
	{
		zk.getChildren("/dist04/tasks", TasksWatcher, TasksCallBack, null);
	}

	public void getWorkers()
	{
		zk.getChildren("/dist04/workers", WorkersWatcher, WorkersCallback, null);
	}

	public void getWorkerTasks() {
		this.zk.getData(workerNodeName, WorkerTaskWatcher, WorkerTaskCallBack, null);
	}

	public void getTaskResult(String task)
	{
		zk.getChildren("/dist04/tasks/" + task, TaskResultWatcher, TaskResultCallBack, null);
	}

	public void assign() {
		if (idleWorkers.size() == 0 || pendingTask.size() == 0) return;
		HashMap<String, String> assigning = new HashMap<>();
		for (String task: pendingTask) {
			if (idleWorkers.isEmpty()) break;
			int rand = new Random().nextInt(idleWorkers.size());
			String worker = (String) idleWorkers.toArray()[rand];
			Logger.info("\nDISTAPP - assigning: " + task + " to " + worker + "\n");
			this.zk.setData("/dist04/workers/" + worker, task.getBytes(), -1, new StatCallback() {
				public void processResult(int i, String s, Object o, Stat stat) {

				}
			}, null);
			idleWorkers.remove(worker);
			busyWorkers.add(worker);
			assigning.put(task, worker);
		}
		assignedTasks.putAll(assigning);
		pendingTask.removeAll(assigning.keySet());
	}

	public void processTasks(String task) {
		try {
			Logger.info("DISTAPP - processTask called:" + task);

			byte[] data = zk.getData("/dist04/tasks/" + task, false, null);

			// Re-construct our task object.
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
			ObjectInput in = new ObjectInputStream(bis);
			DistTask dt = (DistTask) in.readObject();

			Logger.info("dt = " + dt);
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
		}
		catch(NodeExistsException nee){Logger.error(nee);}
		catch(KeeperException ke){Logger.error(ke);}
		catch(InterruptedException ie){Logger.error(ie);}
		catch(IOException io){Logger.error(io);}
		catch(ClassNotFoundException cne){Logger.error(cne);}
	}


	// **** DEALING WITH WORKER STATE CHANGES **** //
	// instantiate and installs a new watcher for when the workers change in the zookeeper system on the workers znode
	Watcher WorkersWatcher = new Watcher()
	{
		public void process(WatchedEvent watchedEvent) {
//			Logger.info("DISTAPP - Event received: " + watchedEvent);
			getWorkers();
		}
	};

	ChildrenCallback WorkersCallback = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children)
		{
			Logger.info("DISTAPP - WorkerChangedCallback:" + rc + ", " + path + ", " + ctx + ", " + children);
			switch (Code.get(rc)) {
				// in the event of connection loss we need to re-execute getWorkers with a new watcher and call back
				case CONNECTIONLOSS: {
					getWorkers();
					break;
				}
				// in the event that the getWorkers call was successful then print to console the available workers
				case OK: {
					HashSet<String> newIdleWorkers = new HashSet<>();
					HashSet<String> newBusyWorkers = new HashSet<>();
					for (String worker: children)
						if (busyWorkers.contains(worker)) {
							newBusyWorkers.add(worker);
						} else if (idleWorkers.contains(worker)) {
							newIdleWorkers.add(worker);
						} else {
							newIdleWorkers.add(worker);
						}
					busyWorkers = newBusyWorkers;
					idleWorkers = newIdleWorkers;

					Logger.info("\nDISTAPP - Idle Workers: " + idleWorkers);
					Logger.info("DISTAPP - Busy Workers: " + busyWorkers + "\n");
					assign();
					break;
				} default: {
					Logger.info("Call to zk.getChildren() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}
		}
	};

	// **** DEALING WITH WORKERS' TASKS STATE CHANGES **** //

	Watcher WorkerTaskWatcher = new Watcher()
	{
		public void process(WatchedEvent e) {
//			Logger.info("DISTAPP - Event received: " + e);
			zk.getData(e.getPath(), WorkerTaskWatcher, WorkerTaskCallBack, null);
		}
	};

	DataCallback WorkerTaskCallBack = new DataCallback()
	{
		public void processResult(int rc, String path, Object o, byte[] bytes, Stat stat) {
			Logger.info("DISTAPP - JobCallBack:" + rc + ", " + path );
			if (Code.get(rc) == Code.OK && bytes!=null && bytes.length > 0) {
				String taskName = new String(bytes);
				Logger.info("\nDISTAPP - " + path + " has been assigned: " + taskName + "\n");
				Thread thread = new Thread(new Runnable() {
					public void run() {
						processTasks(taskName);
					}
				});
				thread.start();
			}
		}
	};

	Watcher TasksWatcher = new Watcher()
	{
		public void process(WatchedEvent watchedEvent) {
//			Logger.info("DISTAPP - Event received: " + watchedEvent);
			getTasks();
		}
	};

	ChildrenCallback TasksCallBack = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children)
		{
			Logger.info("\nDISTAPP - TasksCallBack called:" + rc + ", " + path + ", " + children);
			if (Code.get(rc) == Code.OK) {
				HashMap newMap = new HashMap();
				HashMap<String, String> newAssignedTasks = new HashMap<>();
				SortedSet<String> newPendingTask = new TreeSet<>();
				for (String task:children)
					if (assignedTasks.containsKey(task)) {
						newAssignedTasks.put(task, assignedTasks.get(task));
					} else if (pendingTask.contains(task)) {
						newPendingTask.add(task);
					} else {
						newPendingTask.add(task);
						getTaskResult(task);
					}
				assignedTasks = newAssignedTasks;
				pendingTask = newPendingTask;
				Logger.info("DISTAPP - AssignedTasks: " + assignedTasks + " PendingTasks:" + pendingTask + "\n");
				assign();
			}
		}
	};


	Watcher TaskResultWatcher = new Watcher() {
		public void process(WatchedEvent watchedEvent) {
			String taskName = watchedEvent.getPath().substring("/dist04/tasks/".length());
			Logger.info("DISTAPP - TaskCompletionCallBack:" + watchedEvent.getPath() );
			String worker = assignedTasks.get(taskName);
			assignedTasks.remove(taskName);
			busyWorkers.remove(worker);
			idleWorkers.add(worker);
			assign();
		}
	};

	ChildrenCallback TaskResultCallBack = new ChildrenCallback() {
		public void processResult(int rc, String path, Object ctx, List<String> children) {

		}
	};

	public static void main(String args[]) throws Exception
	{
		// Read the ZooKeeper ensemble information from the environment variable.
		DistProcess distProcess = new DistProcess(System.getenv("ZKSERVER"));

		getRuntime().addShutdownHook(new Thread(() -> {
			try {
				distProcess.stopProcess();
				System.out.println("DISTAPP - Shutting down" + (distProcess.isMaster() ? "master process..." : "worker process..."));
			} catch (InterruptedException | KeeperException e) {
				e.printStackTrace();
			}
		}));

		distProcess.startProcess();
	}
}
