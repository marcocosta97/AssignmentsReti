/*
 * 
 * 
 * 
 */

/**
 *
 * @author mc
 */
public final class Studente extends UtenteLaboratorio {

    public Studente() {
        super();
    }

    @Override
    public int compareTo(UtenteLaboratorio t) {
        if(t.getClass() == Studente.class)
            return 0;
        
        return 1;
    }
    
}
