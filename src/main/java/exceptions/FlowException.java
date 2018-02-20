package exceptions;

/**
 * @author: OnlyElena@mail.ru
 * Date: 3/11/16
 * Time: 7:38 PM
 */
public class FlowException extends Exception {
    public FlowException() {
    }

    public FlowException(String s) {
        super(s);
    }

    public FlowException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public FlowException(Throwable throwable) {
        super(throwable);
    }
}
