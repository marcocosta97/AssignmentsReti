
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * 
 * 
 * 
 */

/**
 * Client utente per la richiesta di registrazione ad una sessione del congresso
 *  e la visualizzazione del programma.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ClientRMI {
    private final Congresso c;
    
    /**
     * Costruttore dell'oggetto. Ottiene l'oggetto remoto Congresso.
     * 
     * @throws RemoteException se si verifica un errore remoto
     */
    public ClientRMI() throws RemoteException {
        try {
            Registry r = LocateRegistry.getRegistry(ServerRMI.SERVER_PORT);
            c = (Congresso) r.lookup(Congresso.SERVICE_NAME);
        }
        catch (RemoteException | NotBoundException ex) {
            throw new RemoteException("Impossibile effettuare il lookup: " + ex);
        }
    }
    
    /**
     * Stampa a video del programma.
     */
    public void printTimetable() {
        try {
            System.out.println(c.show());
        }
        catch (RemoteException ex) {
            System.err.println("Errore! Impossibile stampare il programma: " + ex.getMessage());
        }
    }
    
    /**
     * Registrazione dello speaker alla sessione scelta.
     * 
     * @param name nome speaker
     * @param day giorno
     * @param session numero sessione
     */
    public void signToSession(String name, int day, int session) {
        try {
            int t = c.sign(day, session, name);
            System.out.println(String.format("Registrato lo speaker %s, "
                    + "giorno %d, sessione %d,  intervento %d", name, day, session, t));
        }
        catch (FullSessionException ex) {
            System.err.println("Errore! La sessione scelta è piena!");
        }
        catch (RemoteException | IllegalArgumentException ex) {
            System.err.println("Errore! Impossibile eseguire la richiesta: " + ex);
        }
    }
    
}
