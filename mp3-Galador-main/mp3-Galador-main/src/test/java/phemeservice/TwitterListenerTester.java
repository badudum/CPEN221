package phemeservice;

import io.github.redouane59.twitter.dto.tweet.TweetV2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import twitter.TwitterListener;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TwitterListenerTester {

    @Test
    public void testFetchRecentTweets() {
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        tl.addSubscription("Dream");
        List<TweetV2.TweetData> tweets = tl.getRecentTweets();
        assertTrue(tweets.size() > 0);
    }

    @Test
    public void testDoubleFetchRecentTweets() {
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        tl.addSubscription("Dream");
        List<TweetV2.TweetData> tweets = tl.getRecentTweets();
        assertTrue(tweets.size() > 0);
        tweets = tl.getRecentTweets();
        assertEquals(0, tweets.size()); // second time around, in quick succession, no tweet
    }

    @Test
    public void TestPatternedSub() {
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        assertFalse(tl.addSubscription("retyuyhujkoiuiyutfyrdtdrytf"));
        assertFalse(tl.addSubscription("retyuyhujkoiuiyutfyrdtdrytf", "xx"));
        assertFalse(tl.cancelSubscription("retyuyhujkoiuiyutfyrdtdrytf"));
        assertFalse(tl.cancelSubscription("retyuyhujkoiuiyutfyrdtdrytf", "xx"));
        assertTrue(tl.addSubscription("Dream"));
        List<TweetV2.TweetData> tweets = tl.getRecentTweets();
        assertFalse(tl.addSubscription("Dream"));
        assertTrue(tl.cancelSubscription("Dream"));
        assertTrue(tl.addSubscription("Dream", "George"));
        List<TweetV2.TweetData> tweets2 = tl.getRecentTweets();
        assertTrue(tweets.size() > tweets2.size());
    }

    @Test
    public void TestAndRemoveSubs() {
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        assertTrue(tl.addSubscription("dreamwastaken"));
        assertFalse(tl.addSubscription("dreamwastaken", "George"));
        assertFalse(tl.cancelSubscription("dreamwastaken", "George"));
        assertTrue(tl.cancelSubscription("dreamwastaken"));
        assertEquals(0, tl.getRecentTweets().size());
    }

    @Test
    public void TestOverrideSub() {
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        assertTrue(tl.addSubscription("Dream", "George"));
        List<TweetV2.TweetData> tweets = tl.getRecentTweets();
        assertTrue(tweets.size() > 0);
        assertTrue(tl.addSubscription("Dream"));
        List<TweetV2.TweetData> tweets2 = tl.getRecentTweets();
        assertTrue(tweets.size() < tweets2.size()); // second time, unpatterned subs means more scraped tweets
    }

    @Test
    public void GetRecentForNewSubs() {
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        tl.addSubscription("Dream");
        List<TweetV2.TweetData> tweets = tl.getRecentTweets();
        assertTrue(tweets.size() > 0);
        tl.addSubscription("dreamwastaken");
        assertTrue(tweets.size() > 0);
        tweets = tl.getRecentTweets();
        assertTrue(tweets.size() > 0); // second time around, after adding new sub, still tweets
    }

    @Test
    public void TestOverlappingPatternedSubs() {
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        assertTrue(tl.addSubscription("dreamwastaken", "Georg"));
        assertTrue(tl.addSubscription("dreamwastaken", "George"));
        List<TweetV2.TweetData> tweets = tl.getRecentTweets();
        assertEquals(tweets.stream().distinct().toList().size(), tweets.size());
    }

    @Test
    public void TestValidAndGetUsers() {
        TwitterListener tl = new TwitterListener(new File("secret/credentials.json"));
        assertTrue(tl.isValidUser("Dream"));
        assertFalse(tl.isValidUser("dpoj9192&@)@I*!(pisaouwhae"));
        assertEquals(tl.getUser("Dream").getName(), "Dream");
        assertNull(tl.getUser("tfyghjkolokjihugyutyfrtyugihoi").getId());
    }
}
