package Server.Common;

import java.util.HashMap;
import java.util.Map;

public class TransactionDataManager {

    HashMap<Integer, RMHashMap> hashMap;

    public TransactionDataManager() {
        this.hashMap = new HashMap<Integer, RMHashMap>();
    }


//    public RMHashMap readTransactionData(int xid)
//    {
//        synchronized (hashMap) {
//            return hashMap.get(xid);
//        }
//    }
//    public void writeTransactionData(int xid, RMHashMap data)
//    {
//        synchronized (hashMap) {
//            hashMap.put(xid, data);
//        }
//    }
//
//    public void removeTransactionData(int xid)
//    {
//        synchronized (hashMap) {
//            hashMap.remove(xid);
//        }
//    }

    public HashMap<Integer, RMHashMap> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<Integer, RMHashMap> hashMap) {
        this.hashMap = hashMap;
    }

    public synchronized void addUndoInfo(int xid, String dataName, RMItem data)
    {
        Trace.info("TDM::addUndoInfo(" + xid +"," + dataName + "," + data + ") called");
        RMHashMap transactionData = this.hashMap.get(xid);
        if (transactionData == null) {
            transactionData = new RMHashMap();
            this.hashMap.put(xid, transactionData);
        }
        transactionData.put(dataName, data);
    }
    public synchronized RMHashMap undoTransactionDataList(int xid)
    {
        RMHashMap transactionData = this.hashMap.get(xid);
        cleanTransaction(xid);
        return transactionData;
    }
    public synchronized void cleanTransaction(int xid)
    {
        this.hashMap.remove(xid);
    }

}
