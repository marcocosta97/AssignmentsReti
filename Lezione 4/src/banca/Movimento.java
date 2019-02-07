/*
 * 
 * 
 * 
 */
package banca;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * Classe che rappresenta l'oggetto Movimento, nella cui definizione è contenuto
 * l'elenco delle possibili causali.
 * 
 * Nota: è presente staticamente anche l'array delle statistiche e i metodi
 *      thread-safe per potervi accedere e modificarlo
 *      La scelta di posizionarlo qui è dovuta al fatto che la seguente implementazione
 *      lo rende automaticamente compatibile all'aggiunta e la rimozione di Causali
 * 
 * @author mc - Marco Costa - 545144
 */
 public class Movimento implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final transient Integer[] statsArray;
    
    private final Causali causale;
    private final Date timestamp;
    
    
    /**
     * inizializzazione dell'array
     */
    static {
        statsArray = new Integer[Causali.values().length];
        Arrays.fill(statsArray, 0);
    }
    /**
     * enumeratore delle causali, ad ogni causale è assegnato un numero progressivo
     * che funge da indice dell'array di statistiche per quella causale
     */
    private static enum Causali {
        Bonifico(0),
        Accredito(1),
        Bollettino(2),
        F24(3),
        PagoBancomat(4);
        /* aggiungere qui nuove causali */
        
        private final transient int value;
        private Causali(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }    
    };

    /**
     * conversione di una stringa in una causale, la stringa può essere case insensitive
     * 
     * @param s stringa
     * @return la nuova causale
     * @throws IllegalArgumentException se non vi è alcuna causale corrispondente
     */
    private static Causali StringToCausale(String s) throws IllegalArgumentException {
        for(Causali c : Causali.values())
            if(s.equalsIgnoreCase(c.name()))
                return c;

        throw new IllegalArgumentException("La causale specificata è sconosciuta");
    }

    /**
     * Creazione di un nuovo movimento.
     * Nota: non è presente come parametro la Data in quanto non è ritenuto possibile
     *       aggiungere movimenti pre e post datati
     * @param causale la stringa con la causale
     */
    protected Movimento(String causale) {
        this.causale = StringToCausale(causale);
        timestamp = new Date(); 
    }
    
    /**
     * Aggiornamento delle statistiche in base alla causale del Movimento corrente
     * Nota: il metodo è thread-safe
     */
    protected void updateStats() {
        synchronized(statsArray) {
            statsArray[this.causale.getValue()]++;
        }
    }
    
    /**
     * Ripristino delle statistiche
     * Nota: il metodo è thread-safe
     */
    protected static void restoreStats() {
        synchronized(statsArray) {
            Arrays.fill(statsArray, 0);
        }
    }
    
    /**
     * Restituzione di una stringa contenente le statistiche correnti
     * Nota: il metodo è thread-safe
     * 
     * @return la stringa
     */
    protected static String getPrintableStats() {
        String s = "";
        
        synchronized(statsArray) {
            for(Causali c : Causali.values())
                s += c.name() + ": " + statsArray[c.getValue()] + "\n";
        }
        
        return s;
    }
    
    @Override
    public String toString() {
        return "Movimento{" + "causale=" + causale.name() + ", timestamp=" + timestamp + '}';
    }

}
