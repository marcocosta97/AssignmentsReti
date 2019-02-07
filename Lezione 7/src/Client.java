/*
 * 
 * 
 * 
 */


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client per la richiesta di un file al Server FTP.
 * Sono permesse richieste multiple
 * 
 * @author mc - Marco Costa - 545144
 */
public class Client {
    private final SocketChannel socket;
    
    /**
     * Connessione verso il server all'indirizzo Server.SERVER_ADDRESS sulla porta 
     * Server.SERVER_PORT.
     * 
     * @see Server
     * @throws IOException se non è possibile collegarsi al server
     */
    public Client() throws IOException {
        try {
            socket = SocketChannel.open(new InetSocketAddress(Server.SERVER_ADDRESS, Server.SERVER_PORT));
            socket.finishConnect();
            if(!socket.isConnected())
                throw new IOException("non connesso");       
        } catch (IOException ex) {
            throw new IOException("Impossibile stabilire una connessione col server: " + ex);
        }
    }
    
    /**
     * Metodo per la richiesta del file "filename" al server da salvare nella
     * cartella "destination".
     * Nota: sono permesse richieste multiple 
     * 
     * @param filename il nome del file
     * @param destination la cartella di destinazione
     * @return il nome del file salvato (potrebbe essere diverso in caso di 
     *          file già presenti con lo stesso nome)
     * @throws FileNotFoundException se il server ha risposto con un codice di
     *                               errore, non è stato quindi salvato nessun
     *                               file
     */
    public String requestFile(String filename, String destination) throws FileNotFoundException {
        /* controlli vari di verifica su stringhe e cartelle */
        if((filename == null) || (filename.isEmpty()) 
                || (destination == null) || (destination.isEmpty())) 
            throw new IllegalArgumentException("Errore! Non sono ammessi parametri nulli");
        
        File destDirectory = new File(destination);
        if(!(destDirectory.exists()) || !(destDirectory.canWrite()) || !(destDirectory.isDirectory()))
            throw new IllegalArgumentException("Errore! La cartella di destinazione non è valida");
        
        
        /* wrappo il nome del file in un ByteBuffer */
        ByteBuffer b = ByteBuffer.wrap(filename.getBytes());
        
        try {
            /* invio del filename al server */
            while(b.hasRemaining())
                socket.write(b);
            
            /* allocazione del buffer che conterrà la dimensione del file */
            ByteBuffer fileSizeBuffer = ByteBuffer.allocate(Server.BUFFER_DIMENSION_SIZE);
            
            socket.read(fileSizeBuffer); /* lettura */
            fileSizeBuffer.flip();
            int fileSize = fileSizeBuffer.getInt();
            
            ByteBuffer fileBuffer;
            if(fileSize <= 0) /* errore del server, leggo una stringa di errore */
            {
                fileBuffer = ByteBuffer.allocate(Server.BUFFER_ERROR_SIZE);
                socket.read(fileBuffer);
                System.err.println("Client: " + new String(fileBuffer.array()).trim());
            }
            else
            {          
                fileBuffer = ByteBuffer.allocate(fileSize);
            
                ByteArrayOutputStream file = new ByteArrayOutputStream();

                int readBytes = 0;
                while(readBytes != fileSize)
                {
                    int currRead = socket.read(fileBuffer);
                    file.write(fileBuffer.array(), readBytes, currRead);
                    if(!fileBuffer.hasRemaining())
                        fileBuffer.clear();

                    readBytes += currRead;
                }
                
                File f = new File(destDirectory, filename);
                
                /**
                 * se è già presente nella destinazione un file con lo stesso
                 * nome ne creo un altro con un nome univoco
                 */
                int increase = 1;
                while(f.exists())
                {
                    f = new File(destDirectory, increase + "_" + filename);
                    increase++;
                }
                
                /**
                 * salvataggio dei byte su file
                 */
                try(FileChannel fileChannel = new RandomAccessFile(f, "rw").getChannel();)
                {    
                    fileBuffer = ByteBuffer.wrap(file.toByteArray());
                    while(fileBuffer.hasRemaining())
                        fileChannel.write(fileBuffer);
                }
                
                return f.getName();
            }   
        } catch(IOException ex) {
            throw new FileNotFoundException("Errore! File non ricevuto" + ex);
        }
        
        return null;
    }
    
    /**
     * Chiusura della connessione con il Server
     */
    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
