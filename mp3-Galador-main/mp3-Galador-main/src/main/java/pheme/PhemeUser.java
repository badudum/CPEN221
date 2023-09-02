package pheme;

import io.github.redouane59.twitter.dto.tweet.TweetV2;
import timedelayqueue.*;
import twitter.TwitterListener;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PhemeUser {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    Lock writeLock = lock.writeLock();
    Lock readLock = lock.readLock();
    private final UUID userID;
    private final String username;
    private final String hashPassword;
    private final Map<String, Set<PhemeSub>> subs;
    private final TimeDelayQueue queue;
    private final TwitterListener twitter;

    //rep invariant
    // no fields are null
    // No patterned pheme subs exist at the same time as any patterned subs for a username
    // userID, username, and hashPassword are all the same as they exist in the parent PhemeService
    //Abstraction fcn
    // Represents one user of a PhemeService and their data

    /**
     * Creates a PhemeUser Object
     * @param userID the UUID user id of the user
     * @param username the username String of the user
     * @param hashPassword the hashed password String of the user
     * @param delay the delay for the user's TimeDelayQueue
     * @param credentials twitterAPI credentials to allow the user to access twitter
     */
    public PhemeUser(UUID userID, String username, String hashPassword, int delay, File credentials) {
        this.userID = userID;
        this.username = username;
        this.hashPassword = hashPassword;
        this.subs = new HashMap<>();
        this.queue = new TimeDelayQueue(delay);
        this.twitter = new TwitterListener(credentials);
    }

    /**
     * Adds an unpatterned subscription to a Twitter handle for the user.
     * Overwrites any previous patterned subscription to the Twitter account.
     * Returns false if the subscription already exists or the given Twitter username is invalid.
     *
     * @param handle the handle of the account to subscribe to
     * @return true or false as described above
     */
    public boolean addSubscription(String handle) {
        //check the validity of the given handle
        //and add to user's Twitterlistener
        if (!twitter.addSubscription(handle)) return false;
        PhemeSub currentSub = new PhemeSub(handle);
        try {
            writeLock.lock();
            if (!subs.containsKey(handle)) {
                subs.put(handle, new HashSet<>());
                subs.get(handle).add(currentSub);
            } else {
                subs.get(handle).clear();
                subs.get(handle).add(currentSub);
            }
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    /**
     * Adds a patterned subscription to a Twitter handle for the user.
     * Returns false if this or an unpatterned subscription to this user already exists
     * or the given Twitter username is invalid.
     *
     * @param handle the handle of the account to subscribe to
     * @param pattern the pattern of the subscription
     * @return true or false as described above
     */
    public boolean addSubscription(String handle, String pattern) {
        //check is sub can be added
        if (!twitter.addSubscription(handle, pattern)) return false;
        PhemeSub currentSub = new PhemeSub(handle, pattern);
        try{
            writeLock.lock();
            if (!subs.containsKey(handle)) {
                subs.put(handle, new HashSet<>());
                subs.get(handle).add(currentSub);
            } else if(!subs.get(handle).contains(new PhemeSub(handle))) {
                subs.get(handle).add(currentSub);
            }
        } finally {
            writeLock.unlock();
        }
        return true;
    }

    /**
     * Removes an unpatterned subscription to a Twitter handle for the user.
     * Removes all patterned and unpatterned subscriptions to the handle.
     * Returns false if the subscription does not exist for the user.
     *
     * @param handle the handle of the account to unsubscribe from
     * @return true or false as described above
     */
    public boolean removeSubscription(String handle) {
        //remove the subscription from the user's TwitterListener
        boolean success = twitter.cancelSubscription(handle);
        try {
            writeLock.lock();
            if (success) {
                subs.remove(handle);
            }
        } finally {
            writeLock.unlock();
        }
        return success;
    }

    /**
     * Removes a patterned subscription to a Twitter handle for the user.
     * Removes only the specific patterned subscription if it exists
     * Returns false if the subscription does not exist for the user
     * or an overriding unpatterned subscription exists.
     *
     * @param handle the handle of the account to unsubscribe from
     * @param pattern the pattern of the subscription to be removed
     * @return true or false as described above
     */
    public boolean removeSubscription(String handle, String pattern) {
        //remove the subscription from the user's TwitterListener
        boolean success = twitter.cancelSubscription(handle, pattern);
        PhemeSub currentSub = new PhemeSub(handle, pattern);
        try {
            writeLock.lock();
            if (success) {
                subs.get(handle).remove(currentSub);
            }
        } finally {
            writeLock.unlock();
        }
        return success;
    }

    /**
     * Adds a PubSubMessage to the user's queue.
     * @param msg the PubSubMessage to be added. Must not be of type TWEET
     * @return true if the message was successfully added and false otherwise
     */
    public boolean addMessage(PubSubMessage msg) {
        return queue.add(msg);
    }

    /**
     * Checks if a specific PubSubMessage exists in the user's queue
     * @param msg the PubSubMessage check for
     * @return true if the message exists and false otherwise
     */
    public boolean containsMessage(PubSubMessage msg) {
        return queue.containsMessage(msg);
    }

    /**
     * Get the next ready message in the user's queue.
     * Returns PubSubMessage.NO_MSG if the user does not have a ready message.
     * @return The user's next message or PubSubMessage.NO_MSG if there is no ready message
     */
    public PubSubMessage getNext() {
        //Get all recent tweets since last getNext call,
        //convert them to PubSubMessages and add them to the queue
        List<TweetV2.TweetData> tweets = twitter.getRecentTweets();
        List<PubSubMessage> messages = new ArrayList<>();
        for (TweetV2.TweetData tweet: tweets) {
            messages.add(this.tweetToPSM(tweet));
        }
        queue.add(messages);
        //Find the next message that is a NO.MSG or of type SIMPLEMSG
        //or is of type TWEET and is valid for the current list of subscriptions
        //Then return it.
        PubSubMessage nextMsg;
        outerLoop: while (true) {
            PubSubMessage next = queue.getNext();
            if (next.equals(PubSubMessage.NO_MSG) || next.getType().equals(BasicMessageType.SIMPLEMSG)) {
                nextMsg = next;
                break;
            }
            try {
                readLock.lock();
                for (String user: subs.keySet()) {
                    for (PhemeSub sub: subs.get(user)) {
                        //iterate through individual subscriptions
                        //Check equality of the subscription's user UUID and the message's sender UUID
                        //Additionally, check for pattern equality of the subscription is patterned
                        if (new UUID(Long.parseLong(twitter.getUser(sub.getUsername()).getId()), 0)
                                .equals(next.getSender())) {
                            if (sub.isPatterned()) {
                                if (next.getContent().contains(sub.getPattern())) {
                                    nextMsg = next;
                                    break outerLoop;
                                }
                            } else {
                                nextMsg = next;
                                break outerLoop;
                            }
                        }
                    }
                }
            } finally {
                readLock.unlock();
            }
        }
        return nextMsg;
    }

    /**
     * Get the all ready messages in the user's queue.
     * Returns and empty List if the user does not have a ready message.
     * @return The user's next message or an empty List if there is no ready message
     */
    public List<PubSubMessage> getAllReady() {
        //Get all recent tweets since last getNext call,
        //convert them to PubSubMessages and add them to the queue
        List<TweetV2.TweetData> tweets = twitter.getRecentTweets();
        List<PubSubMessage> messages = new ArrayList<>();
        for (TweetV2.TweetData tweet : tweets) {
            messages.add(this.tweetToPSM(tweet));
        }
        queue.add(messages);
        //fetch all ready and valid messages in the user's queue
        List<PubSubMessage> msgList = new ArrayList<>();
        PubSubMessage next;
        try {
            readLock.lock();
            do {
                next = queue.getNext();
                if (!next.equals(PubSubMessage.NO_MSG)) {
                    if (!next.getType().equals(BasicMessageType.TWEET)) {
                        msgList.add(next);
                    } else {
                        for (String user : subs.keySet()) {
                            for (PhemeSub sub : subs.get(user)) {
                                //iterate through individual subscriptions
                                //Check equality of the subscription's user UUID and the message's sender UUID
                                //Additionally, check for pattern equality of the subscription is patterned
                                if (new UUID(Long.parseLong(twitter.getUser(sub.getUsername()).getId()), 0)
                                        .equals(next.getSender())) {
                                    if (sub.isPatterned()) {
                                        if (next.getContent().contains(sub.getPattern())) {
                                            msgList.add(next);
                                        }
                                    } else {
                                        msgList.add(next);
                                    }
                                }
                            }
                        }
                    }
                }
            } while (!next.equals(PubSubMessage.NO_MSG));
        } finally {
            readLock.unlock();
        }
        return msgList;
    }

    /**
     * Converts a TweetV2.TweetData object to a PubSubMessage Object
     * @param tweet the tweet to be converted
     * @return a PubSubMessage object representing the given tweet
     */
    private PubSubMessage tweetToPSM(TweetV2.TweetData tweet) {
        return new TweetPubSubMessage(tweet);
    }

    /**
     * Obtain the user's UUID
     * @return the user's UUID
     */
    public UUID getUserID() {
        return userID;
    }

    /**
     * Obtain the user's hashed password
     * @return the user's hashPassword String
     */
    public String getHashPassword() {
        return hashPassword;
    }

    public Map<String, Set<PhemeSub>> getAllSubs(){
        return new HashMap<>(this.subs);
    }

    /**
     * Obtain the hashcode of the message
     * @return the int hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(userID);
    }
}
