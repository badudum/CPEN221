package twitter;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;

public class TwitterRequest {
    private final Timestamp reqTime;
    private final String user;
    private final LocalDateTime start;
    private final LocalDateTime end;

    //rep invariant
    // no fields are null
    // start.isBefore(end) == true
    //Abstraction fcn
    // Maps the variable to a previous request made to twitters server at time reqTime, searching for the tweets of
    // user between start and end

    /**
     * Creates a TwitterRequest Object using a user String and a time window defined by start and end.
     *
     * @param user the username of the request to the Twitter API
     * @param start the start LocalDateTime of the request
     * @param end the end LocalDateTime of the request
     */
    public TwitterRequest(String user, LocalDateTime start, LocalDateTime end) {
        this.user = user;
        this.start = start;
        this.end = end;
        this.reqTime = Timestamp.from(Instant.now());
    }

    /**
     * Obtain the start of the request window
     * @return LocalDateTime defining the start of the request
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Obtain the end of the request window
     * @return LocalDateTime defining the end of the request
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Obtain the time the TwitterRequest was created
     * @return a Timestamp defining the TwitterRequest's creation
     */
    public Timestamp getReqTime() {
        return reqTime;
    }

    /**
     * Defines equality depending only on the user string of the object.
     *
     * @param other the TwitterRequest to be compared
     * @return true if the user Strings match and false otherwise
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        TwitterRequest that = (TwitterRequest) other;
        return Objects.equals(user, that.user);
    }

    /**
     * Obtain the hashcode of the TwitterRequest
     * @return the int hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}
