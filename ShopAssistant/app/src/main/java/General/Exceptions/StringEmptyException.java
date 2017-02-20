package General.Exceptions;

/**
 * Created by Hannes on 2016-11-30.
 *
 * Thrown when a string is null or empty
 */

public class StringEmptyException extends Exception {
    public StringEmptyException() { this("String is empty"); }
    public StringEmptyException(String message) { super(message); }
}
