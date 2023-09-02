package timedelayqueue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PubSubMessage implements TimestampedObject {
    public static final UUID ZERO_UUID = new UUID(0L, 0L);
    public static final PubSubMessage NO_MSG = new PubSubMessage(
        ZERO_UUID,
        new Timestamp(0),
        ZERO_UUID,
        ZERO_UUID,
        "",
        BasicMessageType.SIMPLEMSG);
    private final String content;
    private final boolean isTransient;
    private final UUID sender;
    private final List<UUID> receiver;
    private final MessageType type;
    private final UUID id;
    private final Timestamp timestamp;

    // Representation Invariant:
    // - content, receiver, sender, type, id, timestamp are not null
    // - receiver is not empty
    // Abstraction Fcn
    // Represents a general message from one person to 0 or more people

    /**
     * Creates a PubSubMessage instance with explicit args;
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
     */
    public PubSubMessage(UUID id, Timestamp timestamp,
                         UUID sender, UUID receiver, String content, MessageType type) {
        this.id = id;
        this.timestamp = timestamp;
        this.sender = sender;
        this.isTransient = false;
        this.content = content;
        this.receiver = new ArrayList<>();
        this.receiver.add(receiver);
        this.type = type;
    }

    /**
     * Creates a PubSubMessage instance with explicit args;
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
     */
    public PubSubMessage(UUID id, Timestamp timestamp,
                         UUID sender, List<UUID> receiver, String content, MessageType type) {
        this.id = id;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = new ArrayList<>(receiver);
        this.isTransient = false;
        this.content = content;
        this.type = type;
    }

    /**
     * Creates a PubSubMessage instance with implicit args;
     *  id = randomly generated UUID
     *  timestamp = time message was created
     *  type = BasicMessageType.SIMPLEMSG
     *
     * Message intended for a single receiver
     *
     * @param sender the UUID of the sender of the message
     * @param receiver the UUID of the receiver of the message
     * @param content a String representing the content of the message
     */
    public PubSubMessage(UUID sender, UUID receiver, String content) {
        this(
            UUID.randomUUID(),
            new Timestamp(System.currentTimeMillis()),
            sender, receiver,
            content,
            BasicMessageType.SIMPLEMSG
        );
    }

    /**
     * Creates a PubSubMessage instance with implicit args;
     *  id = randomly generated UUID
     *  timestamp = time message was created
     *  type = BasicMessageType.SIMPLEMSG
     *
     * Message intended for multiple receivers
     *
     * @param sender the UUID of the sender of the message
     * @param receiver a list of UUIDs representing all the recipients of the message
     * @param content a String representing the content of the message
     */
    public PubSubMessage(UUID sender, List<UUID> receiver, String content) {
        this(
            UUID.randomUUID(),
            new Timestamp(System.currentTimeMillis()),
            sender, receiver,
            content,
            BasicMessageType.SIMPLEMSG
        );
    }

    /**
     * Obtain the id if the message
     * @return the id UUID of the message
     */
    @Override
    public UUID getId() {
        return id;
    }

    /**
     * Obtain the timestamp of the message
     * @return the Timestamp of the message
     */
    @Override
    public Timestamp getTimestamp() {
        return (Timestamp) timestamp.clone();
    }

    /**
     * Obtain the content of the message;
     * The content will be in JSON format
     *
     * @return the String representing the content of the message
     */
    public String getContent() {
        return content;
    }

    /**
     * Obtain the type of the message
     * @return a MessageType object representing the message's type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * Obtain the sender UUID
     * @return the sender UUID
     */
    public UUID getSender() {
        return sender;
    }

    /**
     * Obtain a list of all the recipient UUIDs
     * @return a list of the UUIDs of all the recipients
     */
    public List<UUID> getReceiver() {
        return new ArrayList<>(receiver);
    }

    /**
     * Obtain if the message is transient
     * Default is false
     *
     * @return true if the message is transient and false otherwise
     */
    public boolean isTransient() {
        return isTransient;
    }

    /**
     * Obtain the hashcode of the message
     * @return the int hashcode
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Compares two PubSubMessages;
     * Returns true if the two messages share a UUID id and false otherwise
     *
     * @param other the message to be compared
     * @return true if the messages are equal as described and false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof PubSubMessage that) {
            return this.id.equals(that.id);
        } else {
            return false;
        }
    }

}