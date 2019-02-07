
import java.util.Random;

/*
 * 
 * 
 * 
 */

/**
 *
 * @author Marco Costa - 545144
 */
public class Persona implements Runnable{
    private final String nome;
    private final String cognome;
    
    public Persona(){
        nome = NameGenerator.generateName();
        cognome = NameGenerator.generateName();
    }

    @Override
    public String toString() {
        return nome + " " + cognome;
    }
    
    
    @Override
    public void run() {
        Random r = new Random();
        int wait = r.nextInt(2000) + 1;
        System.out.printf("[%s] %s: inizio sportello con attesa %dms\n", Thread.currentThread(), this, wait);
        try {
            Thread.sleep(wait);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        System.out.printf("[%s] %s: fine sportello\n", Thread.currentThread(), this);
    }
    
}
