package twitter;


import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.tweet.TweetV2;
import io.github.redouane59.twitter.dto.user.User;
import pheme.PhemeSub;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TwitterListener {

    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    Lock writeLock = lock.writeLock();
    Lock readLock = lock.readLock();
    static TwitterClient twitter;
    static TwitterCache twitterCache;
    private final Map<String, Map<PhemeSub, Boolean>> subscriptions;
    private LocalDateTime recentAccessTime;
    private static final LocalDateTime OCT_1_2022 = LocalDateTime.parse("2022-10-01T00:00:00");
    private static final long CACHE_TIME = 300; //time after which cache entries will be cleared

    //rep invariant
    // no fields are null
    // recentAccessTime.isAfter(OCT_1_2022) == true
    // No patterned pheme subs exist at the same time as any patterned subs for a username
    //Abstraction fcn
    // Represents all the subscriptions that one user has to twitter handles

    static {
        twitterCache = new TwitterCache(CACHE_TIME, new File("secret/credentials.json"));
    }

    /**
     * Creates a new instance of TwitterListener using default cache flush time of 5 minutes;
     * The credentialsFile is a JSON file that contains the Twitter API access keys
     *
     * @param credentialsFile the API credentials JSON file
     */
    public TwitterListener(File credentialsFile) {
        twitter = new TwitterClient(TwitterClient.getAuthentication(credentialsFile));
        subscriptions = new HashMap<>();
        recentAccessTime = OCT_1_2022;
    }

    /**
     * Initialize cache with new value
     * @param cacheTime the value in seconds for the cache to be initialized to
     */
    public static void setCache(long cacheTime) {
        twitterCache = new TwitterCache(cacheTime, new File("secret/credentials.json"));
    }

    /**
     * Tests the validity of a given Twitter handle
     * @param twitterUserName the handle to be tested
     * @return true if the Twitter handle exists and false otherwise
     */
    public boolean isValidUser(String twitterUserName) {
        return twitter.getUserFromUserName(twitterUserName).getData() != null;
    }

    /**
     * Obtain the User object of a Twitter user given a handle;
     * If the handle is invalid returns null.
     *
     * @param twitterUserName the given Twitter handle
     * @return the User object of the handle if it is valid and null otherwise
     */
    public User getUser(String twitterUserName) {
        return twitter.getUserFromUserName(twitterUserName);
    }

    /**
     * Adds an unpatterned subscription to a Twitter handle for the user.
     * Overwrites any previous patterned subscription to the Twitter account.
     * Returns false if the subscription already exists or the given Twitter username is invalid.
     *
     * @param twitterUserName the handle of the account to subscribe to
     * @return true or false as described above
     */
    public boolean addSubscription(String twitterUserName) {
        if (!this.isValidUser(twitterUserName)) return false;
        PhemeSub currentSub = new PhemeSub(twitterUserName);
        boolean success = false;
        try {
            writeLock.lock();
            if (!subscriptions.containsKey(twitterUserName)) {
                subscriptions.put(twitterUserName, new HashMap<>());
                subscriptions.get(twitterUserName).put(currentSub, true);
                success = true;
            } else if (!subscriptions.get(twitterUserName).containsKey(currentSub)){
                subscriptions.get(twitterUserName).clear();
                subscriptions.get(twitterUserName).put(currentSub, true);
                success = true;
            }
        } finally {
            writeLock.unlock();
        }
        return success;
    }

    /**
     * Adds a patterned subscription to a Twitter handle for the user.
     * The pattern is not case-sensitive.
     * Returns false if this or an unpatterned subscription to this user already exists
     * or the given Twitter username is invalid.
     *
     * @param twitterUserName the handle of the account to subscribe to
     * @param pattern the pattern of the subscription
     * @return true or false as described above
     */
    public boolean addSubscription(String twitterUserName, String pattern) {
        if (!this.isValidUser(twitterUserName)) return false;
        PhemeSub currentSub = new PhemeSub(twitterUserName, pattern);
        boolean success = false;
        try {
            writeLock.lock();
            if (!subscriptions.containsKey(twitterUserName)) {
                subscriptions.put(twitterUserName, new HashMap<>());
                subscriptions.get(twitterUserName).put(currentSub, true);
                success = true;
            } else if(!subscriptions.get(twitterUserName).containsKey(new PhemeSub(twitterUserName))) {
                subscriptions.get(twitterUserName).put(currentSub, true);
                success = true;
            }
        } finally {
            writeLock.unlock();
        }
        return success;
    }

    /**
     * Removes an unpatterned subscription to a Twitter handle for the user.
     * Removes all patterned and unpatterned subscriptions to the handle.
     * Returns false if the subscription does not exist for the user.
     *
     * @param twitterUserName the handle of the account to unsubscribe from
     * @return true or false as described above
     */
    public boolean cancelSubscription(String twitterUserName) {
        if (!this.isValidUser(twitterUserName)) return false;
        boolean success = false;
        try {
            writeLock.lock();
            if (subscriptions.containsKey(twitterUserName)) {
                subscriptions.remove(twitterUserName);
                success = true;
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
     * @param twitterUserName the handle of the account to unsubscribe from
     * @param pattern the pattern of the subscription to be removed
     * @return true or false as described above
     */
    public boolean cancelSubscription(String twitterUserName, String pattern) {
        if (!this.isValidUser(twitterUserName)) return false;
        PhemeSub currentSub = new PhemeSub(twitterUserName, pattern);
        boolean success = false;
        try {
            writeLock.lock();
            if (subscriptions.containsKey(twitterUserName)) {
                if(subscriptions.get(twitterUserName).containsKey(currentSub)) {
                    success = subscriptions.get(twitterUserName).remove(currentSub);
                }
            }
        } finally {
            writeLock.unlock();
        }
        return success;
    }

    /**
     *  Get all subscribed tweets since the last tweet or set of tweets was obtained.
     *  For new subscriptions, get all tweets since OCT_1_2022
     *
     * @return a List of TweetV2.TweetData objects representing all the recent Tweets
     */
    public List<TweetV2.TweetData> getRecentTweets() {
        //Collect tweets into set to avoid duplicates
        Set<TweetV2.TweetData> tweetSet = new HashSet<>();
        LocalDateTime currentTime = LocalDateTime.now();
        //flattening subscriptions into a set of PhemeSubs and associated booleans
        Set<Map.Entry<PhemeSub, Boolean>> flattenedSubs = new HashSet<>();
        try {
            readLock.lock();
            for (String twitterUser: subscriptions.keySet()) {
                flattenedSubs.addAll(subscriptions.get(twitterUser).entrySet());
            }
        } finally {
            readLock.unlock();
        }
        //Retrieve all recent subs, from the recent access time if sub has been previously fetched and from the
        //MP start date if the sub is new
        for (Map.Entry<PhemeSub, Boolean> subEntry: flattenedSubs) {
            LocalDateTime startTime;
            try {
                readLock.lock();
                startTime = subEntry.getValue() ? OCT_1_2022 : recentAccessTime;
            } finally {
                readLock.unlock();
            }
            try {
                writeLock.lock();
                subscriptions.get(subEntry.getKey().getUsername()).put(subEntry.getKey(), false);
            } finally {
                writeLock.unlock();
            }
            //Filter tweets by patterned is PhemeSub is Patterned and return bulk otherwise
            if (subEntry.getKey().isPatterned()) {
                List<TweetV2.TweetData> tempList =
                        this.getTweetsByUser(subEntry.getKey().getUsername(), startTime, currentTime);
                tweetSet.addAll(tempList.stream()
                        .filter(x -> x.getText().toLowerCase()
                        .contains(subEntry.getKey().getPattern().toLowerCase()))
                        .toList());
            } else {
                tweetSet.addAll(this.getTweetsByUser(subEntry.getKey().getUsername(), startTime, currentTime));
            }
        }
        //Update recent access time
        try {
            writeLock.lock();
            recentAccessTime = currentTime;
        } finally {
            writeLock.unlock();
        }
        //Convert set back to list sorted by time created and return
        List<TweetV2.TweetData> returnList = new ArrayList<>(tweetSet);
        returnList.sort(new TweetComparator());
        return returnList;
    }

    /**
     * Get all the tweets made by a user within a time range.
     *
     * @param twitterUserName the twitter handle to search for
     * @param startTime the beginning of the time range
     * @param endTime the end of the time range
     * @return a List of TweetV2.TweetData objects representing all the Tweets by the specified user in the time window
     */
    public List<TweetV2.TweetData> getTweetsByUser(String twitterUserName, LocalDateTime startTime,
                                                   LocalDateTime endTime) {
        if (twitterCache.containsRequest(twitterUserName, startTime, endTime)) {
            return twitterCache.getRequest(twitterUserName, startTime, endTime);
        } else {
            User twUser = twitter.getUserFromUserName(twitterUserName);
            if (twUser.getId() == null) {
                throw new IllegalArgumentException();
            }
            TweetList twList = twitter.getUserTimeline(twUser.getId(),
                    AdditionalParameters.builder().startTime(startTime).endTime(endTime).build());
            twitterCache.addRequest(twitterUserName, startTime, endTime, twList.getData());
            return twList.getData();
        }
    }

    /**
     * Comparator to sort Tweets by timestamp
     */
    private static class TweetComparator implements Comparator<TweetV2.TweetData> {
        public int compare(TweetV2.TweetData tweet1, TweetV2.TweetData tweet2) {
            return tweet1.getCreatedAt().compareTo(tweet2.getCreatedAt());
        }
    }
}
