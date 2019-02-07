
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * 
 * 
 * 
 */

/**
 * Esercizio lezione 9.
 * 
 * @author mc - Marco Costa - 545144
 */
public class MainClass {
    private static final int NO_CLIENT = 30;
    private static final int CLIENT_MAX_REQUEST = 9;
    
    /**
     * Main di test per l'esercizio. Esegue:
     *  - Avvio del server sulla sua porta predefinita
     *  - Avvio di NO_CLIENT client di richiesta
     *  - Ogni client esegue un numero random fino a CLIENT_MAX_REQUEST di 
     *      richieste di iscrizione al congresso CASUALI
     *  - Stampa a video del programma del congresso dopo l'esecuzione dei client
     *  - Terminazione controllata del server
     * 
     * @param args none
     */
    public static void main(String[] args) {
        try {
            ServerRMI s = new ServerRMI();
            
            ExecutorService clientEx = Executors.newFixedThreadPool(4);
            Set<Callable<Object>> clientList = new HashSet<>();

            for(int i = 0; i < NO_CLIENT; i++)
            {
                /* generazione dei client random di richiesta */
                clientList.add((Callable) new Callable() {
                    @Override
                    public Object call() throws Exception {
                        Random r = new Random(System.nanoTime());
                        int loopN = r.nextInt(CLIENT_MAX_REQUEST) + 1;
        
                        ClientRMI c = new ClientRMI();
                        for(int j = 0; j < loopN; j++)
                        {
                            int day = r.nextInt(ServerRMI.NO_DAYS) + 1;
                            int session = r.nextInt(ServerRMI.NO_SESSION) + 1;
                            /* registrazione alla sessione */
                            c.signToSession(NameGenerator.generateName(), day, session);
                        }
                        
                        return null;
                    }
                });
            }

            try {
                for(Future f : clientEx.invokeAll(clientList)) /* join sui client */
                    f.get();
            }
            catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
            }

            clientEx.shutdownNow(); /* terminazione dei client */
            try {
                clientEx.awaitTermination(1, TimeUnit.DAYS);
            }
            catch (InterruptedException ex) {
                Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);
            }

            /**
             * avvio di un nuovo client unicamente per la stampa del programma
             */
            try {
                ClientRMI c = new ClientRMI();
                c.printTimetable();
            }
            catch (RemoteException ex) {
                System.err.println("main:\t Errore: " + ex);
            }
            
            /* chiusura del server */
            s.close();
        }
        catch(RemoteException | NotBoundException ex) {
            System.err.println("main:\t Errore: " + ex);
        }
    }
}
