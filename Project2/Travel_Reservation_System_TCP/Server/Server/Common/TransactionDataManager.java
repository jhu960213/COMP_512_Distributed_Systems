package Server.Common;

import java.util.HashMap;
import java.util.Map;

public class TransactionDataManager {
    HashMap<Integer, RMHashMap> hashMap = new HashMap<Integer, RMHashMap>();
    public synchronized void addUndoInfo(int xid, String dataName, RMItem data)
    {
        Trace.info("TDM::addUndoInfo(" + xid +"," + dataName + " ,) called");
        RMHashMap transactionData = hashMap.get(xid);
        if (transactionData == null) {
            transactionData = new RMHashMap();
            hashMap.put(xid, transactionData);
        }
        if (!transactionData.containsKey(dataName)) transactionData.put(dataName, data);
    }
    public synchronized RMHashMap undoTransactionDataList(int xid)
    {
        Trace.info("TDM::undoTransactionDataList(" + xid + ") called");
        RMHashMap transactionData = hashMap.get(xid);
        cleanTransaction(xid);
        return transactionData;
    }
    public synchronized void cleanTransaction(int xid)
    {
        Trace.info("TDM::cleanTransaction(" + xid + ") called");
        hashMap.remove(xid);
    }

}
