package distserver;

import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import task.DistTask;
import utils.Logger;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import static java.lang.Runtime.getRuntime;

public class DistProcess extends Thread
{
	private ZooKeeper zk;
	private Random rand;
	private String zkServer;
	private String pInfo;
	private boolean isMaster;
	private Long pid;
	private DistProcessStatus status;
	private HashMap<String, DistProcessStatus> assignedWorkers;
	private Logger LOG;

	public DistProcess(String zkhost)
	{
		this.rand = new Random();
		this.assignedWorkers = new HashMap<>();
		this.zkServer = zkhost;
		this.isMaster = false;
		this.pInfo = ManagementFactory.getRuntimeMXBean().getName();
		this.pid = ManagementFactory.getRuntimeMXBean().getPid();
	}

	public Random getRand() {
		return rand;
	}

	public void setRand(Random rand) {
		this.rand = rand;
	}

	public HashMap<String, DistProcessStatus> getAssignedWorkers() {
		return assignedWorkers;
	}

	public void setAssignedWorkers(HashMap<String, DistProcessStatus> assignedWorkers) {
		this.assignedWorkers = assignedWorkers;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public DistProcessStatus getStatus() {
		return status;
	}

	public void setStatus(DistProcessStatus status) {
		this.status = status;
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

	public void cleanUpChildrenNodes(String path) throws KeeperException, InterruptedException
	{
		List<String> childrenNodes = getZk().getChildren(path, false);
		if (!childrenNodes.isEmpty()) {
			for (String childName : childrenNodes) {
				getZk().delete(path + "/" + childName, -1);
			}
			LOG.info("DISTAPP - Cleaned up: " + path);
		}
		else {
			LOG.info("DISTAPP - Nothing to clean here: " + path);
		}
	}

	public void stopProcess() throws InterruptedException, KeeperException
	{
		if (isMaster()) {
			cleanUpChildrenNodes("/dist04/workers");
			cleanUpChildrenNodes("/dist04/tasks");
			cleanUpChildrenNodes("/dist04/notify");
		}
		else {
			getZk().delete("/dist04/workers/worker-" + getPid() + "/jobs", -1);
			getZk().delete("/dist04/workers/worker-" + getPid(), -1);
		}
		getZk().close();
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
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	Watcher connectionWatcher = new Watcher() {
		@Override
		public void process(WatchedEvent e) {
			LOG.info("\nDISTAPP - Event recieved: " + e);
		}
	};

	public void bootStrap() throws IOException, KeeperException, InterruptedException
	{
		try {
			String dist04 = getZk().create("/dist04",
					serialize("Top Level Node - dist04"),
					Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			LOG.info("created - " + dist04);
		}
		catch (NodeExistsException nee) {
			LOG.info("/dist04 already exists!");
		}
		try {
			String tasks = getZk().create("/dist04/tasks",
					serialize("Keeps track of all client submitted tasks"),
					Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			LOG.info("created - " + tasks);
		}
		catch (NodeExistsException nee) {
			LOG.info("/dist04/tasks already exists!");
		}
		try {
			String workers = getZk().create("/dist04/workers",
					serialize("Keeps track of all active workers and assigned worker jobs"),
					Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			LOG.info("created - " + workers);
		}
		catch (NodeExistsException nee) {
			LOG.info("/dist04/workers already exists!");
		}
		try {
			String notify = getZk().create("/dist04/notify",
					serialize("Keeps track of all workers who have finished their tasks"),
					Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);
			LOG.info("created - " + notify);
		}
		catch (NodeExistsException nee) {
			LOG.info("/dist04/notify already exists!");
		}
		LOG.info("DISTAPP - Zookeeper Bootstrap process completed");
	}

	public void startProcess() throws IOException, KeeperException, InterruptedException {
		try {
			setZk(new ZooKeeper(getZkServer(), 1000, connectionWatcher));
			runForMaster();
			setMaster(true);
			getWorkers();
			getTasks();
			getNotifications();
		}
		catch(NodeExistsException nee)  {
//			LOG.info("DISTAPP - Exception: " + nee.getMessage());
			runForWorker();
			setMaster(false);
			getJobs();
		}
		LOG.info("DISTAPP - ZK Connection information: " + getZkServer());
		LOG.info("DISTAPP - Role: " + " I will be functioning as " +(isMaster()?"master":"worker"));
		LOG.info("DISTAPP - Process information: " + getpInfo());
		LOG.info("DISTAPP - New " + (isMaster()?"master":"worker") + " started...");
	}

	// **** DEALING WITH WORKER STATE CHANGES **** //

	Watcher workersChangeWatcher = new Watcher()
	{
		@Override
		public void process(WatchedEvent e) {
			if(e.getType() == Event.EventType.NodeChildrenChanged)
			{
				LOG.info("\nDISTAPP - Event received: " + e);
				assert "/dist04/workers".equals(e.getPath());
				getWorkers();
			}
		}
	};

	ChildrenCallback workersChangeChildrenCallback = new ChildrenCallback()
	{
		@Override
		public void processResult(int rc, String path, Object ctx, List<String> workers)
		{
			LOG.info("\nDISTAPP - processResult - ChildrenCallBack:");
			LOG.info("rc: " + Code.get(rc));
			LOG.info("path: " + path);
			LOG.info("Object ctx: " + (String) ctx);
			switch (Code.get(rc)) {
				// in the event of connection loss we need to re-execute getWorkers with a new watcher and call back
				case CONNECTIONLOSS: {
					getWorkers();
					break;
				}
				// in the event that the getWorkers call was successful then print to console the available workers
				case OK: {
					LOG.info("\nDISTAPP - Current available # of workers: " + workers.size());
					showWorkers(workers);
					System.out.println("");
					break;
				} default: {
					LOG.info("\nCall to getWorkers() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}
		}
	};

	public void showWorkers(List<String> workers)
	{
		System.out.print("DISTAPP - WorkerList: ");
		for (String name: workers) {
			System.out.print(name + "|");
		}
	}

	public void getWorkers()
	{
		getZk().getChildren("/dist04/workers",
				workersChangeWatcher,
				workersChangeChildrenCallback,
				null);
	}

	// **** DEALING WITH WORKERS' JOB STATE CHANGES **** //

	Watcher workerJobChangeWatcher = new Watcher()
	{
		@Override
		public void process(WatchedEvent e) {
			if(e.getType() == Event.EventType.NodeChildrenChanged)
			{
				LOG.info("\nDISTAPP - Event received: " + e);
				assert ("/dist04/workers/worker-" + pid + "/jobs").equals(e.getPath());
				getJobs();
			}

		}
	};

	ChildrenCallback workerJobChangeCallBack = new ChildrenCallback()
	{
		@Override
		public void processResult(int rc, String path, Object ctx, List<String> jobs) {

            LOG.info("\nDISTAPP - processResult - ChildrenCallBack:");
            LOG.info("rc: " + Code.get(rc));
            LOG.info("path: " + path);
            LOG.info("Object ctx: " + (String) ctx);
			switch (Code.get(rc)) {
				// in the event of connection loss we need to re-execute getJobs() with a new watcher and call back
				case CONNECTIONLOSS: {
				    LOG.info("\nDISTAPP - lost connection...re-executing getJobs()");
					getJobs();
					break;
				}
				// in the event that the getJob() call was successful, retrieve the job and process it
				case OK: {
					if (jobs.size() == 0)
						LOG.info("\nDISTAPP - worker-" + getPid() + " has: " + jobs.size() + " jobs assigned, waiting for new jobs...");
					else
						LOG.info("\nDISTAPP - worker-" + getPid() + " has: " + jobs.size() + " job(s) assigned, executing now...");
					Thread thread = new Thread(() -> processJob(jobs));
					thread.start();
					break;
				} default: {
					LOG.info("\nCall to getJobs() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}
		}
	};

	DataCallback jobDataCallBack = new DataCallback()
	{
		@Override
		public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

		    LOG.info("\nDISTAPP - processResult - DataCallBack:");
		    LOG.info("rc: " + Code.get(rc));
		    LOG.info("path: " + path);
		    LOG.info("Object ctx: " + (String) ctx);
		    LOG.info("byte[] data: " + data);
		    LOG.info("stat: " + stat.toString());
			switch (Code.get(rc)) {
				case CONNECTIONLOSS: {
					getJobData((String) ctx);
					break;
				}
				case OK: {
					Thread computePieThread = new Thread(() -> {
						try {
							computePieAndNotify(data, ctx);
						} catch (IOException e) {
								e.printStackTrace();
						} catch (ClassNotFoundException e) {
								e.printStackTrace();
						} catch (KeeperException e) {
								e.printStackTrace();
						} catch (InterruptedException e) {
								e.printStackTrace();
						}
					});
					computePieThread.start();
					break;
				} default: {
					LOG.info("\nCall to getJobData() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}

		}
	};

	public byte[] serialize(Object obj) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.flush();
		byte[] dataSerial = bos.toByteArray();
		return dataSerial;
	}

	public Object deserialize(byte[] data) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		ObjectInputStream in = new ObjectInputStream(bis);
		Object obj = in.readObject();
		return obj;
	}

	public void computePieAndNotify(byte[] data, Object ctx) throws IOException, ClassNotFoundException, KeeperException, InterruptedException
	{
		DistTask dt = (DistTask) deserialize(data);
		dt.compute();
		byte[] dataSerial = serialize(dt);

		getZk().create("/dist04/tasks/" + (String) ctx + "/result", dataSerial, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		getZk().delete("/dist04/workers/worker-" + getPid() + "/jobs/" + (String) ctx, -1, null, null);
		getZk().create("/dist04/notify/worker-" + getPid(), serialize(DistProcessStatus.IDLE), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}

	public void processJob(List<String> jobs)
	{
		for (String jobName: jobs) {
			getJobData(jobName);
		}
	}

	public void getJobData(String jobName)
	{
		getZk().getData("/dist04/workers/worker-" + this.pid + "/jobs/" + jobName, false, jobDataCallBack, jobName);
	}

	public void getJobs()
	{
		getZk().getChildren("/dist04/workers/worker-" + getPid() + "/jobs",
				workerJobChangeWatcher,
				workerJobChangeCallBack,
				null);
	}

	// **** DEALING WITH MASTER ASSIGNING NEW TASKS TO WORKERS **** //

	Watcher taskChangeWatcher = new Watcher()
	{
		@Override
		public void process(WatchedEvent e) {
			if(e.getType() == Event.EventType.NodeChildrenChanged)
			{
				LOG.info("\nDISTAPP - Event received: " + e);
				assert ("/dist04/tasks").equals(e.getPath());
				getTasks();
			}

		}
	};

	ChildrenCallback taskChangeChildrenCallBack = new ChildrenCallback()
	{
		@Override
		public void processResult(int rc, String path, Object ctx, List<String> tasks) {

			LOG.info("\nDISTAPP - processResult - ChildrenCallBack:");
			LOG.info("rc: " + Code.get(rc));
			LOG.info("path: " + path);
			LOG.info("Object ctx: " + (String) ctx);
			switch (Code.get(rc)) {
				// in the event of connection loss we need to re-execute getJobs() with a new watcher and call back
				case CONNECTIONLOSS: {
					LOG.info("\nDISTAPP - lost connection...re-executing getTasks()");
					getTasks();
					break;
				}
				// in the event that the getTasks() call was successful, retrieve the tasks and assign them
				case OK: {
					if (tasks.size() == 0)
						LOG.info("\nDISTAPP - master-" + getpInfo() + " retrieved " + tasks.size() + " tasks, waiting for tasks to arrive....");
					else
						LOG.info("\nDISTAPP - master-" + getpInfo() + " retrieved " + tasks.size() + " task(s). Assigning to workers now...");
						Thread thread = new Thread(() -> processTasks(tasks));
						thread.start();
					break;
				} default: {
					LOG.info("\nCall to getTasks() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}
		}
	};

	DataCallback taskDataCallBack = new DataCallback()
	{
		@Override
		public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {

			LOG.info("\nDISTAPP - processResult - DataCallBack:");
			LOG.info("rc: " + Code.get(rc));
			LOG.info("path: " + path);
			LOG.info("Object ctx: " + (String) ctx);
			LOG.info("byte[] data: " + data);
			LOG.info("stat: " + stat.toString());
			switch (Code.get(rc)) {
				// in the event of connection loss we need to re-execute getTaskData() with a new data call back
				case CONNECTIONLOSS: {
					getTaskData((String) ctx);
					break;
				}
				// in the event that the getTaskData() call was successful, assign specific task to worker
				case OK: {
					Thread thread = new Thread(() ->
					{boolean response = false;
						try {
							while (!response) {
								response = assignTask(data, ctx);
								if (response == false)
									Thread.sleep(1000);
							}
						} catch (KeeperException | ClassNotFoundException e) {
							e.printStackTrace();
						} catch (InterruptedException | IOException e) {
							e.printStackTrace();
						}
					});
					thread.start();
					break;
				} default: {
					LOG.info("\nCall to getJobData() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}

		}
	};

	StringCallback assignTaskCallback = new StringCallback()
	{
		public void processResult(int rc, String path, Object ctx, String name) {

			LOG.info("\nDISTAPP - processResult - StringCallBack:");
			LOG.info("rc: " + Code.get(rc));
			LOG.info("path: " + path);
			LOG.info("object ctx: " + ctx.toString());
			LOG.info("name: " + name);
			switch(Code.get(rc)) {
				case CONNECTIONLOSS: {
					createAssignment(path, (byte[]) ctx);
					break;
				}
				 case OK: {
					 LOG.info("\nDISTAPP - Task assigned to: " + path);
					 break;
				 }
				case NODEEXISTS: {
					LOG.info("\nDISTAPP - Task already assigned");
					break;
				}
				default: LOG.info("\nDISTAPP - Error when trying to assign task: " + KeeperException.create(Code.get(rc), path));
			}
		}
	};

	public boolean assignTask(byte[] data, Object ctx) throws KeeperException, InterruptedException, IOException, ClassNotFoundException
	{
		List<String> workers = getZk().getChildren("/dist04/workers", false);
		String workerName = workers.get(getRand().nextInt(workers.size()));

		if (!getAssignedWorkers().containsKey(workerName))
		{
			getAssignedWorkers().put(workerName, DistProcessStatus.BUSY);
			LOG.info("\nDISTAPP - Setting worker: " + workerName + " to " + DistProcessStatus.BUSY.toString());
			String assignPath = "/dist04/workers/" + workerName + "/jobs/" + (String) ctx;
			createAssignment(assignPath, data);
			return true;
		} else {
			if (getAssignedWorkers().get(workerName) == DistProcessStatus.IDLE) {
				getAssignedWorkers().put(workerName, DistProcessStatus.BUSY);
				LOG.info("\nDISTAPP - Setting worker: " + workerName + " to " + DistProcessStatus.BUSY.toString());
				String assignPath = "/dist04/workers/" + workerName + "/jobs/" + (String) ctx;
				createAssignment(assignPath, data);
				return true;
			}
		}
		return false;
	}


	public void createAssignment(String path, byte[] data)
	{
		getZk().create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, assignTaskCallback, data);
	}

	public void processTasks(List<String> tasks)
	{
		for (String taskName: tasks) {
			getTaskData(taskName);
		}
	}

	public void getTaskData(String taskName)
	{
		getZk().getData("/dist04/tasks/" + taskName, false, taskDataCallBack, taskName);
	}

	public void getTasks()
	{
		getZk().getChildren("/dist04/tasks",
				taskChangeWatcher,
				taskChangeChildrenCallBack,
				null);
	}

	// **** NOTIFYING MASTER OF WORKER STATUS **** //

	Watcher notificationsChangeWatcher = new Watcher()
	{
		@Override
		public void process(WatchedEvent e) {
			if(e.getType() == Event.EventType.NodeChildrenChanged)
			{
				LOG.info("\nDISTAPP - Event received: " + e);
				assert ("/dist04/notify").equals(e.getPath());
				getNotifications();
			}

		}
	};

	ChildrenCallback notificationsChangeChildrenCallBack = new ChildrenCallback()
	{
		@Override
		public void processResult(int rc, String path, Object ctx, List<String> workers) {

			LOG.info("\nDISTAPP - processResult - ChildrenCallBack:");
			LOG.info("rc: " + Code.get(rc));
			LOG.info("path: " + path);
			LOG.info("Object ctx: " + (String) ctx);
			switch (Code.get(rc)) {
				case CONNECTIONLOSS: {
					LOG.info("\nDISTAPP - lost connection...re-executing getNotifications()");
					getNotifications();
					break;
				}

				// getNotifications() was successful
				case OK: {
					try {
						notifyMaster(workers);
					} catch (KeeperException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					break;
				} default: {
					LOG.info("\nCall to getNotifications() failed: " + KeeperException.create(Code.get(rc), path));
				}
			}
		}
	};

	public void notifyMaster(List<String> workers) throws KeeperException, InterruptedException, IOException, ClassNotFoundException
	{
		for (String workerName: workers)
		{
			byte[] statData = getZk().getData("/dist04/notify/" + workerName, false, null);
			DistProcessStatus status = (DistProcessStatus) deserialize(statData);
			LOG.info("DISTAPP - Worker: " + workerName + " finished it's job, it is now " + status.toString());
			getAssignedWorkers().put(workerName, status);
			getZk().delete("/dist04/notify/" + workerName, -1);
		}
	}

	public void getNotifications() {
		getZk().getChildren("/dist04/notify",
				notificationsChangeWatcher,
				notificationsChangeChildrenCallBack,
				null);
	}

	// **** DEALING WITH STARTUP AS EITHER MASTER OR WORKER **** //

	public void runForMaster() throws KeeperException, InterruptedException, IOException
	{
		bootStrap();
		this.status = DistProcessStatus.MASTER;
		getZk().create("/dist04/master", this.pInfo.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
	}

	public void runForWorker() throws KeeperException, InterruptedException, IOException
	{
		setStatus(DistProcessStatus.IDLE);
		getZk().create("/dist04/workers/worker-" + getPid(),
				serialize(getStatus()),
				Ids.OPEN_ACL_UNSAFE,
				CreateMode.PERSISTENT);
		getZk().create("/dist04/workers/worker-" + getPid() + "/jobs",
				"Active jobs for this worker".getBytes(),
				Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}

	public static void main(String args[]) throws Exception
	{
		DistProcess distProcess = new DistProcess(System.getenv("ZKSERVER"));
		distProcess.start();

		getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (distProcess.isMaster())
					distProcess.LOG.info("DISTAPP - Shutting down master process... \n");
				else
					distProcess.LOG.info("DISTAPP - Shutting down worker process... \n");
				distProcess.stopProcess();
			} catch (InterruptedException | KeeperException e) {
				e.printStackTrace();
			}
		}));
	}
}
