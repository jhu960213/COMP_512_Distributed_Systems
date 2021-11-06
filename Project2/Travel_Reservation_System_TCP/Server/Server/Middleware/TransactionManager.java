package Server.Middleware;

import Server.Common.Trace;
import Server.Exception.InvalidTransactionException;
import Server.Exception.TransactionAbortedException;

import java.util.HashMap;
import java.util.HashSet;

public class TransactionManager {

    private HashSet<Integer> activeTransactions;
    private HashSet<Integer> abortedTransactions;
    private HashMap<Integer, HashSet<Middleware.ResourceServer>> transactionDataOps;
    private Integer transactionId;

    public TransactionManager() {
        this.activeTransactions  = new HashSet<>();
        this.abortedTransactions = new HashSet<>();
        this.transactionDataOps = new HashMap<>();
        this.transactionId = 0;
    }

    public HashSet<Integer> getActiveTransactions() {
        return activeTransactions;
    }

    public void setActiveTransactions(HashSet<Integer> activeTransactions) {
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
        this.transactionId++;
        this.activeTransactions.add(transactionId);
        this.transactionDataOps.put(transactionId, new HashSet<Middleware.ResourceServer>());
        return transactionId;
    }

    public synchronized boolean commit(int xid)
    {
        Trace.info("TM::commit(" + xid + ") called");
        this.transactionDataOps.remove(xid);
        this.activeTransactions.remove(xid);
        return true;
    }

    public synchronized void abort(int xid)
    {
        Trace.info("TM::abort(" + xid + ") called");
        this.transactionDataOps.remove(xid);
        this.activeTransactions.remove(xid);
        this.abortedTransactions.add(xid);
    }

    public synchronized void checkTransaction(int xid, String caller) throws InvalidTransactionException, TransactionAbortedException
    {
        if (xid > this.transactionId) throw new InvalidTransactionException(xid, caller);
        if (this.abortedTransactions.contains(xid)) throw new TransactionAbortedException(xid, caller);
        if (!this.activeTransactions.contains(xid)) throw new InvalidTransactionException(xid, caller);
    }

    public synchronized HashSet<Middleware.ResourceServer> dataOperationsOfTransaction(int xid)
    {
        if (!this.activeTransactions.contains(xid)) return null;
        return this.transactionDataOps.get(xid);
    }

    public synchronized void addDataOperation(int xid, Middleware.ResourceServer rm)
    {
        if (this.transactionDataOps.containsKey(xid)) this.transactionDataOps.get(xid).add(rm);
    }
}
