package timedelayqueue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class Task1 {

    private static final int DELAY        = 40; // delay of 40 milliseconds
    private static final int MSG_LIFETIME = 80;
    private static final Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeNulls();
        gson = gsonBuilder.create();
    }

    @Test
    public void testBasicAddRetrieve_NoDelay() {
        TimeDelayQueue tdq = new TimeDelayQueue(DELAY);
        UUID sndID     = UUID.randomUUID();
        UUID rcvID     = UUID.randomUUID();
        String msgText = gson.toJson("test");
        PubSubMessage msg1 = new PubSubMessage(sndID, rcvID, msgText);
        tdq.add(msg1);
        PubSubMessage msg2 = tdq.getNext();
        assertEquals(PubSubMessage.NO_MSG, msg2);
    }

    @Test
    public void testBasicAddRetrieve_Delay() {
        TimeDelayQueue tdq = new TimeDelayQueue(DELAY);
        UUID sndID     = UUID.randomUUID();
        UUID rcvID     = UUID.randomUUID();
        String msgText = gson.toJson("test");
        PubSubMessage msg1 = new PubSubMessage(sndID, rcvID, msgText);
        tdq.add(msg1);
        try {
            Thread.sleep(2 * DELAY);
        }
        catch (InterruptedException ie) {
            // nothing to do but ...
            fail();
        }
        PubSubMessage msg2 = tdq.getNext();
        assertEquals(msg1, msg2);
    }

    @Test
    public void testTransientMsg_InTime() {
        TimeDelayQueue tdq = new TimeDelayQueue(DELAY);

        UUID sndID     = UUID.randomUUID();
        UUID rcvID     = UUID.randomUUID();
        String msgText = gson.toJson("test");
        TransientPubSubMessage msg1 = new TransientPubSubMessage(sndID, rcvID, msgText, MSG_LIFETIME);
        PubSubMessage          msg2 = new PubSubMessage(sndID, rcvID, msgText);
        tdq.add(msg1);
        tdq.add(msg2);
        try {
            Thread.sleep(DELAY + 1);
        }
        catch (InterruptedException ie) {
            fail();
        }
        assertEquals(msg1, tdq.getNext());
        assertEquals(msg2, tdq.getNext());
    }

    @Test
    public void testTransientMsg_Late() {
        TimeDelayQueue tdq = new TimeDelayQueue(DELAY);

        UUID sndID     = UUID.randomUUID();
        UUID rcvID     = UUID.randomUUID();
        String msgText = gson.toJson("test");
        TransientPubSubMessage msg1 = new TransientPubSubMessage(sndID, rcvID, msgText, MSG_LIFETIME);
        PubSubMessage          msg2 = new PubSubMessage(sndID, rcvID, msgText);
        tdq.add(msg1);
        tdq.add(msg2);
        try {
            Thread.sleep(MSG_LIFETIME + 1);
        }
        catch (InterruptedException ie) {
            fail();
        }
        assertEquals(msg2, tdq.getNext()); // msg1 would have expired
    }

    @Test
    public void testMsgCount() {
        TimeDelayQueue tdq = new TimeDelayQueue(DELAY);

        final int NUM_MSGS = 10;
        for (int i = 0; i < NUM_MSGS; i++) {
            UUID sndID        = UUID.randomUUID();
            UUID rcvID        = UUID.randomUUID();
            String msgText    = gson.toJson("test");
            PubSubMessage msg = new PubSubMessage(sndID, rcvID, msgText);
            tdq.add(msg);
        }

        try {
            Thread.sleep(2 * DELAY);
        }
        catch (InterruptedException ie) {
            fail();
        }

        for (int i = 0; i < NUM_MSGS; i++) {
            tdq.getNext();
        }

        assertEquals(NUM_MSGS, tdq.getTotalMsgCount());
    }

    //Group-Generated Tests
    @Test
    public void testAdd() {
        TimeDelayQueue tdq = new TimeDelayQueue(DELAY);
        String msgText = gson.toJson("test");

        PubSubMessage msg1 = new PubSubMessage(UUID.randomUUID(), UUID.randomUUID(), msgText);
        PubSubMessage msg2 = new PubSubMessage(UUID.randomUUID(), UUID.randomUUID(), msgText);
        PubSubMessage msg3 = new PubSubMessage(UUID.randomUUID(), UUID.randomUUID(), msgText);
        List<PubSubMessage> msgs = new ArrayList<>();

        msgs.add(msg1);
        msgs.add(msg2);
        msgs.add(msg3);

        tdq.add(msgs);
        assertFalse(tdq.add(msg1));

        try {
            Thread.sleep( DELAY - 10);
        }
        catch (InterruptedException ie) {
            fail();
        }

        assertEquals(3, tdq.getTotalMsgCount());
        assertTrue(tdq.containsMessage(msg1));
    }

    @Test
    public void peakLoadSameTime() {
        TimeDelayQueue tdq = new TimeDelayQueue(DELAY);
        assertEquals(tdq.getPeakLoad(DELAY), 0);
        String msgText = gson.toJson("test");

        PubSubMessage msg1 = new PubSubMessage(UUID.randomUUID(), UUID.randomUUID(), msgText);
        PubSubMessage msg2 = new PubSubMessage(UUID.randomUUID(), UUID.randomUUID(), msgText);
        PubSubMessage msg3 = new PubSubMessage(UUID.randomUUID(), UUID.randomUUID(), msgText);
        List<PubSubMessage> msgs = new ArrayList<>();

        msgs.add(msg1);
        msgs.add(msg2);
        msgs.add(msg3);

        tdq.add(msgs);

        try {
            Thread.sleep(DELAY);
        }
        catch (InterruptedException ie) {
            fail();
        }

        for (int i = 0; i < 3; i++) {
            tdq.getNext();
        }

        assertEquals(tdq.getNext(), PubSubMessage.NO_MSG);
        assertEquals(4, tdq.getPeakLoad(DELAY));

        try {
            Thread.sleep(DELAY);
        }
        catch (InterruptedException ie) {
            fail();
        }

        tdq.add(msgs);
        tdq.add(msgs);
        tdq.add(msgs);
        tdq.add(msgs);
        tdq.add(msgs);

        assertEquals(5, tdq.getPeakLoad(DELAY));
    }

    @Test
    public void testTransient() {
        TimeDelayQueue tdq = new TimeDelayQueue(DELAY);
        String msgText = gson.toJson("test");

        List<UUID> recs = new ArrayList<>();
        recs.add(UUID.randomUUID());
        recs.add(UUID.randomUUID());

        PubSubMessage msg1 = new TransientPubSubMessage(UUID.randomUUID(), new Timestamp(System.currentTimeMillis()), UUID.randomUUID(), UUID.randomUUID(), "Test", BasicMessageType.TWEET, 100);
        PubSubMessage msg2 = new TransientPubSubMessage(UUID.randomUUID(), new Timestamp(System.currentTimeMillis()), UUID.randomUUID(), recs, "Test", BasicMessageType.TWEET, 100);
        PubSubMessage msg3 = new TransientPubSubMessage(UUID.randomUUID(), recs, msgText, 100);
        assertNotEquals(msg1, 9);
        assertNotEquals(msg1, msg2);
        List<PubSubMessage> msgs = new ArrayList<>();

        msgs.add(msg1);
        msgs.add(msg2);
        msgs.add(msg3);

        tdq.add(msgs);

        try {
            Thread.sleep( 200);
        }
        catch (InterruptedException ie) {
            fail();
        }
        assertEquals(BasicMessageType.SIMPLEMSG.getDescription(), "A simple message from a sender to one or more recipients");
        assertEquals(BasicMessageType.TWEET.getDescription(), "A tweet from Twitter with all the metadata");
        assertEquals(PubSubMessage.NO_MSG, tdq.getNext());
    }

}
