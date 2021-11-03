package Server.Middleware;

import Server.LockManager.TransactionObject;

import java.util.HashMap;
import java.util.HashSet;

public class TransactionManager {

    private HashMap<Integer, TransactionObject> activeTransactions = new HashMap<>();
    private HashMap<Integer, HashSet<Middleware.ResourceServer>> transactionDataOps = new HashMap<>();
    private int transactionId = 0;
    public synchronized int start()
    {
        TransactionObject transaction = new TransactionObject(++transactionId);
        activeTransactions.put(transactionId, transaction);
        transactionDataOps.put(transactionId, new HashSet<Middleware.ResourceServer>());
        return transaction.getXId();
    }

    public synchronized boolean commit(int xid)
    {
        activeTransactions.remove(xid);
        transactionDataOps.remove(xid);
        return true;
    }

    public synchronized void abort(int xid)
    {

    }

    public synchronized boolean isActive(int xid)
    {
        return activeTransactions.containsKey(xid);
    }

    public synchronized HashSet<Middleware.ResourceServer> dataOperationsOfTransaction(int xid)
    {
        if (!activeTransactions.containsKey(xid)) return null;
        return transactionDataOps.get(xid);
    }

    public synchronized void addDataOperation(int xid, Middleware.ResourceServer rm)
    {
        if (transactionDataOps.containsKey(xid)) transactionDataOps.get(xid).add(rm);
    }
}
