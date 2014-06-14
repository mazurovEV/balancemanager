package travel.onroute.balancemanager;

/**
 * Created by e.mazurov on 08.06.2014.
 */
public class Traffic {
    private long offsetReceived;
    private long offsetTransfered;

    private long dbReceived;
    private long dbTransfered;

    private long receivedBytes;
    private long transferBytes;

    public Traffic(long offsetR, long offsetT, long dbR, long dbT) {
        offsetReceived = offsetR;
        offsetTransfered = offsetT;

        dbReceived = dbR;
        dbTransfered = dbT;
    }

    public void addTraffic(long r, long t) {
       //TODO: Если перезапустили BM, и траффик шел в тот момент когда БМ не работал, то это не верная формула
       receivedBytes = dbReceived + r - offsetReceived;
       transferBytes = dbTransfered + t - offsetTransfered;
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
}
