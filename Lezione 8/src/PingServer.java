
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * 
 * 
 * 
 */

/**
 * Classe PingServer che implementa un echo server per tutte e sole le richieste
 * di ping. (Ogni messaggio che non inizia con la stringa "PING" verrà scartato).
 * 
 * Il server gestisce richieste contemporanee da più client su protocollo UDP
 * e utilizza una thread pool di schedulatori per simulare la congestione della
 * rete senza bloccare la routine principale.
 * 
 * È possibile modificare i seguenti parametri del server:
 *  - Dimensione data buffer client/server -> BUFFER_SIZE
 *  - Charset utilizzato per lo scambio di messaggi -> CHARSET
 *  - Massimo valore possibile del delay -> MAX_DELAY
 *  - Unità di misura del delay -> DELAY_UNIT
 *  - Dimensione pool di thread schedulatori -> CORE_POOL_SIZE
 * 
 * @author mc - Marco Costa - 545144
 */
public class PingServer implements Runnable {
    /* public */
    public static final int BUFFER_SIZE = 128;
    public static final String CHARSET = "UTF-8";
    
    /* private */
    private static final int MAX_DELAY = 500; 
    private static final TimeUnit DELAY_UNIT = TimeUnit.MILLISECONDS;
    private static final int CORE_POOL_SIZE = 2;
    
    private static final int JOIN_TIMEOUT = 1;
    private static final TimeUnit JOIN_UNIT = TimeUnit.SECONDS;
    
    private final DatagramChannel sockChannel;
    private final DatagramSocket sock;
    private Selector s;
    
    private boolean isRunning = false;
    private boolean isInterrupted = false;
    private final Random r;
    private final long seed;
    
    private static final Logger LOGGER = Logger.getLogger(PingServer.class.getName());
    
    /**
     * Costruttore del server con unico parametro la porta.
     * Per il seed si usa il corrente timestamp.
     * 
     * Nota: il server deve essere mandato in esecuzione
     * @param port la porta di ricezione del server
     * @throws IOException se si verificano errori con l'apertura del server
     */
    public PingServer(int port) throws IOException {
        this(port, System.currentTimeMillis());
    }
    
    /**
     * Costruttore del server con parametri la porta e il seed.
     * 
     * Nota: il server deve essere mandato in esecuzione
     * @param port la porta di ricezione del server
     * @param seed il valore di seed
     * @throws IOException se si verificano errori con l'apertura del server
     */
    public PingServer(int port, long seed) throws IOException {
        this.seed = seed;
        r = new Random(this.seed);
        
        try {
            sockChannel = DatagramChannel.open();
            sock = sockChannel.socket();
            sock.bind(new InetSocketAddress(port));
            sockChannel.configureBlocking(false);
            
        }
        catch (SocketException ex) {
            throw new SocketException("Errore! impossibile aprire la socket: " + ex);
        }
        catch (IOException ex) {
            throw new IOException("Errore! Si è verificato un errore di IO: " + ex);
        }
    }
    
    /**
     * Sincronizzazione sul canale di invio per i thread schedulati.
     * 
     * @param data buffer data
     * @param addr indirizzo ricevente
     * @throws IOException 
     */
    private void synchrSend(byte[] data, SocketAddress addr) throws IOException {
        synchronized(sock) {
            sockChannel.send(ByteBuffer.wrap(data), addr);
        }
    }
    
    /**
     * Metodo per la chiusura controllata del server
     * 
     * Nota: La chiusura dell'eseguiile da console non prevede chiusura controllata
     */
    public void close() {
        isInterrupted = true;
        s.wakeup();
    }
    
