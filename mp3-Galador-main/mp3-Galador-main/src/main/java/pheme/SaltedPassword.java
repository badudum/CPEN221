package pheme;

public record SaltedPassword(String salt, String hashedPassword) {

    //Rep Invariant
    // no fields are null
    //Abstraction Fcn
    // represents a hashedPassword of a user and the salt used to hash it
    /**
     * Created a SaltedPassword object
     * @param salt           the salt of the password
     * @param hashedPassword the hashed password
     */
    public SaltedPassword {
    }

    /**
     * Obtain salt
     * @return the salt of the password
     */
    public String salt() {
        return salt;
    }

    /**
     * Obtain hashed password
     * @return the hashed password
     */
    public String hashedPassword() {
        return hashedPassword;
    }

}
