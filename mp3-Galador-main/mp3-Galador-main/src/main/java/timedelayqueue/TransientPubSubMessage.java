package timedelayqueue;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public class TransientPubSubMessage extends PubSubMessage {
    private final int     lifetime;
    private final boolean isTransient;

    // Representation Invariant:
    // - this.lifetime >= 0
    // - Rep Inv of PubSubMessage Holds
    // Abstraction Function
    // Represents a pubSubMessage that will get deleted after a certain time

    /**
     * Creates a TransientPubSubMessage instance with explicit args;
     * Content should be in JSON format to accommodate a variety of
     * message types (e.g., TweetData)
     *
     * Message intended for a single receiver
     *
     * @param id the UUID of the message
     * @param timestamp the timestamp of the message
     * @param sender the UUID of the sender of the message
     * @param receiver the UUID of the receiver of the message
     * @param content a String representing the content of the message
     * @param type the type of the message
     * @param lifetime the lifetime of the transient message; lifetime >= 0
     */
    public TransientPubSubMessage(UUID id, Timestamp timestamp,
                         UUID sender, UUID receiver, String content, MessageType type, int lifetime) {
        super(id, timestamp, sender, receiver, content, type);
        this.lifetime = Math.max(lifetime, 0);
        this.isTransient = true;
    }

    /**
     * Creates a TransientPubSubMessage instance with explicit args;
     * Content should be in JSON format to accommodate a variety of
     * message types (e.g., TweetData)
     *
     * Message intended for multiple receivers
     *
     * @param id the UUID of the message
     * @param timestamp the timestamp of the message
     * @param sender the UUID of the sender of the message
     * @param receiver a list of UUIDs representing all the recipients of the message
     * @param content a String representing the content of the message
     * @param type the type of the message
     * @param lifetime the lifetime of the transient message; lifetime > 0
     */
    public TransientPubSubMessage(UUID id, Timestamp timestamp,
                                  UUID sender, List<UUID> receiver, String content, MessageType type, int lifetime) {
        super(id, timestamp, sender, receiver, content, type);
        this.lifetime = Math.max(lifetime, 0);
        this.isTransient = true;
    }

    /**
     * Creates a TransientPubSubMessage instance with implicit args;
     *  id = randomly generated UUID
     *  timestamp = time message was created
     *  type = BasicMessageType.SIMPLEMSG
     *
     * Message intended for a single receiver
     *
     * @param sender the UUID of the sender of the message
     * @param receiver the UUID of the receiver of the message
     * @param content a String representing the content of the message
     * @param lifetime the lifetime of the transient message; lifetime > 0
     */
    public TransientPubSubMessage(UUID sender, UUID receiver, String content, int lifetime) {
        super(sender, receiver, content);
        this.lifetime = Math.max(lifetime, 0);
        this.isTransient = true;
    }

    /**
     * Creates a TransientPubSubMessage instance with implicit args;
     *  id = randomly generated UUID
     *  timestamp = time message was created
     *  type = BasicMessageType.SIMPLEMSG
     *
     * Message intended for multiple receivers
     *
     * @param sender the UUID of the sender of the message
     * @param receiver a list of UUIDs representing all the recipients of the message
     * @param content a String representing the content of the message
     * @param lifetime the lifetime of the transient message; lifetime > 0
     */
    public TransientPubSubMessage(UUID sender, List<UUID> receiver, String content, int lifetime) {
        super(sender, receiver, content);
        this.lifetime = Math.max(lifetime, 0);
        this.isTransient = true;
    }

    /**
     * Obtain the lifetime of a TransientPubSubMessage
     * @return lifetime of the message
     */
    public int getLifetime() {
        return lifetime;
    }

    /**
     * Returns true if the message is transient and false otherwise
     * @return true if the message is transient and false otherwise
     */
    @Override
    public boolean isTransient() {
        return this.isTransient;
    }
}
