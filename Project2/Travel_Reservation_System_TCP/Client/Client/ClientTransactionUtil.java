package Client;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

public class ClientTransactionUtil {

    public static class TransactionRecord {

        int transactionId;
        long startTime;
        long endTime;

        public static String columns() {
            return "TXid, StartTime, EndTime\n";
        }
        public String toString() {
            return transactionId + ", " + startTime + ", " + endTime + ", " + "\n";
        }
    }

    private HashMap<Integer, TransactionRecord> txHashMap;
    private BufferedWriter bufferWriter;

    public ClientTransactionUtil(String fileName) {
        this.txHashMap = new HashMap<>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyyMMddHHmmss");
            Date date = new Date();

            File file = new File(fileName + sdf.format(date) + ".csv");
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            this.bufferWriter = new BufferedWriter(fw);
            this.bufferWriter.write(TransactionRecord.columns());

        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private TransactionRecord readRecord(Integer txi) {
        synchronized (this.txHashMap) {
            return this.txHashMap.get(txi);
        }
    }

    private void writeRecord(Integer txi, TransactionRecord record) {
        synchronized (this.txHashMap) {
            this.txHashMap.put(txi, record);
        }
    }

    public void recordStart(Integer txid, long time) {
        TransactionRecord record = new TransactionRecord();
        record.transactionId = txid;
        record.startTime = time;
        writeRecord(txid, record);
    }

    public void recordEnd(Integer txid, long time) throws Exception {
        TransactionRecord record = readRecord(txid);
        record.endTime = time;
        writeRecord(txid, record);
    }

    public void dumpRecords() throws IOException {
        Set<Integer> keys = this.txHashMap.keySet();
        for (Integer k: keys) {
            TransactionRecord record = readRecord(k);
            try {
                bufferWriter.write(record.toString());
            } catch (Exception e) {
                System.out.println("Error writing transaction: " + record.toString() + " to csv...");
            }
        }
    }
}
