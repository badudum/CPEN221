package phemeservice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import pheme.PhemeService;
import pheme.PhemeSub;
import security.BlowfishCipher;
import timedelayqueue.BasicMessageType;
import timedelayqueue.PubSubMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PhemeServiceTester {

    private static PhemeService srv;
    private static String userName1;
    private static UUID userID1;
    private static String userName2;
    private static UUID userID2;
    private static String hashPwd1;
    private static String hashPwd2;
    private static PubSubMessage msg1;
    private static String hashPwd3;

    @BeforeAll
    public static void setup() {
        srv = new PhemeService(new File("secret/credentials.json"));

        userName1 = "Test User 1";
        userID1 = UUID.randomUUID();
        hashPwd1 = BlowfishCipher.hashPassword("Test Password 1", BlowfishCipher.gensalt(12));

        userName2 = "Test User 2";
        userID2 = UUID.randomUUID();
        hashPwd2 = BlowfishCipher.hashPassword("Test Password 2", BlowfishCipher.gensalt(12));

        hashPwd3 = BlowfishCipher.hashPassword("Test Passwd 3", BlowfishCipher.gensalt(12));
    }

    @Test
    @Order(1)
    public void testAddUser() {
        assertTrue(srv.addUser(userID1, userName1, hashPwd1));
    }

    @Test
    @Order(2)
    public void testAddDuplicateUser() {
        String userName = "Test User 1";
        String hashPwd = BlowfishCipher.hashPassword("Test Password 1", BlowfishCipher.gensalt(12));
        UUID userID = UUID.randomUUID();

        assertFalse(srv.addUser(userID, userName, hashPwd));
    }

    @Test
    @Order(3)
    public void testAddSecondUser() {
        assertTrue(srv.addUser(userID2, userName2, hashPwd2));
    }

    @Test
    @Order(4)
    public void testSendMsg() {
        msg1 = new PubSubMessage(
            userID1,
            userID2,
            "Test Msg"
        );
        srv.sendMessage(userName1, hashPwd1, msg1);
        assertEquals(PubSubMessage.NO_MSG, srv.getNext(userName2, hashPwd2));
    }

    @Test
    @Order(5)
    public void testReceiveMsg() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException ie) {
            fail();
        }
        assertEquals(msg1, srv.getNext(userName2, hashPwd2));
    }

    @Test
    @Order(6)
    public void testMsgDelivered() {
        assertTrue(srv.isDelivered(msg1.getId(), userID2));
    }

    @Test
    @Order(7)
    public void testIsUserTrue() {
        assertTrue(srv.isUser(userName2));
    }

    @Test
    @Order(8)
    public void testIsUserFalse() {
        assertFalse(srv.isUser("testuser1"));
    }

    @Test
    @Order(9)
    public void testAddSubscription1() {
        assertTrue(srv.addSubscription(userName1, hashPwd1, "UBC"));
    }

    @Test
    @Order(10)
    public void testAddSubscription2() {
        assertFalse(srv.addSubscription("userName1", hashPwd1, "UBC"));
    }

    @Test
    @Order(11)
    public void testAddSubscription3() {
        assertFalse(srv.addSubscription(userName1, "hashPwd1", "UBC"));
    }

    @Test
    @Order(12)
    public void getRecentMsgs1() {
        assertNotEquals(srv.getNext(userName1, hashPwd1), PubSubMessage.NO_MSG);
        assertTrue(srv.getAllRecent(userName1, hashPwd1).size() > 10);
    }

    @Test
    @Order(13)
    public void getRecentMsgs2() {
        PubSubMessage msg = new PubSubMessage(
            userID2,
            userID1,
            "From 2 to 1"
        );
        srv.sendMessage(userName2, hashPwd2, msg);
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException ie) {
            fail();
        }
        List<PubSubMessage> msgs = srv.getAllRecent(userName1, hashPwd1);
        assertTrue(msgs.size() < 10);
        assertTrue(msgs.size() >= 1);
        assertTrue(msgs.contains(msg));
    }

    @Test
    @Order(14)
    public void getRecentMsgs3() {
        List<PubSubMessage> msgs = srv.getAllRecent(userName1, hashPwd1);
        assertEquals(0, msgs.size());
        assertEquals(PubSubMessage.NO_MSG, srv.getNext(userName1, hashPwd1));
    }

    @Test
    @Order(15)
    public void testSubscribeWithKeywords() {
        srv.addSubscription(userName2, hashPwd2, "HariBalakrish20", "#T20WorldCup");
        List<PubSubMessage> msgs = srv.getAllRecent(userName2, hashPwd2);
        assertEquals(2, msgs.size());
    }

    @Test
    @Order(16)
    public void testMultipleSubscriptionsWithKeywords() {
        String userName3 = "Test User 3";
        UUID userID3 = UUID.randomUUID();
        srv.addUser(userID3, userName3, hashPwd3);
        srv.addSubscription(userName3, hashPwd3, "ubcengineering", "ceremonies");
        srv.addSubscription(userName3, hashPwd3, "ubcappscience", "ceremonies");
        List<PubSubMessage> msgs = srv.getAllRecent(userName3, hashPwd3);
        assertEquals(4, msgs.size());
    }

    @Test
    @Order(17)
    public void testRemoveUserTrue(){
        assertTrue(srv.removeUser("Test User 3", hashPwd3));
    }
    @Test
    @Order(18)
    public void testRemoveUserFalse(){
        assertFalse(srv.removeUser("Test User 3", hashPwd3));
    }
    @Test
    @Order(19)
    public void testRemoveSubscription(){
        String userName4 = "Test User 4";
        String hashPwd4 = BlowfishCipher.hashPassword("Test Passwd 4", BlowfishCipher.gensalt(12));
        UUID userID3 = UUID.randomUUID();
        srv.addUser(userID3, userName4, hashPwd4);
        srv.addSubscription(userName4, hashPwd4, "ubcengineering", "ceremonies");
        srv.addSubscription(userName4, hashPwd4, "ubcappscience", "ceremonies");
        assertFalse(srv.cancelSubscription(userName4, hashPwd4, "UBC"));
        srv.cancelSubscription(userName4, hashPwd4, "ubcengineering");
        List<PubSubMessage> msgs = srv.getAllRecent(userName4, hashPwd4);
        assertEquals(2, msgs.size());
    }
    @Test
    @Order(20)
    public void testRemoveSubscriptionWithPattern(){
        String userName5 = "Test User 5";
        String hashPwd5 = BlowfishCipher.hashPassword("Test Passwd 5", BlowfishCipher.gensalt(12));
        UUID userID3 = UUID.randomUUID();
        srv.addUser(userID3, userName5, hashPwd5);
        srv.addSubscription(userName5, hashPwd5, "ubcengineering", "ceremonies");
        srv.addSubscription(userName5, hashPwd5, "ubcappscience", "ceremonies");
        srv.cancelSubscription(userName5, hashPwd5, "ubcappscience", "ceremonies");
        List<PubSubMessage> msgs = srv.getAllRecent(userName5, hashPwd5);
        assertEquals(2, msgs.size());
    }

    @Test
    @Order(21)
    public void testIsDelivered(){
        List<UUID> test = new ArrayList<>();
        String userName3 = "Test User 3";
        UUID userID3 = UUID.randomUUID();
        srv.addUser(userID3, userName3, hashPwd3);
        srv.addUser(userID2, userName2, hashPwd2);
        test.add(userID2);
        test.add(userID3);
        PubSubMessage msg5 = new PubSubMessage(
                userID1,
                test,
                "Test Msg"
        );
        srv.sendMessage(userName1, hashPwd1, msg5);
        assertFalse(srv.isDelivered(msg5.getId(), test).contains(false));
    }

    @Test
    @Order(22)
    public void testFailedLogin(){
        String username4 = "TestUser4";
        String salt = BlowfishCipher.gensalt();
        String password  = BlowfishCipher.hashPassword("password123", salt);
        assertFalse(srv.removeUser(username4, password));
        assertFalse(srv.addSubscription(username4, password, "Dream", "George"));
        assertFalse(srv.cancelSubscription(username4, password, "Dream"));
        assertFalse(srv.cancelSubscription(username4, password, "Dream", "George"));
        assertFalse(srv.sendMessage(username4, password, new PubSubMessage(UUID.randomUUID(), UUID.randomUUID(), "test")));
        PubSubMessage msgTest = new PubSubMessage(userID2, UUID.randomUUID(), "test");
        assertFalse(srv.sendMessage(userName2, hashPwd2, msgTest));
        assertFalse(srv.isDelivered(msgTest.getId(), UUID.randomUUID()));
        assertEquals(srv.getNext(username4, password), PubSubMessage.NO_MSG);
        assertEquals(srv.getAllRecent(username4, password), new ArrayList<>());
    }

    @Test
    @Order(23)
    public void testOverrideSub() {
        srv.getAllRecent(userName2, hashPwd2);
        assertTrue(srv.addSubscription(userName2, hashPwd2, "Dream", "George"));
        assertTrue(srv.addSubscription(userName2, hashPwd2, "Dream", "Tommy"));
        assertTrue(srv.addSubscription(userName2, hashPwd2, "Dream"));
        assertTrue(srv.cancelSubscription(userName2, hashPwd2, "Dream"));
        srv.addSubscription(userName2, hashPwd2, "tysomillo", "before people stop");
        srv.addSubscription(userName2, hashPwd2, "tysomillo", "1234567890000");
        srv.addSubscription(userName2, hashPwd2, "mooncityrah");
        assertEquals(BasicMessageType.TWEET, srv.getNext(userName2, hashPwd2).getType());
        assertEquals(BasicMessageType.TWEET, srv.getNext(userName2, hashPwd2).getType());
        assertEquals(BasicMessageType.TWEET, srv.getNext(userName2, hashPwd2).getType());
        assertEquals(BasicMessageType.TWEET, srv.getNext(userName2, hashPwd2).getType());
        assertEquals(BasicMessageType.TWEET, srv.getNext(userName2, hashPwd2).getType());
    }

    @Test
    @Order(24)
    public void testPhemeSubEquality() {
        PhemeSub sub1 = new PhemeSub("Dream");
        PhemeSub sub2 = new PhemeSub("Dream");
        PhemeSub sub3 = new PhemeSub("tysomillo");
        PhemeSub sub4 = new PhemeSub("tysomillo", "the");
        PhemeSub sub5 = new PhemeSub("Dream", "George");
        PhemeSub sub6 = new PhemeSub("Dream", "Tommy");
        assertEquals(sub1, sub2);
        assertEquals(sub5, sub5);
        assertNotEquals(sub1, sub3);
        assertNotEquals(sub1, sub5);
        assertNotEquals(sub5, sub6);
        assertNotEquals(sub3, sub5);
        assertNotEquals(sub4, sub5);
        assertNotEquals(sub1, 10);
    }
/*
    @Test
    @Order(25)
    public void testSave(){
        srv.addUser(userID1,userName1, hashPwd1);
        srv.addUser(userID2,userName2, hashPwd2);
        try {
            srv.saveState("C:\\Users\\David Lee\\IdeaProjects\\mp3-Galador\\secret\\info.json");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

*/

}
