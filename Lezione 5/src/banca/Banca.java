package banca;

import generici.Persona;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * 
 * 
 * 
 */

/**
 * La classe Banca permette la dichiarazione di più oggetti di tipo Banca 
 * (da intendersi come la filiale di Roma, Milano, ecc tutte parte dell'oggetto
 * statico Banca)
 * tutti gli oggetti possono accedere ai conticorrenti
 * 
 * nota: l'implementazione non è concorrente verso gli oggetti Banca
 * @author mc - Marco Costa - 545144
 */
public class Banca {
    private static HashSet<ContoCorrente> listaConti = new HashSet<>();
    
    public Banca() {
    }
    
    /**
     * ripristino e ricalcolo delle statistiche globali 
     * (effettuato successivamente al ripristino dello stato da file)
     */
    protected static void restoreStats() {
        Movimento.restoreStats();
    
        /**
         * inizializzazione della thread pool di tipo cached e avvio dei task
         */
        final ExecutorService e = Executors.newCachedThreadPool();
        final Set<Callable<Object>> callables = new HashSet<>();
        
        /**
         * ad ogni Callable viene assegnato un conto corrente
         */
        for(final ContoCorrente c : listaConti)
            callables.add((Callable) () -> { /* classe anonima :-) */
                for(Movimento m : c)
                {
                    if(m != null)
                        m.updateStats(); /* ripristino statistiche */
                }
                return null;
        });
        
        /**
         * invocazione di tutti i callable
         */
        try {
            for(Future o : e.invokeAll(callables))
                o.get(); /* join sui task */
        } catch (ExecutionException | InterruptedException ex) {
            Logger.getGlobal().log(Level.SEVERE, "eccezione inaspettata: ", ex);
        }
        
        e.shutdown();
    }
    
    
    /**
     * apertura di un conto da parte della persona p
     * nota: sono permessi conti multipli per una singola persona
     * 
     * @param p la persona
     * @return il conto corrente aperto
     */
    public ContoCorrente apriConto(Persona p) {
        ContoCorrente c = new ContoCorrente(p);
        
        listaConti.add(c);
        
        return c;
    }
    
    /**
     * aggiunta di una nuova transazione sul conto "c" con causale "causale"
     * Nota: la ridondanza con il metodo "addMovimento" della classe ContoCorrente
     *       è dovuta dalla conseguenza logica di dover contattare prima la banca
     *       per poter registrare il movimento 
     * 
     * @param causale la stringa contenente la causale (case insensitive)
     * @param c il conto
     * @throws IllegalArgumentException se la causale non esiste (si veda la 
     *                                  classe Movimento per la lista delle 
     *                                  causali permesse) o se il conto non è
     *                                  registrato alla banca
     */
    public void aggiungiTransazione(String causale, ContoCorrente c) throws IllegalArgumentException {
        if(!(listaConti.contains(c)))
            throw new IllegalArgumentException("errore: conto sconosciuto!");
        
        try {
            /**
             * aggiungo un nuovo movimento e aggiorno le statistiche globali
             */
            c.addMovimento(causale).updateStats();      
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException("La causale: " + causale + " non è permessa!");
        } 
    }
    
    /**
     * ripristina lo stato GLOBALE della banca da file ed effettua il calcolo
     * delle statistiche restituendo una stringa di statistiche formattata
     * 
     * @param f il file da cui effettuare il ripristino
     * @return una stringa formattata con le statistiche
     * @throws IOException se il file non esiste
     * @throws ClassNotFoundException
     */
    public static String restoreStateFromFile(File f) throws IOException, ClassNotFoundException{
        try
            (
                ObjectInputStream is = new ObjectInputStream(new FileInputStream(f));
            )
        {
            HashSet<ContoCorrente> t = (HashSet<ContoCorrente>)is.readObject();
            if(t == null)
                throw new ClassNotFoundException();
            listaConti = t;
        }
        catch (IOException ex) {
            throw new IOException("impossibile leggere dal file " + f + "\n" + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            throw new ClassNotFoundException("il file è invalido!");
        }
        
        /* ripristino le statistiche delle banche */
        restoreStats();
        return getGlobalStats();
    }
    
    /**
     * salva lo stato GLOBALE della banca sul file f
     * 
     * @param f il file su cui salvare
     * @throws IOException se vi è stato un errore di I/O sul file 
     */
    public static void saveStateToFile(File f) throws IOException {
        try 
        ( 
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
        )
        {
            os.writeObject(listaConti);
            os.flush();
        } catch (IOException ex) {
            throw new IOException("impossibile scrivere sul file " + f + "\n" + ex.getMessage());
        } 
        
    }
    
    /**
     * restituisce la stringa contenente le statistiche globali dei movimenti
     * globali
     * 
     * @return la stringa
     */
    public static String getGlobalStats() {
        return Movimento.getPrintableStats();
    }

    /**
     * restituisce una copia della rappresentazione della classe
     * 
     * @return 
     */
    protected static HashSet<ContoCorrente> getListaConti() {
        return new HashSet<>(listaConti);
    }

    /**
     * imposta una nuova rappresentazione della classe
     * 
     * @param listaConti la rappresentazione
     */
    protected static void setListaConti(HashSet<ContoCorrente> listaConti) {
        Banca.listaConti = listaConti;
    }
    
    
}
