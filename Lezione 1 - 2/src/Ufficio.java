
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
 * 
 * 
 * 
 */

/**
 * esercizio 2 + facoltativo, lezione 1
 * 
 * numero sportelli: 4
 * timeout sportello: 10 secondi
 * flusso continuo: rand(100, 1000) ms
 * tempo di permanenza sportello: rand(1, 2000) ms
 * 
 * @author Marco Costa - 545144
 */
public class Ufficio implements Runnable {
    private static final int NO_SPORTELLI = 4;
    private static final int TIMEOUT_SPORTELLO = 10; //secondi
    private final ThreadPoolExecutor sportelli;
    private final LinkedBlockingQueue<Persona> sala_attesa;
    
    /**
     * REQUIRES: k >= 0
     * 
     * @param k coda massima
     */
    public Ufficio(int k) {
        sportelli = new ThreadPoolExecutor(NO_SPORTELLI, NO_SPORTELLI, TIMEOUT_SPORTELLO, TimeUnit.SECONDS, new LinkedBlockingQueue<>(k));
        sala_attesa = new LinkedBlockingQueue<>();
    }
    
    public void nuovoArrivo(Persona p) {
        if(p == null) throw new NullPointerException("Stai utilizzando un oggetto vuoto!");
        
        sala_attesa.add(p);
        System.out.println(p + " è arrivato in sala d'attesa");
    }

    @Override
    public void run() {
        try{
            while(true)
            {
                Persona curr = sala_attesa.take(); // attesa passiva
                try {
                    sportelli.submit(curr); 
                }
                /**
                 * se il submit non viene concesso ci mettiamo in attesa passiva sulla
                 * coda finché non c'è un posto disponibile
                 */
                catch(RejectedExecutionException ex) {
                    sportelli.getQueue().put(curr);
                }
            }
        } catch (InterruptedException ex) {
            sportelli.shutdown();
            System.out.println("L'ufficio è stato chiuso!");
        }
    }
    
    public static void main(String[] args) throws IOException {
        int k = Integer.MAX_VALUE;
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Inserisci k: ");
        String s = "";
        try{ 
            s = reader.readLine();
            k = Integer.parseInt(s);
            if(k <= 0)
                throw new NumberFormatException();
        }
        catch(NumberFormatException e)
        {
            throw new NumberFormatException(s + " non è un numero > 0!");
        }
        
        System.out.print("Premere un carattere per aprire l'ufficio e per chiuderlo");
        reader.read();
        
        Ufficio uff = new Ufficio(k);
        
        /**
         * thread per la generazione del flusso continuo
         */
        Thread t_uff = new Thread(uff);
        t_uff.start();
        for(int i = 0; i < 10; i++)
            uff.nuovoArrivo(new Persona());
        
        Thread generator = new Thread(new Runnable() {
            @Override
            public void run() {
                Random r = new Random();
                try{
                    while(true)
                    {
                        Thread.sleep(r.nextInt(1000) + 100);
                        uff.nuovoArrivo(new Persona());
                    }
                } catch (InterruptedException ex) {
                }
            }
        });
        
        generator.start();
        
        /**
         * attesa del carattere per la terminazione
         */
        reader.read();
        generator.interrupt();
        t_uff.interrupt();
        
    }
}
