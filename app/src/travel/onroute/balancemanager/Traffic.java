package travel.onroute.balancemanager;

import android.util.Log;

/**
 * Created by e.mazurov on 08.06.2014.
 */
public class Traffic {
    long startReceivedBytes;
    long startTransferBytes;
    long receivedBytes;
    long transferBytes;

    public Traffic(long r, long t) {
        startReceivedBytes = r;
        startTransferBytes = t;
    }

    public void addTraffic(long r, long t) {
        Log.e("Traffic", "startR = " + startReceivedBytes + " r = " + r + " startT = " + startTransferBytes + " t = " + t);
        receivedBytes = (startReceivedBytes > r ? startReceivedBytes : 0) + r;
        transferBytes = (startTransferBytes > t ? startTransferBytes : 0) + t;
        Log.e("Traffic", "R = " + receivedBytes + " T = " + transferBytes);
    }

    public void addReceivedTraffic(long r) {
        addTraffic(r, 0);
    }

    public void addTransferTraffic(long t) {
        addTraffic(0, t);
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }

    public long getTransferBytes() {
        return transferBytes;
    }

    public long getAll() {
        return receivedBytes + transferBytes;
    }

    @Override
    public String toString() {
        return "startR = " + startReceivedBytes + "startT = " + startTransferBytes + "received = " + receivedBytes + ", transfer = " + transferBytes + ", all = " + getAll();
    }
}
