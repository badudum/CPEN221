package phemeservice;

import io.github.redouane59.twitter.dto.tweet.TweetV2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import twitter.TwitterCache;
import twitter.TwitterListener;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TwitterCacheTester {

    @BeforeAll
    public static void initialize() {
        TwitterListener.setCache(1);
    }

    @Test
    public void testCacheRequestOverlap1() {
        //Separate windows, old before new
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        TwitterListener tl2 = new TwitterListener(new File("secret/credentials.json"));
        List<TweetV2.TweetData> tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-01T00:00:00"),
                LocalDateTime.parse("2022-10-10T00:00:00"));
        tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-15T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        LocalDateTime currentTime = LocalDateTime.now();
        while (LocalDateTime.now().isBefore(currentTime.plusSeconds(2))) { }
        List<TweetV2.TweetData> tweets2 = tl2.getTweetsByUser("Dream",
                LocalDateTime.parse("2022-10-15T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        tweets2 = tl2.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-15T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        assertEquals(tweets.size(), tweets2.size());
        assertEquals(tweets.stream().map(TweetV2.TweetData::getId).toList(), tweets2.stream().map(TweetV2.TweetData::getId).toList());
    }

    @Test
    public void testCacheRequestOverlap2() {
        //Separate windows, new before old
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        TwitterListener tl2 = new TwitterListener(new File("secret/credentials.json"));
        List<TweetV2.TweetData> tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-15T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-01T00:00:00"),
                LocalDateTime.parse("2022-10-10T00:00:00"));
        LocalDateTime currentTime = LocalDateTime.now();
        while (LocalDateTime.now().isBefore(currentTime.plusSeconds(2))) { }
        List<TweetV2.TweetData> tweets2 = tl2.getTweetsByUser("Dream",
                LocalDateTime.parse("2022-10-15T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        tweets2 = tl2.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-01T00:00:00"),
                LocalDateTime.parse("2022-10-10T00:00:00"));
        assertEquals(tweets.size(), tweets2.size());
        assertEquals(tweets.stream().map(TweetV2.TweetData::getId).toList(), tweets2.stream().map(TweetV2.TweetData::getId).toList());
    }

    @Test
    public void testCacheRequestOverlap3() {
        //Overlapping windows, old before new
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        TwitterListener tl2 = new TwitterListener(new File("secret/credentials.json"));
        List<TweetV2.TweetData> tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-01T00:00:00"),
                LocalDateTime.parse("2022-10-20T00:00:00"));
        tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-10T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        LocalDateTime currentTime = LocalDateTime.now();
        while (LocalDateTime.now().isBefore(currentTime.plusSeconds(2))) { }
        List<TweetV2.TweetData> tweets2 = tl2.getTweetsByUser("Dream",
                LocalDateTime.parse("2022-10-15T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        tweets2 = tl2.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-10T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        assertEquals(tweets.size(), tweets2.size());
        assertEquals(tweets.stream().map(TweetV2.TweetData::getId).toList(), tweets2.stream().map(TweetV2.TweetData::getId).toList());
    }

    @Test
    public void testCacheRequestOverlap4() {
        //Overlapping windows, new before old
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        TwitterListener tl2 = new TwitterListener(new File("secret/credentials.json"));
        List<TweetV2.TweetData> tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-10T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-01T00:00:00"),
                LocalDateTime.parse("2022-10-20T00:00:00"));
        LocalDateTime currentTime = LocalDateTime.now();
        while (LocalDateTime.now().isBefore(currentTime.plusSeconds(2))) { }
        List<TweetV2.TweetData> tweets2 = tl2.getTweetsByUser("Dream",
                LocalDateTime.parse("2022-10-15T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        tweets2 = tl2.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-01T00:00:00"),
                LocalDateTime.parse("2022-10-20T00:00:00"));
        assertEquals(tweets.size(), tweets2.size());
        assertEquals(tweets.stream().map(TweetV2.TweetData::getId).toList(), tweets2.stream().map(TweetV2.TweetData::getId).toList());
    }

    @Test
    public void testCacheRequestOverlap5() {
        //Old inside new
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        TwitterListener tl2 = new TwitterListener(new File("secret/credentials.json"));
        List<TweetV2.TweetData> tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-10T00:00:00"),
                LocalDateTime.parse("2022-10-20T00:00:00"));
        tweets = tl.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-01T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        LocalDateTime currentTime = LocalDateTime.now();
        while (LocalDateTime.now().isBefore(currentTime.plusSeconds(2))) { }
        List<TweetV2.TweetData> tweets2 = tl2.getTweetsByUser("Dream",
                LocalDateTime.parse("2022-10-15T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        tweets2 = tl2.getTweetsByUser("UBC",
                LocalDateTime.parse("2022-10-01T00:00:00"),
                LocalDateTime.parse("2022-10-25T00:00:00"));
        assertEquals(tweets.size(), tweets2.size());
        assertEquals(tweets.stream().map(TweetV2.TweetData::getId).toList(), tweets2.stream().map(TweetV2.TweetData::getId).toList());

    }

}
