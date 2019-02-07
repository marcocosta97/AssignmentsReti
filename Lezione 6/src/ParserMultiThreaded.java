
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 
 * 
 * 
 */

/**
 * Implementazione multithreaded della classe Parser
 * 
 * @author mc - Marco Costa - 545144
 */
public class ParserMultiThreaded extends Parser {
    /* regex che corrisponde ad un indirizzo IP situato all'inizio della stringa */
    private static final String regex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

    /**
     * Parsing multithreaded del file f
     * 
     * @param f il file
     * @return 
     * @throws IOException se non è possibile leggere o aprire il file o se è mal formattato
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    @Override
    public String parseFile(File f) throws IOException, ExecutionException, InterruptedException {
        String[] lines = parse(f); /* ottengo il file per righe su array */
        Pattern pattern = Pattern.compile(regex); /* compilo il regex */
        
        final ExecutorService e = Executors.newCachedThreadPool();
        final Set<Future<String>> callables = new HashSet<>();
        
        /**
         * per ogni riga del file creo un callable che si occuperà di sostituire
         * alla sua riga di competenza l'indirizzo con il suo hostname
         */
        for (int i = 0; i < lines.length; i++) {
            final int j = i;
            final String work = lines[i];
            
            Callable c = (Callable) () -> {
                Matcher matcher = pattern.matcher(work);
                
                if(matcher.find())
                    lines[j] = work.replaceFirst(regex, Inet4Address.getByName(matcher.group()).getHostName());
                else /* se non trovo corrispondenza allora il file è mal formattato! */
                    throw new IOException("[!!] Errore di parsing! File mal formattato!");
                
                return null;
            };
            
            callables.add(e.submit(c)); /* invoco il callable e salvo il Future in un set */
        }

        try {
            for(Future o : callables)
                o.get(); /* join sui task */
        } catch (ExecutionException | InterruptedException ex) {
            Logger.getGlobal().log(Level.SEVERE, "eccezione inaspettata: ", ex);
            throw ex;
        }
        
        e.shutdown();
        
        return String.join("\n", lines); /* restituisco l'array su un'unica stringa divisa per righe */
    }
    
}
