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
		workerNodeName = this.zk.create("/dist04/workers/worker-", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
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
		this.zk.getChildren(workerNodeName, WorkerTaskWatcher, WorkerTaskWatcherCallBack, null);
	}

	public void assign() {
		if (workers.size() == 0 || pendingTaskList.size() == 0) return;
		for (Map.Entry<String, Boolean> worker:workers.entrySet())
			if (worker.getValue()) {
				if (pendingTaskList.isEmpty()) break;
				String task = pendingTaskList.poll();
				Logger.info("DISTAPP - assign: " + task + " to " + worker.getKey());
				this.zk.create("/dist04/workers/" + worker.getKey() + "/" + task, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, new Create2Callback() {
					public void processResult(int i, String s, Object o, String s1, Stat stat) {
					}
				}, null);
				tasks.put(task, true);
				worker.setValue(false);
			}
	}

	public void processTasks(List<String> tasks) {
		try {
			String task = tasks.get(0);
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
			this.zk.delete(workerNodeName + "/" + task, -1);
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
			Logger.info("DISTAPP - Event received: " + watchedEvent);
			getWorkers();
		}
	};

	ChildrenCallback WorkersCallback = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children)
		{
			Logger.info("DISTAPP - forWorkersChangeChildrenCallback called:" + rc + ", " + path + ", " + ctx + ", " + children);
			switch (Code.get(rc)) {
				// in the event of connection loss we need to re-execute getWorkers with a new watcher and call back
				case CONNECTIONLOSS: {
					getWorkers();
					break;
				}
				// in the event that the getWorkers call was successful then print to console the available workers
				case OK: {
					Logger.info("DISTAPP - Current available # of workers: " + children.size());
					Logger.info("DISTAPP - WorkerList: " + children);
					HashMap<String, Boolean> newMap = new HashMap<>();
					for (String worker: children) {
						newMap.put(worker, workers.getOrDefault(worker, true));
						zk.getChildren("/dist04/workers/" + worker, WorkerTaskWatcher, WorkerTaskWatcherCallBack, null);
					}
					workers = newMap;
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
			Logger.info("DISTAPP - Event received: " + e);
			zk.getChildren(e.getPath(), WorkerTaskWatcher, WorkerTaskWatcherCallBack, null);
		}
	};

	ChildrenCallback WorkerTaskWatcherCallBack = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children) {
			Logger.info("DISTAPP - WorkerTaskWatcherCallBack called:" + rc + ", " + path + ", " + ctx + ", " + children);
			if (Code.get(rc) == Code.OK) {
				if (isMaster) {
					if (children.size() == 0) {
						Logger.info("DISTAPP - " + path + " has finished its task.");
						workers.put(path.substring("/dist04/workers/".length()), true);
						assign();
					}
				} else {
					if (children.size() > 0) {
						Logger.info("DISTAPP - " + path + " has been assigned: " + children);
						Thread thread = new Thread(new Runnable() {
							public void run() {
								processTasks(children);
							}
						});
						thread.start();
					}
				}
			}
		}
	};

	Watcher TasksWatcher = new Watcher()
	{
		public void process(WatchedEvent watchedEvent) {
			Logger.info("DISTAPP - Event received: " + watchedEvent);
			getTasks();
		}
	};

	ChildrenCallback TasksCallBack = new ChildrenCallback()
	{
		public void processResult(int rc, String path, Object ctx, List<String> children)
		{
			Logger.info("DISTAPP - TasksCallBack called:" + rc + ", " + path + ", " + ctx + ", " + children);
			if (Code.get(rc) == Code.OK) {
				Logger.info("DISTAPP - TaskList: " + children);
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
