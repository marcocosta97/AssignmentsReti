
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * 
 * 
 * 
 */

/**
 * Classe Server operante sulla porta "SERVER_PORT" per la gestione del congresso.
 * Nota: l'implementazione è consistente a variazioni sul numero di giorni di
 *       congresso, numero massimo di speaker e numero di sessioni per giorno;
 *       è sufficiente cambiare le relative variabili pubbliche sottostanti.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ServerRMI extends UnicastRemoteObject implements Congresso {
    public static final int SERVER_PORT = 1111;
    
    public static final int NO_SESSION = 12;
    public static final int NO_DAYS = 3;
    public static final int MAX_SPEAKER = 5;
    
    private static final String VOID_SPEAKER = null;
    private final Registry r;
    
    private final String[][][] programma;
    
    /**
     * Creazione della tabella di programma ed esportazione dell'oggetto sulla 
     * porta SERVER_PORT.
     * 
     * @throws java.rmi.RemoteException
     */
    public ServerRMI() throws RemoteException {
        programma = new String[NO_DAYS][NO_SESSION][MAX_SPEAKER];
        /* inizializzazione della matrice */
        for(int i = 0; i < NO_DAYS; i++)
            for(int j = 0; j < NO_SESSION; j++)
                for(int z = 0; z < MAX_SPEAKER; z++)
                    programma[i][j][z] = VOID_SPEAKER;
        
        /* esportazione oggetto */
        r = LocateRegistry.createRegistry(SERVER_PORT);
        r.rebind(Congresso.SERVICE_NAME, (Congresso) this);
    }
    
    /**
     * Gestisce la registrazione di uno speaker al congresso, assegnando il 
     *  primo intervento disponibile per la sessione o lanciando una 
     *  FullSessionException se non vi è più posto disponibile.
     * 
     * @param day giorno
     * @param session numero sessione
     * @param name nome speaker
     * @return il numero dell'intervento ottenuto
     * @throws FullSessionException se la sessione è piena
     * @throws RemoteException in caso di errore remoto
     */
    @Override
    public Integer sign(int day, int session, String name) throws FullSessionException, RemoteException {
        if((session < 1) || (session > NO_SESSION)) throw new IllegalArgumentException();
        if((day < 1) || (day > NO_DAYS)) throw new IllegalArgumentException();
        if(name == null) throw new IllegalArgumentException();
        
        day--;
        session--;
        
        int i;
        for(i = 0; i < MAX_SPEAKER; i++)
            if(programma[day][session][i] == VOID_SPEAKER) /* intervento vuoto */
            {
                programma[day][session][i] = name;
                return i + 1;
            }
        
        
        throw new FullSessionException(); /* sessione piena */
    }

    /**
     * Restituisce una vista testuale dell'intero programma del congresso.
     * 
     * @return vista del programma
     * @throws RemoteException in caso di errore remoto
     */
    @Override
    public String show() throws RemoteException {  
        String s = "\n";
        
        /**
         * Per ogni giorno di congresso viene estratta la matrice relativa 
         *  e passata ad una classe di Pretty Printing per matrici
         */
        for(int day = 0; day < NO_DAYS; day++)
        {
            s += "\t\t\t--- Giorno " + (day + 1) + " ---\n\n";
            
            /* matrice di rappresentazione */
            String[][] printMatrix = new String[NO_SESSION + 1][MAX_SPEAKER + 1];
            /* aggiunta di righe e colonne di indice */
            printMatrix[0][0] = "Sessione";
            for(int i = 0; i < MAX_SPEAKER; i++)
                printMatrix[0][i + 1] = "Intervento " + (i + 1);
            for(int i = 0; i < NO_SESSION; i++)
                printMatrix[i + 1][0] = "S" + (i + 1);
            
            /* copia della matrice congresso */
            for(int i = 0; i < NO_SESSION; i++)
                System.arraycopy(programma[day][i], 0, printMatrix[i + 1], 1, MAX_SPEAKER);
            
            
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream ps = new PrintStream(baos, true, "UTF-8")) {
                PrettyPrinter p = new PrettyPrinter(ps, "(vuoto)");
                p.print(printMatrix);
            }
            catch (UnsupportedEncodingException ex) {
                Logger.getLogger(ServerRMI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            /* concatenazione su stringa s */
            s += new String(baos.toByteArray(), StandardCharsets.UTF_8) + "\n";         
        }
        
        return s;
    }

    /**
     * Chiusura del Server e un-esportazione dell'oggetto corrente.
     * 
     * @throws RemoteException
     * @throws NotBoundException 
     */
    public void close() throws RemoteException, NotBoundException {
        r.unbind(SERVICE_NAME);
        UnicastRemoteObject.unexportObject(this, true);
    }
    
}
