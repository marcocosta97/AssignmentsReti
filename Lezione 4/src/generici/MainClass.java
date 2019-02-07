/*
 * 
 * 
 * 
 */
package generici;

import banca.Banca;
import banca.ContoCorrente;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Esercizio lezione 4
 * 
 * @author mc - Marco Costa - 545144
 */
public class MainClass {
    /**
     * Metodo main di test per il programma, esegue (brevemente):
     * - apertura conti e aggiunta transazioni
     * - calcolo e stampa delle statistiche
     * - salvataggio dello stato della banca sul file "data.dat" creato nella current path
     * - aggiunta di un nuovo correntista e di altri movimenti
     * - calcolo e stampa delle statistiche aggiornate
     * - ripristino dello stato salvato
     * - stampa delle statistiche e controllo dell'uguaglianza con quelle precedenti al salvataggio
     * - aggiunta di un movimento al correntista creato dopo il salvataggio e verifica 
     *      che questo produca il lancio di un'eccezione 
     * 
     * @param args 
     */
    public static void main(String[] args) {
        Banca b = new Banca();
        ArrayList<ContoCorrente> conti = new ArrayList<>();
        
        ContoCorrente c1 = b.apriConto(new Persona("Mario", "Rossi"));
        ContoCorrente c2 = b.apriConto(new Persona("Michele", "Bianchi"));
        
        b.aggiungiTransazione("pagobancomat", c1);
        b.aggiungiTransazione("F24", c2);
        b.aggiungiTransazione("bonifico", c2);
        b.aggiungiTransazione("pagobancomat", c1);
        
        conti.add(c1);
        conti.add(c2);
        
        System.out.println("[++] Sono stati aperti i seguenti conti con relativi movimenti:");
        for(ContoCorrente c : conti)
            System.out.println(c);
        
        String statistiche1 = Banca.getGlobalStats();
        System.out.print("[++] Stampo le statistiche correnti: " + "\n" + statistiche1);
              
        File f = new File("./data.dat");
        
        System.out.println("[++] Salvo lo stato attuale sul file " + f.getName());
        try {
            Banca.saveStateToFile(f);
            System.out.println("[++] Salvataggio completato!");
        } catch (IOException ex) {
            System.err.println(ex);
        }
        
        ContoCorrente c3 = b.apriConto(new Persona("Simone", "Verdi"));
        conti.add(c3);
        
        b.aggiungiTransazione("accredito", c3);
        b.aggiungiTransazione("accredito", c3);
        b.aggiungiTransazione("bollettino", c1);
        
        System.out.println("[++] Sono stati aperti nuovi conti e aggiornati i vecchi nel seguente modo: ");
        for(ContoCorrente c : conti)
            System.out.println(c);
        
        System.out.print("[++] Stampo le statistiche correnti: " + "\n" + Banca.getGlobalStats());
        
        try {
            System.out.println("[++] Carico il salvataggio e stampo le statistiche: ");
            String statistiche2 = Banca.restoreStateFromFile(f);
            System.out.print(statistiche2);
            assert(statistiche1.equals(statistiche2));
            System.out.println("[++] Le statistiche corrispondono a quelle salvate!");
        } catch (ClassNotFoundException | IOException ex) {
            System.err.println(ex);
        } catch(AssertionError ex) {
            throw new AssertionError("[!!] Errore! Le statistiche non corrispondono a quelle salvate!");
        }
        
        System.out.println("[++] Provo ad aggiungere una transazione ad un conto aperto dopo il salvataggio, dovrebe restituire un'eccezione!");
        try {
            b.aggiungiTransazione("f24", c3);
            System.err.println("[!!] Errore! Eccezione non catturata!");
        } catch(IllegalArgumentException ex) {
            System.out.println("[++] Eccezione catturata: \n\t" + ex);
        }
        
        System.out.println("[++] Programma di test eseguito correttamente!");
        
    }
}
