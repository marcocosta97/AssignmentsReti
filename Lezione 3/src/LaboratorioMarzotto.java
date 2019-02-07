



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
    private final PC pcList[];
    private static final int NUMERO_PC = 20;
    private final int pcTesi;
    
    private class PC {
        private UtenteLaboratorio currentUser;

        PC() {
            currentUser = null;
        }

        synchronized void setUser(UtenteLaboratorio u) throws InterruptedException {
            while(currentUser != null) 
                wait();

            currentUser = u;
        }

        synchronized void removeUser() {
            currentUser = null;
            notify();
        }

        synchronized UtenteLaboratorio getCurrentUser() {
            return currentUser;
        }
    }
    
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
        pcList = new PC[NUMERO_PC];
        for(int i = 0; i < NUMERO_PC; i++)
            pcList[i] = new PC();
        
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
        
        /**
         * NOTA: in questo contesto sarebbe stato più "pulito" far gestire le diverse richieste al
         *      tutor (dato che l'interfaccia scritta per l'esercizio precedente
         *      utilizzava dei meccanismi di Lock maggiormente "fine-grained" rispetto
         *      a quanto possibile fare con i Monitor), tuttavia questo avrebbe comportato
         *      la riscrittura di entrambe le interfacce Laboratorio e Tutor
         */
        if(p.getClass() == Tesista.class)
            numberPC = pcTesi;
             
        /* richiesta di un pc specifico */
        try {   
            if(numberPC != null)
            {
                pcList[numberPC].setUser(p);
                System.out.println("[LAB] " + p + " ha ottenuto il pc " + (numberPC + 1));
            }
            else if(p.getClass() == Studente.class)
            {
                /* cerco un pc libero al momento */
                int i;
                for(i = 0; i < NUMERO_PC; i++)
                {
                    if(pcList[i].getCurrentUser() == null)
                    {
                        pcList[i].setUser(p);
                        System.out.println("[LAB] " + p + " ha ottenuto il pc " + (i + 1));
                        break;
                    }
                }
                if(i >= NUMERO_PC)
                    throw new IllegalStateException("non ci sono pc liberi");
                else
                    return i + 1;
            }
            else if(p.getClass() == Professore.class)
            {
                /* mi blocco su tutto il laboratorio */
                for(int i = 0; i < NUMERO_PC; i++)
                    pcList[i].setUser(p);
                
                System.out.println("[LAB] " + p + " ha ottenuto l'intero laboratorio");
            }
            else throw new IllegalArgumentException("non conosco la classe di utenti");
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }      
        
        return null;
    }

    /**
     * gestisce la richiesta di restituzione di uno o più pc (in base alla classe)
     * REQUIRES: p possiede il pc numerPC
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
        
        if(p.getClass() == Professore.class)
            for(int i = 0; i < NUMERO_PC; i++)
                pcList[i].removeUser();

        else if((p.getClass() == Tesista.class) || (p.getClass() == Studente.class))
            pcList[numberPC].removeUser();
        
        else throw new IllegalStateException();
        
    }
    
    public static int getNumeroPCTotali() {
        return NUMERO_PC;
    }
}
