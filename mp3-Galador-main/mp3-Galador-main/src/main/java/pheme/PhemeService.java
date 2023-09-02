package pheme;

import security.BlowfishCipher;
import timedelayqueue.PubSubMessage;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.DeflaterOutputStream;

public class PhemeService {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    Lock writeLock = lock.writeLock();
    Lock readLock = lock.readLock();
    public static final int DELAY = 1000; // 1 second or 1000 milliseconds
    private final File twitterCredentialsFile;
    private final Map<String, PhemeUser> users;
    private final Map<String, SaltedPassword> passwords;
    private final Map<UUID, String> uuidMap;

    //rep invariant
    // no fields are null
    // If a String exists in either users, passwords, or uuidMap it must exist in all 3
    // twitterCredentialsFile is a set of valid API keys
    //Abstraction fcn
    // Represents a service where users can be added and deleted and each user has a set of subscriptions
    // either patterned or unpatterned to Twitter users.

    public PhemeService(File twitterCredentialsFile) {
        this.twitterCredentialsFile = twitterCredentialsFile;
        this.users = new HashMap<>();
        this.passwords = new HashMap<>();
        this.uuidMap = new HashMap<>();
    }

    /**
     * Saves the state of the PhemeService to a file
     * @param configDirName The config file directory, is not null
     *
     */
    synchronized public void saveState(String configDirName) throws FileNotFoundException {
        File dir = new File(configDirName);
        try{
            Properties property = new Properties();
            FileOutputStream configFile =  new FileOutputStream(dir);
            DeflaterOutputStream compressor = new DeflaterOutputStream(configFile);
            for(Map.Entry<String, PhemeUser> i: this.users.entrySet()){
                property.put(i.getValue(), i.getValue().getHashPassword());
                property.put("Subs : ", i.getValue().getAllSubs().keySet().stream().toList());
            }
            property.store(compressor, "User Information");
            configFile.close();
            compressor.close();
            property.clear();
        }catch (FileNotFoundException e) {
            throw new FileNotFoundException("The file does not exist");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add a user to the PhemeService. Returns false if the user already exists.
     *
     * @param userID the UUID of the user to be added
     * @param userName the userName string of the user to be added
     * @param password the password string of the user to be added
     * @return true if the user was successfully added and false otherwise.
     */
    public boolean addUser(UUID userID, String userName, String password) {
        boolean success = false;
        try {
            writeLock.lock();
            if (!users.containsKey(userName)) {
                users.put(userName, new PhemeUser(userID, userName, password, DELAY, twitterCredentialsFile));
                String salt = BlowfishCipher.gensalt();
                writeLock.unlock();
                String hashedPassword = BlowfishCipher.hashPassword(password, salt);
                writeLock.lock();
                passwords.put(userName, new SaltedPassword(salt, hashedPassword));
                uuidMap.put(userID, userName);
                success = true;
            }
        } finally {
            writeLock.unlock();
        }
        return success;
    }

    /**
     * Remove a user to the PhemeService.
     * To remove a user, the userName and password must be correct and the user must already exist.
     *
     * @param userName the userName string of the user to be removed
     * @param password the hashed password string of the user to be removed
     * @return true if the user was successfully removed and false otherwise.
     */
    public boolean removeUser(String userName, String password) {
        if (!this.login(userName, password)) return false;
        try {
            writeLock.lock();
            uuidMap.remove(users.get(userName).getUserID());
            users.remove(userName);
            passwords.remove(userName);
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    /**
     * Checks the validity of a given hashed Password for a username.
     *
     * @param userName the userName string of the user
     * @param password the password string of the user
     * @return true is the password matches for the user and false otherwise
     */
    private boolean login(String userName, String password) {
        boolean success = false;
        try {
            readLock.lock();
            if (passwords.containsKey(userName)) {
                String salt = passwords.get(userName).salt();
                readLock.unlock();
                String hashedPassword = BlowfishCipher.hashPassword(password, salt);
                readLock.lock();
                success = hashedPassword.equals(passwords.get(userName).hashedPassword());
            }
        } finally {
            readLock.unlock();
        }
        return success;
    }

    /**
     * Adds an unpatterned subscription for a user.
     * Returns false if the user credentials are wrong, the user does not exist,
     * or the subscription could not be added.
     *
     * @param userName the username of the user
     * @param password the password of the user
     * @param twitterUserName the handle to subscribed to
     * @return true or false as described above
     */
    public boolean addSubscription(String userName, String password,
                                   String twitterUserName) {
        if (!this.login(userName, password)) return false;
        boolean success;
        try {
            readLock.lock();
            success = users.get(userName).addSubscription(twitterUserName);
        } finally {
            readLock.unlock();
        }
        return success;
    }

    /**
     * Adds a patterned subscription for a user.
     * Returns false if the user credentials are wrong, the user does not exist,
     * or the subscription could not be added.
     *
     * @param userName the username of the user
     * @param password the password of the user
     * @param twitterUserName the handle to subscribed to
     * @param pattern the pattern of the subscription
     * @return true or false as described above
     */
    public boolean addSubscription(String userName, String password,
                                   String twitterUserName, String pattern) {
        if (!this.login(userName, password)) return false;
        boolean success;
        try {
            readLock.lock();
            success = users.get(userName).addSubscription(twitterUserName, pattern);
        } finally {
            readLock.unlock();
        }
        return success;
    }

    /**
     * Removes an unpatterned subscription to a Twitter handle for the user.
     * Returns false if the user credentials are wrong, the user does not exist,
     * or the subscription does not exist.
     *
     * @param userName the username of the user
     * @param password the password of the user
     * @param twitterUserName the handle to unsubscribed from
     * @return true or false as described above
     */
    public boolean cancelSubscription(String userName, String password,
                                      String twitterUserName) {
        if (!this.login(userName, password)) return false;
        boolean success;
        try {
            readLock.lock();
            success = users.get(userName).removeSubscription(twitterUserName);
        } finally {
            readLock.unlock();
        }
        return success;
    }

    /**
     * Removes a patterned subscription to a Twitter handle for the user.
     * Returns false if the user credentials are wrong, the user does not exist,
     * or the subscription does not exist or is overridden.
     *
     * @param userName the username of the user
     * @param password the password of the user
     * @param twitterUserName the handle to unsubscribed from
     * @param pattern the pattern of to unsubscribe from
     * @return true or false as described above
     */
    public boolean cancelSubscription(String userName, String password,
                                      String twitterUserName, String pattern) {
        if (!this.login(userName, password)) return false;
        boolean success;
        try {
            readLock.lock();
            success = users.get(userName).removeSubscription(twitterUserName, pattern);
        } finally {
            readLock.unlock();
        }
        return success;
    }

    /**
     * Sends a message to a user of the PhemeService.
     * Returns false if the user credentials are wrong, the user does not exist,
     * or the message failed to be added for any of the users.
     * @param userName the username of the sender
     * @param password the password of the sender
     * @param msg the message to be sent
     * @return true or false as described above
     */
    public boolean sendMessage(String userName, String password,
                               PubSubMessage msg) {
        if (!this.login(userName, password)) return false;
        int failed = 0;
        try {
            readLock.lock();
            for (UUID recipient: msg.getReceiver()) {
                boolean failCheck = false;
                if (uuidMap.containsKey(recipient)) {
                    failCheck = users.get(uuidMap.get(recipient)).addMessage(msg);
                }
                if (!failCheck) failed++;
            }
        } finally {
            readLock.unlock();
        }
        return (failed == 0);
    }

    /**
     * Checks if the message with a given ID has been delivered to a list of users.
     *
     * @param msgID the ID if the message to be checked for
     * @param userList the list of userIds to check
     * @return A list of booleans corresponding to the delivery status of each user
     */
    public List<Boolean> isDelivered(UUID msgID, List<UUID> userList) {
        List<Boolean> returnList = new ArrayList<>();
        for (UUID uuid: userList) {
            returnList.add(this.isDelivered(msgID, uuid));
        }
        return returnList;
    }

    /**
     * Checks if the message with a given ID has been delivered to a user.
     *
     * @param msgID the ID if the message to be checked for
     * @param user the user to check
     * @return A boolean corresponding to the delivery status of the message
     */
    public boolean isDelivered(UUID msgID, UUID user) {
        boolean success = false;
        try {
            readLock.lock();
            if (uuidMap.containsKey(user)) {
                success = !users.get(uuidMap.get(user)).containsMessage(new PubSubMessage(msgID, user, null));
            }
        } finally {
            readLock.unlock();
        }
        return success;
    }

    /**
     * Check if a user exists within the PhemeService
     *
     * @param userName the userName to be checked for
     * @return true if the user exists and false otherwise
     */
    public boolean isUser(String userName) {
        boolean success;
        try {
            readLock.lock();
            success = users.containsKey(userName);
        } finally {
            readLock.unlock();
        }
        return success;
    }

    /**
     * Get the next message for a user of the PhemeService.
     * Returns PubSubMessage.NO_MSG if the user does not have a ready message or the user does not exist
     *
     * @param userName the username of the user
     * @param password the user's hashed password
     * @return The user's next message or PubSubMessage.NO_MSG if there is no ready message
     */
    public PubSubMessage getNext(String userName, String password) {
        if (!this.login(userName, password)) return PubSubMessage.NO_MSG;
        PubSubMessage msg;
        try {
            readLock.lock();
            msg = users.get(userName).getNext();
        } finally {
            readLock.unlock();
        }
        return msg;
    }

    /**
     * Get all the ready messages of a user
     * Returns an empty list if the user does not have a ready message or the user does not exist
     *
     * @param userName the username of the user
     * @param password the user's hashed password
     * @return The user's next message or an empty list if there are no ready messages
     */
    public List<PubSubMessage> getAllRecent(String userName, String password) {
        if (!this.login(userName, password)) return new ArrayList<>();
        List<PubSubMessage> msgs;
        try {
            readLock.lock();
            msgs = users.get(userName).getAllReady();
        } finally {
            readLock.unlock();
        }
        return msgs;
    }
}