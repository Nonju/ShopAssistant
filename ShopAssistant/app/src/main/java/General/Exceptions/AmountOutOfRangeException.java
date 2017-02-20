package General.Exceptions;

/**
 * Created by hannes on 2016-12-05.
 *
 * Thrown when an int is out of a given range
 */

public class AmountOutOfRangeException extends Exception {
    public AmountOutOfRangeException() { this("Given amount is out of set range!"); }
    public AmountOutOfRangeException(String message) { super(message); }
}
