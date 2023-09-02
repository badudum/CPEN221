package timedelayqueue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.redouane59.twitter.dto.tweet.TweetV2;

import java.sql.Timestamp;
import java.util.UUID;

public class TweetPubSubMessage extends PubSubMessage{
    private final TweetV2.TweetData tweet;
    //rep invariant
    // no fields are null
    // the rep invariant of PubSubMessage Holds
    //Abstraction fcn
    // Represents a tweets as a PubSubMesssage

    /**
     * Creates a PubSubMessage
     * <p>
     * Message intended for a single receiver
     *
     * @param tweet the tweet data of a TweetV2 object
     */
    public TweetPubSubMessage(TweetV2.TweetData tweet) {
        super(new UUID(Long.parseLong(tweet.getId()), 0),
                Timestamp.valueOf(tweet.getCreatedAt()),
                new UUID(Long.parseLong(tweet.getAuthorId()), 0),
                PubSubMessage.ZERO_UUID,
                tweet.getText(),
                BasicMessageType.TWEET);
        this.tweet = tweet;
    }

    /**
     * Obtain the tweet of a tweet PSM
     * @return the tweet of a tweet PSM
     */
    public TweetV2.TweetData getTweet() {
        return tweet;
    }

}
