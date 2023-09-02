package pheme;

import java.util.Objects;

public class PhemeSub {
    private final String username;
    private final String pattern;
    private final boolean isPatterned;
    //rep invariant
    // username is not null
    // is isPatterned is true, pattern is not null, it is null otherwise
    //Abstraction fcn
    // Represents a subscription to a Twitter user

    /**
     * Creates a patterned PhemeSub object
     * @param username the username to be subscribed to
     * @param pattern the pattern of the subscription
     */
    public PhemeSub(String username, String pattern) {
        this.username = username;
        this.pattern = pattern;
        this.isPatterned = true;
    }

    /**
     * Creates an unpatterned PhemeSub object
     * @param username the username to be subscribed to
     */
    public PhemeSub(String username) {
        this.username = username;
        this.pattern = null;
        this.isPatterned = false;
    }

    /**
     * Obtain the pattern of the PhemeSub
     * @return The PhemeSub pattern String or null if the sub is unpatterned
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Obtain the username of the PhemeSub
     * @return The PhemeSub username String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Check the if the PhemeSub is patterned
     * @return true if the object is patterned and false otherwise
     */
    public boolean isPatterned() {
        return isPatterned;
    }

    /**
     * Compares two PhemeSubs;
     * Returns true if the two messages share a username and pattern and false otherwise
     *
     * @param other the message to be compared
     * @return true if the messages are equal as described and false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof PhemeSub that) {
            if (this.isPatterned() == that.isPatterned()) {
                if (this.isPatterned()) {
                    return this.getUsername().equals(that.getUsername()) && this.getPattern().equalsIgnoreCase(that.getPattern());
                } else {
                    return this.getUsername().equals(that.getUsername());
                }
            }
        }
        return false;
    }

    /**
     * Obtain the hashcode of the message
     * @return the int hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, pattern);
    }
}
