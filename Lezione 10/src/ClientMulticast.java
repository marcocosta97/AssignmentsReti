
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

/*
 * 
 * 
 * 
 */

/**
 * Semplice Client multicast per la ricezione di un numero determinato di 
 *  timestamp.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ClientMulticast {
    private static final int NO_RECEIVE = 10;
    
    public ClientMulticast() {}
    
    /**
     * Effettua la join del gruppo sull'indirizzo address, riceve NO_RECEIVE
     *  timestamp e termina, lasciando il gruppo.
     * 
     * @param address l'indirizzo multicast
     * @throws IOException in caso di errori di IO
     */
    public void listen(String address) throws IOException {
        String curr = Thread.currentThread().getName();
        
        try (MulticastSocket sock = new MulticastSocket(ServerMulticast.SERVER_PORT);)
        {   
            InetAddress addr = InetAddress.getByName(address);
            sock.joinGroup(addr);
            
            byte[] buffer = new byte[ServerMulticast.BUFFER_SIZE];
            Arrays.fill(buffer, (byte) 0);
            
            for(int i = 0; i < NO_RECEIVE; i++)
            {
                DatagramPacket packet = new DatagramPacket(buffer, ServerMulticast.BUFFER_SIZE);
                sock.receive(packet);
                
                String time = new String(packet.getData(), ServerMulticast.CHARSET);
                System.out.println(curr + " -> Timestamp " + (i + 1) + ": " + time);
            }
            
            System.out.println(curr + " -> Ricevuti " + NO_RECEIVE + " timestamp. Termino.");
            
            sock.leaveGroup(addr);
        }
        catch (IOException ex) {
            throw ex;
        }
    }
    
    /**
     * Riceve un argomento da linea di comando (l'indirizzo di multicast) e
     *  invoca il metodo listen sull'indirizzo.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        if(args.length != 1) throw new IllegalArgumentException("#arg != 1");
        
        ClientMulticast c = new ClientMulticast();
        try {
            c.listen(args[0]);
        }
        catch (IOException ex) {
            System.err.println("Errore! " + ex);
        }
    }
}
