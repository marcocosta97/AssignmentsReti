
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/*
 * 
 * 
 * 
 */

/**
 * Esercizio lezione 6
 * 
 * input: logFile
 * @author mc - Marco Costa - 545144
 */
public class MainClass {
    /**
     * Main di test dell'esercizio, viene eseguita la risoluzione degli indirizzi
     * prima in modalità single thread e poi multi thread stampando i rispettivi
     * tempi di esecuzione
     * Dopodiché viene stampato il file passato come input con gli hostname sostituiti
     * agli indirizzi
     * 
     * REQUIRES: args[0] deve essere un file di testo
     * @param args
     * @throws IOException se non è possibile aprire o leggere il file
     */
    public static void main(String[] args) throws IOException {
        if(args.length != 1)
            throw new IllegalArgumentException("[!!] Errore! Il programma prende in input un parametro!");
        
        File f = new File(args[0]);      
        if(!(f.exists() && f.isFile() && f.canRead()))
            throw new IOException("[!!] Errore! Impossibile aprire in lettura il file specificato!");
        
        System.out.println("[++] File " + f + " aperto correttamente!");
              
        DateFormat formatter = new SimpleDateFormat("m 'minuti e' ss 'secondi'");
        formatter.setTimeZone(TimeZone.getDefault());
        
        String output = "";
        System.out.println("[++] Avvio la traduzione degli indirizzi in modalità Single Threaded");
        try {
            long startTime = System.currentTimeMillis();
            new ParserSingleThreaded().parseFile(f);
            System.out.println("[++] Traduzione effettuata in " + formatter.format(new Date(System.currentTimeMillis() - startTime)));
        } catch(IOException ex) {
            System.err.println("[!!] Errore!" + ex);
            System.exit(1);
        }
        
        System.out.println("[++] Avvio la traduzione degli indirizzi in modalità Multi Threaded");
        try {
            long startTime = System.currentTimeMillis();
            output = new ParserMultiThreaded().parseFile(f);
            System.out.println("[++] Traduzione effettuata in " + formatter.format(new Date(System.currentTimeMillis() - startTime)));
        } catch(IOException | ExecutionException | InterruptedException ex) {
            System.err.println("[!!] Errore!" + ex);
            System.exit(1);
        }
        
        
        System.out.println("[++] Benchmark effettuato correttamente, stampo l'output e termino il programma");
        System.out.println(output);
    }
}
