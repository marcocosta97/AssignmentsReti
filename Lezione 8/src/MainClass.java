
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * 
 * 
 * 
 */

/**
 * Esercizio lezione 8
 * 
 * @author mc - Marco Costa - 545144
 */
public class MainClass {
    /**
     * Semplice main di test:
     *  - Avvio di un nuovo PingServer sulla porta 1112
     *  - Avvio di 20 client paralleli che eseguono il ping al server
     *  - Terminazione controllata del server 
     * @param args 
     */
    public static void main(String[] args) {
        int port = 1112;
        try {
            PingServer s = new PingServer(port);
            ExecutorService e = Executors.newSingleThreadExecutor();
            
            e.submit(s);
            
            ExecutorService client = Executors.newFixedThreadPool(8);
            for(int i = 0; i < 20; i++)
            {
                Runnable r = () -> {
                    PingClient c = new PingClient();
                    try {
                        System.out.println(c.ping("localhost", port));
                    }
                    catch (IOException ex) {
                        System.err.println("client: Errore! " + ex);
                    }
                };
                client.submit(r);
            }
            
            client.shutdown();
            client.awaitTermination(1, TimeUnit.DAYS);
            s.close();
            e.shutdown();
            try {
                e.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                System.err.println("main: \t[!!] Errore! Il server non si è chiuso correttamente");
            }
        }
        catch (IOException | InterruptedException ex) {
            System.err.println("main: \t[!!] Errore! " + ex);
        }
    }
}
