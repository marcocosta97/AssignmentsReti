
import java.io.IOException;
import java.util.HashSet;
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
 * Esercizio lezione 10
 * 
 * @author mc - Marco Costa - 545144
 */
public class MainClass {
    private static final String DEFAULT_IP = "226.226.226.226";
    private static final int NO_CLIENT = 4;
    
    /**
     * Main di test. Esegue:
     *  - Esecuzione del server con indirizzo multicast DEFAULT_IP
     *  - Esecuzione contemporanea di NO_CLIENT client in ascolto sul server
     *  - Terminazione controllata del server
     * 
     * @param args
     * @throws ExecutionException 
     */
    public static void main(String[] args) throws ExecutionException {
        try {
            ExecutorService e = Executors.newSingleThreadExecutor();
            ServerMulticast s = new ServerMulticast(DEFAULT_IP);
            e.submit(s);
            
            Set<Callable<Object>> futureList = new HashSet<>();
            ExecutorService ex = Executors.newCachedThreadPool();
            
            for(int i = 0; i < NO_CLIENT; i++)
            {
                Callable c = new Callable() {
                    @Override
                    public Object call() throws Exception {
                        ClientMulticast c = new ClientMulticast();
                        c.listen(DEFAULT_IP);

                        return null;
                    }
                };
                
                futureList.add(c);
            }
            
            try {
                for(Future f : ex.invokeAll(futureList)) /* wait sui client */
                    f.get();
            }
            catch (InterruptedException ex1) {
                Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex1);
            }
            
            /* terminazione esecutore client */
            ex.shutdown();
            ex.awaitTermination(1, TimeUnit.MINUTES);
            
            /* terminazione server */
            s.closeServer();
            e.shutdown();
            e.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch (IOException | InterruptedException ex) {
            System.err.println("main:\tErrore! " + ex);
        }

    }
}
