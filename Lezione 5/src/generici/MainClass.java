/*
 * 
 * 
 * 
 */
package generici;

import banca.Banca;
import banca.BancaV2;
import banca.ContoCorrente;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Esercizio lezione 5 + facoltativo.
 * 
 * Nota: L'unica modifica rispetto al precedente esercizio (oltre al main di test) è la classe
 *          BancaV2 la quale eredita il comportamento dalla classe Banca,
 *          e ne sovrascrive unicamente i metodi per il caricamento e il salvataggio.
 *          Infatti non è stato necessario fare altro che aggiungere la classe che
 *          implementa l'aggiornamento di funzionalità. Le due classi sono infatti
 *          completamente interscambiabili (e compatibili) a seconda che si voglia utilizzare il
 *          salvataggio serializzato con Java.IO o su JSON con Java.NIO
 * 
 * Nota2: sono necessari i seguenti .jar (già presenti nella current directory
 *       e importati nel progetto):
 *  - commons-io-2.6.jar
 *  - jackson-annotations-2.9.7.jar
 *  - jackson-core-2.9.7.jar
 *  - jackson-databind-2.9.7.jar
 * 
 * Nota3: al termine del programma si troveranno generati i seguenti file sulla home
 *          del progetto:
 *  - data.json
 *  - test1KB.json
 *  - test1KB.ser
 *  - test1MB.json
 *  - test1MB.ser
 *  - test10MB.json
 *  - test10MB.ser
 * 
 * @author mc - Marco Costa - 545144
 */
