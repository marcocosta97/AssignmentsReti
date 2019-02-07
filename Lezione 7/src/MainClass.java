/*
 * 
 * 
 * 
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esercizio lezione 7
 * 
 * @author mc - Marco Costa - 545144
 */
public class MainClass {
    /**
     * Il seguente main di test richiede al Server i file presenti nella main directory 
     * del progetto (build.xml e manifest.mf) e, dopo averli inviati al client,
     * quest'ultimo li salva nella cartella (la crea se non esiste) "output" del progetto; 
     * dopodiché viene confrontato il  file "manifest.mf" presente nella directory del 
     * Server e quello salvato dal client nella cartella "output".
     * 
     * Nota: è possibile cambiare la directory remota del server, la directory 
     *       locale dei client e il file sul quale eseguire la comparazione
     *       modificando rispettivamente le variabili "serverDir", "clientDir" e
     *       "fileExample" sottostanti.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        String serverDir = ".";
        String clientDir = "./output";
        String fileExample = "manifest.mf";
        String fileExampleOut = fileExample;
        
        File dir = new File(clientDir);
        if(!dir.exists())
            dir.mkdir();
        
        try {
            /* creazione ed esecuzione del Server */
            Server s;
            
            s = new Server(serverDir);
            ExecutorService e = Executors.newSingleThreadExecutor();
            e.submit(s);
            
            try {
                Client c = new Client();
                fileExampleOut = c.requestFile(fileExample, clientDir);
                c.requestFile("build.xml", clientDir);
                Client d = new Client();
                d.requestFile("build.xml", clientDir);
                System.out.println("main: \t[++] Richiedo un file non presente");
                d.requestFile("filenonpresente.txt", clientDir); /* file non presente */
                c.closeConnection();
                d.closeConnection();
            } catch(IllegalArgumentException | IOException ex) {
                System.err.println("Client: [!!] " + ex);
            }
            
            /* terminazione controllata del Server */
            s.close();
            e.shutdown();
            try {
                e.awaitTermination(1, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                System.err.println("main: \t[!!] Errore! Il server non si è chiuso correttamente");
            }
        } catch(IOException ex) {
            Logger.getLogger(MainClass.class.getName()).log(Level.SEVERE, null, ex);   
        }
        
        try {
            /**
             * controllo che il file "fileExample" ricevuto sia uguale a quello del
             * server
             */
            File f1 = new File(serverDir, fileExample);
            File f2 = new File(clientDir, fileExampleOut);
            byte[] a1 = Files.readAllBytes(f1.toPath());
            byte[] a2 = Files.readAllBytes(f2.toPath());
            
            System.out.println("main: \t[++] Controllo che i due file " + f1 + " e " + f2 + " coincidano");
            
            if(Arrays.equals(a1, a2))
                System.out.println("\t[++] I due file " + f1 + " e " + f2 + " coincidono!");
            else
                System.err.println("\t[!!] I due file " + f1 + " e " + f2 + " non coincidono!");
        }
        catch (IOException ex) {
            System.err.println("main: \t[!!] Errore! " + ex);
        }

    }
}
