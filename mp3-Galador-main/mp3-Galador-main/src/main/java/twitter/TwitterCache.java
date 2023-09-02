package twitter;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.TweetV2;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TwitterCache {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    Lock writeLock = lock.writeLock();
    Lock readLock = lock.readLock();
    static TwitterClient twitter;
    private final Map<TwitterRequest, List<TweetV2.TweetData>> cache;
    private final long cacheClearTime;
    private final long SEC_TO_MS = 1000;

    //rep invariant
    // no fields are null
    //Abstraction fcn
    // A self deleting cache for all twitter listeners where previous requests live for 5 mins before being deleted

    /**
     * Creates a TwitterCache object using a clearTime to define how long the cache holds data before purging and
     * a credentials file to allow TwitterCache to make API calls.
     *
     * @param clearTime the time cached data is held for before it is purged; in minutes
     * @param credentialsFile a Twitter API credentials file
     */
    public TwitterCache(long clearTime, File credentialsFile) {
        cache = new HashMap<>();
        cacheClearTime = clearTime * SEC_TO_MS;
        twitter = new TwitterClient(TwitterClient.getAuthentication(credentialsFile));
    }

    /**
     * Checks if the cache contains the result of a getTweetsByUser request.
     * Returns true if a twitter request with the same username exists in the cache and false otherwise.
     *
     * @param username the username of the request
     * @param start the start time of the request
     * @param end the end time of the request
     */
    public boolean containsRequest(String username, LocalDateTime start, LocalDateTime end) {
        boolean success;
        try {
            readLock.lock();
            success = cache.containsKey(new TwitterRequest(username, start, end));
        } finally {
            readLock.unlock();
        }
        return success;
    }

    /**
     * Adds a request and its result to the cache, all requests are deleted after existing in the cache for cacheClearTime minutes.
     * Additionally, prunes all expired results in the cache.
     *
     * @param username the username of the request
     * @param start the start time of the request
     * @param end the end time of the request
     * @param data the result of the request
     */
    public void addRequest(String username, LocalDateTime start, LocalDateTime end, List<TweetV2.TweetData> data) {
        try {
            writeLock.lock();
            cache.put(new TwitterRequest(username, start, end), data);
        } finally {
            writeLock.unlock();
        }
        this.prune();
    }

    /**
     * Get the cached data of the current request.
     * If the window of the current request falls outside the window of the cached data,
     * update the cache with the larger window, then return the window of the current request
     *
     * @param username the username of the request
     * @param start the start time of the request
     * @param end the end time of the request
     * @return the result of the cached request
     */
    public List<TweetV2.TweetData> getRequest(String username, LocalDateTime start, LocalDateTime end) {
        List<TweetV2.TweetData> returnList = new ArrayList<>();
        List<TweetV2.TweetData> totalList = new ArrayList<>();
        TwitterRequest totalReq = null;
        boolean cacheUpdate = false;
        try {
            readLock.lock();
            for (TwitterRequest req: cache.keySet()) {
                if (req.equals(new TwitterRequest(username, start, end))) {
                    //If the window the current request is within the cached data, filter and return the cached data.
                    //Otherwise, pull any additional required tweets to complete the union of both windows, then update the cache
                    if (req.getStart().isBefore(start) && req.getEnd().isAfter(end)) {
                        returnList.addAll(cache
                                .get(req)
                                .stream().filter(x -> x.getCreatedAt().isAfter(start) && x.getCreatedAt().isBefore(end))
                                .toList());
                    } else {
                        cacheUpdate = true;
                        LocalDateTime earliestStart;
                        LocalDateTime latestEnd;
                        // get the earliest start and latest end
                        earliestStart = (req.getStart().isBefore(start)) ? req.getStart() : start;
                        latestEnd = (req.getEnd().isAfter(end)) ? req.getEnd() : end;
                        //add cached tweets then obtain additional required tweets from the Twitter API
                        totalList.addAll(cache.get(req));
                        if (end.isBefore(req.getStart())) {
                            //Cached request totally before current
                            totalList.addAll(twitter.getUserTimeline(twitter.getUserFromUserName(username).getId(),
                                    AdditionalParameters.builder().startTime(start).endTime(req.getStart()).build()).getData());
                        } else if (start.isAfter(req.getEnd())) {
                            //Cached request totally after current
                            totalList.addAll(twitter.getUserTimeline(twitter.getUserFromUserName(username).getId(),
                                    AdditionalParameters.builder().startTime(req.getEnd()).endTime(end).build()).getData());
                        } else {
                            if (start.isBefore(req.getEnd())) {
                                //Cached request overlaps the start of the current
                                totalList.addAll(twitter.getUserTimeline(twitter.getUserFromUserName(username).getId(),
                                        AdditionalParameters.builder().startTime(req.getEnd()).endTime(end).build()).getData());
                            }
                            if (end.isAfter(req.getStart())) {
                                //Cached request overlaps the end of the current
                                totalList.addAll(twitter.getUserTimeline(twitter.getUserFromUserName(username).getId(),
                                        AdditionalParameters.builder().startTime(start).endTime(req.getStart()).build()).getData());
                            }
                        }
                        totalReq = new TwitterRequest(username, earliestStart, latestEnd);
                        //stream totalList into returnList, filtering by the conditions of the current request
                        returnList.addAll(totalList
                                .stream().filter(x -> x.getCreatedAt().isAfter(start) && x.getCreatedAt().isBefore(end))
                                .toList());
                    }
                }
            }
        } finally {
            readLock.unlock();
        }
        //If cache needs updating, update cache
        try {
            writeLock.lock();
            if (cacheUpdate) {
                cache.remove(totalReq);
                cache.put(totalReq, totalList.stream().distinct().toList());
            }
        } finally {
            writeLock.unlock();
        }
        return returnList.stream().distinct().toList();
    }

    /**
     * Iterate through the cache and prune any expired cached data.
     */
    private void prune(){
        try {
            writeLock.lock();
            List<TwitterRequest> reqList = new ArrayList<>(cache.keySet());
            for (TwitterRequest req: reqList) {
                if (req.getReqTime().getTime() + cacheClearTime < System.currentTimeMillis()) {
                    cache.remove(req);
                }
            }
        } finally {
            writeLock.unlock();
        }
    }

}
