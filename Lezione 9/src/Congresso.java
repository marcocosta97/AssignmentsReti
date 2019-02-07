
import java.rmi.Remote;
import java.rmi.RemoteException;

/*
 * 
 * 
 * 
 */

/**
 * Interfaccia Congresso per la definizione dei metodi di registrazione e 
 *  visualizzazione del programma.
 * 
 * @author mc - Marco Costa - 545144
 */
public interface Congresso extends Remote {
    public static final String SERVICE_NAME = "CongressoService";
    
    /**
     * Registrazione ad una sessione del congresso come speaker.
     * 
     * @param day il giorno della sessione
     * @param session il numero di sessione
     * @param name il nome dello speaker
     * @return il numero di intervento assegnato dal server
     * @throws FullSessionException se la sessione scelta è già piena
     * @throws RemoteException se si verifica un errore remoto
     */
    public Integer sign(int day, int session, String name) throws FullSessionException,RemoteException;
    
    /**
     * Restituisce una vista testuale dell'intero programma del congresso.
     * 
     * @return una visualizzazione testuale
     * @throws RemoteException se si verifica un errore remoto
     */
    public String show() throws RemoteException;
}
