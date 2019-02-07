
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
 * 
 * 
 * 
 */

/**
 * esercizio lezione 2 
 * 
 * tempo massimo esecuzione: 2 minuti
 * (in UtenteLaboratorio.java):
 *      max numero di richieste: 3
 *      max secondi attesa: 5
 * 
 * @author mc - Marco Costa - 545144
 */
public class MainClass {
    private static final int MAX_EXECUTION = 2; // minuti
    
    /**
     * 
     * @param args (numero studenti, n. tesisti, n. professori)  (senza virgole!)
     */
    public static void main(String[] args) {
        if(args.length != 3)
            throw new IllegalArgumentException("i parametri di ingresso non sono validi, inserire n.studenti, n.tesisti, n.professori");
        
        Integer input[] = new Integer[3];
        
        try {
            for(int i = 0; i < 3; i++)
            {
                input[i] = Integer.parseInt(args[i]);
                if(input[i] < 0)
                    throw new IllegalArgumentException();
            }
        }
        catch(NumberFormatException ex) {
            throw new IllegalArgumentException("presenza di parametri non validi!");
        }
        
        Random r = new Random();
        
        int pcTesi = r.nextInt(LaboratorioMarzotto.getNumeroPCTotali()) + 1; 
        System.out.println("Pc tesi generato casualmente al numero: " + pcTesi);
        
        LaboratorioMarzotto l = new LaboratorioMarzotto(pcTesi);
        TutorMarzotto t = new TutorMarzotto(l);
        /**
         * viene aggiunto un thread per ogni runnable o riutilizzati quelli vecchi dopo la terminazione dei task, quindi ok
         */
        ExecutorService tutorService = Executors.newSingleThreadExecutor();
        ExecutorService c = Executors.newCachedThreadPool();
        tutorService.submit(t);
        
        /**
         * è un po' bruttino ma almeno non partono prima tutti gli studenti, poi tutti i tesisti, ecc.
         */
        int maxInput = Integer.max(input[0], Integer.max(input[1], input[2]));
        System.out.println("Parametri: " + Arrays.toString(input));
        for(int i = 0; i < maxInput; i++)
        {
            if(i < input[0])
                c.submit(new Studente());
            if(i < input[1])
                c.submit(new Tesista());
            if(i < input[2])
                c.submit(new Professore());
        }
        
        
        c.shutdown();
        try {            
            c.awaitTermination(MAX_EXECUTION, TimeUnit.MINUTES);
            tutorService.shutdownNow();
            System.out.println("Programma terminato correttamente!");
        } catch (InterruptedException ex) {
            c.shutdownNow();
            tutorService.shutdownNow();
            System.err.println("Programma terminato mediante timeout!");
        }
        
        
    }
}