public class MainClass {
    /**
     * Metodo main di test per il programma, esegue (in due parti):
     * - test sulla correttezza delle operazioni di salvataggio e ripristino su JSON (lo stesso main del precedente esercizio)
     *      - apertura conti e aggiunta transazioni
     *      - calcolo e stampa delle statistiche
     *      - salvataggio dello stato della banca sul file "data.json" creato nella current path
     *      - aggiunta di un nuovo correntista e di altri movimenti
     *      - calcolo e stampa delle statistiche aggiornate
     *      - ripristino dello stato salvato
     *      - stampa delle statistiche e controllo dell'uguaglianza con quelle precedenti al salvataggio
     *      - aggiunta di un movimento al correntista creato dopo il salvataggio e verifica 
     *              che questo produca il lancio di un'eccezione 
     * - benchmark e comparativa con file seriali e JSON di 1KB, 1MB e 10MB (creati dal metodo)
     *      - contiene verifica della correttezza del ripristino dei dati salvati per le varie dimensioni
     * 
     * @param args 
     */
    public static void main(String[] args) {
        BancaV2 b = new BancaV2();
        
        /**********************************************************************
         *                           Test correttezza
         **********************************************************************/
        System.out.println("********************************** Test Correttezza **********************************");   
        ArrayList<ContoCorrente> conti = new ArrayList<>();
        
        ContoCorrente c1 = b.apriConto(new Persona("Mario", "Rossi"));
        ContoCorrente c2 = b.apriConto(new Persona("Michele", "Bianchi"));
        
        b.aggiungiTransazione("pagobancomat", c1);
        b.aggiungiTransazione("F24", c2);
        b.aggiungiTransazione("bonifico", c2);
        b.aggiungiTransazione("pagobancomat", c1);
        b.aggiungiTransazione("pagobancomat", c1);
        
        conti.add(c1);
        conti.add(c2);
        
        System.out.println("[++] Sono stati aperti i seguenti conti con relativi movimenti:");
        for(ContoCorrente c : conti)
            System.out.println(c);
        
        String statistiche1 = BancaV2.getGlobalStats();
        System.out.print("[++] Stampo le statistiche correnti: " + "\n" + statistiche1);
              
        File f = new File("./data.json");
        
        System.out.println("[++] Salvo lo stato attuale sul file " + f.getName());
        try {
            long startTime = System.currentTimeMillis();
            BancaV2.saveStateToFile(f);  
                System.out.println("[++] Salvataggio completato! Tempo impiegato: " + (System.currentTimeMillis() - startTime) + " ms, "
                        + "dimensione del file: " + f.length() + " Byte");   
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
            long startTime = System.currentTimeMillis();
            String statistiche2 = BancaV2.restoreStateFromFile(f);
            System.out.println("[++] Ripristino completato! Tempo impiegato: " + (System.currentTimeMillis() - startTime) + " ms");
            System.out.print(statistiche2);
            assert(statistiche1.equals(statistiche2));
            System.out.println("[++] Le statistiche corrispondono a quelle salvate!");
        } catch (ClassNotFoundException | IOException ex) {
            System.err.println(ex);
            System.exit(1);
        } catch(AssertionError ex) {
            throw new AssertionError("[!!] Errore! Le statistiche non corrispondono a quelle salvate!");
        }
        
        System.out.println("[++] Provo ad aggiungere una transazione ad un conto aperto dopo il salvataggio, dovrebe restituire un'eccezione!");
        try {
            b.aggiungiTransazione("f24", c3);
            System.err.println("[!!] Errore! Eccezione non catturata!");
            System.exit(1);
        } catch(IllegalArgumentException ex) {
            System.out.println("[++] Eccezione catturata: \n\t" + ex);
        }
        
        System.out.println("[++] Test di correttezza eseguito correttamente!");
             
        /**********************************************************************
         *                           Test con File
         **********************************************************************/
        String json_test = "", ser_test = "";
        
        int i;
        for(i = 0; i < 3; i++)
        {
            String dimension;
            
            if(i == 0)
                dimension = "1KB";
            else
            {
                if(i == 1)
                    dimension = "1MB";
                else
                {
                    dimension = "10MB";
                    i = 10;
                }     
                /**
                 * Circa 1MB di json con i = 1 e 400KB di file seriale
                 */
                for(int j = 0; j < (1250 * i); j++)
                {
                    c1 = b.apriConto(new Persona("Mario" + i, "Rossi" + j));
                    c2 = b.apriConto(new Persona("Michele" + i, "Bianchi" + j));
                    c3 = b.apriConto(new Persona("Ciccio" + i, "Barto" + j));

                    b.aggiungiTransazione("pagobancomat", c1);
                    b.aggiungiTransazione("F24", c2);
                    b.aggiungiTransazione("bonifico", c2);
                    b.aggiungiTransazione("pagobancomat", c1);
                    b.aggiungiTransazione("bonifico", c3);
                    b.aggiungiTransazione("bonifico", c3);
                }
                
                json_test = BancaV2.getGlobalStats();
            }    
                           
            System.out.printf("********************************** Test File %s **********************************\n", dimension);
            
            File test_ser = new File("./test" + dimension + ".ser");
            File test_json = new File("./test" + dimension + ".json");
            
            
            System.out.println("Genero il file " + test_json + " con dimensione approssimativa di " + dimension);
            System.out.println("[++] Salvo lo stato attuale sul file " + test_json + " utilizzando le NUOVE API");    
            try {
                long startTime = System.currentTimeMillis();
                BancaV2.saveStateToFile(test_json);
                System.out.println("[++] Salvataggio completato! Tempo impiegato: " + (System.currentTimeMillis() - startTime) + " ms, "
                        + "dimensione del file: " + ((test_json.length()/1000) + 1) + " KByte");    
            } catch (IOException ex) {
                System.err.println(ex);
                System.exit(1);
            }
            
            /**
             * generazione di ulteriori conti e transazioni in modo da allineare la 
             * dimensione del JSON con quella del file seriale, infatti un JSON
             * a parità di dimensione contiene molte meno informazioni!
             */
            for(int j = 0; j < (1850 * (i)); j++)
            {
                ContoCorrente c4 = b.apriConto(new Persona("Alberto" + j, "Bollo" + i));
                ContoCorrente c5 = b.apriConto(new Persona("Michele" + j, "Giallo" + i));
                ContoCorrente c6 = b.apriConto(new Persona("Giuseppe" + j, "Neri" + i));
                
                b.aggiungiTransazione("pagobancomat", c4);
                b.aggiungiTransazione("F24", c5);
                b.aggiungiTransazione("bonifico", c4);
                b.aggiungiTransazione("pagobancomat", c6);
                b.aggiungiTransazione("bonifico", c1);
                b.aggiungiTransazione("bonifico", c2);
            }
            
            ser_test = BancaV2.getGlobalStats();
            
            System.out.println("Genero il file " + test_ser + " con dimensione approssimativa di " + dimension);
            System.out.println("[++] Salvo lo stato attuale sul file " + test_ser + " utilizzando le VECCHIE API");    
            try {
                long startTime = System.currentTimeMillis();
                Banca.saveStateToFile(test_ser);
                System.out.println("[++] Salvataggio completato! Tempo impiegato: " + (System.currentTimeMillis() - startTime) + " ms, "
                        + "dimensione del file: " + ((test_ser.length()/1000) + 1) + " KByte");
            } catch (IOException ex) {
                System.err.println(ex);
                System.exit(1);
            }
            
            
            System.out.println("[++] Ripristino lo stato dal file " + test_ser + " utilizzando le VECCHIE API");    
            try {
                long startTime = System.currentTimeMillis();
                String stats = Banca.restoreStateFromFile(test_ser);
                System.out.println("[++] Ripristino completato! Tempo impiegato: " + (System.currentTimeMillis() - startTime) + " ms");
                assert(ser_test.equals(stats));
            } catch (ClassNotFoundException | IOException ex) {
                System.err.println(ex);
                System.exit(1);
            } catch(AssertionError ex) {
                throw new AssertionError("[!!] Errore! Le statistiche non corrispondono a quelle salvate!");
            }

            System.out.println("[++] Ripristino lo stato dal file " + test_json + " utilizzando le NUOVE API");    
            try {
                long startTime = System.currentTimeMillis();
                String stats = BancaV2.restoreStateFromFile(test_json);
                System.out.println("[++] Ripristino completato! Tempo impiegato: " + (System.currentTimeMillis() - startTime) + " ms");
                assert(json_test.equals(stats));
            } catch (ClassNotFoundException | IOException ex) {
                System.err.println(ex);
                System.exit(1);
            } catch(AssertionError ex) {
                throw new AssertionError("[!!] Errore! Le statistiche non corrispondono a quelle salvate!");
            }

            System.out.println("***********************************************************************************");
        }
        
        System.out.println("[++] Benchmark e verifica eseguita correttamente!");
    }
}
