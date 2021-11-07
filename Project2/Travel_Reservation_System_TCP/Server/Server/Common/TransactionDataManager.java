package Server.Common;

import java.util.HashMap;
import java.util.Map;

public class TransactionDataManager {

    HashMap<Integer, RMHashMap> hashMap;

    public TransactionDataManager() {
        this.hashMap= new HashMap<Integer, RMHashMap>();
    }

    public HashMap<Integer, RMHashMap> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<Integer, RMHashMap> hashMap) {
        this.hashMap = hashMap;
    }

    public synchronized void addUndoInfo(int xid, String dataName, RMItem data)
    {
        Trace.info("TDM::addUndoInfo(" + xid +"," + dataName + " ,) called");
        RMHashMap transactionData = this.hashMap.get(xid);
        if (transactionData == null) {
            transactionData = new RMHashMap();
            this.hashMap.put(xid, transactionData);
        }
        if (!transactionData.containsKey(dataName)) transactionData.put(dataName, data);
    }
    public synchronized RMHashMap undoTransactionDataList(int xid)
    {
        Trace.info("TDM::undoTransactionDataList(" + xid + ") called");
        RMHashMap transactionData = this.hashMap.get(xid);
        cleanTransaction(xid);
        return transactionData;
    }
    public synchronized void cleanTransaction(int xid)
    {
        Trace.info("TDM::cleanTransaction(" + xid + ") called");
        this.hashMap.remove(xid);
    }

}
