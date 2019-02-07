/*
 * 
 * 
 * 
 */
package banca;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import org.apache.commons.io.FilenameUtils;

/**
 * Estensione della classe Banca dell'esercizio precedente che definisce l'overriding dei metodi 
 * per la gestione  del backup per poter utilizzare JSON e le librerie NIO
 * 
 * Nota: la classe utilizza le strutture dati e i metodi (eccetto quelli di ripristino e salvataggio)
 *       della classe superiore, (dichiarare un nuovo conto da un oggetto di tipo
 *      Banca o da un oggetto di tipo BancaV2 produce lo stesso comportamento [e finisce nella 
 *      stessa struttura dati!])
 * 
 * @see banca.Banca
 * @author mc - Marco Costa - 545144
 */
public class BancaV2 extends Banca {
    /* formato del timestamp su JSON */
    private static final DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private static final int MAX_BUFFER_SIZE = 20000000; // 20 MB
    
    private static final ObjectMapper om; /* oggetto della libreria Jackson per permettere il mapping tra oggetti e JSON */
    
    /**
     * costruttore statico
     */
    static {
        om = new ObjectMapper();
        om.setDateFormat(df); /* assegno al mapper il formato del timestamp scelto */
    }
    
    /**
     * Costruttore della classe
     */
    public BancaV2() {
        super();
    }
    
    /**
     * Metodo per verificare che la stringa contenente il filename abbia
     * estensione ".json"
     * 
     * @param f il filename
     * @throws IOException se il file non ha estensione JSON
     */
    private static void checkJSONExtension(String f) throws IOException {
        if(!(FilenameUtils.getExtension(f).equals("json")))
            throw new IOException("Errore: " + f + " non è un file json!");
    }
    
    /**
     * Ripristino del backup da file JSON e ricalcolo delle statistiche, mediante librerie NIO
     * e restituzione delle statistiche successive al ripristino
     * REQUIRES: f deve avere charset "UTF-8"
     * 
     * @param file il file da cui ripristinare
     * @return le statistiche dopo il ripristino
     * @throws IOException se non è possibile aprire o parsare il file f o se il file
     *                      non ha estensione .json
     * @throws ClassNotFoundException se il file f possiede errori di formattazione
     */
    public static String restoreStateFromFile(File file) throws IOException, ClassNotFoundException{
        checkJSONExtension(file.getName());
        
        String fileString;
        
        /** se la dimensione del file supera MAX_BUFFER_SIZE imposto un buffer di
         *    dimensione predefinita MAX_BUFFER_SIZE
         */
        int buffer_size = (int) file.length();
        if(buffer_size > MAX_BUFFER_SIZE)
                buffer_size = MAX_BUFFER_SIZE;
        
        try (FileChannel inChannel = new RandomAccessFile(file, "r").getChannel()) {           
            ByteBuffer buffer = ByteBuffer.allocate(buffer_size);
            Charset charset = Charset.forName("UTF-8");
            
            fileString = "";
            
            /**
             * lettura da file e scrittura su "fileString"
             */
            while(inChannel.read(buffer) != -1)
            {
                buffer.flip();
                fileString += charset.decode(buffer).toString();
                buffer.clear();
            }     
        }
        
        /**
         * parsing della stringa mediante libreria JACKSON
         */
        try {
            HashSet<ContoCorrente> temp = om.readValue(fileString, new TypeReference<HashSet<ContoCorrente>>() {});
            if(temp == null)
                throw new ClassNotFoundException("Errore: impossibile parsare il file!");
            Banca.setListaConti(temp);
        } catch(JsonMappingException | JsonParseException ex) {
            throw new IOException("Errore: impossibile parsare il file, " + ex);
        }
        
        /**
         * ricalcolo delle statistiche
         */
        Banca.restoreStats();
        return Banca.getGlobalStats();
    } 
    
    /**
     * Salvataggio dello stato attuale su File JSON f mediante lirerie NIO
     * 
     * @param f il file 
     * @throws IOException se non è possibile aprire o parsare il file f o se il file
     *                      non ha estensione .json
     */
    public static void saveStateToFile(File f) throws IOException {
        checkJSONExtension(f.getName());
        
        /**
         * per sicurezza elimino e ricreo il file con lo stesso nome
         */
        f.delete();
        f.createNewFile();
        
        try (FileChannel outChannel = new RandomAccessFile(f, "rw").getChannel()) {
            ObjectWriter writer = om.writer().withDefaultPrettyPrinter();
            String outputString = "";
            
            try {
                /**
                 * stampa dello stato attuale della Banca su stringa formattata in JSON
                 */
                outputString = writer.writeValueAsString(getListaConti());
            } catch(JsonGenerationException ex) {
                throw new IOException("Errore: impossibile generare il file, " + ex.getLocalizedMessage());
            }
            
            ByteBuffer buffer = ByteBuffer.allocate(outputString.getBytes().length);
            /**
             * inserimento stringa su buffer
             */
            buffer.put(outputString.getBytes());
            buffer.flip();
            /**
             * inserimento buffer su file
             */
            while(buffer.hasRemaining())
                outChannel.write(buffer);
            
        }
    
    }
}
