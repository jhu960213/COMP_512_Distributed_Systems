package Server.Common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class TransactionRecordUtil {
    public class TransactionRecord {
        int transactionId;
        int state;
        long startTime;
        long endTime;
        long readTime;
        long writeTime;
        public static String columns() { //transactionId,state,startTime,endTime,executeTime,readTime,writeTime,databaseTime
            return "Xid, State, StartTime, EndTime, ExecuteTime, ReadTime, WriteTime, DBTime\n";
        }
        public String toString() { //transactionId,state,startTime,endTime,executeTime,readTime,writeTime,databaseTime
            return transactionId + ", " + state + ", " + startTime + ", " + endTime + ", " + (endTime - startTime) + ", " + readTime + ", " + writeTime + ", " + (readTime + writeTime) + "\n";
        }
    }

    private HashMap<Integer, TransactionRecord> hashMap;
    private BufferedWriter bufferWriter;

    public TransactionRecordUtil (String rmName) {
        hashMap = new HashMap<>();
        try{
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyyMMddHHmmss");
            Date date = new Date();

            File file =new File(rmName + sdf.format(date) + ".csv");
            if(!file.exists()){
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            bufferWriter = new BufferedWriter(fw);
            bufferWriter.write(TransactionRecord.columns());

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public TransactionRecord readRecord(int transactionId)
    {
        synchronized (hashMap) {
            return hashMap.get(transactionId);
        }
    }

    public void writeRecord(int transactionId, TransactionRecord record)
    {
        synchronized (hashMap) {
            hashMap.put(transactionId, record);
        }
    }

    public void start(int transactionId)
    {
        TransactionRecord record = readRecord(transactionId);
        if (record == null) {
            record = new TransactionRecord();
            record.transactionId = transactionId;
            record.startTime = System.currentTimeMillis();
            writeRecord(transactionId, record);
        }
    }

    public void commit(int transactionId)
    {
        TransactionRecord record = readRecord(transactionId);
        if (record != null) {
            record.state = 1;
            record.endTime = System.currentTimeMillis();
            writeRecord(transactionId, record);
            try {
                bufferWriter.write(record.toString());
            } catch (IOException e) {
            }
        }
    }

    public void abort(int transactionId)
    {
        TransactionRecord record = readRecord(transactionId);
        if (record != null) {
            record.state = 2;
            record.endTime = System.currentTimeMillis();
            writeRecord(transactionId, record);
            try {
                bufferWriter.write(record.toString());
            } catch (IOException e) {
            }
        }
    }

    public void addReadTime(int transactionId, long time)
    {
        TransactionRecord record = readRecord(transactionId);
        if (record != null) {
            record.readTime += time;
            writeRecord(transactionId, record);
        }
    }

    public void addWriteTime(int transactionId, long time)
    {
        TransactionRecord record = readRecord(transactionId);
        if (record != null) {
            record.writeTime += time;
            writeRecord(transactionId, record);
        }
    }

    public void close()
    {
        try {
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
