
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

/*
 * 
 * 
 * 
 */

/**
 *
 * @author mc
 */
public class TutorMarzotto implements Runnable {
    private final LaboratorioMarzotto workingLab;
    private final PriorityBlockingQueue<UtenteLaboratorio> coda; /* concurrent */
    private final ConcurrentHashMap<Studente, Integer> associazioniPC; /* concurrent */
    
    /**
     * possiamo avere una singola istanza del tutor per laboratorio (singleton)
     */
    private static TutorMarzotto INSTANCE = null;
    
    /**
     * singolo costruttore della classe tutor 
     * 
     * @param workingLab laboratorio sul quale lavora il tutor
     */
    public TutorMarzotto(LaboratorioMarzotto workingLab) {
        if(INSTANCE != null) throw new IllegalStateException("il tutor è già stato inizializzato!");
        if(workingLab == null) throw new NullPointerException("il laboratorio non può essere null!");
        
        this.workingLab = workingLab;
        coda = new PriorityBlockingQueue<>();
        associazioniPC = new ConcurrentHashMap<>();
       
        INSTANCE = this;
    }
    
    /**
     * @throws IllegalStateException se il tutor non è stato ancora inizializzato
     * @return l'istanza della classe tutor
     */
    public static TutorMarzotto getTutor() throws IllegalStateException {
        if(INSTANCE == null) throw new IllegalStateException("il tutor non è stato inizializzato");
        
        return INSTANCE; 
   }
    
    /**
     * metodo per la richiesta della/e postazione/i prevista/e al tutor
     * al termine del metodo l'utente possiede il/i pc
     * 
     * @param u richiedente
     * @throws NullPointerException se l'utente è null
     * @throws InterruptedException se l'attesa viene interrotta
     */
    public void requestPC(UtenteLaboratorio u) throws NullPointerException, InterruptedException {
        if(u == null) throw new NullPointerException("l'utente non può essere null!");
        
        coda.offer(u);
        
        /**
         * attesa sull'oggetto richiedente finché non viene gestita la sua richiesta
         */
        synchronized(u) {
            u.wait();
        }
        
        this.giveAccess(u);
    }
    
    /**
     * metodo per la richiesta di terminazione accesso al/ai pc al tutor
     * 
     * @param u richiedente
     * @throws IllegalArgumentException se la classe non è conosciuta
     */
    public void leavePC(UtenteLaboratorio u) throws IllegalArgumentException {
        if(u.getClass() == Studente.class)
        {
            Integer i = associazioniPC.remove((Studente)u);

            workingLab.leavePC(u, i);
        }
        else if((u.getClass() == Professore.class) || (u.getClass() == Tesista.class))
            workingLab.leavePC(u, null);
        
        else
            throw new IllegalArgumentException("non conosco questa classe di utenti");
    }
    
    /**
     * interazione tra tutor e laboratorio per la gestione della richiesta di t
     * 
     * @param t
     * @throws IllegalArgumentException 
     */
    private void giveAccess(UtenteLaboratorio t) throws IllegalArgumentException {
        if(t.getClass() == Studente.class)
        {
            Integer assignedPC = null;
            
            try {
                assignedPC = workingLab.getAccess(t, null);
            }
            /**
             * non ho pc disponibili, mi metto in attesa su un pc occupato a caso
             */
            catch(IllegalStateException e) {
                Random r = new Random();
                
                assignedPC = r.nextInt(LaboratorioMarzotto.getNumeroPCTotali()) + 1;
                workingLab.getAccess(t, assignedPC);
            }
            finally {
                /**
                 * devo ricordarmi a che posizione ho inserito lo studente 
                 */
                associazioniPC.put((Studente) t, assignedPC);
            }
        }
        else if((t.getClass() == Professore.class) || (t.getClass() == Tesista.class))
            workingLab.getAccess(t, null);
        else
            throw new IllegalArgumentException("non conosco questa classe di utenti");
    }

    @Override
    public void run() {
        try {
            while(true)
            {
                /* il metodo prevede l'attesa in caso di lista vuota */
                UtenteLaboratorio curr = coda.take();
                
                System.out.println("[TUTOR] gestisco la richiesta di " + curr + ", rimangono in coda: " + coda.toString());
                synchronized(curr) {
                    curr.notify();
                }
            }
        }
        catch(InterruptedException e) {
            System.out.println("[TUTOR] ho ricevuto segnale di terminazione");
        }
        
    }
    
}