    /**
     * Routine principale del server
     */
    @Override
    public void run() {
        if(isRunning) throw new IllegalStateException("Errore! Il server è già in esecuzione!");
        
        /* mantengo un set contenente i task schedulati */
        Set<ScheduledFuture<?>> responseThreads = new HashSet<>();
        /* apro il servizio di thread schedulatori */
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(PingServer.CORE_POOL_SIZE);
        
        try {
            isRunning = true;
            
            /* apertura del selettore e registrazione dell'operazione di lettura */
            s = Selector.open();
            sockChannel.register(s, SelectionKey.OP_READ);
            
            System.out.println("[++] Server in esecuzione sulla porta " 
                    + sock.getLocalPort() + ", seed: " + seed);
            
            while(true)
            {
                s.select();
                
                if(isInterrupted)
                    return; /* goto finally */
                
                Set<SelectionKey> keys = s.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                
                while(it.hasNext())
                {
                    SelectionKey k = it.next();
                    it.remove();
                    
                    try {
                        if(!k.isValid()); /* non faccio nulla */
                        else if(k.isReadable()) /* richiesta arrivata */
                        {
                            ByteBuffer buffer = ByteBuffer.allocate(PingServer.BUFFER_SIZE);
                            SocketAddress add;
                            
                            add = sockChannel.receive(buffer); /* ricezione del datagramma */
                            DatagramPacket packet = new DatagramPacket(buffer.array(), PingServer.BUFFER_SIZE, add);
                            
                            /** 
                             *  se il campo data del pacchetto è più grande di 
                             *  quanto previsto o se il messaggio non inizia
                             *  con "PING" -> scarto il pacchetto
                             */
                            if(packet.getLength() <= PingServer.BUFFER_SIZE)
                            {
                                String data = new String(packet.getData(), PingServer.CHARSET).trim();
                                if(data.startsWith("PING"))
                                {
                                    k.attach(packet);
                                    k.interestOps(SelectionKey.OP_WRITE); /* pachetto ok, passo in scrittura */
                                } 
                            }
                        }
                        else if(k.isWritable())
                        {
                            final DatagramPacket request = (DatagramPacket) k.attachment();  
                            final byte[] requestData = request.getData();
                            final SocketAddress requestAddr = request.getSocketAddress();
                            final String pingMessage = new String(requestData, PingServer.CHARSET).trim();
                            String s = requestAddr + "> " + pingMessage + " ACTION: ";
                            
                            /* simulazione delay */
                            int lossPacket = r.nextInt(4);

                            if(lossPacket == 0) /* 1/4 -> pacchetto perso */
                                s += "not sent";
                            else /* pacchetto non perso */
                            {
                                /**
                                 * calcolo il delay casualmente e passo la
                                 * operazione allo schedulatore
                                 */
                                final int delay = r.nextInt(MAX_DELAY) + 1;
                                
                                Runnable r = () -> {
                                    try {
                                        synchrSend(requestData, requestAddr);
                                    }
                                    catch (IOException ex) {
                                        LOGGER.log(Level.SEVERE, null, ex);
                                    }
                                };
                                responseThreads.add(executor.schedule(r, delay, DELAY_UNIT));
                                
                                s += "delayed " + delay + " " + DELAY_UNIT;
                            }

                            System.out.println(s);
                            k.interestOps(SelectionKey.OP_READ); /* ritorno in lettura */
                        }
                    }
                    catch(IOException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            }
        
        }
        catch (IOException ex) {    
            LOGGER.log(Level.SEVERE, null, ex);
        }
        catch(Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally {
            /**
             * chiusura controllata: attendo che tutti i task abbiano finito,
             *  chiudo l'esecutore e chiudo la socket
             */
            responseThreads.forEach((f) -> {
                try {
                    f.get(JOIN_TIMEOUT, JOIN_UNIT);
                }
                catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });
            executor.shutdown();
            try {
                executor.awaitTermination(1, TimeUnit.MINUTES);
                sock.close();
                sockChannel.close();
                System.out.println("[++] Server chiuso correttamente");
            }
            catch (IOException | InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    /**
     * Main per l'esecuzione del server da command line.
     * Vengono eseguiti i controlli previsti sui parametri di ingresso
     * 
     * Accetta:
     *  - 1 parametro: porta
     *  - 2 parametri: porta e seed
     * 
     * @param args 
     */
    public static void main(String[] args) {
        try {
            if((args.length < 1) || (args.length > 2))
                throw new IllegalArgumentException("#arg != 1 or 2");
            
            int port = Integer.parseInt(args[0]);
            if(port <= 0)
                throw new IllegalArgumentException("1");
            
            PingServer s;
            if(args.length == 1)
                s = new PingServer(port);
            else
            {
                long seed = Long.parseLong(args[1]);
                if(seed <= 0)
                    throw new IllegalArgumentException("2");
                
                s = new PingServer(port, seed);
            }
            
            s.run();
        }
        catch(IllegalArgumentException | IOException ex) {
            System.err.println("ERR - arg " + ex.getLocalizedMessage());
        }
    }

    
}
