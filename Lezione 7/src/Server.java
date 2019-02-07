/*
 * 
 * 
 * 
 */

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe Server operante sulla porta 41.
 * Utilizza JAVA nio e Select.
 * Dimensione massima file inviabile -> MAX_FILE_SIZE
 * 
 * @author mc - Marco Costa - 545144
 */
public class Server implements Runnable {
    /* pubblico */
    public static final String SERVER_ADDRESS = "localhost";
    public static final int SERVER_PORT = 41;
    public static final int BUFFER_DIMENSION_SIZE = Integer.BYTES;
    public static final int BUFFER_ERROR_SIZE = 256; /* lunghezza massima stringa di errore*/
    public static final int ERROR_FILE_NOT_FOUND = -1; /* codice errore file non trovato */
    
    /* privato */
    private static final int MAX_FILE_SIZE = 50 * 1000 * 1000; // 50 MB
    private final int MAX_STRING_SIZE; /* lunghezza massima del filename in lettura */
    
    private ServerSocketChannel acceptSock;
    private final File ftpDir;
    private final ArrayList<File> fileList;
    
    private Boolean isInterrupted = false;
    private Boolean isRunning = false;
    
    private Selector s;
    
    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());
    
    /**
     * Crea una nuova istanza della classe Server operante sulla porta 41.
     * 
     * @param directory la directory contenente i file inviabili dal server
     *                  Nota: le cartelle contenute all'interno verranno escluse
     * 
     * @throws IOException se vi è un errore con l'apertura della directory e la 
     *                     lettura dei relativi file
     * @throws SocketException se vi è un errore con l'apertura della Socket
     */
    public Server(String directory) throws IOException, SocketException {
        /* controlli di verifica sulla directory */
        ftpDir = new File(directory);
        if(!ftpDir.isDirectory())
            throw new IOException("[!!] Errore! " + directory + " non è una directory");
        fileList = new ArrayList<>(Arrays.asList(ftpDir.listFiles()));
        
        if(fileList.size() <= 0)
            throw new IOException("[!!] Errore! non ci sono file nella directory");
        
        /**
         * escludo tutte le cartelle, file non leggibili, file che superano la 
         * dimensione massima "MAX_FILE_SIZE" presenti all'interno della directory
         * e imposto la dimensione della massima stringa "filename" ricevibile
         * dal server come il nome del file più lungo presente
         */
        Iterator<File> it = fileList.iterator();
        int temp = -1;
        while(it.hasNext())
        {
            File f = it.next();
            String name = f.getName();
            
            if((f.exists()) && (f.isFile()) && (f.canRead()) && (f.length() < MAX_FILE_SIZE))
            {
                int length = name.length();
                if(length > temp)
                    temp = length;
            }
            else /* il file non rispetta i requisiti, lo rimuovo dall'elenco */
                it.remove();
        }
        MAX_STRING_SIZE = temp + 1;
        
        if(fileList.size() <= 0)
            throw new IOException("[!!] Errore! non ci sono file nella directory");
        
        /**
         * apertura della socket, associazione dell'indirizzo e della porta
         * e configurazione non bloccante
         */
        try {
            acceptSock = ServerSocketChannel.open();
            acceptSock.socket().bind(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
            acceptSock.configureBlocking(false);
        } catch(IOException ex) {
            throw new SocketException("[!!] Errore! Impossibile aprire la socket: " + ex);
        }
    } 
    
    /**
     * Metodo per restituire un vettore di stringhe contenente i nomi dei file
     * inviabili dal server.
     * 
     * @return il vettore di stringhe
     */
    private String[] getFileList() {
        String s[] = new String[fileList.size()];
        
        int i = 0;
        for(File k : fileList)
        {
            s[i] = k.getName();
            i++;
        }
        
        return s;
    }
    
    /**
     * Stampa del messaggio di benvenuto del Server.
     */
    private void printWelcomeMessage() {
        System.out.println("Server: \n\t[++] Il server è attivo sulla porta: " + SERVER_PORT);
        System.out.println("\t[++] E' possibile richiedere i seguenti file: ");
        for(String s : getFileList())
            System.out.println("\t\t" + s);
    }
    
    /**
     * Restituisce un ByteBuffer wrappato con il contenuto del file "filename"
     * se presente nella lista dei file inviabili.
     * 
     * @param filename il nome del file richiesto
     * 
     * @return il buffer già wrappato se "filename" appartiene alla
     *         lista dei file e non si verifica nessun errore a runtime,
     *         null altrimenti
     */
    private ByteBuffer getFileBuffer(String filename) {
        if(filename == null) return null;
        /* controllo che il file sia presente nella mia lista */
        for(File f : fileList)
        {
            String s = f.getName();
            
            if(filename.equals(s))
            {
                ByteBuffer fileBuffer = null;
                
                try {
                    fileBuffer = ByteBuffer.wrap(java.nio.file.Files.readAllBytes(f.toPath()));
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
                
                return fileBuffer;
            }
        }
        
        return null; /* file non presente */
    }
    
    /**
     * Chiusura delle Socket delle connessioni ancora aperte e chiusura della
     * Socket per l'accept
     */
    private void free() {
        Set<SelectionKey> keys = s.keys();
        Iterator<SelectionKey> it = keys.iterator();
        
        /* chiudo le connessioni con i client */
        while(it.hasNext())
        {
            SelectionKey k = it.next();
            if(k.isValid())
            {
                k.cancel();
                try {
                    k.channel().close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }   
        }
        /* chiudo la accept socket */
        try 
        {
            acceptSock.socket().close();
            acceptSock.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        System.out.println("[++] Il server è stato chiuso correttamente");
    }
    
    /**
     * Metodo da invocare per iniziare la routine di chiusura del Server.
     * 
     * Nota: il metodo deve essere invocato solo se il Server è già in esecuzione
     * Nota2: il Server non può più essere eseguito dopo aver chiamato questo metodo:
     *        creare un nuovo oggetto
     */
    public void close() {
        isInterrupted = true;
        s.wakeup();
    }
    
    /**
     * Routine del Server.
     */
    @Override
    public void run() {
        if(isRunning) throw new IllegalStateException("Errore! Il server è già in esecuzione!");
        
        printWelcomeMessage();
        
        try {
            s = Selector.open();
            
            /* registra l'operazione di Accept sulla acceptSock */
            acceptSock.register(s, acceptSock.validOps());
            isRunning = true;
            
            while(true) 
            {
                s.select();
                if(isInterrupted) /* passo al blocco finally */
                    return;
                
                Set<SelectionKey> keys = s.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                
                /* Loop sulle SelectedKeys */
                while(it.hasNext())
                {
                    SelectionKey k = it.next();
                    it.remove();
                    
                    try {
                        if(k.isAcceptable())
                        {   
                            SocketChannel client = acceptSock.accept();
                            
                            /* registro la nuova socket in SOLA LETTURA */
                            client.configureBlocking(false);
                            client.register(s, SelectionKey.OP_READ);

                            LOGGER.log(Level.INFO, "Connessione accettata: {0}",
                                    client.getLocalAddress());
                        }
                        /* invio di un filename di richiesta da parte del client */
                        else if(k.isReadable())
                        {
                            SocketChannel clientSock = (SocketChannel) k.channel();
                            ByteBuffer b = ByteBuffer.allocate(MAX_STRING_SIZE);
                            boolean limitExceeded = false;
                            
                            /* leggo al massimo MAX_STRING_SIZE bytes */
                            clientSock.read(b);
                            String filename = new String(b.array()).trim();
                            
                            /**
                             * verifico che non si tenti di inviare una stringa
                             * che possa causare un overflow
                            */
                            int overflowSize = 0, currRead;
                            b.clear();
                            while((currRead = clientSock.read(b)) > 0)
                            {
                                b.clear();
                                limitExceeded = true;
                                overflowSize += currRead;
                                
                                /**
                                 * se il client mi invia una stringa lunga più
                                 * di 5 volte la lunghezza della mia stringa 
                                 * massima lo disconnetto
                                 */
                                if(overflowSize >= (MAX_STRING_SIZE * 5))
                                    throw new IOException("Errore! Tentativo di overflow, disconnessione");
                            }
                            
                            /**
                             * se la dimensione non supera la dimensione 
                             * soprastante lo considero un errore accettabile
                             */
                            if(!limitExceeded)
                                k.attach(filename);
                            else
                                k.attach(null);
                            
                            /* imposto il client in modalità attesa di ricezione */
                            k.interestOps(SelectionKey.OP_WRITE);
                        }
                        /* risposta alla richiesta del client */
                        else if(k.isWritable())
                        {
                            SocketChannel clientSock = (SocketChannel) k.channel();
                            String filename = (String) k.attachment();
                            boolean error = false;
                            
                            if(filename != null)
                                LOGGER.log(Level.INFO, "Richiesto file {0} da {1}", 
                                    new Object[]{filename, clientSock.getLocalAddress()});
                            else
                                LOGGER.log(Level.INFO, "Invio messaggio di errore a {0}",
                                        clientSock.getLocalAddress());
                            
                            ByteBuffer message = getFileBuffer(filename);
                            ByteBuffer fileSize = ByteBuffer.allocate(BUFFER_DIMENSION_SIZE);
                            
                            /**
                             * errore: impossibile trovare il file
                             * invio un codice di errore
                             */
                            if(message == null)
                            {
                                fileSize.putInt(ERROR_FILE_NOT_FOUND);
                                message = ByteBuffer.wrap("Errore! File non trovato".getBytes());
                                error = true;
                            }
                            /* invio la dimensione del file e successivamente il file*/
                            else
                                fileSize.putInt(message.capacity());
                            
                            fileSize.flip();
                            while(fileSize.hasRemaining()) /* invio dimensione */
                                clientSock.write(fileSize);
                            
                            while(message.hasRemaining()) /* invio effettivo */
                                clientSock.write(message);
                            
                            if(error)
                                LOGGER.log(Level.INFO, "File richiesto non trovato");
                            else
                                LOGGER.log(Level.INFO, "Inviato file {0} a {1}", 
                                        new Object[]{filename, clientSock.getLocalAddress()});
                            
                            /* imposto il client in modalità attesa di scittura */
                            k.interestOps(SelectionKey.OP_READ);
                        }
                    } /* chisura della socket in caso di eccezione */ 
                    catch(IOException e) {
                        k.cancel();
                        try {
                            k.channel().close();
                        } catch (IOException ex) {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }  catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally { /* pulizia e chiusura */
            free();
            isRunning = false;
        }
    }

}
