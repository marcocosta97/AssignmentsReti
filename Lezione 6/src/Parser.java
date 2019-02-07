
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/*
 * 
 * 
 * 
 */

/**
 * Classe astratta che definisce l'interfaccia astratta per il parsing e 
 * definisce un metodo univoco per leggere dal file e restituire un array di 
 * stringhe contenente le linee del file
 * 
 * @author mc - Marco Costa - 545144
 */
public abstract class Parser {
    
    /**
     * Restituisce le righe del file "f" in un array di stringhe
     * 
     * @param f il file
     * @return array di stringhe
     * @throws IOException se non è possibile aprire o parsare il file
     */
    protected static String[] parse(File f) throws IOException {
        if(!(f.exists() && f.isFile() && f.canRead()))
            throw new IOException("[!!] Errore! Impossibile aprire in lettura il file specificato!");
        
        ArrayList<String> a = new ArrayList<>();

        try (final BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line = "";            
            while ((line = reader.readLine()) != null) {
                a.add(line);
            }      
        }
        
        if (a.isEmpty()) {
            throw new IOException("Impossibile parsare il file!");
        }
        
        return a.toArray(new String[a.size()]);
    }
    
    /**
     * Interfaccia astratta per il metodo di parsing
     * 
     * @param f il file da cui parsare
     * @return il documento con gli indirizzi sostituiti dagli hostname
     * @throws Exception 
     */
    public abstract String parseFile(File f) throws Exception;
}
