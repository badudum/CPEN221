package timedelayqueue;


import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TimeDelayQueue {
    final private int timeDelay;
    private int historySize;
    private LinkedList<PubSubMessage> messageQueue;
    private final List<Timestamp> opHistory;

    // Representation Invariant:
    // - this.timeDelay >= 0
    // - this.historySize >= 0
    // - this.messageQueue and this.opHistory are not null
    // Abstraction Fcn
    // represents a queue where objects are sorted by time and objects are only accessible after they have
    // existed for a certain length of time

    /**
     * Create a new TimeDelayQueue
     *
     * @param delay the delay, in milliseconds, that the queue can tolerate, >= 0
     * A negative given timeDelay will result in a timeDelay of 0
     */
    public TimeDelayQueue(int delay) {
        this.timeDelay = Math.max(delay, 0);
        this.historySize = 0;
        this.messageQueue = new LinkedList<>();
        this.opHistory = new ArrayList<>();
    }

    /**
     * Add a message to the TimeDelayQueue;
     * if a message with the same id already exists, returns false
     *
     * @param msg, the PubSubMessage to be added
     * @return a boolean that represents the success of the operation
     */
    synchronized public boolean add(PubSubMessage msg) {
        opHistory.add(new Timestamp(System.currentTimeMillis()));
        if (!messageQueue.contains(msg)) {
            messageQueue.add(msg);
            messageQueue.sort(new MessageComparator());
            historySize++;
            return true;
        }
        return false;
    }

    /**
     * Add a list of messages to the TimeDelayQueue;
     * returns false if any message fails to be added and true otherwise
     *
     * @param msgs, the list of PubSubMessages to be added
     * @return false if any message fails to be added and true otherwise
     */
    synchronized public boolean add(List<PubSubMessage> msgs) {
        opHistory.add(new Timestamp(System.currentTimeMillis()));
        int failed = 0;
        for (PubSubMessage msg: msgs) {
            if (!messageQueue.contains(msg)) {
                messageQueue.add(msg);
                historySize++;
            } else {
                failed++;
            }
        }
        messageQueue.sort(new MessageComparator());
        return (failed == 0);
    }

    /**
     * Checks the messages stored in the queue for expired transient messages and deletes them.
     */
    synchronized private void prune() {
         messageQueue = new LinkedList<>(messageQueue.stream().filter(msg -> {
            if (msg.isTransient()) {
                TransientPubSubMessage tMsg = (TransientPubSubMessage) msg;
                return (tMsg.getTimestamp().getTime() + tMsg.getLifetime()) >= System.currentTimeMillis();
            }
            return true;
        }).toList());
    }

    /**
     * Checks if the TimeDelayQueue contains a given message
     * @param msg the message to be checked for
     */
    synchronized public boolean containsMessage(PubSubMessage msg) {return messageQueue.contains(msg);
    }

    /**
     * Get the count of the total number of messages processed
     * by this TimeDelayQueue.
     *
     * @return the total number of messages processed
     */
    public long getTotalMsgCount() {
        return historySize;
    }

    /**
     * Returns the next ready message.
     * if there is no suitable message returns PubSubMessage.NO_MSG.
     *
     * @return The next ready message or PubSubMessage.NO_MSG if no messages are ready
     */
    synchronized public PubSubMessage getNext() {
        opHistory.add(new Timestamp(System.currentTimeMillis()));
        prune();
        if (messageQueue.size() == 0) {
            return PubSubMessage.NO_MSG;
        }
        if (System.currentTimeMillis() - messageQueue.getFirst().getTimestamp().getTime() < timeDelay) {
            return PubSubMessage.NO_MSG;
        } else {
            PubSubMessage next = messageQueue.getFirst();
            messageQueue.removeFirst();
            return next;
        }
    }

    /**
     * Returns the maximum number of operations performed on this TimeDelayQueue over
     * any window of length timeWindow given in milliseconds.
     *
     * Operations of interest are add and getNext.
     *
     * @param timeWindow the length of the timeWindow
     * @return the maximum number of operations performed during
     *         any window of length timeWindow in milliseconds
     */
    synchronized public int getPeakLoad(int timeWindow) {
        int peak = 0;
        int curr = 1;
        for (int i = 0; i < opHistory.size(); i++) {
            //end of current time window
            long end = opHistory.get(i).getTime() + timeWindow;

            for (int j = i + curr; j < opHistory.size(); j++) {
                if (opHistory.get(j).getTime() <= end) {
                    curr++;
                } else {
                    peak = Math.max(peak, curr);
                    curr = Math.max(curr - 1, 1);
                    break;
                }

                //If the for loop terminates at the end of opHistory,
                // peak must have been found, return peak
                if (j + 1 == opHistory.size()) {
                    peak = Math.max(peak, curr);
                    return peak;
                }
            }
        }
        return peak;
    }

    /**
     * Comparator to sort PubSubMessages by timestamp
     */
    private static class MessageComparator implements Comparator<PubSubMessage> {
        public int compare(PubSubMessage psm1, PubSubMessage psm2) {
            return psm1.getTimestamp().compareTo(psm2.getTimestamp());
        }
    }

}
