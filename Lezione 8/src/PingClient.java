
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/*
 * 
 * 
 * 
 */

/**
 * Client per il ping verso l'echo server PingServer.
 * 
 * Nota: nulla vieta che la classe funzioni anche verso altri echo server UDP,
 *       tuttavia la classe utilizza il parametro BUFFER_SIZE concordato dalla classe 
 *       PingServer per la dimensione del buffer di ricezione e il parametro
 *       CHARSET per il charset utilizzato. Vanno cambiati i parametri qui sotto.
 * 
 * È possibile modificare i seguenti parametri del server:
 *  - Dimensione data buffer client/server -> BUFFER_SIZE
 *  - Charset utilizzato per lo scambio di messaggi -> CHARSET
 *  - Numero di Ping inviati -> NO_MESSAGE
 *  - Valore del timeout -> TIMEOUT
 *  - Unità di misura del timeout -> TIMEOUT_UNIT
 * 
 * @author mc - Marco Costa - 545144
 */
public class PingClient {
    private static final String CHARSET = PingServer.CHARSET;
    private static final int BUFFER_SIZE = PingServer.BUFFER_SIZE;
    
    private static final int NO_MESSAGE = 10;
    
    private static final int TIMEOUT = 2;
    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;
    
    /**
     * costruttore
     */
    public PingClient() {}
    
    /**
     * Ping verso un server con indirizzo address su porta port.
     * 
     * Nota: uno stesso oggetto può effettuare più invocazioni a server diversi
     * 
     * @param address indirizzo del server
     * @param port porta
     * @return stringa contenente le statistiche
     * @throws java.io.IOException
     */
    public String ping(String address, int port) throws IOException {
        try {
            DatagramSocket sock = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(new byte[1], 0, InetAddress.getByName(address), port);
            byte[] receiveBuffer = new byte[BUFFER_SIZE];
            long[] rtt = new long[NO_MESSAGE];
            
            int ansReceived = 0;
            long maxRTT = Long.MIN_VALUE, minRTT = Long.MAX_VALUE;
            double avgRTT = 0.f;
            
            sock.setSoTimeout((int) TIMEOUT_UNIT.toMillis(TIMEOUT));
            for(int i = 0; i < NO_MESSAGE; i++)
            {
                /* costruisco il pacchetto corrente */
                long timestamp = System.currentTimeMillis();
                String pingString = "PING " + i + " " + timestamp;
                byte[] pingByte = pingString.getBytes(CHARSET);
                
                if(pingByte.length > BUFFER_SIZE)
                    throw new IOException("il server non supporta stringhe così lunghe");
                
                packet.setData(pingByte);
                sock.send(packet); /* invio */
                
                DatagramPacket received = new DatagramPacket(receiveBuffer, BUFFER_SIZE);
                try {
                    sock.receive(received); /* ricezione */
                    String serverAns = new String(received.getData(), CHARSET).trim();
                    if(serverAns == null)
                        throw new SocketTimeoutException();
                    
                    rtt[i] = System.currentTimeMillis() - timestamp;
                    System.out.println(pingString + " RTT: " + rtt[i] + " ms");
                    
                    /* aggiornamento statistiche */
                    if(rtt[i] > maxRTT) maxRTT = rtt[i];
                    if(rtt[i] < minRTT) minRTT  = rtt[i];            
                    avgRTT += rtt[i];
                    ansReceived++;
                }
                /**
                 * timeout in ricezione: pacchetto non arrivato
                 */
                catch(SocketTimeoutException ex) { 
                    rtt[i] = Long.MIN_VALUE;
                    System.out.println(pingString + " RTT: *");
                }
            }
            
            if(ansReceived == 0)
                maxRTT = minRTT = 0;
            else
                avgRTT = avgRTT / ansReceived;
            
            /* costruisco stringa statistiche */
            String s = "---- PING Statistics ----\n";
            s += String.format("%d packets transmitted, %d packets received, %.2f%% packet loss\n",
                    NO_MESSAGE, ansReceived, (1 - ((double) ansReceived / NO_MESSAGE)) * 100);
            s += String.format("round-trip (ms) min/avg/max = %d/%.2f/%d\n", minRTT, avgRTT, maxRTT); 
            
            return s;
        }
        catch (SocketException | UnknownHostException | UnsupportedEncodingException ex) {
            throw new SocketException("Impossibile aprire la socket: " + ex);
        }
        catch (IOException ex) {
            throw new IOException("Errore di IO: " + ex);
        }
    }
            
    public static void main(String[] args) {
        try {
            if(args.length != 2)
                throw new IllegalArgumentException("#arg != 2");
            
            int port = Integer.parseInt(args[1]);
            if(port <= 0)
                throw new IllegalArgumentException("2");
            
            PingClient c = new PingClient();
            System.out.println(c.ping(args[0], port));
        }
        catch(IllegalArgumentException ex) {
            System.err.println("ERR - arg " + ex.getLocalizedMessage());
        }
        catch (IOException ex) {
            System.err.println("ERR -> " + ex);
        }
    }          
}
