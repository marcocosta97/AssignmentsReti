/*
 * 
 * 
 * 
 */

/**
 * Eccezione utilizzata per indicare una sessione come piena.
 * 
 * @author mc - Marco Costa - 545144
 */
public class FullSessionException extends Exception {

    public FullSessionException() {
        super();
    }

    public FullSessionException(String message) {
        super(message);
    }
    
}
