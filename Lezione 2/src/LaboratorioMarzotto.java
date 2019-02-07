
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 
 * 
 * 
 */

/**
 *
 * @author mc
 */
public class LaboratorioMarzotto  {
    private final UtenteLaboratorio pc[];
    private final Lock labAccess;
    private final Condition labBusy; /* blocco sull'intero laboratorio */
    private final Condition pcBusy[]; /* blocco sul singolo pc */
    private Professore labAcquirer;
    private static final int NUMERO_PC = 20;
    private final int pcTesi;
    
    /**
     * verifica che il valore richiesto sia interno all'insieme dei pc
     * @throws IllegalArgumentException se il valore non è interno 
     * 
     * @param i numero pc da controllare (app [1, 20])
     * @return (i - 1) se il valore è interno (=> (i - 1) app [0, 19])
     */
    private int checkPCNumber(int i) throws IllegalArgumentException{
        if((i < 1) || (i > NUMERO_PC)) throw new IllegalArgumentException("pc " + i + " inesistente");
        
        return i - 1;
    }
    
    /**
     * costruttore per la classe
     * 
     * @param pcTesi il pc utilizzato per le tesi
     */
    public LaboratorioMarzotto(int pcTesi) {
        this.pcTesi = checkPCNumber(pcTesi);
        
        pc = new UtenteLaboratorio[NUMERO_PC];
        
        labAccess = new ReentrantLock();
        pcBusy = new Condition[NUMERO_PC];
        labBusy = labAccess.newCondition();
        for(int i = 0; i < NUMERO_PC; i++)
            pcBusy[i] = labAccess.newCondition();
        labAcquirer = null;
        
    }
    
    /**
     * gestisce la richiesta di accesso (compresa l'attesa) ad un computer in base alla classe
     * richiedente, al termine del metodo se non è stata sollevata alcuna 
     * eccezione il richiedente ha ottenuto l'accesso
     * se uno studente invoca il metodo senza specificare numberPC viene cercato
     * un pc momentaneamente libero senza effettuare l'attesa
     * @throws IllegalArgumentException se il pc richiesto non è congruo
     * @throws IllegalStateException se uno studente non trova un pc momentaneamente libero
     * 
     * @param p richiedente accesso
     * @param numberPC numero del pc da richiedere (se significativo), può
     *                  essere null (es. per professori e tesisti)
     * @return numero del pc ottenuto (se significativo) (es per studenti)
     *          null altrimenti
     */
    public Integer getAccess(UtenteLaboratorio p, Integer numberPC) throws IllegalArgumentException, IllegalStateException {
        if(numberPC != null)
            numberPC = checkPCNumber(numberPC);
        
        labAccess.lock();
        try {
            /* mi blocco se qualche professore ha bloccato l'intero laboratorio */
            while(labAcquirer != null)
                labBusy.await();
            
            /* richiesta di un pc specifico */
            if(numberPC != null)
            {
                while(pc[numberPC] != null)
                    pcBusy[numberPC].await();
                
                pc[numberPC] = p;
                System.out.println("[LAB] " + this + " ha ottenuto il pc " + (numberPC + 1));
            }
            else if(p.getClass() == Studente.class)
            {
                /* cerco un pc libero al momento */
                int i;
                for(i = 0; i < NUMERO_PC; i++)
                {
                    if(pc[i] == null)
                    {
                        pc[i] = p;
                        System.out.println("[LAB] " + p + " ha ottenuto il pc " + (i + 1));
                        break;
                    }
                }
                if(i >= NUMERO_PC)
                    throw new IllegalStateException("non ci sono pc liberi");
                else
                    return i + 1;
                
            }
            else if(p.getClass() == Tesista.class)
            {
                /* mi blocco sul pc della tesi */
                while(pc[pcTesi] != null)
                    pcBusy[pcTesi].await();
                
                pc[pcTesi] = p;
                System.out.println("[LAB] " + p + " ha ottenuto il pc " + pcTesi);
            }
            else if(p.getClass() == Professore.class)
            {
                /* mi blocco su tutto il laboratorio */
                for(int i = 0; i < NUMERO_PC; i++)
                {
                    while(pc[i] != null)
                        pcBusy[i].await();
                    pc[i] = p;
                }
                System.out.println("[LAB] " + p + " ha ottenuto l'intero laboratorio");
            }
            else throw new IllegalArgumentException("non conosco la classe di utenti");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }      
        finally {
            labAccess.unlock();
        }
        
        return null;
    }

    /**
     * gestisce la richiesta di restituzione di uno o più pc (in base alla classe)
     * REQUIRES: p possiede il pc
     * @throws IllegalArgumentException se il pc richiesto non è congruo
     * 
     * @param p utente richiedente
     * @param numberPC pc da restituire, se necessario (es studenti)
     */
    public void leavePC(UtenteLaboratorio p, Integer numberPC) throws IllegalArgumentException {
        if(numberPC != null)
            numberPC = checkPCNumber(numberPC);
        
        if(p.getClass() == Tesista.class)
            numberPC = pcTesi;
        
        labAccess.lock();
        try { 
            if(p.getClass() == Professore.class)
            {
                labAcquirer = null;
                labBusy.signal();

                for(int i = 0; i < NUMERO_PC; i++)
                {
                    pc[i] = null;
                    pcBusy[i].signal();
                }
            }
            else if((p.getClass() == Tesista.class) || (p.getClass() == Studente.class))
            {
                pc[numberPC] = null;
                pcBusy[numberPC].signal();
            }
            else throw new IllegalStateException(); 
        }
        finally {
            labAccess.unlock();
        }
    }
    
    public static int getNumeroPCTotali() {
        return NUMERO_PC;
    }
}
