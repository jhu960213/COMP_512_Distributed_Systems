package Server.Middleware;

import Server.Common.Trace;
import Server.Exception.InvalidTransactionException;
import Server.Exception.TransactionAbortedException;
import Server.LockManager.TransactionObject;

import java.util.*;

public class TransactionManager {
    private static int TRANSACTION_TIMEOUT = 10000;

    private HashMap<Integer, TransactionObject> activeTransactions;
    private HashSet<Integer> abortedTransactions;
    private HashMap<Integer, HashSet<Middleware.ResourceServer>> transactionDataOps;
    private Integer transactionId;
    private Middleware middleware;

    public TransactionManager(Middleware middleware) {
        this.activeTransactions  = new HashMap<>();
        this.abortedTransactions = new HashSet<>();
        this.transactionDataOps = new HashMap<>();
        this.middleware = middleware;
        this.transactionId = 0;
    }

    public HashMap<Integer, TransactionObject> getActiveTransactions() {
        return activeTransactions;
    }

    public void setActiveTransactions(HashMap<Integer, TransactionObject> activeTransactions) {
        this.activeTransactions = activeTransactions;
    }

    public HashSet<Integer> getAbortedTransactions() {
        return abortedTransactions;
    }

    public void setAbortedTransactions(HashSet<Integer> abortedTransactions) {
        this.abortedTransactions = abortedTransactions;
    }

    public HashMap<Integer, HashSet<Middleware.ResourceServer>> getTransactionDataOps() {
        return transactionDataOps;
    }

    public void setTransactionDataOps(HashMap<Integer, HashSet<Middleware.ResourceServer>> transactionDataOps) {
        this.transactionDataOps = transactionDataOps;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public synchronized int start()
    {
        Trace.info("TM::start() called");
        this.transactionId++;
        int transactionID = this.transactionId;
        TransactionObject transaction = new TransactionObject(transactionID);
        this.activeTransactions.put(transactionId, transaction);
        transaction.setTimeout(TRANSACTION_TIMEOUT, new TimerTask() {
            public void run() {
                timeout(transactionID);
            }
        });
        this.transactionDataOps.put(transactionId, new HashSet<Middleware.ResourceServer>());
        return transactionId;
    }

    public synchronized boolean commit(int xid)
    {
        Trace.info("TM::commit(" + xid + ") called");
        TransactionObject transaction = this.activeTransactions.get(xid);
        if (transaction != null) transaction.cancelTime();
        this.transactionDataOps.remove(xid);
        this.activeTransactions.remove(xid);
        return true;
    }

    public synchronized void abort(int xid)
    {
        Trace.info("TM::abort(" + xid + ") called");
        TransactionObject transaction = this.activeTransactions.get(xid);
        if (transaction != null) transaction.cancelTime();
        this.transactionDataOps.remove(xid);
        this.activeTransactions.remove(xid);
        this.abortedTransactions.add(xid);
    }

    public synchronized void checkTransaction(int xid, String caller) throws InvalidTransactionException, TransactionAbortedException
    {
        Trace.info("TM::checkTransaction(" + xid + "," + caller + ") called");
        if (xid > this.transactionId) throw new InvalidTransactionException(xid, caller);
        else if (this.abortedTransactions.contains(xid)) throw new TransactionAbortedException(xid, caller);
        else if (!this.activeTransactions.containsKey(xid)) throw new InvalidTransactionException(xid, caller);
        else {
            TransactionObject transaction = this.activeTransactions.get(xid);
            transaction.updateTimer(TRANSACTION_TIMEOUT, new TimerTask() {
                public void run() {
                    timeout(xid);
                }
            });
        }
    }

    public synchronized HashSet<Middleware.ResourceServer> dataOperationsOfTransaction(int xid)
    {
        if (!this.activeTransactions.containsKey(xid)) return null;
        return this.transactionDataOps.get(xid);
    }

    public synchronized void addDataOperation(int xid, Middleware.ResourceServer rm)
    {
        if (this.transactionDataOps.containsKey(xid)) this.transactionDataOps.get(xid).add(rm);
    }

    public synchronized void timeout(int xid) {
        Trace.info("TM::timeout(" + xid + ") called");
        middleware.passivelyAbort(xid);
    }
}
