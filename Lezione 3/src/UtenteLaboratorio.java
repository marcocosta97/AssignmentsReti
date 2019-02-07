
import java.util.Random;

/*
 * 
 * 
 * 
 */

/**
 *
 * @author mc
 */
public abstract class UtenteLaboratorio implements Runnable, Comparable<UtenteLaboratorio> {
    private final int MAX_K = 3; // massimo numero di richieste consecutive
    private final int MAX_WAIT = 5; // max secondi di attesa
    private final String nome;
    private final String cognome;
    
    public UtenteLaboratorio() {
        nome = NameGenerator.generateName();
        cognome = NameGenerator.generateName();
    }

    @Override
    public void run() {
        try {
            String className = this.getClass().getSimpleName();
            Random r = new Random();
            TutorMarzotto t = TutorMarzotto.getTutor();

            int k = r.nextInt(MAX_K) + 1;
            for(int i = 0; i < k; i++)
            {
                System.out.println(this + " [" + className + "] ha richiesto l'accesso ad uno o più computer");
                t.requestPC(this);
                System.out.println(this + " [" + className + "] ha ottenuto l'accesso ad uno o più computer");
                Thread.sleep((r.nextInt(MAX_WAIT) + 1) * 1000);
                t.leavePC(this);
                System.out.println(this + " [" + className + "] ha terminato l'accesso ad uno o più computer");
                Thread.sleep((r.nextInt(MAX_WAIT) + 1) * 1000);
            }
            
            /* utilizzo err solo per il colore */
            System.err.println(this + " terminato!");
        }
        catch (InterruptedException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * comparatore per la gestione automatica delle priorità delle sottoclassi
     * 
     * @param t utente da comparare
     * @return 1, 0, -1
     */
    @Override
    public abstract int compareTo(UtenteLaboratorio t);
    
    @Override
    public String toString() {
        return nome + " " + cognome;
    }
}
