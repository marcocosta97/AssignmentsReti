
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * 
 * 
 * 
 */

/**
 * Semplice server in esecuzione sulla porta SERVER_PORT per l'invio di 
 *  timestamp ad intervalli predeterminati.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ServerMulticast implements Runnable {
    public static final int SERVER_PORT = 1111;
    public static final int BUFFER_SIZE = 256;
    public static final String CHARSET = "UTF-8";
    
    private static final int SERVER_SLEEP = 2000;
    
    private final MulticastSocket sock;
    private final InetAddress addr;
    
    private boolean serverRunning;
    
    private static final Logger LOGGER = Logger.getLogger(ServerMulticast.class.getName());
    
    /**
     * Crea un nuovo Server con indirizzo multicast "groupIp".
     * Nota: il server deve essere fatto partire
     * 
     * @param groupIp indirizzo multicast
     * @throws IOException in caso di errore di IO
     */
    public ServerMulticast(String groupIp) throws IOException {
        sock = new MulticastSocket(SERVER_PORT);
        addr = InetAddress.getByName(groupIp);
        sock.joinGroup(addr);
        
        serverRunning = false;
    }
    
    /**
     * Permette la chiusura asincrona del server
     */
    public void closeServer() {
        serverRunning = false;
    }
    
    /**
     * Routine del server
     */
    @Override
    public void run() {
        if(serverRunning) throw new IllegalStateException("server is already running");
        
        byte[] buffer;
        
        serverRunning = true;
        try {
            while(serverRunning)
            {   
                Date d = new Date(); /* data corrente */
                buffer = d.toString().getBytes(CHARSET);

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, addr, SERVER_PORT);

                sock.send(packet);

                LOGGER.log(Level.INFO, "Timestamp {0} inviato!", d.toString());
                Thread.sleep(SERVER_SLEEP);  /* wait del server */
            }
        }
        catch (IOException | InterruptedException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        try {
            sock.leaveGroup(addr);
            sock.close();
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        LOGGER.log(Level.INFO, "Server terminato");
    }
    
    /**
     * Semplice main per l'esecuzione del server sull'indirizzo args[0]
     * Prende solo un argomento come parametro.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        if(args.length != 1) throw new IllegalArgumentException("#arg != 1");
        
        try {
            ServerMulticast s = new ServerMulticast(args[0]);
            s.run();
        }
        catch (IOException ex) {
            Logger.getLogger(ServerMulticast.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
