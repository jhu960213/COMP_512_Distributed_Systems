package Server.Middleware;

import Server.Common.Trace;
import Server.Exception.InvalidTransactionException;
import Server.Exception.TransactionAbortedException;

import java.util.HashMap;
import java.util.HashSet;

public class TransactionManager {

    private HashSet<Integer> activeTransactions = new HashSet<>();
    private HashSet<Integer> abortedTransactions = new HashSet<>();
    private HashMap<Integer, HashSet<Middleware.ResourceServer>> transactionDataOps = new HashMap<>();
    private Integer transactionId = 0;


    public synchronized int start()
    {
        transactionId++;
        activeTransactions.add(transactionId);
        transactionDataOps.put(transactionId, new HashSet<Middleware.ResourceServer>());
        return transactionId;
    }


    public synchronized boolean commit(int xid)
    {
        Trace.info("TM::commit(" + xid + ") called");
        transactionDataOps.remove(xid);
        activeTransactions.remove(xid);
        return true;
    }

    public synchronized void abort(int xid)
    {
        Trace.info("TM::abort(" + xid + ") called");
        transactionDataOps.remove(xid);
        activeTransactions.remove(xid);
        abortedTransactions.add(xid);
    }

    public synchronized void checkTransaction(int xid, String caller) throws InvalidTransactionException, TransactionAbortedException
    {
        if (xid > transactionId) throw new InvalidTransactionException(xid, caller);
        if (abortedTransactions.contains(xid)) throw new TransactionAbortedException(xid, caller);
        if (!activeTransactions.contains(xid)) throw new InvalidTransactionException(xid, caller);
    }

    public synchronized HashSet<Middleware.ResourceServer> dataOperationsOfTransaction(int xid)
    {
        if (!activeTransactions.contains(xid)) return null;
        return transactionDataOps.get(xid);
    }

    public synchronized void addDataOperation(int xid, Middleware.ResourceServer rm)
    {
        if (transactionDataOps.containsKey(xid)) transactionDataOps.get(xid).add(rm);
    }
}
